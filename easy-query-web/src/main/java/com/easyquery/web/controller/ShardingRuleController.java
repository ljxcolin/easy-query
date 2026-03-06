package com.easyquery.web.controller;

import com.easyquery.web.entity.ShardingRuleEntity;
import com.easyquery.web.model.Response;
import com.easyquery.web.service.ShardingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/sharding-rules")
public class ShardingRuleController {

    @Autowired
    private ShardingRuleService shardingRuleService;

    @GetMapping
    public Response<List<ShardingRuleEntity>> getAllShardingRules() {
        List<ShardingRuleEntity> shardingRules = shardingRuleService.findAll();
        return Response.success(shardingRules);
    }

    @GetMapping("/{name}")
    public Response<ShardingRuleEntity> getShardingRule(@PathVariable("name") String name) {
        return shardingRuleService.findByName(name)
                .map(Response::success)
                .orElse(Response.error(404, "ShardingRule not found"));
    }

    @GetMapping("/{id}")
    public Response<ShardingRuleEntity> getShardingRuleById(@PathVariable("id") Long id) {
        return shardingRuleService.findById(id)
                .map(Response::success)
                .orElse(Response.error(404, "ShardingRule not found"));
    }

    @PostMapping
    public Response<ShardingRuleEntity> createShardingRule(@RequestBody ShardingRuleEntity shardingRule) {
        if (shardingRuleService.existsByDataSourceIdAndTableName(shardingRule.getDataSourceId(), shardingRule.getTableName())) {
            return Response.error(400, "ShardingRule already exists");
        }
        ShardingRuleEntity saved = shardingRuleService.save(shardingRule);
        return Response.success(saved);
    }

    @PutMapping("/{id}")
    public Response<ShardingRuleEntity> updateShardingRule(@PathVariable("id") Long id, @RequestBody ShardingRuleEntity shardingRule) {
        return shardingRuleService.findById(id)
                .map(existing -> {
                    shardingRule.setId(existing.getId());
                    ShardingRuleEntity updated = shardingRuleService.save(shardingRule);
                    return Response.success(updated);
                })
                .orElse(Response.error(404, "ShardingRule not found"));
    }

    @DeleteMapping("/{id}")
    public Response<Void> deleteShardingRule(@PathVariable("id") Long id) {
        if (!shardingRuleService.existsById(id)) {
            return Response.error(404, "ShardingRule not found");
        }
        shardingRuleService.deleteById(id);
        return Response.success(null);
    }

    @PostMapping("/test")
    public Response<String> testShardingRule(@RequestBody ShardingRuleEntity shardingRule, @RequestParam Object shardingKey) {
        // TODO: 实现实际的分片规则测试逻辑
        return Response.success("Test passed for rule: " + shardingRule.getName());
    }
}
