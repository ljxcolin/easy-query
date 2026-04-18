package com.easyquery.web.controller;

import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.model.DataSourceEntry;
import com.easyquery.web.entity.DataSourceEntity;
import com.easyquery.web.model.Response;
import com.easyquery.web.service.DataSourceService;
import com.easyquery.web.loader.DataSourceLoader;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 数据源管理控制器
 */
@RestController
@RequestMapping("/data-sources")
public class DataSourceController extends BaseController {

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private DataSourceLoader dataSourceLoader;

    /**
     * 获取所有数据源
     */
    @GetMapping
    public Response<List<DataSourceEntity>> getAllDataSources() {
        List<DataSourceEntity> dataSources = dataSourceService.findAll();
        return Response.success(dataSources);
    }

    /**
     * 根据 ID 获取数据源
     */
    @GetMapping("/{id}")
    public Response<DataSourceEntity> getDataSourceById(@PathVariable("id") Long id) {
        DataSourceEntity existing = dataSourceService.findById(id);
        if (existing == null) {
            return Response.error(404, "数据源不存在");
        }
        return Response.success(existing);
    }

    /**
     * 根据名称获取数据源
     */
    @GetMapping("/name/{name}")
    public Response<DataSourceEntity> getDataSourceByName(@PathVariable("name") String name) {
        DataSourceEntity existing = dataSourceService.findByName(name);
        if (existing == null) {
            return Response.error(404, "数据源不存在");
        }
        return Response.success(existing);
    }

    /**
     * 创建数据源
     */
    @PostMapping
    public Response<DataSourceEntity> createDataSource(@RequestBody DataSourceEntity dataSource) {
        // 检查是否已存在
        if (dataSourceService.existsByName(dataSource.getName())) {
            return Response.error(400, "数据源已存在");
        }

        // 保存到数据库
        DataSourceEntity saved = dataSourceService.save(dataSource);

        // 动态加载到运行时环境
        try {
            dataSourceLoader.addDataSource(saved);
        } catch (Exception e) {
            return Response.error(500, "数据源创建成功但加载失败：" + e.getMessage());
        }

        return Response.success(saved);
    }

    /**
     * 更新数据源
     */
    @PutMapping("/{id}")
    public Response<DataSourceEntity> updateDataSource(@PathVariable("id") Long id,
            @RequestBody DataSourceEntity dataSource) {
        // 检查是否存在
        DataSourceEntity existing = dataSourceService.findById(id);
        if (existing == null) {
            return Response.error(404, "数据源不存在");
        }

        // 更新字段
        BeanUtils.copyProperties(dataSource, existing, "id");
        // 保存到数据库
        DataSourceEntity updated = dataSourceService.save(existing);

        // 动态更新运行时环境
        try {
            dataSourceLoader.updateDataSource(updated);
        } catch (Exception e) {
            return Response.error(500, "数据源更新成功但加载失败：" + e.getMessage());
        }

        return Response.success(updated);
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

        // 从运行时环境移除
        dataSourceLoader.removeDataSource(id);

        // 从数据库删除
        dataSourceService.deleteById(id);

        return Response.success(null);
    }

    /**
     * 测试数据源连接
     */
    @GetMapping("/test-connection/{id}")
    public Response<Boolean> testConnection(@PathVariable("id") Long dataSourceId) {
        try {
            DataSourceEntry dataSourceEntry = dataSourceLoader.getDataSourceEntry(dataSourceId);
            if (dataSourceEntry == null) {
                return Response.error(404, "数据源不存在");
            }

            // 创建数据库适配器
            DatabaseAdapter adapter = dataSourceEntry.getDatabaseAdapter();

            // 测试连接
            boolean success = adapter.testConnection();

            return Response.success(success);
        } catch (Exception e) {
            return Response.error(500, "连接测试失败：" + e.getMessage());
        }
    }

    /**
     * 获取数据源下的所有表
     */
    @GetMapping("/{id}/tables")
    public Response<List<String>> getTables(@PathVariable("id") Long id) {
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