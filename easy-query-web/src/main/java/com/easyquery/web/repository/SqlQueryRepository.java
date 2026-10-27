package com.easyquery.web.repository;

import com.easyquery.web.entity.SqlQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SqlQueryRepository extends JpaRepository<SqlQueryEntity, Long> {
    List<SqlQueryEntity> findByDataSourceId(Long dataSourceId);
    Optional<SqlQueryEntity> findByName(String name);
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
