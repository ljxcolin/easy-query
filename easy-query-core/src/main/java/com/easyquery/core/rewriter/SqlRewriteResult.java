package com.easyquery.core.rewriter;

import java.util.List;

import com.easyquery.core.enums.SqlType;

import net.sf.jsqlparser.statement.Statement;

/**
 * SQL重写结果
 */
public class SqlRewriteResult {

    /**
     * 原始SQL
     */
    private String originalSql;

    /**
     * SQL类型
     */
    private SqlType sqlType;


    /**
     * 重写后的SQL语句对象
     */
    private List<Statement> statements;

    public SqlRewriteResult() {
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
    
    public List<Statement> getStatements() {
        return statements;
    }
    
    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }
}
