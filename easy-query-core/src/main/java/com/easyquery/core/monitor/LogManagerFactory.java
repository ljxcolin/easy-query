package com.easyquery.core.monitor;

import com.easyquery.core.monitor.impl.DefaultLogManager;
import java.util.Map;

/**
 * 日志管理器工厂
 */
public class LogManagerFactory {
    
    /**
     * 创建日志管理器实例
     * @param config 日志管理器配置
     * @return 日志管理器实例
     */
    public static LogManager createLogManager(Map<String, Object> config) {
        LogManager logManager;
        
        // 检查是否使用自定义日志管理器
        if (config.containsKey("customClassName")) {
            try {
                String className = (String) config.get("customClassName");
                Class<?> clazz = Class.forName(className);
                logManager = (LogManager) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create custom log manager", e);
            }
        } else {
            // 使用默认日志管理器
            logManager = new DefaultLogManager();
        }
        
        // 初始化日志管理器
        logManager.init(config);
        
        return logManager;
    }
    
    /**
     * 创建默认的日志管理器实例
     * @return 默认日志管理器实例
     */
    public static LogManager createDefaultLogManager() {
        LogManager logManager = new DefaultLogManager();
        logManager.init(java.util.Collections.emptyMap());
        return logManager;
    }
}
