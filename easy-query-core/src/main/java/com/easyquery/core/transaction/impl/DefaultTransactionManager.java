package com.easyquery.core.transaction.impl;

import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.transaction.Transaction;
import com.easyquery.core.transaction.TransactionManager;
import com.easyquery.core.transaction.TransactionStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 默认事务管理器实现
 */
public class DefaultTransactionManager implements TransactionManager {
    
    /**
     * 数据库适配器
     */
    private DatabaseAdapter databaseAdapter;
    
    /**
     * 事务管理器配置
     */
    private Map<String, Object> config;
    
    @Override
    public Transaction beginTransaction(String transactionId) throws SQLException {
        return new DefaultTransaction(transactionId);
    }
    
    @Override
    public void commit(Transaction transaction) throws SQLException {
        if (transaction.getStatus() != TransactionStatus.STARTED) {
            throw new SQLException("Transaction is not in started status");
        }
        
        try {
            // 提交所有数据源的事务
            for (Map.Entry<String, Connection> entry : transaction.getConnectionMap().entrySet()) {
                Connection connection = entry.getValue();
                if (connection != null && !connection.isClosed()) {
                    connection.commit();
                }
            }
            transaction.setStatus(TransactionStatus.COMMITTED);
        } catch (SQLException e) {
            // 提交失败，回滚所有事务
            try {
                rollback(transaction);
            } catch (SQLException ex) {
                // 忽略回滚异常
            }
            throw e;
        } finally {
            // 释放所有连接
            releaseConnections(transaction);
        }
    }
    
    @Override
    public void rollback(Transaction transaction) throws SQLException {
        if (transaction.getStatus() != TransactionStatus.STARTED) {
            throw new SQLException("Transaction is not in started status");
        }
        
        try {
            // 回滚所有数据源的事务
            for (Map.Entry<String, Connection> entry : transaction.getConnectionMap().entrySet()) {
                Connection connection = entry.getValue();
                if (connection != null && !connection.isClosed()) {
                    connection.rollback();
                }
            }
            transaction.setStatus(TransactionStatus.ROLLED_BACK);
        } finally {
            // 释放所有连接
            releaseConnections(transaction);
        }
    }
    
    @Override
    public Connection getConnection(Transaction transaction, String dataSourceName) throws SQLException {
        // 检查事务中是否已有该数据源的连接
        Connection connection = transaction.getConnection(dataSourceName);
        if (connection == null || connection.isClosed()) {
            // 获取新连接并设置为手动提交
            connection = databaseAdapter.getConnection(dataSourceName);
            connection.setAutoCommit(false);
            transaction.addConnection(dataSourceName, connection);
        }
        return connection;
    }
    
    /**
     * 释放事务中的所有连接
     */
    private void releaseConnections(Transaction transaction) {
        for (Map.Entry<String, Connection> entry : transaction.getConnectionMap().entrySet()) {
            Connection connection = entry.getValue();
            if (connection != null) {
                try {
                    databaseAdapter.releaseConnection(connection);
                } catch (SQLException e) {
                    // 忽略释放异常
                }
            }
        }
    }
    
    @Override
    public void init(Map<String, Object> config) {
        this.config = config;
        
        // 初始化数据库适配器
        if (config.containsKey("databaseAdapter")) {
            this.databaseAdapter = (DatabaseAdapter) config.get("databaseAdapter");
        } else {
            throw new IllegalArgumentException("Database adapter is required");
        }
    }
}
