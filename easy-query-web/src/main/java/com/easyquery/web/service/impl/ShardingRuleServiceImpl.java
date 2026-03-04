package com.easyquery.web.service.impl;

import com.easyquery.web.entity.ShardingRuleEntity;
import com.easyquery.web.repository.ShardingRuleRepository;
import com.easyquery.web.service.ShardingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

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
    public Optional<ShardingRuleEntity> findById(Long id) {
        return shardingRuleRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShardingRuleEntity> findByName(String name) {
        return shardingRuleRepository.findByName(name);
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
    public boolean existsByName(String name) {
        return shardingRuleRepository.existsByName(name);
    }
}
