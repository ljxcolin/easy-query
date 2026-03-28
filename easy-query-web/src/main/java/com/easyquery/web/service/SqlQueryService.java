package com.easyquery.web.service;

import com.easyquery.web.entity.SqlQueryEntity;
import java.util.List;

public interface SqlQueryService {
    List<SqlQueryEntity> findAll();
    SqlQueryEntity findById(Long id);
    List<SqlQueryEntity> findByDataSourceName(String dataSourceName);
    SqlQueryEntity save(SqlQueryEntity sqlQuery);
    void deleteById(Long id);
    boolean existsByName(String name);
}
