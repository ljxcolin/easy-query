package com.easyquery.core.adapter.impl;

import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.enums.DatabaseType;
import com.easyquery.core.model.DataSourceConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

/**
 * PostgreSQL数据库适配器实现
 */
public class PostgreSqlAdapter implements DatabaseAdapter {

    /**
     * 数据源
     */
    private DataSource dataSource;

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource not initialized");
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
    public void init(DataSourceConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());

        // 设置连接池参数
        hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());

        hikariConfig.setMinimumIdle(config.getMinimumIdle());

        hikariConfig.setIdleTimeout(600000); // 10分钟
        hikariConfig.setMaxLifetime(1800000); // 30分钟

        dataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.POSTGRESQL;
    }

    @Override
    public boolean testConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5秒超时
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<String> getTables() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 关闭所有数据源
     */
    public void close() {
        
    }
}
