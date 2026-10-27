package com.easyquery.web.vo;

import com.easyquery.web.entity.SqlQueryEntity;

import java.time.LocalDateTime;

/**
 * SQL 查询响应对象（出参 VO）
 * <p>
 * 不暴露 userId 等内部字段。
 * </p>
 *
 * @author easy-query
 */
public class SqlQueryVO {

    private Long id;
    private String name;
    private Long dataSourceId;
    private String dataSourceName;
    private String sqlContent;
    private String description;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    /**
     * 实体转 VO
     *
     * @param entity SQL 查询实体
     * @return VO，entity 为 null 时返回 null
     */
    public static SqlQueryVO from(SqlQueryEntity entity) {
        if (entity == null) {
            return null;
        }
        SqlQueryVO vo = new SqlQueryVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDataSourceId(entity.getDataSourceId());
        vo.setDataSourceName(entity.getDataSourceName());
        vo.setSqlContent(entity.getSqlContent());
        vo.setDescription(entity.getDescription());
        vo.setCreatedTime(entity.getCreatedTime());
        vo.setUpdatedTime(entity.getUpdatedTime());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}
