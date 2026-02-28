package com.easyquery.core.parser;

import com.easyquery.core.sharding.ShardingResult;
import java.util.Map;

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
    
    /**
     * 重写SQL语句
     * @param sql SQL语句
     * @param shardingResult 分片结果
     * @param params SQL参数
     * @return 重写后的SQL语句
     */
    String rewrite(String sql, ShardingResult shardingResult, Map<String, Object> params);
    
    /**
     * 初始化SQL解析器
     * @param config SQL解析器配置
     */
    void init(Map<String, Object> config);
}
