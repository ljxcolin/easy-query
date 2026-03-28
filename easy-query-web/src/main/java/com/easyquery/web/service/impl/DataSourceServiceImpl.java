package com.easyquery.web.service.impl;

import com.easyquery.web.entity.DataSourceEntity;
import com.easyquery.web.repository.DataSourceRepository;
import com.easyquery.web.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DataSourceServiceImpl implements DataSourceService {

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DataSourceEntity> findAll() {
        return dataSourceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public DataSourceEntity findById(Long id) {
        return dataSourceRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public DataSourceEntity findByName(String name) {
        return dataSourceRepository.findByName(name).orElse(null);
    }

    @Override
    public DataSourceEntity save(DataSourceEntity dataSource) {
        return dataSourceRepository.save(dataSource);
    }

    @Override
    public void deleteById(Long id) {
        dataSourceRepository.deleteById(id);
    }

    @Override
    public void deleteByName(String name) {
        dataSourceRepository.findByName(name).ifPresent(dataSource -> {
            dataSourceRepository.deleteById(dataSource.getId());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return dataSourceRepository.existsByName(name);
    }
}
