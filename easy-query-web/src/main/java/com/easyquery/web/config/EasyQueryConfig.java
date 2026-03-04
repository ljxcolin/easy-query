package com.easyquery.web.config;

import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.adapter.DatabaseAdapterFactory;
import com.easyquery.core.adapter.DatabaseType;
import java.sql.Connection;
import java.sql.SQLException;
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
import com.easyquery.core.sharding.ShardingStrategy;
import com.easyquery.core.sharding.ShardingStrategyFactory;
import com.easyquery.core.sharding.ShardingStrategyType;
import com.easyquery.core.transaction.TransactionManager;
import com.easyquery.core.transaction.TransactionManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class EasyQueryConfig {

    @Bean
    public ConfigManager configManager() {
        return ConfigManagerFactory.createDefaultConfigManager();
    }

    @Bean
    public LogManager logManager() {
        return LogManagerFactory.createDefaultLogManager();
    }

    @Bean
    public MonitorManager monitorManager() {
        return MonitorManagerFactory.createDefaultMonitorManager();
    }

    @Bean
    public ShardingStrategy shardingStrategy() {
        Map<String, Object> config = new HashMap<>();
        config.put("dataSourceCount", 2);
        config.put("tableCount", 4);
        return ShardingStrategyFactory.createStrategy(ShardingStrategyType.HASH, config);
    }

    @Bean
    public SqlParser sqlParser() {
        return SqlParserFactory.createDefaultParser();
    }

    @Bean
    public ResultHandler resultHandler() {
        return ResultHandlerFactory.createDefaultResultHandler();
    }

    @Bean
    public DatabaseAdapter databaseAdapter() {
        try {
            Map<String, Object> config = new HashMap<>();
            Map<String, Map<String, Object>> dataSources = new HashMap<>();

            // 默认数据源配置
            Map<String, Object> ds0Config = new HashMap<>();
            ds0Config.put("url", "jdbc:mysql://localhost:3306/db0");
            ds0Config.put("username", "root");
            ds0Config.put("password", "password");
            ds0Config.put("driverClassName", "com.mysql.cj.jdbc.Driver");
            dataSources.put("ds0", ds0Config);

            Map<String, Object> ds1Config = new HashMap<>();
            ds1Config.put("url", "jdbc:mysql://localhost:3306/db1");
            ds1Config.put("username", "root");
            ds1Config.put("password", "password");
            ds1Config.put("driverClassName", "com.mysql.cj.jdbc.Driver");
            dataSources.put("ds1", ds1Config);

            config.put("dataSources", dataSources);
            return DatabaseAdapterFactory.createAdapter(DatabaseType.MYSQL, config);
        } catch (Exception e) {
            // 如果数据库连接失败，返回一个空的适配器
            // 这样服务可以正常启动，后续可以通过API测试连接
            return new DatabaseAdapter() {
                @Override
                public Connection getConnection(String dataSourceName) throws SQLException {
                    throw new SQLException("Database not initialized");
                }

                @Override
                public void releaseConnection(Connection connection) throws SQLException {
                }

                @Override
                public void init(Map<String, Object> config) {
                }

                @Override
                public DatabaseType getDatabaseType() {
                    return DatabaseType.MYSQL;
                }

                @Override
                public boolean testConnection(String dataSourceName) {
                    return false;
                }
            };
        }
    }

//    @Bean
//    public TransactionManager transactionManager(DatabaseAdapter databaseAdapter) {
//        Map<String, Object> config = new HashMap<>();
//        config.put("databaseAdapter", databaseAdapter);
//        return TransactionManagerFactory.createTransactionManager(config);
//    }
}
