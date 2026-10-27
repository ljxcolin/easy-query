package com.easyquery.web.service;

import com.easyquery.web.entity.DataSourceEntity;
import java.util.List;

public interface DataSourceService {
    List<DataSourceEntity> findAll();
    DataSourceEntity findById(Long id);
    DataSourceEntity findByName(String name);
    DataSourceEntity save(DataSourceEntity dataSource);
    void deleteById(Long id);
    void deleteByName(String name);
    boolean existsByName(String name);

    /**
     * 按归属用户查询数据源
     * @param userId 用户 ID
     * @return 数据源列表
     */
    List<DataSourceEntity> findByUserId(Long userId);

    /**
     * 统计归属用户的数据源数量
     * @param userId 用户 ID
     * @return 数量
     */
    long countByUserId(Long userId);
}
