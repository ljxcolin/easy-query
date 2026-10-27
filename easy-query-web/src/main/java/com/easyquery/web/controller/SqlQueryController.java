package com.easyquery.web.controller;

import com.easyquery.core.model.DataSourceEntry;
import com.easyquery.web.auth.LoginUser;
import com.easyquery.web.auth.UserContext;
import com.easyquery.web.dto.SqlQueryRequest;
import com.easyquery.web.entity.DataSourceEntity;
import com.easyquery.web.entity.SqlQueryEntity;
import com.easyquery.web.loader.DataSourceLoader;
import com.easyquery.web.model.Response;
import com.easyquery.web.service.DataSourceService;
import com.easyquery.web.service.SqlQueryService;
import com.easyquery.web.vo.SqlQueryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Objects;

/**
 * SQL查询管理控制器（数据按登录用户隔离，出参使用 VO）
 */
@RestController
@RequestMapping("/sql-queries")
public class SqlQueryController extends BaseController {

    @Autowired
    private SqlQueryService sqlQueryService;

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private DataSourceLoader dataSourceLoader;

    /**
     * 获取当前登录用户 ID
     * @return 用户 ID
     */
    private Long currentUserId() {
        LoginUser loginUser = UserContext.get();
        return loginUser == null ? null : loginUser.userId();
    }

    /**
     * 判断 SQL 查询是否属于当前用户
     * @param sqlQuery SQL 查询实体
     * @return 是否归属当前用户
     */
    private boolean isOwner(SqlQueryEntity sqlQuery) {
        return sqlQuery != null && Objects.equals(sqlQuery.getUserId(), currentUserId());
    }

    /**
     * 判断数据源是否属于当前用户
     * @param dataSourceId 数据源 ID
     * @return 是否归属当前用户
     */
    private boolean isDataSourceOwner(Long dataSourceId) {
        if (dataSourceId == null) {
            return false;
        }
        DataSourceEntity dataSource = dataSourceService.findById(dataSourceId);
        return dataSource != null && Objects.equals(dataSource.getUserId(), currentUserId());
    }

    /**
     * 获取当前用户的所有SQL查询
     */
    @GetMapping
    public Response<List<SqlQueryVO>> getAllSqlQueries() {
        List<SqlQueryVO> sqlQueries = sqlQueryService.findByUserId(currentUserId())
                .stream()
                .map(SqlQueryVO::from)
                .toList();
        return Response.success(sqlQueries);
    }

    /**
     * 根据 ID 获取SQL查询
     */
    @GetMapping("/{id}")
    public Response<SqlQueryVO> getSqlQueryById(@PathVariable("id") Long id) {
        SqlQueryEntity sqlQuery = sqlQueryService.findById(id);
        if (sqlQuery == null) {
            return Response.error(404, "SQL查询不存在");
        }
        if (!isOwner(sqlQuery)) {
            return Response.error(403, "无权访问该SQL查询");
        }
        return Response.success(SqlQueryVO.from(sqlQuery));
    }

    /**
     * 根据数据源获取当前用户的SQL查询列表
     */
    @GetMapping("/data-source/{dataSourceId}")
    public Response<List<SqlQueryVO>> getSqlQueriesByDataSourceId(@PathVariable("dataSourceId") Long dataSourceId) {
        List<SqlQueryVO> sqlQueries = sqlQueryService.findByUserIdAndDataSourceId(currentUserId(), dataSourceId)
                .stream()
                .map(SqlQueryVO::from)
                .toList();
        return Response.success(sqlQueries);
    }

    /**
     * 创建SQL查询（归属当前登录用户）
     */
    @PostMapping
    public Response<SqlQueryVO> createSqlQuery(@RequestBody SqlQueryRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return Response.error(400, "查询名称不能为空");
        }
        // 检查名称是否已存在
        if (sqlQueryService.existsByName(request.getName())) {
            return Response.error(400, "SQL查询名称已存在");
        }
        // 数据源必须属于当前用户
        if (!isDataSourceOwner(request.getDataSourceId())) {
            return Response.error(400, "数据源不存在：" + request.getDataSourceId());
        }

        SqlQueryEntity entity = new SqlQueryEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setUserId(currentUserId());
        entity.setDataSourceName(resolveDataSourceName(request.getDataSourceId()));

        SqlQueryEntity saved = sqlQueryService.save(entity);
        return Response.success(SqlQueryVO.from(saved));
    }

    /**
     * 更新SQL查询
     */
    @PutMapping("/{id}")
    public Response<SqlQueryVO> updateSqlQuery(@PathVariable("id") Long id,
            @RequestBody SqlQueryRequest request) {
        SqlQueryEntity existing = sqlQueryService.findById(id);
        if (existing == null) {
            return Response.error(404, "SQL查询不存在");
        }
        if (!isOwner(existing)) {
            return Response.error(403, "无权操作该SQL查询");
        }
        // 数据源必须属于当前用户
        if (!isDataSourceOwner(request.getDataSourceId())) {
            return Response.error(400, "数据源不存在：" + request.getDataSourceId());
        }

        // 更新字段（归属关系不可被修改）
        BeanUtils.copyProperties(request, existing, "userId");
        existing.setDataSourceName(resolveDataSourceName(request.getDataSourceId()));

        SqlQueryEntity updated = sqlQueryService.save(existing);
        return Response.success(SqlQueryVO.from(updated));
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
        if (!isOwner(existing)) {
            return Response.error(403, "无权操作该SQL查询");
        }

        sqlQueryService.deleteById(id);
        return Response.success(null);
    }

    /**
     * 以运行时已加载的数据源配置为准解析数据源名称
     *
     * @param dataSourceId 数据源 ID
     * @return 数据源名称，未加载时返回 null
     */
    private String resolveDataSourceName(Long dataSourceId) {
        DataSourceEntry entry = dataSourceLoader.getDataSourceEntry(dataSourceId);
        return entry == null ? null : entry.getDataSourceConfig().getName();
    }
}
