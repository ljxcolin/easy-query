package com.easyquery.core.parser.impl;

import com.easyquery.core.enums.SqlType;
import com.easyquery.core.parser.SqlParseResult;
import com.easyquery.core.parser.SqlParser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

/**
 * 默认SQL解析器实现
 */
public class DefaultSqlParser implements SqlParser {
    
    @Override
    public SqlParseResult parse(String sql) {
        SqlParseResult result = new SqlParseResult();
        
        try {
            // 解析SQL语句
            Statement statement = CCJSqlParserUtil.parse(sql);
            result.setStatement(statement);
            result.setOriginalSql(statement.toString());
            // 提取SQL类型
            if (statement instanceof Select) {
                result.setSqlType(SqlType.SELECT);
            } else if (statement instanceof Insert) {
                result.setSqlType(SqlType.INSERT);
            } else if (statement instanceof Update) {
                result.setSqlType(SqlType.UPDATE);
            } else if (statement instanceof Delete) {
                result.setSqlType(SqlType.DELETE);
            } else {
                result.setSqlType(SqlType.OTHER);
            }
        } catch (JSQLParserException e) {
            throw new RuntimeException("Failed to parse SQL: " + sql, e);
        }
        
        return result;
    }
    
}
