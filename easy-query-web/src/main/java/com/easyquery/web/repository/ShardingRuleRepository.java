package com.easyquery.web.repository;

import com.easyquery.web.entity.ShardingRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShardingRuleRepository extends JpaRepository<ShardingRuleEntity, Long> {
    Optional<ShardingRuleEntity> findByName(String name);
    boolean existsById(Long id);
    
    /**
     * 根据数据源 ID 和逻辑表名判断分片规则是否存在
     * @param dataSourceId 数据源 ID
     * @param tableName 逻辑表名
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByDataSourceIdAndTableName(Long dataSourceId, String tableName);
}
