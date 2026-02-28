package com.easyquery.core.parser;

import com.easyquery.core.parser.impl.DefaultSqlParser;
import java.util.Map;

/**
 * SQL解析器工厂
 */
public class SqlParserFactory {
    
    /**
     * 创建SQL解析器实例
     * @param config SQL解析器配置
     * @return SQL解析器实例
     */
    public static SqlParser createParser(Map<String, Object> config) {
        SqlParser parser;
        
        // 检查是否使用自定义解析器
        if (config.containsKey("customClassName")) {
            try {
                String className = (String) config.get("customClassName");
                Class<?> clazz = Class.forName(className);
                parser = (SqlParser) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create custom SQL parser", e);
            }
        } else {
            // 使用默认解析器
            parser = new DefaultSqlParser();
        }
        
        // 初始化解析器
        parser.init(config);
        
        return parser;
    }
    
    /**
     * 创建默认的SQL解析器实例
     * @return 默认SQL解析器实例
     */
    public static SqlParser createDefaultParser() {
        SqlParser parser = new DefaultSqlParser();
        parser.init(java.util.Collections.emptyMap());
        return parser;
    }
}
