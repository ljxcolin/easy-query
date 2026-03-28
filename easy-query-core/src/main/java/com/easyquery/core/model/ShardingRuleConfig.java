package com.easyquery.core.model;

import java.util.List;

public class ShardingRuleConfig {
    private Long id;
    private Long dataSourceId;
    private String dataSourceName;
    private String tableName;
    private String shardingColumn;
    private String strategyType;
    private StrategyConfig strategyConfig;
    private List<String> availableTableNames;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getShardingColumn() {
        return shardingColumn;
    }

    public void setShardingColumn(String shardingColumn) {
        this.shardingColumn = shardingColumn;
    }

    public String getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(String strategyType) {
        this.strategyType = strategyType;
    }

    public StrategyConfig getStrategyConfig() {
        return strategyConfig;
    }

    public void setStrategyConfig(StrategyConfig strategyConfig) {
        this.strategyConfig = strategyConfig;
    }

    public List<String> getAvailableTableNames() {
        return availableTableNames;
    }

    public void setAvailableTableNames(List<String> availableTableNames) {
        this.availableTableNames = availableTableNames;
    }
}
