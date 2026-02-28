package com.easyquery.core.transaction;

/**
 * 事务状态枚举
 */
public enum TransactionStatus {
    
    /**
     * 事务开始
     */
    STARTED,
    
    /**
     * 事务提交
     */
    COMMITTED,
    
    /**
     * 事务回滚
     */
    ROLLED_BACK,
    
    /**
     * 事务失败
     */
    FAILED
    
}
