package com.easyquery.core.sharding.impl;

import com.easyquery.core.sharding.SqlCondition;
import com.easyquery.core.utils.TableUtils;
import com.easyquery.core.sharding.ShardingStrategy;
import com.easyquery.core.enums.ConditionType;
import com.easyquery.core.enums.StrategyType;
import com.easyquery.core.model.ShardingRuleConfig;
import com.easyquery.core.model.StrategyConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 哈希分片策略实现
 */
public class HashShardingStrategy implements ShardingStrategy {

    @Override
    public List<String> doSharding(ShardingRuleConfig shardingRuleConfig, List<SqlCondition> conditions) {
        List<String> shardingResult = new ArrayList<>();
        if (conditions.isEmpty()) {
            return shardingResult;
        }

        String tableName = shardingRuleConfig.getTableName();
        List<String> availableTableNames = shardingRuleConfig.getAvailableTableNames();
        StrategyConfig strategyConfig = shardingRuleConfig.getStrategyConfig();
        int shardingCount = strategyConfig.getShardingCount();
        int shardingStart = strategyConfig.getShardingStart();

        if (conditions.size() == 1) {
            SqlCondition condition = conditions.get(0);
            Object value = condition.getValue();
            ConditionType conditionType = condition.getConditionType();
            if (conditionType == ConditionType.EQUALS) {
                Long hashValue = Long.parseLong(value.toString());
                int hashIndex = (int) (hashValue % shardingCount);
                hashIndex += shardingStart;
                String physicalTableName = TableUtils.getPhysicalTableName(tableName, String.valueOf(hashIndex));
                if (!availableTableNames.contains(physicalTableName)) {
                    throw new IllegalArgumentException("Physical table name not available: " + physicalTableName);
                }

                shardingResult.add(physicalTableName);
            } else if (conditionType == ConditionType.IN) {
                for (Object item : (List<?>) value) {
                    Long hashValue = Long.parseLong(item.toString());
                    int hashIndex = (int) (hashValue % shardingCount);
                    hashIndex += shardingStart;
                    String physicalTableName = TableUtils.getPhysicalTableName(tableName, String.valueOf(hashIndex));
                    if (!availableTableNames.contains(physicalTableName)) {
                        throw new IllegalArgumentException("Physical table name not available: " + physicalTableName);
                    }
                    if (!shardingResult.contains(physicalTableName)) {
                        shardingResult.add(physicalTableName);
                    }
                    if (shardingResult.size() == availableTableNames.size()) {
                        break;
                    }
                }
            } else {
                shardingResult.addAll(availableTableNames);
            }
        } else {
            shardingResult.addAll(availableTableNames);
        }

        return shardingResult;
    }

    @Override
    public StrategyType getType() {
        return StrategyType.HASH;
    }

}
