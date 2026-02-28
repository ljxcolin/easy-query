package com.easyquery.core.monitor.impl;

import com.easyquery.core.monitor.MonitorManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 默认监控管理器实现
 */
public class DefaultMonitorManager implements MonitorManager {
    
    /**
     * 监控指标
     */
    private final Map<String, AtomicLong> metrics;
    
    /**
     * 监控管理器配置
     */
    private Map<String, Object> config;
    
    public DefaultMonitorManager() {
        this.metrics = new HashMap<>();
        // 初始化监控指标
        initMetrics();
    }
    
    /**
     * 初始化监控指标
     */
    private void initMetrics() {
        metrics.put("totalQueries", new AtomicLong(0));
        metrics.put("successQueries", new AtomicLong(0));
        metrics.put("failedQueries", new AtomicLong(0));
        metrics.put("totalShardingOperations", new AtomicLong(0));
        metrics.put("totalQueryTime", new AtomicLong(0));
        metrics.put("totalShardingTime", new AtomicLong(0));
    }
    
    @Override
    public void recordQueryStart(String queryId, String sql) {
        // 记录查询开始
        metrics.get("totalQueries").incrementAndGet();
    }
    
    @Override
    public void recordQueryEnd(String queryId, long elapsedTime, boolean success, String errorMessage) {
        // 记录查询结束
        if (success) {
            metrics.get("successQueries").incrementAndGet();
        } else {
            metrics.get("failedQueries").incrementAndGet();
        }
        metrics.get("totalQueryTime").addAndGet(elapsedTime);
    }
    
    @Override
    public void recordShardingOperation(String queryId, String dataSourceName, String tableName, long elapsedTime) {
        // 记录分片操作
        metrics.get("totalShardingOperations").incrementAndGet();
        metrics.get("totalShardingTime").addAndGet(elapsedTime);
    }
    
    @Override
    public Map<String, Object> getMetrics() {
        // 转换监控指标
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, AtomicLong> entry : metrics.entrySet()) {
            result.put(entry.getKey(), entry.getValue().get());
        }
        
        // 计算平均值
        long totalQueries = metrics.get("totalQueries").get();
        if (totalQueries > 0) {
            result.put("avgQueryTime", metrics.get("totalQueryTime").get() / totalQueries);
        }
        
        long totalShardingOperations = metrics.get("totalShardingOperations").get();
        if (totalShardingOperations > 0) {
            result.put("avgShardingTime", metrics.get("totalShardingTime").get() / totalShardingOperations);
        }
        
        return Collections.unmodifiableMap(result);
    }
    
    @Override
    public void init(Map<String, Object> config) {
        this.config = config;
    }
}
