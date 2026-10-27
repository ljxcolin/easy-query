package com.easyquery.core.rewriter;

import com.easyquery.core.model.DataSourceConfig;
import com.easyquery.core.model.DataSourceEntry;
import com.easyquery.core.model.ShardingRuleConfig;
import com.easyquery.core.model.StrategyConfig;
import com.easyquery.core.parser.SqlParser;
import com.easyquery.core.parser.SqlParserFactory;
import com.easyquery.core.rewriter.impl.SelectSqlRewriter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SELECT 分片改写测试：校验「逻辑表 → 物理表」替换、UNION ALL 拼接与谓词保留。
 */
class SelectSqlRewriterTest {

    private final SqlParser parser = SqlParserFactory.createDefaultParser();

    private final SqlRewriter rewriter = new SelectSqlRewriter(parser);

    @Test
    @DisplayName("RANGE + BETWEEN：只改写落在区间内的分片，且保留原谓词")
    void shouldRewriteRangeBetween() throws Exception {
        DataSourceEntry entry = entry(rangeRule("t_order", 0, 1000),
                Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3"));

        String rewritten = rewrite(entry, "SELECT * FROM t_order WHERE id BETWEEN 1200 AND 2800");

        assertTrue(rewritten.contains("t_order_1"), rewritten);
        assertTrue(rewritten.contains("t_order_2"), rewritten);
        assertFalse(rewritten.contains("t_order_0"), rewritten);
        assertFalse(rewritten.contains("t_order_3"), rewritten);
        assertTrue(rewritten.toUpperCase().contains("UNION ALL"), rewritten);
        assertTrue(rewritten.contains("BETWEEN 1200 AND 2800"), "谓词必须原样保留：" + rewritten);
    }

    @Test
    @DisplayName("YEAR + 时间区间：只扫年份命中的物理表")
    void shouldRewriteYearRange() throws Exception {
        DataSourceEntry entry = entry(yearRule("t_order"),
                Arrays.asList("t_order_2022", "t_order_2023", "t_order_2024"));

        String rewritten = rewrite(entry,
                "SELECT * FROM t_order WHERE create_time >= '2023-01-01' AND create_time < '2025-01-01'");

        assertTrue(rewritten.contains("t_order_2023"), rewritten);
        assertTrue(rewritten.contains("t_order_2024"), rewritten);
        assertFalse(rewritten.contains("t_order_2022"), rewritten);
    }

    @Test
    @DisplayName("没有匹配的分片键：退化为全部可用分片")
    void shouldFallbackToAllShardsWhenShardingKeyMissing() throws Exception {
        DataSourceEntry entry = entry(yearRule("t_order"),
                Arrays.asList("t_order_2022", "t_order_2023"));

        String rewritten = rewrite(entry, "SELECT * FROM t_order WHERE status = 1");

        assertTrue(rewritten.contains("t_order_2022"), rewritten);
        assertTrue(rewritten.contains("t_order_2023"), rewritten);
    }

    @Test
    @DisplayName("表上没有分片规则：原样返回")
    void shouldKeepSqlWhenNoRuleMatched() throws Exception {
        DataSourceEntry entry = entry(rangeRule("t_order", 0, 1000),
                Arrays.asList("t_order_0", "t_order_1"));

        String rewritten = rewrite(entry, "SELECT * FROM t_user WHERE id = 1");

        assertEquals(1, countOccurrences(rewritten.toUpperCase(), "SELECT"), rewritten);
        assertTrue(rewritten.contains("t_user"), rewritten);
        assertFalse(rewritten.toUpperCase().contains("UNION"), rewritten);
    }

    // ---------- helpers ----------

    private String rewrite(DataSourceEntry entry, String sql) throws Exception {
        return rewriter.rewrite(entry, parser.parse(sql));
    }

    private DataSourceEntry entry(ShardingRuleConfig rule, List<String> tables) throws Exception {
        DataSourceEntry entry = new DataSourceEntry(1L, new DataSourceConfig(),
                new ArrayList<>(List.of(rule)));
        entry.setTables(tables);
        entry.refreshShardingRule(rule);
        return entry;
    }

    private ShardingRuleConfig rangeRule(String tableName, Integer rangeStart, Integer rangeStep) {
        ShardingRuleConfig config = new ShardingRuleConfig();
        config.setTableName(tableName);
        config.setShardingColumn("id");
        config.setStrategyType("RANGE");
        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig.setRangeStart(rangeStart);
        strategyConfig.setRangeStep(rangeStep);
        strategyConfig.setShardingStart(0);
        config.setStrategyConfig(strategyConfig);
        return config;
    }

    private ShardingRuleConfig yearRule(String tableName) {
        ShardingRuleConfig config = new ShardingRuleConfig();
        config.setTableName(tableName);
        config.setShardingColumn("create_time");
        config.setStrategyType("YEAR");
        config.setStrategyConfig(new StrategyConfig());
        return config;
    }

    private int countOccurrences(String text, String keyword) {
        int count = 0;
        int index = text.indexOf(keyword);
        while (index >= 0) {
            count++;
            index = text.indexOf(keyword, index + keyword.length());
        }
        return count;
    }
}
