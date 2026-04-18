package com.easyquery.core.sharding.impl;

import com.easyquery.core.sharding.SqlCondition;
import com.easyquery.core.enums.StrategyType;
import com.easyquery.core.model.ShardingRuleConfig;
import com.easyquery.core.sharding.ShardingStrategy;

import java.util.List;

/**
 * 范围分片策略实现
 */
public class RangeShardingStrategy implements ShardingStrategy {
    
        @Override
    public List<String> doSharding(ShardingRuleConfig shardingRuleConfig, List<SqlCondition> conditions) {
        
        
        return null;
    }
    
    @Override
    public StrategyType getType() {
        return StrategyType.RANGE;
    }

    @Override
    public void setAvailableTables(ShardingRuleConfig shardingRuleConfig, List<String> tables) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setAvailableTables'");
    }
}
