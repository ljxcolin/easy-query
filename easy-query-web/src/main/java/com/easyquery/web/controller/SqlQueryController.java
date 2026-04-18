package com.easyquery.web.controller;

import com.easyquery.web.entity.SqlQueryEntity;
import com.easyquery.web.model.Response;
import com.easyquery.web.service.SqlQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * SQL查询管理控制器
 */
@RestController
@RequestMapping("/sql-queries")
public class SqlQueryController extends BaseController {

    @Autowired
    private SqlQueryService sqlQueryService;

    /**
     * 获取所有SQL查询
     */
    @GetMapping
    public Response<List<SqlQueryEntity>> getAllSqlQueries() {
        List<SqlQueryEntity> sqlQueries = sqlQueryService.findAll();
        return Response.success(sqlQueries);
    }

    /**
     * 根据 ID 获取SQL查询
     */
    @GetMapping("/{id}")
    public Response<SqlQueryEntity> getSqlQueryById(@PathVariable("id") Long id) {
        SqlQueryEntity sqlQuery = sqlQueryService.findById(id);
        if (sqlQuery == null) {
            return Response.error(404, "SQL查询不存在");
        }
        return Response.success(sqlQuery);
    }

    /**
     * 根据数据源名称获取SQL查询列表
     */
    @GetMapping("/data-source/{dataSourceId}")
    public Response<List<SqlQueryEntity>> getSqlQueriesByDataSourceId(@PathVariable("dataSourceId") Long dataSourceId) {
        List<SqlQueryEntity> sqlQueries = sqlQueryService.findByDataSourceId(dataSourceId);
        return Response.success(sqlQueries);
    }

    /**
     * 创建SQL查询
     */
    @PostMapping
    public Response<SqlQueryEntity> createSqlQuery(@RequestBody SqlQueryEntity sqlQuery) {
        // 检查名称是否已存在
        if (sqlQueryService.existsByName(sqlQuery.getName())) {
            return Response.error(400, "SQL查询名称已存在");
        }

        SqlQueryEntity saved = sqlQueryService.save(sqlQuery);
        return Response.success(saved);
    }

    /**
     * 更新SQL查询
     */
    @PutMapping("/{id}")
    public Response<SqlQueryEntity> updateSqlQuery(@PathVariable("id") Long id,
            @RequestBody SqlQueryEntity sqlQuery) {
        SqlQueryEntity existing = sqlQueryService.findById(id);
        if (existing == null) {
            return Response.error(404, "SQL查询不存在");
        }

        // 更新字段，保留 id
        sqlQuery.setId(id);
        SqlQueryEntity updated = sqlQueryService.save(sqlQuery);
        return Response.success(updated);
    }

    /**
     * 删除SQL查询
     */
    @DeleteMapping("/{id}")
    public Response<Void> deleteSqlQuery(@PathVariable("id") Long id) {
        SqlQueryEntity existing = sqlQueryService.findById(id);
        if (existing == null) {
            return Response.error(404, "SQL查询不存在");
        }

        sqlQueryService.deleteById(id);
        return Response.success(null);
    }
}
