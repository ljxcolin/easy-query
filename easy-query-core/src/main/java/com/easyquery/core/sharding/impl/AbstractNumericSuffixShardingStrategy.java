package com.easyquery.core.sharding.impl;

import com.easyquery.core.enums.ConditionType;
import com.easyquery.core.model.ShardingRuleConfig;
import com.easyquery.core.sharding.ShardingStrategy;
import com.easyquery.core.sharding.SqlCondition;
import com.easyquery.core.utils.TableUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 基于「物理表后缀数值」定位分片的策略基类。
 *
 * 子类只需回答两个问题：
 * <ol>
 *     <li>{@link #suffixPattern()}：物理表后缀长什么样（如 RANGE 为任意数字、YEAR 为 4 位年份）</li>
 *     <li>{@link #toShardNumber(ShardingRuleConfig, Object)}：分片键值换算成哪个后缀数值</li>
 * </ol>
 *
 * 其余公共逻辑在此统一实现：
 * 可用物理表过滤、条件推导（= / IN / &gt; / &lt; / BETWEEN）、多条件求交、
 * 以及无法定位时退化为「全部可用分片」的兜底策略。
 *
 * <p>实现类必须无状态：策略实例由 {@code ShardingStrategyFactory} 以单例形式被所有数据源共享。</p>
 */
abstract class AbstractNumericSuffixShardingStrategy implements ShardingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNumericSuffixShardingStrategy.class);

    /**
     * 物理表后缀需要匹配的正则，例如 {@code \d+}、{@code \d{4}}
     */
    protected abstract String suffixPattern();

    /**
     * 把分片键值换算成「后缀数值」。
     * 无法换算（如值类型不认识、配置缺失）时返回 {@code null}，调用方会退化为全部可用分片，
     * <b>不要抛异常</b>——这会让一次查询直接失败。
     *
     * @param shardingRuleConfig 分片规则配置
     * @param value              分片键值
     * @return 后缀数值，无法换算时返回 null
     */
    protected abstract Integer toShardNumber(ShardingRuleConfig shardingRuleConfig, Object value);

    /**
     * 配置合法性校验，在加载规则（{@link #setAvailableTables}）时调用一次，仅用于告警。
     */
    protected void validateConfig(ShardingRuleConfig shardingRuleConfig) {
        // 默认无需校验
    }

    /**
     * 过滤出该逻辑表对应的物理表，并按后缀数值升序排列（保证 UNION ALL 顺序稳定）。
     * 本方法不抛异常：即使没有匹配到任何表，也只是把可用表置为空列表。
     */
    @Override
    public void setAvailableTables(ShardingRuleConfig shardingRuleConfig, List<String> tables) {
        if (shardingRuleConfig == null) {
            return;
        }

        validateConfig(shardingRuleConfig);

        List<String> available = new ArrayList<>();
        String logicalTableName = shardingRuleConfig.getTableName();
        if (tables != null && logicalTableName != null && !logicalTableName.isEmpty()) {
            String prefix = TableUtils.getPrefix(logicalTableName);
            String pattern = suffixPattern();
            for (String table : tables) {
                if (table == null || !table.startsWith(prefix)) {
                    continue;
                }
                String suffix = table.substring(prefix.length());
                // 后缀里还有下划线，说明是更细粒度的表（如 t_order_2024_01），不属于本规则管辖
                if (suffix.isEmpty() || suffix.contains(TableUtils.SEPARATOR)) {
                    continue;
                }
                if (!suffix.matches(pattern)) {
                    continue;
                }
                available.add(table);
            }
            available.sort(Comparator.comparingLong(AbstractNumericSuffixShardingStrategy::suffixValue));
        }

        shardingRuleConfig.setAvailableTableNames(available);
        if (available.isEmpty()) {
            logger.warn("分片规则 [{}] 未匹配到任何物理表，策略 {} 将无法路由",
                    logicalTableName, getType());
        } else {
            logger.debug("分片规则 [{}] 匹配到 {} 张物理表：{}", logicalTableName, available.size(), available);
        }
    }

    /**
     * 计算目标分片后缀。
     *
     * <p>多条件按 AND 语义求交；某个条件无法推导出分片范围时忽略该条件；
     * 全部条件都推不出来、或求交结果为空时，退化为全部可用分片（宁可多扫，不可漏数据）。</p>
     */
    @Override
    public List<String> doSharding(ShardingRuleConfig shardingRuleConfig, List<SqlCondition> conditions) {
        Map<Integer, String> available = availableSuffixes(shardingRuleConfig);
        List<String> allSuffixes = new ArrayList<>(available.values());

        if (available.isEmpty() || conditions == null || conditions.isEmpty()) {
            return allSuffixes;
        }

        Set<Integer> allowed = null;
        for (SqlCondition condition : conditions) {
            Set<Integer> candidates = resolve(condition, available, shardingRuleConfig);
            if (candidates == null) {
                continue;
            }
            allowed = allowed == null ? candidates : intersect(allowed, candidates);
        }

        if (allowed == null || allowed.isEmpty()) {
            return allSuffixes;
        }

        List<String> result = new ArrayList<>(allowed.size());
        for (Integer number : allowed) {
            result.add(available.get(number));
        }
        return result;
    }

    /**
     * 把单个条件换算成候选后缀数值集合。
     *
     * @return 无法用于推导时返回 null（该条件被忽略）；否则返回候选集合（可能为空）
     */
    private Set<Integer> resolve(SqlCondition condition, Map<Integer, String> available,
            ShardingRuleConfig shardingRuleConfig) {
        if (condition == null || condition.getConditionType() == null) {
            return null;
        }

        ConditionType type = condition.getConditionType();
        switch (type) {
            case EQUALS:
                return single(toShardNumber(shardingRuleConfig, condition.getValue()), available);
            case IN:
                return inValues(condition.getValue(), available, shardingRuleConfig);
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL:
                return window(toShardNumber(shardingRuleConfig, condition.getValue()), null, available);
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL:
                return window(null, toShardNumber(shardingRuleConfig, condition.getValue()), available);
            case BETWEEN:
                return between(condition.getValue(), available, shardingRuleConfig);
            default:
                // != / NOT IN / LIKE / IS NULL 等无法把范围收窄，交给其他条件或全分片
                return null;
        }
    }

    private Set<Integer> single(Integer number, Map<Integer, String> available) {
        Set<Integer> candidates = new TreeSet<>();
        if (number != null && available.containsKey(number)) {
            candidates.add(number);
        }
        return candidates;
    }

    private Set<Integer> inValues(Object value, Map<Integer, String> available,
            ShardingRuleConfig shardingRuleConfig) {
        if (!(value instanceof Collection)) {
            return null;
        }
        Set<Integer> candidates = new TreeSet<>();
        boolean parsed = false;
        for (Object item : (Collection<?>) value) {
            Integer number = toShardNumber(shardingRuleConfig, item);
            if (number == null) {
                continue;
            }
            parsed = true;
            if (available.containsKey(number)) {
                candidates.add(number);
            }
        }
        return parsed ? candidates : null;
    }

    private Set<Integer> between(Object value, Map<Integer, String> available,
            ShardingRuleConfig shardingRuleConfig) {
        if (!(value instanceof Collection) || ((Collection<?>) value).size() != 2) {
            return null;
        }
        List<?> bounds = new ArrayList<>((Collection<?>) value);
        Integer lower = toShardNumber(shardingRuleConfig, bounds.get(0));
        Integer upper = toShardNumber(shardingRuleConfig, bounds.get(1));
        if (lower == null || upper == null) {
            return null;
        }
        if (lower > upper) {
            Integer swap = lower;
            lower = upper;
            upper = swap;
        }
        return window(lower, upper, available);
    }

    /**
     * 取 [lower, upper] 区间内的可用后缀。
     *
     * <p>边界可能多包含一个分片（例如 {@code id > 1999} 仍会带上 1999 所在分片），
     * 这是有意为之：宁可多扫一个分片，也不能漏掉边界值所在分片内的合法数据。</p>
     */
    private Set<Integer> window(Integer lower, Integer upper, Map<Integer, String> available) {
        Set<Integer> candidates = new TreeSet<>();
        for (Integer number : available.keySet()) {
            if (lower != null && number < lower) {
                continue;
            }
            if (upper != null && number > upper) {
                continue;
            }
            candidates.add(number);
        }
        return candidates;
    }

    private Set<Integer> intersect(Set<Integer> left, Set<Integer> right) {
        Set<Integer> result = new TreeSet<>(left);
        result.retainAll(right);
        return result;
    }

    /**
     * 已过滤好的可用物理表 → 「后缀数值 → 后缀」映射，按数值升序。
     */
    private Map<Integer, String> availableSuffixes(ShardingRuleConfig shardingRuleConfig) {
        Map<Integer, String> suffixes = new TreeMap<>();
        if (shardingRuleConfig == null || shardingRuleConfig.getAvailableTableNames() == null) {
            return suffixes;
        }
        for (String tableName : shardingRuleConfig.getAvailableTableNames()) {
            if (tableName == null) {
                continue;
            }
            String suffix = TableUtils.getSuffix(tableName);
            if (suffix.isEmpty()) {
                continue;
            }
            try {
                suffixes.put(Integer.valueOf(suffix), suffix);
            } catch (NumberFormatException e) {
                logger.warn("物理表 [{}] 的后缀 [{}] 不是合法数字，已忽略", tableName, suffix);
            }
        }
        return suffixes;
    }

    private static long suffixValue(String tableName) {
        String suffix = TableUtils.getSuffix(tableName);
        try {
            return Long.parseLong(suffix);
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * 把 Number / 数字字符串解析成 long，失败返回 null。
     */
    protected static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 取 Integer 配置值，为空时使用默认值。
     */
    protected static int valueOr(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
