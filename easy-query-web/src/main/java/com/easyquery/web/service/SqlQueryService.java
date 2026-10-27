package com.easyquery.web.service;

import com.easyquery.web.entity.SqlQueryEntity;
import java.util.List;

public interface SqlQueryService {
    List<SqlQueryEntity> findAll();
    SqlQueryEntity findById(Long id);
    List<SqlQueryEntity> findByDataSourceId(Long dataSourceId);
    SqlQueryEntity save(SqlQueryEntity sqlQuery);
    void deleteById(Long id);
    boolean existsByName(String name);

    /**
     * 按归属用户查询 SQL 查询
     * @param userId 用户 ID
     * @return SQL 查询列表
     */
    List<SqlQueryEntity> findByUserId(Long userId);

    /**
     * 按归属用户与数据源查询 SQL 查询
     * @param userId 用户 ID
     * @param dataSourceId 数据源 ID
     * @return SQL 查询列表
     */
    List<SqlQueryEntity> findByUserIdAndDataSourceId(Long userId, Long dataSourceId);

    /**
     * 统计归属用户的 SQL 查询数量
     * @param userId 用户 ID
     * @return 数量
     */
    long countByUserId(Long userId);
}
