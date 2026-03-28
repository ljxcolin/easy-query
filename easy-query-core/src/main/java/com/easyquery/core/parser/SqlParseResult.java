package com.easyquery.core.parser;

import com.easyquery.core.enums.SqlType;

import net.sf.jsqlparser.statement.Statement;

/**
 * SQL解析结果
 */
public class SqlParseResult {
    
    /**
     * 原始SQL
     */
    private String originalSql;

    /**
     * SQL类型
     */
    private SqlType sqlType;

    
    /**
     * 解析后的SQL语句对象
     */
    private Statement statement;
    
    public SqlParseResult() {
    }
    
    public String getOriginalSql() {
        return originalSql;
    }
    
    public void setOriginalSql(String originalSql) {
        this.originalSql = originalSql;
    }

    public SqlType getSqlType() {
        return sqlType;
    }
    
    public void setSqlType(SqlType sqlType) {
        this.sqlType = sqlType;
    }
    
    public Statement getStatement() {
        return statement;
    }
    
    public void setStatement(Statement statement) {
        this.statement = statement;
    }
}
