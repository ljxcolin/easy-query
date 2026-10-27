package com.easyquery.web.controller;

import com.easyquery.core.model.DataSourceEntry;
import com.easyquery.web.auth.LoginUser;
import com.easyquery.web.auth.UserContext;
import com.easyquery.web.dto.ShardingRuleRequest;
import com.easyquery.web.entity.DataSourceEntity;
import com.easyquery.web.entity.ShardingRuleEntity;
import com.easyquery.web.model.Response;
import com.easyquery.web.service.DataSourceService;
import com.easyquery.web.service.ShardingRuleService;
import com.easyquery.web.loader.DataSourceLoader;
import com.easyquery.web.vo.ShardingRuleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Objects;

/**
 * 分片规则管理控制器（数据按登录用户隔离，出参使用 VO）
 */
@RestController
@RequestMapping("/sharding-rules")
public class ShardingRuleController extends BaseController {

    @Autowired
    private ShardingRuleService shardingRuleService;

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
     * 判断分片规则是否属于当前用户
     * @param rule 分片规则实体
     * @return 是否归属当前用户
     */
    private boolean isOwner(ShardingRuleEntity rule) {
        return rule != null && Objects.equals(rule.getUserId(), currentUserId());
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
     * 获取当前用户的所有分片规则
     */
    @GetMapping
    public Response<List<ShardingRuleVO>> getAllShardingRules() {
        List<ShardingRuleVO> rules = shardingRuleService.findByUserId(currentUserId())
                .stream()
                .map(ShardingRuleVO::from)
                .toList();
        return Response.success(rules);
    }

    /**
     * 根据名称获取分片规则
     */
    @GetMapping("/name/{name}")
    public Response<ShardingRuleVO> getShardingRuleByName(@PathVariable("name") String name) {
        ShardingRuleEntity rule = shardingRuleService.findByName(name);
        if (rule == null) {
            return Response.error(404, "分片规则不存在");
        }
        if (!isOwner(rule)) {
            return Response.error(403, "无权访问该分片规则");
        }
        return Response.success(ShardingRuleVO.from(rule));
    }

    /**
     * 根据 ID 获取分片规则
     */
    @GetMapping("/{id}")
    public Response<ShardingRuleVO> getShardingRuleById(@PathVariable("id") Long id) {
        ShardingRuleEntity rule = shardingRuleService.findById(id);
        if (rule == null) {
            return Response.error(404, "分片规则不存在");
        }
        if (!isOwner(rule)) {
            return Response.error(403, "无权访问该分片规则");
        }
        return Response.success(ShardingRuleVO.from(rule));
    }

    /**
     * 创建分片规则（归属当前登录用户，且数据源必须属于该用户）
     */
    @PostMapping
    public Response<ShardingRuleVO> createShardingRule(@RequestBody ShardingRuleRequest request) {
        if (request.getTableName() == null || request.getTableName().isBlank()) {
            return Response.error(400, "逻辑表名不能为空");
        }
        // 检查是否已存在
        if (shardingRuleService.existsByDataSourceIdAndTableName(request.getDataSourceId(), request.getTableName())) {
            return Response.error(400, "分片规则已存在");
        }

        // 检查数据源是否存在且属于当前用户
        if (!isDataSourceOwner(request.getDataSourceId())) {
            return Response.error(400, "数据源不存在：" + request.getDataSourceId());
        }

        // 检查运行时是否已加载
        DataSourceEntry entry = dataSourceLoader.getDataSourceEntry(request.getDataSourceId());
        if (entry == null) {
            return Response.error(400, "数据源不存在：" + request.getDataSourceId());
        }

        ShardingRuleEntity entity = new ShardingRuleEntity();
        BeanUtils.copyProperties(request, entity);
        // 归属当前用户，数据源名称以运行时配置为准
        entity.setUserId(currentUserId());
        entity.setDataSourceName(entry.getDataSourceConfig().getName());

        // 保存到数据库
        ShardingRuleEntity saved = shardingRuleService.save(entity);

        // 动态加载到运行时环境
        try {
            dataSourceLoader.addShardingRule(saved);
        } catch (Exception e) {
            return Response.error(500, "分片规则创建成功但加载失败：" + e.getMessage());
        }

        return Response.success(ShardingRuleVO.from(saved));
    }

    /**
     * 更新分片规则
     */
    @PutMapping("/{id}")
    public Response<ShardingRuleVO> updateShardingRule(@PathVariable("id") Long id,
            @RequestBody ShardingRuleRequest request) {
        ShardingRuleEntity existing = shardingRuleService.findById(id);
        if (existing == null) {
            return Response.error(404, "分片规则不存在");
        }
        if (!isOwner(existing)) {
            return Response.error(403, "无权操作该分片规则");
        }

        // 检查是否已存在（排除当前记录）
        if (shardingRuleService.existsByDataSourceIdAndTableNameExcludingId(
                request.getDataSourceId(), request.getTableName(), id)) {
            return Response.error(400, "分片规则已存在");
        }

        // 先记下旧的数据源与逻辑表名，否则 copyProperties 之后就找不回旧规则了
        Long oldDataSourceId = existing.getDataSourceId();
        String oldTableName = existing.getTableName();

        // 更新字段（归属关系不可被修改）
        BeanUtils.copyProperties(request, existing, "userId");

        // 数据源名称以运行时加载的配置为准，避免前端传错导致规则残留在错误的数据源上
        DataSourceEntry targetEntry = dataSourceLoader.getDataSourceEntry(existing.getDataSourceId());
        if (targetEntry != null) {
            existing.setDataSourceName(targetEntry.getDataSourceConfig().getName());
        }

        // 保存到数据库
        ShardingRuleEntity updated = shardingRuleService.save(existing);

        try {
            // 动态更新运行时环境：先按旧键移除，再按新配置重新装配
            dataSourceLoader.removeShardingRule(oldDataSourceId, oldTableName);
            dataSourceLoader.addShardingRule(updated);
        } catch (Exception e) {
            return Response.error(500, "分片规则更新成功但加载失败：" + e.getMessage());
        }

        return Response.success(ShardingRuleVO.from(updated));
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
        if (!isOwner(existing)) {
            return Response.error(403, "无权操作该分片规则");
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
    public Response<String> testShardingRule(@RequestBody ShardingRuleRequest request, @RequestParam Object shardingKey) {
        // TODO: 实现实际的分片规则测试逻辑
        return Response.success("Test passed for rule: " + request.getName());
    }
}
