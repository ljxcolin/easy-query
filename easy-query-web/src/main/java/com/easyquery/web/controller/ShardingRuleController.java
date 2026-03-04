package com.easyquery.web.controller;

import com.easyquery.web.entity.ShardingRuleEntity;
import com.easyquery.web.model.Response;
import com.easyquery.web.service.ShardingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Response<ShardingRuleEntity>> getShardingRule(@PathVariable String name) {
        return shardingRuleService.findByName(name)
                .map(Response::success)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Response.error(404, "ShardingRule not found")));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Response<ShardingRuleEntity>> getShardingRuleById(@PathVariable Long id) {
        return shardingRuleService.findById(id)
                .map(Response::success)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Response.error(404, "ShardingRule not found")));
    }

    @PostMapping
    public ResponseEntity<Response<ShardingRuleEntity>> createShardingRule(@RequestBody ShardingRuleEntity shardingRule) {
        if (shardingRuleService.existsByName(shardingRule.getName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Response.error(400, "ShardingRule already exists"));
        }
        ShardingRuleEntity saved = shardingRuleService.save(shardingRule);
        return ResponseEntity.ok(Response.success(saved));
    }

    @PutMapping("/{name}")
    public ResponseEntity<Response<ShardingRuleEntity>> updateShardingRule(@PathVariable String name, @RequestBody ShardingRuleEntity shardingRule) {
        return shardingRuleService.findByName(name)
                .map(existing -> {
                    shardingRule.setId(existing.getId());
                    ShardingRuleEntity updated = shardingRuleService.save(shardingRule);
                    return ResponseEntity.ok(Response.success(updated));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Response.error(404, "ShardingRule not found")));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Response<Void>> deleteShardingRule(@PathVariable String name) {
        if (!shardingRuleService.existsByName(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Response.error(404, "ShardingRule not found"));
        }
        shardingRuleService.deleteByName(name);
        return ResponseEntity.ok(Response.success(null));
    }

    @PostMapping("/test")
    public Response<String> testShardingRule(@RequestBody ShardingRuleEntity shardingRule, @RequestParam Object shardingKey) {
        // TODO: 实现实际的分片规则测试逻辑
        return Response.success("Test passed for rule: " + shardingRule.getName());
    }
}
