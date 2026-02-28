package com.easyquery.core.sharding;

import java.util.Map;

/**
 * 分片策略接口
 */
public interface ShardingStrategy {
    
    /**
     * 根据分片键值计算分片
     * @param shardingKey 分片键值
     * @param shardingParams 分片参数
     * @return 分片结果
     */
    ShardingResult doSharding(Object shardingKey, Map<String, Object> shardingParams);
    
    /**
     * 获取分片策略类型
     * @return 分片策略类型
     */
    ShardingStrategyType getType();
    
    /**
     * 初始化分片策略
     * @param config 分片策略配置
     */
    void init(Map<String, Object> config);
}
