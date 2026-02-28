package com.easyquery.core.monitor;

import java.util.Map;

/**
 * 监控管理器接口
 */
public interface MonitorManager {
    
    /**
     * 记录查询开始
     * @param queryId 查询ID
     * @param sql SQL语句
     */
    void recordQueryStart(String queryId, String sql);
    
    /**
     * 记录查询结束
     * @param queryId 查询ID
     * @param elapsedTime 耗时（毫秒）
     * @param success 是否成功
     * @param errorMessage 错误信息
     */
    void recordQueryEnd(String queryId, long elapsedTime, boolean success, String errorMessage);
    
    /**
     * 记录分片操作
     * @param queryId 查询ID
     * @param dataSourceName 数据源名称
     * @param tableName 表名
     * @param elapsedTime 耗时（毫秒）
     */
    void recordShardingOperation(String queryId, String dataSourceName, String tableName, long elapsedTime);
    
    /**
     * 获取监控指标
     * @return 监控指标
     */
    Map<String, Object> getMetrics();
    
    /**
     * 初始化监控管理器
     * @param config 监控管理器配置
     */
    void init(Map<String, Object> config);
}
