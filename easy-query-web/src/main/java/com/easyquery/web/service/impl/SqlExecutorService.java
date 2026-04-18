package com.easyquery.web.service.impl;

import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.enums.SqlType;
import com.easyquery.core.model.DataSourceEntry;
import com.easyquery.core.parser.SqlParser;
import com.easyquery.core.parser.SqlParseResult;
import com.easyquery.core.rewriter.SqlRewriter;
import com.easyquery.core.result.ResultHandler;
import com.easyquery.web.loader.DataSourceLoader;

import net.sf.jsqlparser.JSQLParserException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL 执行服务
 * 负责根据数据源和分片规则自动执行 SQL
 */
@Service
public class SqlExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(SqlExecutorService.class);

    @Autowired
    private DataSourceLoader dataSourceLoader;

    @Autowired
    private SqlParser sqlParser;

    @Autowired
    private SqlRewriter sqlRewriter;

    @Autowired
    private ResultHandler resultHandler;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 执行查询 SQL（SELECT）
     * 
     * @param dataSourceId 数据源 ID
     * @param sql          SQL 语句
     * @return 查询结果
     */
    public List<Map<String, Object>> executeQuery(Long dataSourceId, String sql) throws SQLException {
        logger.info("执行查询 SQL - 数据源ID：{}, SQL: {}", dataSourceId, sql);

        // 1. 获取数据源条目
        DataSourceEntry entry = dataSourceLoader.getDataSourceEntry(dataSourceId);
        if (entry == null) {
            throw new SQLException("数据源不存在：" + dataSourceId);
        }

        // 2. 获取数据库适配器
        DatabaseAdapter adapter = entry.getDatabaseAdapter();

        // 3. 解析 SQL
        SqlParseResult parseResult = sqlParser.parse(sql);
        if (parseResult.getSqlType() != SqlType.SELECT) {
            throw new SQLException("该方法仅用于执行 SELECT 查询");
        }

        // 4. 重写 SQL 并执行
        String rewrittenSql;
        try {
            rewrittenSql = sqlRewriter.rewrite(entry, parseResult);
        } catch (JSQLParserException e) {
            throw new SQLException("SQL 解析失败", e);
        }
        logger.info("重写后的 SQL: {}", rewrittenSql);

        List<Map<String, Object>> allResults = new ArrayList<>();
        // 5 执行查询
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = adapter.getConnection();
            stmt = conn.prepareStatement(rewrittenSql);
            rs = stmt.executeQuery();

            // 5.4 处理结果
            List<Map<String, Object>> results = resultHandler.handleResultSet(rs);
            allResults.addAll(results);

            logger.info("查询完成，返回 {} 条记录", results.size());

        } catch (SQLException e) {
            throw new SQLException("执行查询 SQL 失败", e);
        } finally {
            // 释放资源
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    adapter.releaseConnection(conn);
                } catch (SQLException e) {
                }
            }
        }

        return allResults;
    }

    /**
     * 执行更新 SQL（INSERT/UPDATE/DELETE）
     * 
     * @param dataSourceId 数据源 ID
     * @param sql          SQL 语句
     * @return 影响的行数
     */
    public int executeUpdate(Long dataSourceId, String sql) throws SQLException {
        logger.info("执行更新 SQL - 数据源ID：{}, SQL: {}", dataSourceId, sql);

        // 1. 获取数据源条目
        DataSourceEntry entry = dataSourceLoader.getDataSourceEntry(dataSourceId);
        if (entry == null) {
            throw new SQLException("数据源不存在：" + dataSourceId);
        }

        // 2. 获取数据库适配器
        DatabaseAdapter adapter = entry.getDatabaseAdapter();

        // 3. 解析 SQL
        SqlParseResult parseResult = sqlParser.parse(sql);
        if (parseResult.getSqlType() == SqlType.SELECT) {
            throw new IllegalArgumentException("该方法用于执行 INSERT/UPDATE/DELETE 操作");
        }

        // 4. 重写 SQL 并执行
        String rewrittenSql;
        try {
            rewrittenSql = sqlRewriter.rewrite(entry, parseResult);
        } catch (JSQLParserException e) {
            throw new SQLException("SQL 解析失败", e);
        }
        logger.info("重写后的 SQL: {}", rewrittenSql);

        int totalRows = 0;

        // 5 执行更新
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = adapter.getConnection();
            stmt = conn.prepareStatement(rewrittenSql);
            totalRows = stmt.executeUpdate();
            logger.info("更新完成，影响 {} 行", totalRows);

        } finally {
            // 释放资源
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            if (conn != null)
                try {
                    adapter.releaseConnection(conn);
                } catch (SQLException e) {
                }
        }

        return totalRows;
    }

    /**
     * 执行 SQL（不区分查询和更新，自动判断）
     * 
     * @param dataSourceId 数据源 ID
     * @param sql          SQL 语句
     * @return 执行结果（查询返回结果集，更新返回影响行数）
     */
    public Object execute(Long dataSourceId, String sql) throws SQLException {
        // 解析 SQL 判断类型
        SqlParseResult parseResult = sqlParser.parse(sql);

        if (parseResult.getSqlType() == SqlType.SELECT) {
            return executeQuery(dataSourceId, sql);
        } else {
            return executeUpdate(dataSourceId, sql);
        }
    }

}