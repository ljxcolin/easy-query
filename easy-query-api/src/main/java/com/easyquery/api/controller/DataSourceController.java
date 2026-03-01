package com.easyquery.api.controller;

import com.easyquery.api.model.DataSource;
import com.easyquery.api.model.Response;
import com.easyquery.core.adapter.DatabaseAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/data-sources")
public class DataSourceController {

    @Autowired
    private DatabaseAdapter databaseAdapter;

    // 模拟数据源列表
    private List<DataSource> dataSources = new ArrayList<>();

    public DataSourceController() {
        // 初始化默认数据源
        DataSource ds0 = new DataSource();
        ds0.setName("ds0");
        ds0.setUrl("jdbc:mysql://localhost:3306/db0");
        ds0.setUsername("root");
        ds0.setPassword("password");
        ds0.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds0.setMaximumPoolSize(10);
        ds0.setMinimumIdle(5);
        dataSources.add(ds0);

        DataSource ds1 = new DataSource();
        ds1.setName("ds1");
        ds1.setUrl("jdbc:mysql://localhost:3306/db1");
        ds1.setUsername("root");
        ds1.setPassword("password");
        ds1.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds1.setMaximumPoolSize(10);
        ds1.setMinimumIdle(5);
        dataSources.add(ds1);
    }

    @GetMapping
    public Response<List<DataSource>> getAllDataSources() {
        return Response.success(dataSources);
    }

    @GetMapping("/{name}")
    public Response<DataSource> getDataSource(@PathVariable String name) {
        for (DataSource ds : dataSources) {
            if (ds.getName().equals(name)) {
                return Response.success(ds);
            }
        }
        return Response.error(404, "DataSource not found");
    }

    @PostMapping
    public Response<DataSource> createDataSource(@RequestBody DataSource dataSource) {
        dataSources.add(dataSource);
        return Response.success(dataSource);
    }

    @PutMapping("/{name}")
    public Response<DataSource> updateDataSource(@PathVariable String name, @RequestBody DataSource dataSource) {
        for (int i = 0; i < dataSources.size(); i++) {
            if (dataSources.get(i).getName().equals(name)) {
                dataSources.set(i, dataSource);
                return Response.success(dataSource);
            }
        }
        return Response.error(404, "DataSource not found");
    }

    @DeleteMapping("/{name}")
    public Response<Void> deleteDataSource(@PathVariable String name) {
        dataSources.removeIf(ds -> ds.getName().equals(name));
        return Response.success(null);
    }

    @PostMapping("/test-connection")
    public Response<Boolean> testConnection(@RequestBody DataSource dataSource) {
        // 这里应该实现实际的连接测试逻辑
        return Response.success(true);
    }
}
