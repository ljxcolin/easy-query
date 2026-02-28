package com.easyquery.core.sharding;

import com.easyquery.core.sharding.impl.HashShardingStrategy;
import com.easyquery.core.sharding.impl.ListShardingStrategy;
import com.easyquery.core.sharding.impl.RangeShardingStrategy;
import java.util.Map;

/**
 * 分片策略工厂
 */
public class ShardingStrategyFactory {
    
    /**
     * 创建分片策略实例
     * @param strategyType 分片策略类型
     * @param config 分片策略配置
     * @return 分片策略实例
     */
    public static ShardingStrategy createStrategy(ShardingStrategyType strategyType, Map<String, Object> config) {
        ShardingStrategy strategy;
        
        switch (strategyType) {
            case HASH:
                strategy = new HashShardingStrategy();
                break;
            case RANGE:
                strategy = new RangeShardingStrategy();
                break;
            case LIST:
                strategy = new ListShardingStrategy();
                break;
            case CUSTOM:
                // 自定义分片策略，需要通过反射创建
                if (config.containsKey("customClassName")) {
                    try {
                        String className = (String) config.get("customClassName");
                        Class<?> clazz = Class.forName(className);
                        strategy = (ShardingStrategy) clazz.newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create custom sharding strategy", e);
                    }
                } else {
                    throw new IllegalArgumentException("Custom sharding strategy class name is required");
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported sharding strategy type: " + strategyType);
        }
        
        // 初始化分片策略
        strategy.init(config);
        
        return strategy;
    }
    
    /**
     * 创建默认的分片策略实例
     * @return 默认分片策略实例
     */
    public static ShardingStrategy createDefaultStrategy() {
        ShardingStrategy strategy = new HashShardingStrategy();
        strategy.init(java.util.Collections.emptyMap());
        return strategy;
    }
}
