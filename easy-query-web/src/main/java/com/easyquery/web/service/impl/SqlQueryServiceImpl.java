package com.easyquery.web.service.impl;

import com.easyquery.web.entity.SqlQueryEntity;
import com.easyquery.web.repository.SqlQueryRepository;
import com.easyquery.web.service.SqlQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class SqlQueryServiceImpl implements SqlQueryService {

    @Autowired
    private SqlQueryRepository sqlQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SqlQueryEntity> findAll() {
        return sqlQueryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public SqlQueryEntity findById(Long id) {
        return sqlQueryRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SqlQueryEntity> findByDataSourceId(Long dataSourceId) {
        return sqlQueryRepository.findByDataSourceId(dataSourceId);
    }

    @Override
    public SqlQueryEntity save(SqlQueryEntity sqlQuery) {
        return sqlQueryRepository.save(sqlQuery);
    }

    @Override
    public void deleteById(Long id) {
        sqlQueryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return sqlQueryRepository.existsByName(name);
    }
}
