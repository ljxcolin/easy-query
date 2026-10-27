package com.easyquery.core.sharding.impl;

import com.easyquery.core.enums.StrategyType;
import com.easyquery.core.model.ShardingRuleConfig;
import com.easyquery.core.model.StrategyConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 范围分片策略。
 *
 * <p>按分片键数值落在哪个区间来定位物理表：</p>
 * <pre>
 * 后缀数值 = floor((分片键 - rangeStart) / rangeStep) + shardingStart
 * </pre>
 *
 * <p>配置示例（{@code t_order_0 ~ t_order_3}，0~999 落 t_order_0，依次类推）：</p>
 * <pre>
 * {"rangeStart": 0, "rangeStep": 1000, "shardingStart": 0}
 * </pre>
 *
 * <p>支持的条件：{@code =}、{@code IN}、{@code >}、{@code >=}、{@code <}、{@code <=}、{@code BETWEEN}。
 * 其中比较类条件会换算成「后缀数值区间」，只扫描区间内的分片。
 * 分片键必须是数值（Number 或纯数字字符串）。</p>
 */
public class RangeShardingStrategy extends AbstractNumericSuffixShardingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(RangeShardingStrategy.class);

    @Override
    public StrategyType getType() {
        return StrategyType.RANGE;
    }

    @Override
    protected String suffixPattern() {
        return "\\d+";
    }

    @Override
    protected void validateConfig(ShardingRuleConfig shardingRuleConfig) {
        StrategyConfig config = shardingRuleConfig.getStrategyConfig();
        if (config == null || config.getRangeStep() == null || config.getRangeStep() <= 0) {
            logger.warn("RANGE 分片规则 [{}] 缺少合法的 rangeStep 配置，查询时将退化为全分片扫描",
                    shardingRuleConfig.getTableName());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>分片键低于 {@code rangeStart} 或超出已配置区间时会得到不存在的后缀数值，
     * 上层因此退化为全分片，不会抛异常。</p>
     */
    @Override
    protected Integer toShardNumber(ShardingRuleConfig shardingRuleConfig, Object value) {
        Long numeric = toLong(value);
        if (numeric == null) {
            return null;
        }

        StrategyConfig config = shardingRuleConfig.getStrategyConfig();
        if (config == null || config.getRangeStep() == null || config.getRangeStep() <= 0) {
            return null;
        }

        long rangeStart = valueOr(config.getRangeStart(), 0);
        long rangeStep = config.getRangeStep();
        int shardingStart = valueOr(config.getShardingStart(), 0);

        long index = Math.floorDiv(numeric - rangeStart, rangeStep);
        if (index > Integer.MAX_VALUE - (long) shardingStart || index < Integer.MIN_VALUE) {
            return null;
        }
        return (int) index + shardingStart;
    }
}
