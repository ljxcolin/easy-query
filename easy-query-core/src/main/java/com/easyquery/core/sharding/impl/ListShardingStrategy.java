package com.easyquery.core.sharding.impl;

import com.easyquery.core.sharding.ShardingResult;
import com.easyquery.core.sharding.ShardingStrategy;
import com.easyquery.core.sharding.ShardingStrategyType;
import java.util.List;
import java.util.Map;

/**
 * 列表分片策略实现
 */
public class ListShardingStrategy implements ShardingStrategy {
    
    /**
     * 数据源映射
     */
    private Map<Object, String> dataSourceMap;
    
    /**
     * 表映射
     */
    private Map<Object, String> tableMap;
    
    /**
     * 默认数据源
     */
    private String defaultDataSource;
    
    /**
     * 默认表
     */
    private String defaultTable;
    
    @Override
    public ShardingResult doSharding(Object shardingKey, Map<String, Object> shardingParams) {
        if (shardingKey == null) {
            throw new IllegalArgumentException("Sharding key cannot be null");
        }
        
        // 查找数据源
        String dataSourceName = dataSourceMap.getOrDefault(shardingKey, defaultDataSource);
        
        // 查找表
        String tableName = tableMap.getOrDefault(shardingKey, defaultTable);
        
        return new ShardingResult(dataSourceName, tableName);
    }
    
    @Override
    public ShardingStrategyType getType() {
        return ShardingStrategyType.LIST;
    }
    
    @Override
    public void init(Map<String, Object> config) {
        dataSourceMap = new java.util.HashMap<>();
        tableMap = new java.util.HashMap<>();
        
        // 初始化数据源映射
        if (config.containsKey("dataSourceMappings")) {
            Map<?, ?> dataSourceMappings = (Map<?, ?>) config.get("dataSourceMappings");
            for (Map.Entry<?, ?> entry : dataSourceMappings.entrySet()) {
                if (entry.getValue() instanceof List) {
                    List<?> values = (List<?>) entry.getValue();
                    for (Object value : values) {
                        dataSourceMap.put(value, (String) entry.getKey());
                    }
                } else {
                    dataSourceMap.put(entry.getValue(), (String) entry.getKey());
                }
            }
        }
        
        // 初始化表映射
        if (config.containsKey("tableMappings")) {
            Map<?, ?> tableMappings = (Map<?, ?>) config.get("tableMappings");
            for (Map.Entry<?, ?> entry : tableMappings.entrySet()) {
                if (entry.getValue() instanceof List) {
                    List<?> values = (List<?>) entry.getValue();
                    for (Object value : values) {
                        tableMap.put(value, (String) entry.getKey());
                    }
                } else {
                    tableMap.put(entry.getValue(), (String) entry.getKey());
                }
            }
        }
        
        // 初始化默认值
        defaultDataSource = config.containsKey("defaultDataSource") ? (String) config.get("defaultDataSource") : "ds0";
        defaultTable = config.containsKey("defaultTable") ? (String) config.get("defaultTable") : "t0";
    }
}
