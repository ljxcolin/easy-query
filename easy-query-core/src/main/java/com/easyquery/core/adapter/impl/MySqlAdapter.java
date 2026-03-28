package com.easyquery.core.adapter.impl;

import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.enums.DatabaseType;
import com.easyquery.core.model.DataSourceConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MySQL数据库适配器实现
 */
public class MySqlAdapter implements DatabaseAdapter {

    /**
     * 数据源配置
     */
    private DataSourceConfig config;

    /**
     * 数据源
     */
    private DataSource dataSource;

    @Override
    public void init(DataSourceConfig config) {
        this.config = config;

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
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
    public DatabaseType getDatabaseType() {
        return DatabaseType.MYSQL;
    }

    @Override
    public boolean testConnection() {
        if (dataSource == null) {
            throw new RuntimeException("DataSource not initialized");
        }

        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5秒超时
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<String> getTables() throws SQLException {
        List<String> tables = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String schema = config.getDatabase();

            try (ResultSet rs = metaData.getTables(schema, "%", "%", new String[] { "TABLE" })) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    tables.add(tableName);
                }
            }
        }
        return tables;
    }

    /**
     * 关闭所有数据源
     */
    public void close() {

    }
}
