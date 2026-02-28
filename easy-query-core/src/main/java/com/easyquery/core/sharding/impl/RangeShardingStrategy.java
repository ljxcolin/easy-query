package com.easyquery.core.sharding.impl;

import com.easyquery.core.sharding.ShardingResult;
import com.easyquery.core.sharding.ShardingStrategy;
import com.easyquery.core.sharding.ShardingStrategyType;
import java.util.Map;
import java.util.TreeMap;

/**
 * 范围分片策略实现
 */
public class RangeShardingStrategy implements ShardingStrategy {
    
    /**
     * 数据源范围映射
     */
    private TreeMap<Comparable<?>, String> dataSourceRangeMap;
    
    /**
     * 表范围映射
     */
    private TreeMap<Comparable<?>, String> tableRangeMap;
    
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
        
        if (!(shardingKey instanceof Comparable)) {
            throw new IllegalArgumentException("Sharding key must be Comparable");
        }
        
        Comparable<?> key = (Comparable<?>) shardingKey;
        
        // 查找数据源
        String dataSourceName = findByRange(dataSourceRangeMap, key, defaultDataSource);
        
        // 查找表
        String tableName = findByRange(tableRangeMap, key, defaultTable);
        
        return new ShardingResult(dataSourceName, tableName);
    }
    
    /**
     * 根据范围查找对应的值
     */
    private <T extends Comparable<?>> String findByRange(TreeMap<Comparable<?>, String> rangeMap, T key, String defaultValue) {
        Map.Entry<Comparable<?>, String> entry = rangeMap.floorEntry(key);
        return entry != null ? entry.getValue() : defaultValue;
    }
    
    @Override
    public ShardingStrategyType getType() {
        return ShardingStrategyType.RANGE;
    }
    
    @Override
    public void init(Map<String, Object> config) {
        dataSourceRangeMap = new TreeMap<>();
        tableRangeMap = new TreeMap<>();
        
        // 初始化数据源范围
        if (config.containsKey("dataSourceRanges")) {
            Map<?, ?> dataSourceRanges = (Map<?, ?>) config.get("dataSourceRanges");
            for (Map.Entry<?, ?> entry : dataSourceRanges.entrySet()) {
                dataSourceRangeMap.put((Comparable<?>) entry.getKey(), (String) entry.getValue());
            }
        }
        
        // 初始化表范围
        if (config.containsKey("tableRanges")) {
            Map<?, ?> tableRanges = (Map<?, ?>) config.get("tableRanges");
            for (Map.Entry<?, ?> entry : tableRanges.entrySet()) {
                tableRangeMap.put((Comparable<?>) entry.getKey(), (String) entry.getValue());
            }
        }
        
        // 初始化默认值
        defaultDataSource = config.containsKey("defaultDataSource") ? (String) config.get("defaultDataSource") : "ds0";
        defaultTable = config.containsKey("defaultTable") ? (String) config.get("defaultTable") : "t0";
    }
}
