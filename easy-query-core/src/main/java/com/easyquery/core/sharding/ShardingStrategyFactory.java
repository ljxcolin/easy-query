package com.easyquery.core.sharding;

import com.easyquery.core.sharding.impl.HashShardingStrategy;
import com.easyquery.core.sharding.impl.YearShardingStrategy;
import com.easyquery.core.sharding.impl.RangeShardingStrategy;
import com.easyquery.core.enums.StrategyType;

import java.util.Map;

/**
 * 分片策略工厂
 */
public class ShardingStrategyFactory {
    
    private static final Map<String, ShardingStrategy> SHARDING_STRATEGIES = Map.of(
        StrategyType.HASH.name(), new HashShardingStrategy(),
        StrategyType.RANGE.name(), new RangeShardingStrategy(),
        StrategyType.YEAR.name(), new YearShardingStrategy()
    );

    public static ShardingStrategy getShardingStrategy(String strategyType) {
        return SHARDING_STRATEGIES.get(strategyType);
    }
}
