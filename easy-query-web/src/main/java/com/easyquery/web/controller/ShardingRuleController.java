package com.easyquery.web.controller;

import com.easyquery.core.model.DataSourceEntry;
import com.easyquery.web.entity.ShardingRuleEntity;
import com.easyquery.web.model.Response;
import com.easyquery.web.service.ShardingRuleService;
import com.easyquery.web.loader.DataSourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

/**
 * 分片规则管理控制器
 */
@RestController
@RequestMapping("/sharding-rules")
public class ShardingRuleController extends BaseController {

    @Autowired
    private ShardingRuleService shardingRuleService;
    
    @Autowired
    private DataSourceLoader dataSourceLoader;

    /**
     * 获取所有分片规则
     */
    @GetMapping
    public Response<List<ShardingRuleEntity>> getAllShardingRules() {
        List<ShardingRuleEntity> rules = shardingRuleService.findAll();
        return Response.success(rules);
    }

    /**
     * 根据名称获取分片规则
     */
    @GetMapping("/{name}")
    public Response<ShardingRuleEntity> getShardingRule(@PathVariable("name") String name) {
        ShardingRuleEntity rule = shardingRuleService.findByName(name);
        if (rule == null) {
            return Response.error(404, "分片规则不存在");
        }
        return Response.success(rule);
    }

    /**
     * 根据 ID 获取分片规则
     */
    @GetMapping("/{id}")
    public Response<ShardingRuleEntity> getShardingRuleById(@PathVariable("id") Long id) {
        ShardingRuleEntity rule = shardingRuleService.findById(id);
        if (rule == null) {
            return Response.error(404, "分片规则不存在");
        }
        return Response.success(rule);
    }

    /**
     * 创建分片规则
     */
    @PostMapping
    public Response<ShardingRuleEntity> createShardingRule(@RequestBody ShardingRuleEntity shardingRule) {
        // 检查是否已存在
        if (shardingRuleService.existsByDataSourceIdAndTableName(shardingRule.getDataSourceId(), shardingRule.getTableName())) {
            return Response.error(400, "分片规则已存在");
        }
        
        // 检查数据源是否存在
        DataSourceEntry entry = dataSourceLoader.getDataSourceEntry(shardingRule.getDataSourceId());
        if (entry == null) {
            return Response.error(400, "数据源不存在：" + shardingRule.getDataSourceId());
        }
        
        // 保存到数据库
        shardingRule.setDataSourceName(entry.getDataSourceConfig().getName());
        ShardingRuleEntity saved = shardingRuleService.save(shardingRule);
        
        // 动态加载到运行时环境
        try {
            dataSourceLoader.addShardingRule(saved);
        } catch (Exception e) {
            return Response.error(500, "分片规则创建成功但加载失败：" + e.getMessage());
        }
        
        return Response.success(saved);
    }

    /**
     * 更新分片规则
     */
    @PutMapping("/{id}")
    public Response<ShardingRuleEntity> updateShardingRule(@PathVariable("id") Long id, @RequestBody ShardingRuleEntity shardingRule) {
        ShardingRuleEntity existing = shardingRuleService.findById(id);
        if (existing == null) {
            return Response.error(404, "分片规则不存在");
        }
        
        // 检查是否已存在（排除当前记录）
        if (shardingRuleService.existsByDataSourceIdAndTableNameExcludingId(
                shardingRule.getDataSourceId(), shardingRule.getTableName(), id)) {
            return Response.error(400, "分片规则已存在");
        }
        
        // 更新字段
        BeanUtils.copyProperties(shardingRule, existing);
        
        // 保存到数据库
        ShardingRuleEntity updated = shardingRuleService.save(existing);
        
        try {
            // 动态更新运行时环境
            dataSourceLoader.removeShardingRule(existing.getDataSourceId(), existing.getTableName());
            dataSourceLoader.addShardingRule(updated);
        } catch (Exception e) {
            return Response.error(500, "分片规则更新成功但加载失败：" + e.getMessage());
        }
        
        return Response.success(updated);
    }

    /**
     * 删除分片规则
     */
    @DeleteMapping("/{id}")
    public Response<Void> deleteShardingRule(@PathVariable("id") Long id) {
        ShardingRuleEntity existing = shardingRuleService.findById(id);
        if (existing == null) {
            return Response.error(404, "分片规则不存在");
        }
        
        // 从运行时环境移除
        dataSourceLoader.removeShardingRule(existing.getDataSourceId(), existing.getTableName());
        
        // 从数据库删除
        shardingRuleService.deleteById(id);
        
        return Response.success(null);
    }
    
    /**
     * 测试分片规则
     */
    @PostMapping("/test")
    public Response<String> testShardingRule(@RequestBody ShardingRuleEntity shardingRule, @RequestParam Object shardingKey) {
        // TODO: 实现实际的分片规则测试逻辑
        return Response.success("Test passed for rule: " + shardingRule.getName());
    }
}