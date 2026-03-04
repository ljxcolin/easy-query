package com.easyquery.web.service;

import com.easyquery.web.entity.ShardingRuleEntity;
import java.util.List;
import java.util.Optional;

public interface ShardingRuleService {
    List<ShardingRuleEntity> findAll();
    Optional<ShardingRuleEntity> findById(Long id);
    Optional<ShardingRuleEntity> findByName(String name);
    ShardingRuleEntity save(ShardingRuleEntity shardingRule);
    void deleteById(Long id);
    void deleteByName(String name);
    boolean existsByName(String name);
}
