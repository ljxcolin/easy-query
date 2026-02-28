package com.easyquery.core.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 结果处理器接口
 */
public interface ResultHandler {
    
    /**
     * 处理单个结果集
     * @param resultSet 结果集
     * @return 处理后的结果
     * @throws SQLException SQL异常
     */
    List<Map<String, Object>> handleResultSet(ResultSet resultSet) throws SQLException;
    
    /**
     * 合并多个结果集
     * @param resultSets 多个结果集
     * @return 合并后的结果
     */
    List<Map<String, Object>> mergeResults(List<List<Map<String, Object>>> resultSets);
    
    /**
     * 对结果进行排序
     * @param results 结果集
     * @param orderBy 排序字段和方向
     * @return 排序后的结果
     */
    List<Map<String, Object>> sortResults(List<Map<String, Object>> results, Map<String, String> orderBy);
    
    /**
     * 对结果进行分页
     * @param results 结果集
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 分页后的结果
     */
    List<Map<String, Object>> pageResults(List<Map<String, Object>> results, int offset, int limit);
    
    /**
     * 计算聚合函数
     * @param results 结果集
     * @param aggregations 聚合函数配置
     * @return 聚合结果
     */
    Map<String, Object> calculateAggregations(List<Map<String, Object>> results, List<String> aggregations);
    
    /**
     * 初始化结果处理器
     * @param config 配置
     */
    void init(Map<String, Object> config);
}
