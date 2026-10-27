package com.easyquery.core.sharding;

import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.enums.ConditionType;
import com.easyquery.core.enums.DatabaseType;
import com.easyquery.core.model.DataSourceConfig;
import com.easyquery.core.model.DataSourceEntry;
import com.easyquery.core.model.ShardingRuleConfig;
import com.easyquery.core.model.StrategyConfig;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 分片策略路由测试。
 *
 * <p>覆盖 RANGE / YEAR 的可用表过滤、条件推导、边界兜底，以及
 * {@link DataSourceEntry#refresh()} 面对异常规则时的隔离行为。</p>
 */
class ShardingStrategyTest {

    private static final List<String> RANGE_TABLES = Arrays.asList(
            "t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_10",
            "t_order_item", "t_order_log_0", "t_user_0");

    private static final List<String> YEAR_TABLES = Arrays.asList(
            "t_order_2022", "t_order_2023", "t_order_2024", "t_order_0", "t_order_item");

    @Test
    @DisplayName("RANGE：只看逻辑表同前缀、纯数字后缀的物理表，且按数值排序")
    void rangeShouldFilterAvailableTables() {
        ShardingRuleConfig config = rangeRule("t_order", 0, 1000, 0);

        strategy("RANGE").setAvailableTables(config, RANGE_TABLES);

        assertEquals(Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_10"),
                config.getAvailableTableNames());
    }

    @Test
    @DisplayName("RANGE：等值条件命中唯一分片，IN 命中多个分片")
    void rangeShouldRouteByEqualsAndIn() {
        ShardingStrategy strategy = strategy("RANGE");
        ShardingRuleConfig config = rangeRule("t_order", 0, 1000, 0);
        strategy.setAvailableTables(config, RANGE_TABLES);

        assertEquals(List.of("1"), strategy.doSharding(config, condition(ConditionType.EQUALS, 1500L)));
        assertEquals(List.of("0"), strategy.doSharding(config, condition(ConditionType.EQUALS, "999")));
        assertEquals(List.of("0", "2"),
                strategy.doSharding(config, condition(ConditionType.IN, Arrays.asList(100L, 2500L, 7777L))));
    }

    @Test
    @DisplayName("RANGE：比较与区间条件收窄到对应分片区间")
    void rangeShouldRouteByComparison() {
        ShardingStrategy strategy = strategy("RANGE");
        ShardingRuleConfig config = rangeRule("t_order", 0, 1000, 0);
        strategy.setAvailableTables(config, RANGE_TABLES);

        assertEquals(List.of("2", "3", "10"),
                strategy.doSharding(config, condition(ConditionType.GREATER_THAN, 2500L)));
        assertEquals(List.of("0", "1"),
                strategy.doSharding(config, condition(ConditionType.LESS_THAN, 1500L)));
        assertEquals(List.of("1", "2"),
                strategy.doSharding(config, condition(ConditionType.BETWEEN, Arrays.asList(1200L, 2800L))));
    }

    @Test
    @DisplayName("RANGE：无法定位或配置缺失时退化为全部分片，不抛异常")
    void rangeShouldFallbackToAllShards() {
        ShardingStrategy strategy = strategy("RANGE");

        ShardingRuleConfig config = rangeRule("t_order", 0, 1000, 0);
        strategy.setAvailableTables(config, RANGE_TABLES);
        assertEquals(Arrays.asList("0", "1", "2", "3", "10"),
                strategy.doSharding(config, condition(ConditionType.EQUALS, 99999L)));
        assertEquals(Arrays.asList("0", "1", "2", "3", "10"),
                strategy.doSharding(config, condition(ConditionType.LIKE, "1%")));
        assertEquals(Arrays.asList("0", "1", "2", "3", "10"),
                strategy.doSharding(config, new ArrayList<>()));

        ShardingRuleConfig broken = rangeRule("t_order", 0, null, 0);
        strategy.setAvailableTables(broken, RANGE_TABLES);
        assertEquals(Arrays.asList("0", "1", "2", "3", "10"),
                strategy.doSharding(broken, condition(ConditionType.EQUALS, 1500L)));
    }

    @Test
    @DisplayName("RANGE：shardingStart 作为后缀偏移量生效")
    void rangeShouldRespectShardingStart() {
        ShardingStrategy strategy = strategy("RANGE");
        ShardingRuleConfig config = rangeRule("t_user", 0, 100, 1);
        strategy.setAvailableTables(config, Arrays.asList("t_user_1", "t_user_2", "t_user_3"));

        assertEquals(List.of("3"), strategy.doSharding(config, condition(ConditionType.EQUALS, 250L)));
    }

    @Test
    @DisplayName("YEAR：4 位年份后缀才纳入可用表，0 后缀的哈希表不会被误收")
    void yearShouldFilterAvailableTables() {
        ShardingRuleConfig config = yearRule("t_order");

        strategy("YEAR").setAvailableTables(config, YEAR_TABLES);

        assertEquals(Arrays.asList("t_order_2022", "t_order_2023", "t_order_2024"),
                config.getAvailableTableNames());
    }

    @Test
    @DisplayName("YEAR：支持日期文本/数字/日期类型等多种分片键")
    void yearShouldExtractYearFromVariousValues() {
        ShardingStrategy strategy = strategy("YEAR");
        ShardingRuleConfig config = yearRule("t_order");
        strategy.setAvailableTables(config, YEAR_TABLES);

        assertEquals(List.of("2023"), strategy.doSharding(config, condition(ConditionType.EQUALS, "2023-05-06")));
        assertEquals(List.of("2023"),
                strategy.doSharding(config, condition(ConditionType.EQUALS, "2023-05-06 10:30:00")));
        assertEquals(List.of("2024"), strategy.doSharding(config, condition(ConditionType.EQUALS, 2024)));
        assertEquals(List.of("2024"), strategy.doSharding(config, condition(ConditionType.EQUALS, 20240315)));
        assertEquals(List.of("2022"),
                strategy.doSharding(config, condition(ConditionType.EQUALS, LocalDate.of(2022, 3, 1))));
    }

    @Test
    @DisplayName("YEAR：区间条件换算为年份区间，边界年份保留")
    void yearShouldRouteByRange() {
        ShardingStrategy strategy = strategy("YEAR");
        ShardingRuleConfig config = yearRule("t_order");
        strategy.setAvailableTables(config, YEAR_TABLES);

        assertEquals(List.of("2022", "2023"),
                strategy.doSharding(config, condition(ConditionType.LESS_THAN, "2023-06-01")));
        // < 2024-01-01 仍保留 2024：改写后的 SQL 保留原谓词，多扫一个分片不会多出数据
        assertEquals(Arrays.asList("2022", "2023", "2024"),
                strategy.doSharding(config, condition(ConditionType.LESS_THAN, "2024-01-01")));
        assertEquals(List.of("2023", "2024"),
                strategy.doSharding(config,
                        condition(ConditionType.BETWEEN, Arrays.asList("2023-01-01", "2024-12-31"))));
        assertEquals(List.of("2023"),
                strategy.doSharding(config,
                        condition(ConditionType.IN, Arrays.asList("2021-01-01", "2023-06-01"))));
    }

    @Test
    @DisplayName("refresh：单条规则异常不影响整个数据源加载")
    void refreshShouldIsolateBrokenRules() {
        ShardingRuleConfig rangeRule = rangeRule("t_order", 0, 1000, 0);
        ShardingRuleConfig unsupported = yearRule("t_log");
        unsupported.setStrategyType("MONTH");
        ShardingRuleConfig missingStep = rangeRule("t_user", 0, null, 0);

        List<ShardingRuleConfig> rules = new ArrayList<>(Arrays.asList(rangeRule, unsupported, missingStep));
        DataSourceEntry entry = new DataSourceEntry(1L, new DataSourceConfig(), rules);
        entry.setDatabaseAdapter(new FakeAdapter(Arrays.asList("t_order_0", "t_order_1", "t_user_0")));

        assertDoesNotThrow(entry::refresh);
        assertEquals(Arrays.asList("t_order_0", "t_order_1"), rangeRule.getAvailableTableNames());
        assertNull(unsupported.getAvailableTableNames(), "不支持的策略类型应被跳过");
        assertEquals(List.of("t_user_0"), missingStep.getAvailableTableNames());
    }

    @Test
    @DisplayName("HASH 行为保持不变")
    void hashShouldStillWork() {
        ShardingStrategy strategy = strategy("HASH");
        ShardingRuleConfig config = new ShardingRuleConfig();
        config.setTableName("t_user");
        config.setShardingColumn("id");
        config.setStrategyType("HASH");
        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig.setShardingCount(4);
        strategyConfig.setShardingStart(0);
        config.setStrategyConfig(strategyConfig);

        strategy.setAvailableTables(config, Arrays.asList("t_user_0", "t_user_1", "t_user_2", "t_user_3"));
        assertEquals(List.of("1"), strategy.doSharding(config, condition(ConditionType.EQUALS, 5L)));
        // HASH 无法收窄范围，比较条件退化为全部分片
        assertEquals(4, strategy.doSharding(config, condition(ConditionType.GREATER_THAN, 5L)).size());
    }

    // ---------- helpers ----------

    private static ShardingStrategy strategy(String type) {
        return ShardingStrategyFactory.getShardingStrategy(type);
    }

    private static ShardingRuleConfig rangeRule(String tableName, Integer rangeStart, Integer rangeStep,
            Integer shardingStart) {
        ShardingRuleConfig config = new ShardingRuleConfig();
        config.setTableName(tableName);
        config.setShardingColumn("id");
        config.setStrategyType("RANGE");
        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig.setRangeStart(rangeStart);
        strategyConfig.setRangeStep(rangeStep);
        strategyConfig.setShardingStart(shardingStart);
        config.setStrategyConfig(strategyConfig);
        return config;
    }

    private static ShardingRuleConfig yearRule(String tableName) {
        ShardingRuleConfig config = new ShardingRuleConfig();
        config.setTableName(tableName);
        config.setShardingColumn("create_time");
        config.setStrategyType("YEAR");
        config.setStrategyConfig(new StrategyConfig());
        return config;
    }

    private static List<SqlCondition> condition(ConditionType type, Object value) {
        List<SqlCondition> conditions = new ArrayList<>();
        conditions.add(new SqlCondition("id", type, value));
        return conditions;
    }

    /**
     * 只提供表列表的数据库适配器桩
     */
    private static class FakeAdapter implements DatabaseAdapter {

        private final List<String> tables;

        FakeAdapter(List<String> tables) {
            this.tables = tables;
        }

        @Override
        public void init(DataSourceConfig config) {
        }

        @Override
        public Connection getConnection() throws SQLException {
            throw new SQLException("测试桩不支持获取连接");
        }

        @Override
        public void releaseConnection(Connection connection) {
        }

        @Override
        public DatabaseType getDatabaseType() {
            return DatabaseType.MYSQL;
        }

        @Override
        public boolean testConnection() {
            return true;
        }

        @Override
        public List<String> getTables() {
            return tables;
        }
    }
}
