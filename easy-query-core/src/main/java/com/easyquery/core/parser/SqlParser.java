package com.easyquery.core.parser;

/**
 * SQL解析器接口
 */
public interface SqlParser {
    
    /**
     * 解析SQL语句
     * @param sql SQL语句
     * @return SQL解析结果
     */
    SqlParseResult parse(String sql);
    
}
