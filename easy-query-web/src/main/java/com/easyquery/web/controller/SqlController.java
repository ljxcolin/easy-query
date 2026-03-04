package com.easyquery.web.controller;

import com.easyquery.web.model.Response;
import com.easyquery.web.model.SqlRequest;
import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.parser.SqlParser;
import com.easyquery.core.result.ResultHandler;
import com.easyquery.core.sharding.ShardingResult;
import com.easyquery.core.sharding.ShardingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sql")
public class SqlController {

    @Autowired
    private ShardingStrategy shardingStrategy;

    @Autowired
    private SqlParser sqlParser;

    @Autowired
    private ResultHandler resultHandler;

    @Autowired
    private DatabaseAdapter databaseAdapter;

    @PostMapping("/query")
    public Response<List<Map<String, Object>>> executeQuery(@RequestBody SqlRequest sqlRequest) {
        try {
            String sql = sqlRequest.getSql();
            Object shardingKey = sqlRequest.getShardingKey();

            // 计算分片
            ShardingResult shardingResult = shardingStrategy.doSharding(shardingKey, new HashMap<>());

            // 重写SQL
            Map<String, Object> params = new HashMap<>();
            params.put("shardingKey", shardingKey);
            String rewrittenSql = sqlParser.rewrite(sql, shardingResult, params);

            // 执行查询
            try (Connection connection = databaseAdapter.getConnection(shardingResult.getDataSourceName());
                 PreparedStatement ps = connection.prepareStatement(rewrittenSql)) {
                // 设置参数
                if (shardingKey != null) {
                    ps.setObject(1, shardingKey);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    List<Map<String, Object>> results = resultHandler.handleResultSet(rs);
                    return Response.success(results);
                }
            }
        } catch (SQLException e) {
            return Response.error(500, "Query failed: " + e.getMessage());
        } catch (Exception e) {
            return Response.error(500, "Error: " + e.getMessage());
        }
    }

    @PostMapping("/execute")
    public Response<Integer> executeUpdate(@RequestBody SqlRequest sqlRequest) {
        try {
            String sql = sqlRequest.getSql();
            Object shardingKey = sqlRequest.getShardingKey();

            // 计算分片
            ShardingResult shardingResult = shardingStrategy.doSharding(shardingKey, new HashMap<>());

            // 重写SQL
            Map<String, Object> params = new HashMap<>();
            params.put("shardingKey", shardingKey);
            String rewrittenSql = sqlParser.rewrite(sql, shardingResult, params);

            // 执行更新
            try (Connection connection = databaseAdapter.getConnection(shardingResult.getDataSourceName());
                 PreparedStatement ps = connection.prepareStatement(rewrittenSql)) {
                // 设置参数
                if (shardingKey != null) {
                    ps.setObject(1, shardingKey);
                }
                int rows = ps.executeUpdate();
                return Response.success(rows);
            }
        } catch (SQLException e) {
            return Response.error(500, "Execution failed: " + e.getMessage());
        } catch (Exception e) {
            return Response.error(500, "Error: " + e.getMessage());
        }
    }
}
