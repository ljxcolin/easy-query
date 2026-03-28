package com.easyquery.core.rewriter;

import com.easyquery.core.model.DataSourceEntry;
import com.easyquery.core.parser.SqlParseResult;

/**
 * @author lijinxuan
 * @date 2026/3/11 9:52
 */
public interface SqlRewriter {

    /**
     * 重写 SQL
     * @param sql 原始 SQL
     * @param params SQL 参数
     * @return 重写后的 SQL
     */
    String rewrite(DataSourceEntry dataSourceEntry, SqlParseResult sqlParseResult);
    
}
