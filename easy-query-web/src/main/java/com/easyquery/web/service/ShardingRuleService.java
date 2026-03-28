package com.easyquery.web.service;

import com.easyquery.web.entity.ShardingRuleEntity;
import java.util.List;

public interface ShardingRuleService {
    List<ShardingRuleEntity> findAll();
    ShardingRuleEntity findById(Long id);
    ShardingRuleEntity findByName(String name);
    ShardingRuleEntity save(ShardingRuleEntity shardingRule);
    void deleteById(Long id);
    void deleteByName(String name);
    boolean existsById(Long id);
    
    /**
     * 根据数据源 ID 和逻辑表名判断分片规则是否存在
     * @param dataSourceId 数据源 ID
     * @param tableName 逻辑表名
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByDataSourceIdAndTableName(Long dataSourceId, String tableName);
    
    /**
     * 根据数据源 ID 和逻辑表名判断分片规则是否存在，排除指定ID的记录
     * @param dataSourceId 数据源 ID
     * @param tableName 逻辑表名
     * @param excludeId 排除的记录ID
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByDataSourceIdAndTableNameExcludingId(Long dataSourceId, String tableName, Long excludeId);
    
    /**
     * 根据数据源 ID 查询分片规则
     * @param dataSourceId 数据源 ID
     * @return 分片规则列表
     */
    List<ShardingRuleEntity> findByDataSourceId(Long dataSourceId);
}
