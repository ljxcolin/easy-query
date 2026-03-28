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
}
