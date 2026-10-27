package com.easyquery.web.dto;

/**
 * SQL 查询保存请求（入参 DTO）
 *
 * @author easy-query
 */
public class SqlQueryRequest {

    /** 查询名称 */
    private String name;

    /** 数据源 ID */
    private Long dataSourceId;

    /** 数据源名称（可选，后端以运行时配置为准） */
    private String dataSourceName;

    /** SQL 内容 */
    private String sqlContent;

    /** 描述 */
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getSqlContent() {
        return sqlContent;
    }

    public void setSqlContent(String sqlContent) {
        this.sqlContent = sqlContent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
