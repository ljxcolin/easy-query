package com.easyquery.web.service;

import com.easyquery.web.entity.ShardingRuleEntity;
import java.util.List;
import java.util.Optional;

public interface ShardingRuleService {
    List<ShardingRuleEntity> findAll();
    Optional<ShardingRuleEntity> findById(Long id);
    Optional<ShardingRuleEntity> findByName(String name);
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
}
