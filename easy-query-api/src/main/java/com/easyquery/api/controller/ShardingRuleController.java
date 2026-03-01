package com.easyquery.api.controller;

import com.easyquery.api.model.Response;
import com.easyquery.api.model.ShardingRule;
import com.easyquery.core.sharding.ShardingStrategy;
import com.easyquery.core.sharding.ShardingStrategyFactory;
import com.easyquery.core.sharding.ShardingStrategyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sharding-rules")
public class ShardingRuleController {

    @Autowired
    private ShardingStrategy shardingStrategy;

    // 模拟分片规则列表
    private List<ShardingRule> shardingRules = new ArrayList<>();

    public ShardingRuleController() {
        // 初始化默认分片规则
        ShardingRule hashRule = new ShardingRule();
        hashRule.setName("hash");
        hashRule.setStrategyType("HASH");
        Map<String, Object> hashConfig = new HashMap<>();
        hashConfig.put("dataSourceCount", 2);
        hashConfig.put("tableCount", 4);
        hashRule.setConfig(hashConfig);
        shardingRules.add(hashRule);

        ShardingRule rangeRule = new ShardingRule();
        rangeRule.setName("range");
        rangeRule.setStrategyType("RANGE");
        Map<String, Object> rangeConfig = new HashMap<>();
        Map<String, String> dataSourceRanges = new HashMap<>();
        dataSourceRanges.put("1000", "ds0");
        dataSourceRanges.put("2000", "ds1");
        rangeConfig.put("dataSourceRanges", dataSourceRanges);
        Map<String, String> tableRanges = new HashMap<>();
        tableRanges.put("250", "t0");
        tableRanges.put("500", "t1");
        tableRanges.put("750", "t2");
        tableRanges.put("1000", "t3");
        rangeConfig.put("tableRanges", tableRanges);
        rangeRule.setConfig(rangeConfig);
        shardingRules.add(rangeRule);

        ShardingRule listRule = new ShardingRule();
        listRule.setName("list");
        listRule.setStrategyType("LIST");
        Map<String, Object> listConfig = new HashMap<>();
        Map<String, Object> dataSourceMappings = new HashMap<>();
        List<Integer> ds0Values = new ArrayList<>();
        ds0Values.add(1);
        ds0Values.add(2);
        ds0Values.add(3);
        ds0Values.add(4);
        ds0Values.add(5);
        dataSourceMappings.put("ds0", ds0Values);
        List<Integer> ds1Values = new ArrayList<>();
        ds1Values.add(6);
        ds1Values.add(7);
        ds1Values.add(8);
        ds1Values.add(9);
        ds1Values.add(10);
        dataSourceMappings.put("ds1", ds1Values);
        listConfig.put("dataSourceMappings", dataSourceMappings);
        Map<String, Object> tableMappings = new HashMap<>();
        List<Integer> t0Values = new ArrayList<>();
        t0Values.add(1);
        t0Values.add(2);
        tableMappings.put("t0", t0Values);
        List<Integer> t1Values = new ArrayList<>();
        t1Values.add(3);
        t1Values.add(4);
        tableMappings.put("t1", t1Values);
        List<Integer> t2Values = new ArrayList<>();
        t2Values.add(5);
        t2Values.add(6);
        tableMappings.put("t2", t2Values);
        List<Integer> t3Values = new ArrayList<>();
        t3Values.add(7);
        t3Values.add(8);
        t3Values.add(9);
        t3Values.add(10);
        tableMappings.put("t3", t3Values);
        listConfig.put("tableMappings", tableMappings);
        listRule.setConfig(listConfig);
        shardingRules.add(listRule);
    }

    @GetMapping
    public Response<List<ShardingRule>> getAllShardingRules() {
        return Response.success(shardingRules);
    }

    @GetMapping("/{name}")
    public Response<ShardingRule> getShardingRule(@PathVariable String name) {
        for (ShardingRule rule : shardingRules) {
            if (rule.getName().equals(name)) {
                return Response.success(rule);
            }
        }
        return Response.error(404, "ShardingRule not found");
    }

    @PostMapping
    public Response<ShardingRule> createShardingRule(@RequestBody ShardingRule shardingRule) {
        shardingRules.add(shardingRule);
        return Response.success(shardingRule);
    }

    @PutMapping("/{name}")
    public Response<ShardingRule> updateShardingRule(@PathVariable String name, @RequestBody ShardingRule shardingRule) {
        for (int i = 0; i < shardingRules.size(); i++) {
            if (shardingRules.get(i).getName().equals(name)) {
                shardingRules.set(i, shardingRule);
                return Response.success(shardingRule);
            }
        }
        return Response.error(404, "ShardingRule not found");
    }

    @DeleteMapping("/{name}")
    public Response<Void> deleteShardingRule(@PathVariable String name) {
        shardingRules.removeIf(rule -> rule.getName().equals(name));
        return Response.success(null);
    }

    @PostMapping("/test")
    public Response<String> testShardingRule(@RequestBody ShardingRule shardingRule, @RequestParam Object shardingKey) {
        try {
            ShardingStrategyType strategyType = ShardingStrategyType.valueOf(shardingRule.getStrategyType());
            ShardingStrategy strategy = ShardingStrategyFactory.createStrategy(strategyType, shardingRule.getConfig());
            com.easyquery.core.sharding.ShardingResult result = strategy.doSharding(shardingKey, new HashMap<>());
            return Response.success("DataSource: " + result.getDataSourceName() + ", Table: " + result.getTableName());
        } catch (Exception e) {
            return Response.error(500, "Test failed: " + e.getMessage());
        }
    }
}
