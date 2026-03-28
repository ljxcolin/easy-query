package com.easyquery.core.enums;

public enum StrategyType {
    /**
     * 哈希分片策略
     */
    HASH,
    /**
     * 范围分片策略
     */
    RANGE,
    /**
     * 按年分片策略
     */
    YEAR,
    /**
     * 按月分片策略
     */
    MONTH,
    /**
     * 按天分片策略
     */
    DAY,
    /**
     * 按年月分片策略
     */
    YEAR_MONTH,
    ;

}
