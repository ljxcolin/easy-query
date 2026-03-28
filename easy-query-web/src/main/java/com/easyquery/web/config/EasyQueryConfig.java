package com.easyquery.web.config;

import com.easyquery.core.config.ConfigManager;
import com.easyquery.core.config.ConfigManagerFactory;
import com.easyquery.core.monitor.LogManager;
import com.easyquery.core.monitor.LogManagerFactory;
import com.easyquery.core.monitor.MonitorManager;
import com.easyquery.core.monitor.MonitorManagerFactory;
import com.easyquery.core.parser.SqlParser;
import com.easyquery.core.parser.SqlParserFactory;
import com.easyquery.core.result.ResultHandler;
import com.easyquery.core.result.ResultHandlerFactory;
import com.easyquery.core.rewriter.SqlRewriter;
import com.easyquery.core.rewriter.impl.SelectSqlRewriter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EasyQuery 配置类
 * 提供核心组件的 Bean 定义
 */
@Configuration
public class EasyQueryConfig {

    /**
     * 配置管理器
     */
    @Bean
    public ConfigManager configManager() {
        return ConfigManagerFactory.createDefaultConfigManager();
    }

    /**
     * 日志管理器
     */
    @Bean
    public LogManager logManager() {
        return LogManagerFactory.createDefaultLogManager();
    }

    /**
     * 监控管理器
     */
    @Bean
    public MonitorManager monitorManager() {
        return MonitorManagerFactory.createDefaultMonitorManager();
    }

    /**
     * SQL 解析器
     */
    @Bean
    public SqlParser sqlParser() {
        return SqlParserFactory.createDefaultParser();
    }

        /**
     * SQL 解析器
     */
    @Bean
    public SqlRewriter sqlRewriter() {
        return new SelectSqlRewriter(sqlParser());
    }

    /**
     * 结果处理器
     */
    @Bean
    public ResultHandler resultHandler() {
        return ResultHandlerFactory.createDefaultResultHandler();
    }
    
    // 注意：
    // 1. DatabaseAdapter 和 ShardingStrategy 不再在这里创建
    // 2. 它们由 EasyQueryStartupLoader 在应用启动时动态加载
    // 3. 这样支持运行时动态增删改配置
}
