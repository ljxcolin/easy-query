package com.easyquery.core.model;

import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.sharding.ShardingStrategy;
import com.easyquery.core.sharding.ShardingStrategyFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        this.shardingRuleConfigs = shardingRuleConfigs;
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

    public void refresh() throws Exception {
        if (databaseAdapter == null) {
            logger.warn("DataSourceEntry [{}] 数据库适配器未初始化，无法刷新", id);
            return;
        }

        List<String> allTables = databaseAdapter.getTables();
        this.tables = allTables;
        logger.info("DataSourceEntry [{}] 刷新表列表成功，共 {} 个表", id, allTables.size());

        if (shardingRuleConfigs != null) {
            for (ShardingRuleConfig ruleConfig : shardingRuleConfigs) {
                String tableName = ruleConfig.getTableName();
                if (tableName != null && !tableName.isEmpty()) {
                    ShardingStrategy shardingStrategy = ShardingStrategyFactory.getShardingStrategy(ruleConfig.getStrategyType());
                    shardingStrategy.setAvailableTables(ruleConfig, allTables);
                    logger.info("分片规则 [{}] 匹配到 {} 个可用表: {}",
                            tableName, ruleConfig.getAvailableTableNames().size(), ruleConfig.getAvailableTableNames());    
                }
            }
        }
    }
}
