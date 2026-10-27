package com.easyquery.web.vo;

import com.easyquery.web.entity.ShardingRuleEntity;

import java.time.LocalDateTime;

/**
 * 分片规则响应对象（出参 VO）
 * <p>
 * 不暴露 userId 等内部字段。
 * </p>
 *
 * @author easy-query
 */
public class ShardingRuleVO {

    private Long id;
    private String name;
    private Long dataSourceId;
    private String dataSourceName;
    private String tableName;
    private String shardingColumn;
    private String strategyType;
    private String strategyConfig;
    private String description;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    /**
     * 实体转 VO
     *
     * @param entity 分片规则实体
     * @return VO，entity 为 null 时返回 null
     */
    public static ShardingRuleVO from(ShardingRuleEntity entity) {
        if (entity == null) {
            return null;
        }
        ShardingRuleVO vo = new ShardingRuleVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDataSourceId(entity.getDataSourceId());
        vo.setDataSourceName(entity.getDataSourceName());
        vo.setTableName(entity.getTableName());
        vo.setShardingColumn(entity.getShardingColumn());
        vo.setStrategyType(entity.getStrategyType());
        vo.setStrategyConfig(entity.getStrategyConfig());
        vo.setDescription(entity.getDescription());
        vo.setCreatedTime(entity.getCreatedTime());
        vo.setUpdatedTime(entity.getUpdatedTime());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}
