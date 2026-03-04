package com.easyquery.web.repository;

import com.easyquery.web.entity.ShardingRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShardingRuleRepository extends JpaRepository<ShardingRuleEntity, Long> {
    Optional<ShardingRuleEntity> findByName(String name);
    boolean existsByName(String name);
}
