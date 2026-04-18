package com.easyquery.core.sharding;

import com.easyquery.core.enums.StrategyType;
import com.easyquery.core.model.ShardingRuleConfig;

import java.util.List;

/**
 * 分片策略接口
 */
public interface ShardingStrategy {
    
    /**
     * 根据分片键值计算分片
     * @param shardingRuleConfig 分片规则配置
     * @param conditions 分片条件列表
     * @return 分片结果
     */
    List<String> doSharding(ShardingRuleConfig shardingRuleConfig, List<SqlCondition> conditions);
    
    /**
     * 设置可用的物理表名
     * @param shardingRuleConfig 分片规则配置
     * @param tables 可用的物理表名列表
     */
    void setAvailableTables(ShardingRuleConfig shardingRuleConfig, List<String> tables);

    /**
     * 获取分片策略类型
     * @return 分片策略类型
     */
    StrategyType getType();
    
}
