package com.easyquery.core.sharding;

/**
 * 分片策略类型
 */
public enum ShardingStrategyType {
    
    /**
     * 哈希分片
     */
    HASH,
    
    /**
     * 范围分片
     */
    RANGE,
    
    /**
     * 列表分片
     */
    LIST,
    
    /**
     * 自定义分片
     */
    CUSTOM
    
}
