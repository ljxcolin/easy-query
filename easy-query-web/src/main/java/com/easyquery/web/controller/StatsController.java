package com.easyquery.web.controller;

import com.easyquery.web.auth.LoginUser;
import com.easyquery.web.auth.UserContext;
import com.easyquery.web.model.Response;
import com.easyquery.web.service.DataSourceService;
import com.easyquery.web.service.ShardingRuleService;
import com.easyquery.web.service.SqlQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计控制器：当前登录用户的资源概览
 */
@RestController
@RequestMapping("/stats")
public class StatsController extends BaseController {

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private ShardingRuleService shardingRuleService;

    @Autowired
    private SqlQueryService sqlQueryService;

    /**
     * 获取当前用户的资源统计（数据源 / 分片规则 / SQL 查询数量）
     *
     * @return 统计结果
     */
    @GetMapping("/summary")
    public Response<Map<String, Long>> summary() {
        LoginUser loginUser = UserContext.get();
        Long userId = loginUser == null ? null : loginUser.userId();

        Map<String, Long> data = new HashMap<>();
        data.put("dataSourceCount", dataSourceService.countByUserId(userId));
        data.put("shardingRuleCount", shardingRuleService.countByUserId(userId));
        data.put("sqlQueryCount", sqlQueryService.countByUserId(userId));
        return Response.success(data);
    }
}
