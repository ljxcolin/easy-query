package com.easyquery.web.controller;

import com.easyquery.web.auth.LoginUser;
import com.easyquery.web.auth.UserContext;
import com.easyquery.web.entity.DataSourceEntity;
import com.easyquery.web.model.Response;
import com.easyquery.web.model.SqlRequest;
import com.easyquery.web.service.DataSourceService;
import com.easyquery.web.service.impl.SqlExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * SQL 执行控制器
 * 提供统一的 SQL 执行接口，自动处理分片逻辑（数据源按登录用户隔离）
 */
@RestController
@RequestMapping("/sql")
public class SqlController extends BaseController {

    @Autowired
    private SqlExecutorService sqlExecutorService;

    @Autowired
    private DataSourceService dataSourceService;

    /**
     * 获取当前登录用户 ID
     * @return 用户 ID
     */
    private Long currentUserId() {
        LoginUser loginUser = UserContext.get();
        return loginUser == null ? null : loginUser.userId();
    }

    /**
     * 校验请求中的数据源是否属于当前用户
     * @param dataSourceId 数据源 ID
     * @return 校验失败时返回错误响应，通过则返回 null
     */
    private Response<Object> checkDataSourceOwnership(Long dataSourceId) {
        if (dataSourceId == null) {
            return Response.error(400, "数据源 ID 不能为空");
        }
        DataSourceEntity dataSource = dataSourceService.findById(dataSourceId);
        if (dataSource == null || !Objects.equals(dataSource.getUserId(), currentUserId())) {
            return Response.error(403, "无权访问该数据源");
        }
        return null;
    }

    /**
     * 执行查询 SQL（SELECT）
     *
     * @param sqlRequest SQL 请求（包含 sql、dataSourceId）
     * @return 查询结果
     */
    @PostMapping("/query")
    public Response<List<Map<String, Object>>> executeQuery(@RequestBody SqlRequest sqlRequest) {
        Response<Object> denied = checkDataSourceOwnership(sqlRequest.getDataSourceId());
        if (denied != null) {
            return new Response<>(denied.getCode(), denied.getMessage(), null);
        }
        try {
            Long dataSourceId = sqlRequest.getDataSourceId();
            String sql = sqlRequest.getSql();

            // 使用 SQL 执行服务执行查询
            List<Map<String, Object>> results = sqlExecutorService.executeQuery(
                dataSourceId, sql
            );

            return Response.success(results);
        } catch (Exception e) {
            logger.error("查询执行失败", e);
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
        Response<Object> denied = checkDataSourceOwnership(sqlRequest.getDataSourceId());
        if (denied != null) {
            return new Response<>(denied.getCode(), denied.getMessage(), null);
        }
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
        Response<Object> denied = checkDataSourceOwnership(sqlRequest.getDataSourceId());
        if (denied != null) {
            return denied;
        }
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