package com.easyquery.web.service;

import com.easyquery.web.entity.DataSourceEntity;
import java.util.List;
import java.util.Optional;

public interface DataSourceService {
    List<DataSourceEntity> findAll();
    Optional<DataSourceEntity> findById(Long id);
    Optional<DataSourceEntity> findByName(String name);
    DataSourceEntity save(DataSourceEntity dataSource);
    void deleteById(Long id);
    void deleteByName(String name);
    boolean existsByName(String name);
}
