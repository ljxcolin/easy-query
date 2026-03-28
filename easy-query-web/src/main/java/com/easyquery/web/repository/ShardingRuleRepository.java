package com.easyquery.web.repository;

import com.easyquery.web.entity.ShardingRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    
    /**
     * 根据数据源 ID 和逻辑表名判断分片规则是否存在，排除指定ID的记录
     * @param dataSourceId 数据源 ID
     * @param tableName 逻辑表名
     * @param excludeId 排除的记录ID
     * @return 如果存在返回 true，否则返回 false
     */
    @Query("SELECT COUNT(s) > 0 FROM ShardingRuleEntity s " +
           "WHERE s.dataSourceId = :dataSourceId AND s.tableName = :tableName AND s.id != :excludeId")
    boolean existsByDataSourceIdAndTableNameExcludingId(@Param("dataSourceId") Long dataSourceId, 
                                                         @Param("tableName") String tableName, 
                                                         @Param("excludeId") Long excludeId);
    
    /**
     * 根据数据源 ID 查询分片规则
     * @param dataSourceId 数据源 ID
     * @return 分片规则列表
     */
    List<ShardingRuleEntity> findByDataSourceId(Long dataSourceId);
}
