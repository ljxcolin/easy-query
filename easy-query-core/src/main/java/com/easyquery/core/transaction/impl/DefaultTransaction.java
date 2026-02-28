package com.easyquery.core.transaction.impl;

import com.easyquery.core.transaction.Transaction;
import com.easyquery.core.transaction.TransactionStatus;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认事务实现
 */
public class DefaultTransaction implements Transaction {
    
    /**
     * 事务ID
     */
    private String transactionId;
    
    /**
     * 事务状态
     */
    private TransactionStatus status;
    
    /**
     * 数据库连接映射
     */
    private Map<String, Connection> connectionMap;
    
    public DefaultTransaction(String transactionId) {
        this.transactionId = transactionId;
        this.status = TransactionStatus.STARTED;
        this.connectionMap = new HashMap<>();
    }
    
    @Override
    public String getTransactionId() {
        return transactionId;
    }
    
    @Override
    public TransactionStatus getStatus() {
        return status;
    }
    
    @Override
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    @Override
    public Map<String, Connection> getConnectionMap() {
        return connectionMap;
    }
    
    @Override
    public void addConnection(String dataSourceName, Connection connection) {
        connectionMap.put(dataSourceName, connection);
    }
    
    @Override
    public Connection getConnection(String dataSourceName) {
        return connectionMap.get(dataSourceName);
    }
}
