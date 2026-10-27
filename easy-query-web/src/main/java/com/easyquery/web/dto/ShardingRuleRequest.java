package com.easyquery.web.dto;

/**
 * 分片规则创建/更新请求（入参 DTO）
 *
 * @author easy-query
 */
public class ShardingRuleRequest {

    /** 规则名称 */
    private String name;

    /** 所属数据源 ID */
    private Long dataSourceId;

    /** 逻辑表名 */
    private String tableName;

    /** 分片字段 */
    private String shardingColumn;

    /** 策略类型：HASH / RANGE / YEAR */
    private String strategyType;

    /** 策略配置（JSON 字符串） */
    private String strategyConfig;

    /** 描述 */
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
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

    public String getStrategyConfig() {
        return strategyConfig;
    }

    public void setStrategyConfig(String strategyConfig) {
        this.strategyConfig = strategyConfig;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
