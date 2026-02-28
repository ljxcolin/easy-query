package com.easyquery.core.sharding.impl;

import com.easyquery.core.sharding.ShardingResult;
import com.easyquery.core.sharding.ShardingStrategy;
import com.easyquery.core.sharding.ShardingStrategyType;
import java.util.Map;

/**
 * 哈希分片策略实现
 */
public class HashShardingStrategy implements ShardingStrategy {
    
    /**
     * 数据源数量
     */
    private int dataSourceCount;
    
    /**
     * 表数量
     */
    private int tableCount;
    
    /**
     * 数据源前缀
     */
    private String dataSourcePrefix = "ds";
    
    /**
     * 表前缀
     */
    private String tablePrefix = "t";
    
    @Override
    public ShardingResult doSharding(Object shardingKey, Map<String, Object> shardingParams) {
        if (shardingKey == null) {
            throw new IllegalArgumentException("Sharding key cannot be null");
        }
        
        int hashCode = shardingKey.hashCode();
        
        // 计算数据源索引
        int dataSourceIndex = Math.abs(hashCode) % dataSourceCount;
        String dataSourceName = dataSourcePrefix + dataSourceIndex;
        
        // 计算表索引
        int tableIndex = Math.abs(hashCode) % tableCount;
        String tableName = tablePrefix + tableIndex;
        
        return new ShardingResult(dataSourceName, tableName);
    }
    
    @Override
    public ShardingStrategyType getType() {
        return ShardingStrategyType.HASH;
    }
    
    @Override
    public void init(Map<String, Object> config) {
        if (config.containsKey("dataSourceCount")) {
            dataSourceCount = (int) config.get("dataSourceCount");
        } else {
            dataSourceCount = 2;
        }
        
        if (config.containsKey("tableCount")) {
            tableCount = (int) config.get("tableCount");
        } else {
            tableCount = 4;
        }
        
        if (config.containsKey("dataSourcePrefix")) {
            dataSourcePrefix = (String) config.get("dataSourcePrefix");
        }
        
        if (config.containsKey("tablePrefix")) {
            tablePrefix = (String) config.get("tablePrefix");
        }
    }
}
