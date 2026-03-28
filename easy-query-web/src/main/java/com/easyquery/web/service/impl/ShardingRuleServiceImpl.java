package com.easyquery.web.service.impl;

import com.easyquery.web.entity.ShardingRuleEntity;
import com.easyquery.web.repository.ShardingRuleRepository;
import com.easyquery.web.service.ShardingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ShardingRuleServiceImpl implements ShardingRuleService {

    @Autowired
    private ShardingRuleRepository shardingRuleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ShardingRuleEntity> findAll() {
        return shardingRuleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ShardingRuleEntity findById(Long id) {
        return shardingRuleRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public ShardingRuleEntity findByName(String name) {
        return shardingRuleRepository.findByName(name).orElse(null);
    }

    @Override
    public ShardingRuleEntity save(ShardingRuleEntity shardingRule) {
        return shardingRuleRepository.save(shardingRule);
    }

    @Override
    public void deleteById(Long id) {
        shardingRuleRepository.deleteById(id);
    }

    @Override
    public void deleteByName(String name) {
        shardingRuleRepository.findByName(name).ifPresent(shardingRule -> {
            shardingRuleRepository.deleteById(shardingRule.getId());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return shardingRuleRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDataSourceIdAndTableName(Long dataSourceId, String tableName) {
        return shardingRuleRepository.existsByDataSourceIdAndTableName(dataSourceId, tableName);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ShardingRuleEntity> findByDataSourceId(Long dataSourceId) {
        return shardingRuleRepository.findByDataSourceId(dataSourceId);
    }

    @Override
    public boolean existsByDataSourceIdAndTableNameExcludingId(Long dataSourceId, String tableName, Long excludeId) {
        return shardingRuleRepository.existsByDataSourceIdAndTableNameExcludingId(dataSourceId, tableName, excludeId);
    }
}
