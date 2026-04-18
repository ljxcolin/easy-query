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
import java.util.stream.Collectors;

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
                String suffix = String.valueOf(hashIndex + shardingStart);
                String physicalTableName = TableUtils.getPhysicalTableName(tableName, suffix);
                if (!availableTableNames.contains(physicalTableName)) {
                    throw new IllegalArgumentException("Physical table name not available: " + physicalTableName);
                }

                shardingResult.add(suffix);
            } else if (conditionType == ConditionType.IN) {
                for (Object item : (List<?>) value) {
                    Long hashValue = Long.parseLong(item.toString());
                    int hashIndex = (int) (hashValue % shardingCount);
                    String suffix = String.valueOf(hashIndex + shardingStart);
                    String physicalTableName = TableUtils.getPhysicalTableName(tableName, suffix);
                    if (!availableTableNames.contains(physicalTableName)) {
                        throw new IllegalArgumentException("Physical table name not available: " + physicalTableName);
                    }
                    if (!shardingResult.contains(suffix)) {
                        shardingResult.add(suffix);
                    }
                    if (shardingResult.size() == availableTableNames.size()) {
                        break;
                    }
                }
            } else {
                shardingResult.addAll(availableTableNames.stream()
                        .map(TableUtils::getSuffix)
                        .collect(Collectors.toList()));
            }
        } else {
            shardingResult.addAll(availableTableNames.stream()
                    .map(TableUtils::getSuffix)
                    .collect(Collectors.toList()));
        }

        return shardingResult;
    }

    @Override
    public StrategyType getType() {
        return StrategyType.HASH;
    }

    @Override
    public void setAvailableTables(ShardingRuleConfig shardingRuleConfig, List<String> tables) {
        String tableName = shardingRuleConfig.getTableName();
        List<String> availableTables = tables.stream()
                .filter(table -> {
                    // 过滤出逻辑表名前缀相同的物理表名
                    String prefix = TableUtils.getPrefix(tableName);
                    boolean flag = table.startsWith(prefix);
                    if (flag) {
                        // 检查前缀之后是否仍有下划线,如果有,则不是哈希分片表
                        flag = table.substring(prefix.length()).contains(TableUtils.SEPARATOR);
                        if (flag) {
                            return false;
                        }
                        // 判断后缀是否为数字
                        String suffix = TableUtils.getSuffix(table);
                        if (suffix.matches("\\d+")) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        shardingRuleConfig.setAvailableTableNames(availableTables);
    }

}
