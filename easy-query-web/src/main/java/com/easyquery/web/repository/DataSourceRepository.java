package com.easyquery.web.repository;

import com.easyquery.web.entity.DataSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DataSourceRepository extends JpaRepository<DataSourceEntity, Long> {
    Optional<DataSourceEntity> findByName(String name);
    boolean existsByName(String name);

    /**
     * 按归属用户查询数据源
     * @param userId 用户 ID
     * @return 数据源列表
     */
    List<DataSourceEntity> findByUserId(Long userId);

    /**
     * 统计归属用户的的数据源数量
     * @param userId 用户 ID
     * @return 数量
     */
    long countByUserId(Long userId);
}
