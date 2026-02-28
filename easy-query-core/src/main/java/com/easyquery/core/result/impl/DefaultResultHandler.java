package com.easyquery.core.result.impl;

import com.easyquery.core.result.ResultHandler;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 默认结果处理器实现
 */
public class DefaultResultHandler implements ResultHandler {
    
    /**
     * 配置
     */
    private Map<String, Object> config;
    
    @Override
    public List<Map<String, Object>> handleResultSet(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object value = resultSet.getObject(i);
                row.put(columnName, value);
            }
            results.add(row);
        }
        
        return results;
    }
    
    @Override
    public List<Map<String, Object>> mergeResults(List<List<Map<String, Object>>> resultSets) {
        List<Map<String, Object>> mergedResults = new ArrayList<>();
        for (List<Map<String, Object>> resultSet : resultSets) {
            mergedResults.addAll(resultSet);
        }
        return mergedResults;
    }
    
    @Override
    public List<Map<String, Object>> sortResults(List<Map<String, Object>> results, Map<String, String> orderBy) {
        if (orderBy == null || orderBy.isEmpty()) {
            return results;
        }
        
        // 构建比较器
        Comparator<Map<String, Object>> comparator = (row1, row2) -> {
            for (Map.Entry<String, String> entry : orderBy.entrySet()) {
                String column = entry.getKey();
                String direction = entry.getValue();
                
                Object value1 = row1.get(column);
                Object value2 = row2.get(column);
                
                int compareResult = 0;
                if (value1 != null && value2 != null) {
                    if (value1 instanceof Comparable && value2 instanceof Comparable) {
                        compareResult = ((Comparable) value1).compareTo(value2);
                    }
                } else if (value1 != null) {
                    compareResult = -1;
                } else if (value2 != null) {
                    compareResult = 1;
                }
                
                if ("desc".equalsIgnoreCase(direction)) {
                    compareResult = -compareResult;
                }
                
                if (compareResult != 0) {
                    return compareResult;
                }
            }
            return 0;
        };
        
        // 排序
        return results.stream().sorted(comparator).collect(Collectors.toList());
    }
    
    @Override
    public List<Map<String, Object>> pageResults(List<Map<String, Object>> results, int offset, int limit) {
        if (offset < 0) {
            offset = 0;
        }
        if (limit < 0) {
            limit = Integer.MAX_VALUE;
        }
        
        int endIndex = Math.min(offset + limit, results.size());
        if (offset >= endIndex) {
            return new ArrayList<>();
        }
        
        return results.subList(offset, endIndex);
    }
    
    @Override
    public Map<String, Object> calculateAggregations(List<Map<String, Object>> results, List<String> aggregations) {
        Map<String, Object> aggregationResult = new HashMap<>();
        
        if (aggregations == null || aggregations.isEmpty()) {
            return aggregationResult;
        }
        
        for (String aggregation : aggregations) {
            // 简单实现，后续可以扩展支持更多聚合函数
            if (aggregation.equalsIgnoreCase("count")) {
                aggregationResult.put("count", results.size());
            } else if (aggregation.startsWith("sum(")) {
                String column = aggregation.substring(4, aggregation.length() - 1);
                double sum = 0;
                for (Map<String, Object> row : results) {
                    Object value = row.get(column);
                    if (value instanceof Number) {
                        sum += ((Number) value).doubleValue();
                    }
                }
                aggregationResult.put("sum(" + column + ")", sum);
            } else if (aggregation.startsWith("avg(")) {
                String column = aggregation.substring(4, aggregation.length() - 1);
                double sum = 0;
                int count = 0;
                for (Map<String, Object> row : results) {
                    Object value = row.get(column);
                    if (value instanceof Number) {
                        sum += ((Number) value).doubleValue();
                        count++;
                    }
                }
                double avg = count > 0 ? sum / count : 0;
                aggregationResult.put("avg(" + column + ")", avg);
            } else if (aggregation.startsWith("max(")) {
                String column = aggregation.substring(4, aggregation.length() - 1);
                Object max = null;
                for (Map<String, Object> row : results) {
                    Object value = row.get(column);
                    if (value != null) {
                        if (max == null || (value instanceof Comparable && ((Comparable) value).compareTo(max) > 0)) {
                            max = value;
                        }
                    }
                }
                aggregationResult.put("max(" + column + ")", max);
            } else if (aggregation.startsWith("min(")) {
                String column = aggregation.substring(4, aggregation.length() - 1);
                Object min = null;
                for (Map<String, Object> row : results) {
                    Object value = row.get(column);
                    if (value != null) {
                        if (min == null || (value instanceof Comparable && ((Comparable) value).compareTo(min) < 0)) {
                            min = value;
                        }
                    }
                }
                aggregationResult.put("min(" + column + ")", min);
            }
        }
        
        return aggregationResult;
    }
    
    @Override
    public void init(Map<String, Object> config) {
        this.config = config;
    }
}
