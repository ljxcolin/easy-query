package com.easyquery.core.parser.impl;

import com.easyquery.core.parser.SqlParseResult;
import com.easyquery.core.parser.SqlParser;
import com.easyquery.core.parser.SqlType;
import com.easyquery.core.sharding.ShardingResult;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import java.util.List;
import java.util.Map;

/**
 * 默认SQL解析器实现
 */
public class DefaultSqlParser implements SqlParser {
    
    /**
     * SQL解析配置
     */
    private Map<String, Object> config;
    
    @Override
    public SqlParseResult parse(String sql) {
        SqlParseResult result = new SqlParseResult();
        result.setOriginalSql(sql);
        
        try {
            // 解析SQL语句
            Statement statement = CCJSqlParserUtil.parse(sql);
            
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
            
            // 提取表名
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableNames = tablesNamesFinder.getTableList(statement);
            result.setTableNames(tableNames);
            
            // 后续可以添加更多解析逻辑，如提取分片键等
            
        } catch (JSQLParserException e) {
            throw new RuntimeException("Failed to parse SQL: " + sql, e);
        }
        
        return result;
    }
    
    @Override
    public String rewrite(String sql, ShardingResult shardingResult, Map<String, Object> params) {
        // 简单实现：替换表名为分片后的表名
        // 后续可以根据SQL类型和具体情况实现更复杂的重写逻辑
        String originalSql = sql;
        
        // 提取原始表名
        SqlParseResult parseResult = parse(sql);
        List<String> tableNames = parseResult.getTableNames();
        
        // 替换表名
        String rewrittenSql = originalSql;
        for (String tableName : tableNames) {
            rewrittenSql = rewrittenSql.replaceAll("\\b" + tableName + "\\b", shardingResult.getTableName());
        }
        
        return rewrittenSql;
    }
    
    @Override
    public void init(Map<String, Object> config) {
        this.config = config;
    }
}
