package com.easyquery.web.controller;

import com.easyquery.web.entity.DataSourceEntity;
import com.easyquery.web.model.Response;
import com.easyquery.web.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/data-sources")
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    @GetMapping
    public Response<List<DataSourceEntity>> getAllDataSources() {
        List<DataSourceEntity> dataSources = dataSourceService.findAll();
        return Response.success(dataSources);
    }

    @GetMapping("/name/{name}")
    public Response<DataSourceEntity> getDataSourceByName(@PathVariable("name") String name) {
        return dataSourceService.findByName(name)
                .map(Response::success)
                .orElse(Response.error(404, "DataSource not found"));
    }

    @GetMapping("/{id}")
    public Response<DataSourceEntity> getDataSourceById(@PathVariable("id") Long id) {
        return dataSourceService.findById(id)
                .map(Response::success)
                .orElse(Response.error(404, "DataSource not found"));
    }

    @PostMapping
    public Response<DataSourceEntity> createDataSource(@RequestBody DataSourceEntity dataSource) {
        if (dataSourceService.existsByName(dataSource.getName())) {
            return Response.error(400, "DataSource already exists");
        }
        DataSourceEntity saved = dataSourceService.save(dataSource);
        return Response.success(saved);
    }

    @PutMapping("/{id}")
    public Response<DataSourceEntity> updateDataSource(@PathVariable("id") Long id, @RequestBody DataSourceEntity dataSource) {
        return dataSourceService.findById(id)
                .map(existing -> {
                    dataSource.setId(existing.getId());
                    DataSourceEntity updated = dataSourceService.save(dataSource);
                    return Response.success(updated);
                })
                .orElse(Response.error(404, "DataSource not found"));
    }

    @DeleteMapping("/{id}")
    public Response<Void> deleteDataSource(@PathVariable("id") Long id) {
        if (!dataSourceService.findById(id).isPresent()) {
            return Response.error(404, "DataSource not found");
        }
        dataSourceService.deleteById(id);
        return Response.success(null);
    }

    @PostMapping("/test-connection")
    public Response<Boolean> testConnection(@RequestBody DataSourceEntity dataSource) {
        // TODO: 实现实际的数据库连接测试逻辑
        return Response.success(true);
    }
}
