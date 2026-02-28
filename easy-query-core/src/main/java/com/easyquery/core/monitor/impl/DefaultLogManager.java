package com.easyquery.core.monitor.impl;

import com.easyquery.core.monitor.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Map;

/**
 * 默认日志管理器实现
 */
public class DefaultLogManager implements LogManager {
    
    /**
     * 日志记录器
     */
    private Logger logger;
    
    /**
     * 日志管理器配置
     */
    private Map<String, Object> config;
    
    @Override
    public void debug(String message, Object... params) {
        logger.debug(message, params);
    }
    
    @Override
    public void info(String message, Object... params) {
        logger.info(message, params);
    }
    
    @Override
    public void warn(String message, Object... params) {
        logger.warn(message, params);
    }
    
    @Override
    public void error(String message, Throwable throwable, Object... params) {
        logger.error(message, throwable, params);
    }
    
    @Override
    public void init(Map<String, Object> config) {
        this.config = config;
        // 初始化日志记录器
        String loggerName = config.containsKey("loggerName") ? (String) config.get("loggerName") : "easy-query";
        this.logger = org.apache.logging.log4j.LogManager.getLogger(loggerName);
    }
}
