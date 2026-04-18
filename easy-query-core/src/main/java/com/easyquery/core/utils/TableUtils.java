package com.easyquery.core.utils;

/**
 * 表名工具类
 */
public class TableUtils {

    public static final String SEPARATOR = "_";

    /**
     * 根据逻辑表名和后缀拼接物理表名（默认使用下划线连接）
     *
     * @param logicalTableName 逻辑表名
     * @param suffix           后缀（如 0, 1, 2）
     * @return 物理表名（如 user_0, user_1）
     */
    public static String getPhysicalTableName(String logicalTableName, String suffix) {
        if (logicalTableName == null || logicalTableName.isEmpty()) {
            throw new IllegalArgumentException("逻辑表名不能为空");
        }
        if (suffix == null || suffix.isEmpty()) {
            return logicalTableName;
        }
        return logicalTableName + SEPARATOR + suffix;
    }

    /**
     * 从物理表名提取逻辑表名
     *
     * @param physicalTableName 物理表名（如 user_0, user_1）
     * @return 逻辑表名
     */
    public static String getLogicalTableName(String physicalTableName) {
        if (physicalTableName == null || physicalTableName.isEmpty()) {
            throw new IllegalArgumentException("物理表名不能为空");
        }
        int lastIndex = physicalTableName.lastIndexOf(SEPARATOR);
        if (lastIndex > 0) {
            return physicalTableName.substring(0, lastIndex);
        }
        return physicalTableName;
    }

    /**
     * 从物理表名提取后缀
     *
     * @param physicalTableName 物理表名（如 user_0, user_1）
     * @return 后缀，如果不存在返回 ""
     */
    public static String getSuffix(String physicalTableName) {
        if (physicalTableName == null || physicalTableName.isEmpty()) {
            return "";
        }
        int lastIndex = physicalTableName.lastIndexOf(SEPARATOR);
        if (lastIndex > 0) {
            return physicalTableName.substring(lastIndex + 1);
        }
        return "";
    }

    public static String getPrefix(String logicalTableName) {
        return logicalTableName.concat(SEPARATOR);
    }

}
