package com.easyquery.core.adapter.impl;

import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.adapter.DatabaseType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * PostgreSQL数据库适配器实现
 */
public class PostgreSqlAdapter implements DatabaseAdapter {
    
    /**
     * 数据源映射
     */
    private Map<String, HikariDataSource> dataSourceMap;
    
    @Override
    public Connection getConnection(String dataSourceName) throws SQLException {
        HikariDataSource dataSource = dataSourceMap.get(dataSourceName);
        if (dataSource == null) {
            throw new SQLException("DataSource not found: " + dataSourceName);
        }
        return dataSource.getConnection();
    }
    
    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    @Override
    public void init(Map<String, Object> config) {
        dataSourceMap = new java.util.HashMap<>();
        
        // 初始化数据源
        if (config.containsKey("dataSources")) {
            Map<?, ?> dataSources = (Map<?, ?>) config.get("dataSources");
            for (Map.Entry<?, ?> entry : dataSources.entrySet()) {
                String dataSourceName = (String) entry.getKey();
                Map<?, ?> dataSourceConfig = (Map<?, ?>) entry.getValue();
                
                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setDriverClassName("org.postgresql.Driver");
                hikariConfig.setJdbcUrl((String) dataSourceConfig.get("url"));
                hikariConfig.setUsername((String) dataSourceConfig.get("username"));
                hikariConfig.setPassword((String) dataSourceConfig.get("password"));
                
                // 设置连接池参数
                if (dataSourceConfig.containsKey("maximumPoolSize")) {
                    hikariConfig.setMaximumPoolSize((int) dataSourceConfig.get("maximumPoolSize"));
                } else {
                    hikariConfig.setMaximumPoolSize(10);
                }
                
                if (dataSourceConfig.containsKey("minimumIdle")) {
                    hikariConfig.setMinimumIdle((int) dataSourceConfig.get("minimumIdle"));
                } else {
                    hikariConfig.setMinimumIdle(5);
                }
                
                if (dataSourceConfig.containsKey("idleTimeout")) {
                    hikariConfig.setIdleTimeout((long) dataSourceConfig.get("idleTimeout"));
                } else {
                    hikariConfig.setIdleTimeout(600000); // 10分钟
                }
                
                if (dataSourceConfig.containsKey("maxLifetime")) {
                    hikariConfig.setMaxLifetime((long) dataSourceConfig.get("maxLifetime"));
                } else {
                    hikariConfig.setMaxLifetime(1800000); // 30分钟
                }
                
                HikariDataSource dataSource = new HikariDataSource(hikariConfig);
                dataSourceMap.put(dataSourceName, dataSource);
            }
        }
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.POSTGRESQL;
    }
    
    @Override
    public boolean testConnection(String dataSourceName) {
        HikariDataSource dataSource = dataSourceMap.get(dataSourceName);
        if (dataSource == null) {
            return false;
        }
        
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5秒超时
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * 关闭所有数据源
     */
    public void close() {
        for (HikariDataSource dataSource : dataSourceMap.values()) {
            dataSource.close();
        }
    }
}
