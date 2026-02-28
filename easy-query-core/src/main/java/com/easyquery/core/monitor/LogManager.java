package com.easyquery.core.monitor;

import java.util.Map;

/**
 * 日志管理器接口
 */
public interface LogManager {
    
    /**
     * 记录调试日志
     * @param message 日志消息
     * @param params 日志参数
     */
    void debug(String message, Object... params);
    
    /**
     * 记录信息日志
     * @param message 日志消息
     * @param params 日志参数
     */
    void info(String message, Object... params);
    
    /**
     * 记录警告日志
     * @param message 日志消息
     * @param params 日志参数
     */
    void warn(String message, Object... params);
    
    /**
     * 记录错误日志
     * @param message 日志消息
     * @param throwable 异常对象
     * @param params 日志参数
     */
    void error(String message, Throwable throwable, Object... params);
    
    /**
     * 初始化日志管理器
     * @param config 日志管理器配置
     */
    void init(Map<String, Object> config);
}
