package com.easyquery.core.enums;

/**
 * SQL条件类型枚举
 */
public enum ConditionType {

    /**
     * 等于 =
     */
    EQUALS,

    /**
     * 不等于 !=
     */
    NOT_EQUALS,

    /**
     * 大于 >
     */
    GREATER_THAN,

    /**
     * 大于等于 >=
     */
    GREATER_THAN_OR_EQUAL,

    /**
     * 小于 <
     */
    LESS_THAN,

    /**
     * 小于等于 <=
     */
    LESS_THAN_OR_EQUAL,

    /**
     * LIKE 模糊匹配
     */
    LIKE,

    /**
     * NOT LIKE 不模糊匹配
     */
    NOT_LIKE,

    /**
     * IN 包含
     */
    IN,

    /**
     * NOT IN 不包含
     */
    NOT_IN,

    /**
     * IS NULL 为空
     */
    IS_NULL,

    /**
     * IS NOT NULL 不为空
     */
    IS_NOT_NULL,

    /**
     * BETWEEN 区间（包括边界）
     */
    BETWEEN,

    /**
     * NOT BETWEEN 不在区间
     */
    NOT_BETWEEN

}
