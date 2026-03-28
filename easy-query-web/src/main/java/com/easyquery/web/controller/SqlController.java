package com.easyquery.web.controller;

import com.easyquery.web.model.Response;
import com.easyquery.web.model.SqlRequest;
import com.easyquery.web.service.impl.SqlExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * SQL 执行控制器
 * 提供统一的 SQL 执行接口，自动处理分片逻辑
 */
@RestController
@RequestMapping("/sql")
public class SqlController {

    @Autowired
    private SqlExecutorService sqlExecutorService;

    /**
     * 执行查询 SQL（SELECT）
     * 
     * @param sqlRequest SQL 请求（包含 sql、dataSourceId）
     * @return 查询结果
     */
    @PostMapping("/query")
    public Response<List<Map<String, Object>>> executeQuery(@RequestBody SqlRequest sqlRequest) {
        try {
            Long dataSourceId = sqlRequest.getDataSourceId();
            String sql = sqlRequest.getSql();
            
            // 使用 SQL 执行服务执行查询
            List<Map<String, Object>> results = sqlExecutorService.executeQuery(
                dataSourceId, sql
            );
            
            return Response.success(results);
        } catch (Exception e) {
            return Response.error(500, "查询执行失败：" + e.getMessage());
        }
    }

    /**
     * 执行更新 SQL（INSERT/UPDATE/DELETE）
     * 
     * @param sqlRequest SQL 请求（包含 sql、dataSourceId）
     * @return 影响的行数
     */
    @PostMapping("/execute")
    public Response<Integer> executeUpdate(@RequestBody SqlRequest sqlRequest) {
        try {
            Long dataSourceId = sqlRequest.getDataSourceId();
            String sql = sqlRequest.getSql();
            
            // 使用 SQL 执行服务执行更新
            int rows = sqlExecutorService.executeUpdate(
                dataSourceId, sql
            );
            
            return Response.success(rows);
        } catch (Exception e) {
            return Response.error(500, "SQL 执行失败：" + e.getMessage());
        }
    }
    
    /**
     * 执行 SQL（自动判断查询或更新）
     * 
     * @param sqlRequest SQL 请求
     * @return 执行结果
     */
    @PostMapping("/execute-auto")
    public Response<Object> execute(@RequestBody SqlRequest sqlRequest) {
        try {
            Long dataSourceId = sqlRequest.getDataSourceId();
            String sql = sqlRequest.getSql();
            
            // 使用 SQL 执行服务执行 SQL
            Object result = sqlExecutorService.execute(
                dataSourceId, sql
            );
            
            return Response.success(result);
        } catch (Exception e) {
            return Response.error(500, "SQL 执行失败：" + e.getMessage());
        }
    }
}