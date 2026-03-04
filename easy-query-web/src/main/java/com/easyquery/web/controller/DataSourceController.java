package com.easyquery.web.controller;

import com.easyquery.web.entity.DataSourceEntity;
import com.easyquery.web.model.Response;
import com.easyquery.web.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{name}")
    public ResponseEntity<Response<DataSourceEntity>> getDataSource(@PathVariable String name) {
        return dataSourceService.findByName(name)
                .map(Response::success)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Response.error(404, "DataSource not found")));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Response<DataSourceEntity>> getDataSourceById(@PathVariable Long id) {
        return dataSourceService.findById(id)
                .map(Response::success)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Response.error(404, "DataSource not found")));
    }

    @PostMapping
    public ResponseEntity<Response<DataSourceEntity>> createDataSource(@RequestBody DataSourceEntity dataSource) {
        if (dataSourceService.existsByName(dataSource.getName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Response.error(400, "DataSource already exists"));
        }
        DataSourceEntity saved = dataSourceService.save(dataSource);
        return ResponseEntity.ok(Response.success(saved));
    }

    @PutMapping("/{name}")
    public ResponseEntity<Response<DataSourceEntity>> updateDataSource(@PathVariable String name, @RequestBody DataSourceEntity dataSource) {
        return dataSourceService.findByName(name)
                .map(existing -> {
                    dataSource.setId(existing.getId());
                    DataSourceEntity updated = dataSourceService.save(dataSource);
                    return ResponseEntity.ok(Response.success(updated));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Response.error(404, "DataSource not found")));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Response<Void>> deleteDataSource(@PathVariable String name) {
        if (!dataSourceService.existsByName(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Response.error(404, "DataSource not found"));
        }
        dataSourceService.deleteByName(name);
        return ResponseEntity.ok(Response.success(null));
    }

    @PostMapping("/test-connection")
    public Response<Boolean> testConnection(@RequestBody DataSourceEntity dataSource) {
        // TODO: 实现实际的数据库连接测试逻辑
        return Response.success(true);
    }
}
