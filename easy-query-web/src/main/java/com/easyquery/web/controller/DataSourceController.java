package com.easyquery.web.controller;

import com.easyquery.core.model.DataSourceEntry;
import com.easyquery.web.auth.LoginUser;
import com.easyquery.web.auth.UserContext;
import com.easyquery.web.dto.DataSourceRequest;
import com.easyquery.web.entity.DataSourceEntity;
import com.easyquery.web.enums.ConnectionStatus;
import com.easyquery.web.model.Response;
import com.easyquery.web.service.DataSourceConnectionStatusService;
import com.easyquery.web.service.DataSourceService;
import com.easyquery.web.loader.DataSourceLoader;
import com.easyquery.web.vo.DataSourceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Objects;

/**
 * 数据源管理控制器（数据按登录用户隔离，出参不返回密码）
 */
@RestController
@RequestMapping("/data-sources")
public class DataSourceController extends BaseController {

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private DataSourceLoader dataSourceLoader;

    @Autowired
    private DataSourceConnectionStatusService connectionStatusService;

    /**
     * 获取当前登录用户 ID
     * @return 用户 ID
     */
    private Long currentUserId() {
        LoginUser loginUser = UserContext.get();
        return loginUser == null ? null : loginUser.userId();
    }

    /**
     * 判断数据源是否属于当前用户
     * @param dataSource 数据源实体
     * @return 是否归属当前用户
     */
    private boolean isOwner(DataSourceEntity dataSource) {
        return dataSource != null && Objects.equals(dataSource.getUserId(), currentUserId());
    }

    /**
     * 实体转 VO 并填充连接状态
     * @param entity 数据源实体
     * @return 响应对象
     */
    private DataSourceVO toVO(DataSourceEntity entity) {
        return DataSourceVO.from(entity).withConnectionStatus(connectionStatusService.get(entity.getId()));
    }

    /**
     * 获取当前用户的所有数据源（含连接状态，不含密码）
     */
    @GetMapping
    public Response<List<DataSourceVO>> getAllDataSources() {
        List<DataSourceVO> dataSources = dataSourceService.findByUserId(currentUserId())
                .stream()
                .map(this::toVO)
                .toList();
        return Response.success(dataSources);
    }

    /**
     * 根据 ID 获取数据源
     */
    @GetMapping("/{id}")
    public Response<DataSourceVO> getDataSourceById(@PathVariable("id") Long id) {
        DataSourceEntity existing = dataSourceService.findById(id);
        if (existing == null) {
            return Response.error(404, "数据源不存在");
        }
        if (!isOwner(existing)) {
            return Response.error(403, "无权访问该数据源");
        }
        return Response.success(toVO(existing));
    }

    /**
     * 根据名称获取数据源
     */
    @GetMapping("/name/{name}")
    public Response<DataSourceVO> getDataSourceByName(@PathVariable("name") String name) {
        DataSourceEntity existing = dataSourceService.findByName(name);
        if (existing == null) {
            return Response.error(404, "数据源不存在");
        }
        if (!isOwner(existing)) {
            return Response.error(403, "无权访问该数据源");
        }
        return Response.success(toVO(existing));
    }

    /**
     * 创建数据源（归属当前登录用户）
     */
    @PostMapping
    public Response<DataSourceVO> createDataSource(@RequestBody DataSourceRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return Response.error(400, "数据源名称不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return Response.error(400, "密码不能为空");
        }
        // 检查是否已存在
        if (dataSourceService.existsByName(request.getName())) {
            return Response.error(400, "数据源已存在");
        }

        DataSourceEntity entity = new DataSourceEntity();
        BeanUtils.copyProperties(request, entity);
        // 归属当前用户
        entity.setUserId(currentUserId());

        // 保存到数据库
        DataSourceEntity saved = dataSourceService.save(entity);

        // 动态加载到运行时环境
        try {
            dataSourceLoader.addDataSource(saved);
        } catch (Exception e) {
            return Response.error(500, "数据源创建成功但加载失败：" + e.getMessage());
        }

        // 刷新连接状态
        ConnectionStatus status = connectionStatusService.refresh(saved.getId());
        return Response.success(DataSourceVO.from(saved).withConnectionStatus(status));
    }

    /**
     * 更新数据源（密码留空表示不修改）
     */
    @PutMapping("/{id}")
    public Response<DataSourceVO> updateDataSource(@PathVariable("id") Long id,
            @RequestBody DataSourceRequest request) {
        // 检查是否存在
        DataSourceEntity existing = dataSourceService.findById(id);
        if (existing == null) {
            return Response.error(404, "数据源不存在");
        }
        if (!isOwner(existing)) {
            return Response.error(403, "无权操作该数据源");
        }

        // 更新字段：password 单独处理，留空则保留原密码
        BeanUtils.copyProperties(request, existing, "password");
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existing.setPassword(request.getPassword());
        }

        // 保存到数据库
        DataSourceEntity updated = dataSourceService.save(existing);

        // 动态更新运行时环境
        try {
            dataSourceLoader.updateDataSource(updated);
        } catch (Exception e) {
            return Response.error(500, "数据源更新成功但加载失败：" + e.getMessage());
        }

        // 连接参数可能变化，重新检测
        ConnectionStatus status = connectionStatusService.refresh(updated.getId());
        return Response.success(DataSourceVO.from(updated).withConnectionStatus(status));
    }

    /**
     * 删除数据源
     */
    @DeleteMapping("/{id}")
    public Response<Void> deleteDataSource(@PathVariable("id") Long id) {
        DataSourceEntity existing = dataSourceService.findById(id);
        if (existing == null) {
            return Response.error(404, "数据源不存在");
        }
        if (!isOwner(existing)) {
            return Response.error(403, "无权操作该数据源");
        }

        // 从运行时环境移除
        dataSourceLoader.removeDataSource(id);

        // 清除连接状态缓存
        connectionStatusService.evict(id);

        // 从数据库删除
        dataSourceService.deleteById(id);

        return Response.success(null);
    }

    /**
     * 检测数据源连接（同步刷新并返回最新连接状态）
     */
    @PostMapping("/{id}/test-connection")
    public Response<DataSourceVO> testConnection(@PathVariable("id") Long id) {
        DataSourceEntity existing = dataSourceService.findById(id);
        if (existing == null || !isOwner(existing)) {
            return Response.error(404, "数据源不存在");
        }
        if (dataSourceLoader.getDataSourceEntry(id) == null) {
            return Response.error(400, "数据源尚未加载，请检查配置");
        }
        ConnectionStatus status = connectionStatusService.refresh(id);
        return Response.success(DataSourceVO.from(existing).withConnectionStatus(status));
    }

    /**
     * 获取数据源下的所有表
     */
    @GetMapping("/{id}/tables")
    public Response<List<String>> getTables(@PathVariable("id") Long id) {
        DataSourceEntity existing = dataSourceService.findById(id);
        if (existing == null || !isOwner(existing)) {
            return Response.error(404, "数据源不存在");
        }
        try {
            DataSourceEntry dataSourceEntry = dataSourceLoader.getDataSourceEntry(id);
            if (dataSourceEntry == null) {
                return Response.error(404, "数据源不存在");
            }
            dataSourceEntry.refresh();
            List<String> tables = dataSourceEntry.getTables();
            return Response.success(tables);
        } catch (Exception e) {
            return Response.error(500, "获取数据表失败：" + e.getMessage());
        }
    }
}
