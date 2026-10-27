package com.easyquery.core.model;

import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.sharding.ShardingStrategy;
import com.easyquery.core.sharding.ShardingStrategyFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lijinxuan
 * @date 2026/3/9 17:00
 */
public class DataSourceEntry {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceEntry.class);

    private Long id;
    private DataSourceConfig dataSourceConfig;
    private List<ShardingRuleConfig> shardingRuleConfigs;
    private DatabaseAdapter databaseAdapter;
    private List<String> tables;

    public DataSourceEntry(Long id, DataSourceConfig dataSourceConfig, List<ShardingRuleConfig> shardingRuleConfigs) {
        this.id = id;
        this.dataSourceConfig = dataSourceConfig;
        this.shardingRuleConfigs = shardingRuleConfigs == null ? new ArrayList<>() : shardingRuleConfigs;
    }

    public Long getId() {
        return id;
    }

    public DataSourceConfig getDataSourceConfig() {
        return dataSourceConfig;
    }

    public List<ShardingRuleConfig> getShardingRuleConfigs() {
        return shardingRuleConfigs;
    }

    public void setDatabaseAdapter(DatabaseAdapter databaseAdapter) {
        this.databaseAdapter = databaseAdapter;
    }

    public DatabaseAdapter getDatabaseAdapter() {
        return databaseAdapter;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public List<String> getTables() {
        return tables;
    }

    /**
     * 刷新数据源的物理表列表，并重新计算每条分片规则的可用表。
     *
     * <p>单条规则失败（策略不支持、配置缺失、实现抛异常）只影响该规则本身，
     * 不会中断整个数据源的加载。</p>
     */
    public void refresh() throws Exception {
        if (databaseAdapter == null) {
            logger.warn("DataSourceEntry [{}] 数据库适配器未初始化，无法刷新", id);
            return;
        }

        List<String> allTables = databaseAdapter.getTables();
        this.tables = allTables;
        logger.info("DataSourceEntry [{}] 刷新表列表成功，共 {} 个表", id, allTables.size());

        if (shardingRuleConfigs == null || shardingRuleConfigs.isEmpty()) {
            return;
        }

        for (ShardingRuleConfig ruleConfig : shardingRuleConfigs) {
            refreshShardingRule(ruleConfig);
        }
    }

    /**
     * 重新计算单条分片规则的可用物理表。
     *
     * <p>刻意吞掉所有异常：一条坏规则不应该让整个数据源不可用（历史上就因为未实现的策略
     * 直接抛 {@link UnsupportedOperationException} 导致数据源加载整体失败）。</p>
     *
     * @param ruleConfig 分片规则配置
     */
    public void refreshShardingRule(ShardingRuleConfig ruleConfig) {
        if (ruleConfig == null) {
            return;
        }

        String tableName = ruleConfig.getTableName();
        if (tableName == null || tableName.isEmpty()) {
            logger.warn("DataSourceEntry [{}] 存在逻辑表名为空的分片规则，已跳过", id);
            return;
        }

        ShardingStrategy shardingStrategy = ShardingStrategyFactory.getShardingStrategy(ruleConfig.getStrategyType());
        if (shardingStrategy == null) {
            logger.warn("分片规则 [{}] 的策略类型 [{}] 暂不支持，已跳过该规则", tableName, ruleConfig.getStrategyType());
            return;
        }

        try {
            shardingStrategy.setAvailableTables(ruleConfig, tables);
            List<String> availableTableNames = ruleConfig.getAvailableTableNames();
            int size = availableTableNames == null ? 0 : availableTableNames.size();
            if (size == 0) {
                logger.warn("分片规则 [{}] 未匹配到可用物理表，请检查物理表命名是否为「逻辑表名_后缀」", tableName);
            } else {
                logger.info("分片规则 [{}] 匹配到 {} 个可用表: {}", tableName, size, availableTableNames);
            }
        } catch (Exception e) {
            logger.error("分片规则 [{}] 初始化可用表失败，已跳过该规则", tableName, e);
            ruleConfig.setAvailableTableNames(new ArrayList<>());
        }
    }
}
