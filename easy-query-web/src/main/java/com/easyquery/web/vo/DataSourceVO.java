package com.easyquery.web.vo;

import com.easyquery.web.entity.DataSourceEntity;
import com.easyquery.web.enums.ConnectionStatus;

import java.time.LocalDateTime;

/**
 * 数据源响应对象（出参 VO）
 * <p>
 * 注意：<b>不包含 password 字段</b>，避免凭据通过接口泄漏。
 * </p>
 *
 * @author easy-query
 */
public class DataSourceVO {

    private Long id;
    private String name;
    private String url;
    private String username;
    private String driverClassName;
    private Integer maximumPoolSize;
    private Integer minimumIdle;
    private Long connectionTimeout;
    private Long idleTimeout;

    /** 连接状态：CONNECTED / DISCONNECTED */
    private ConnectionStatus connectionStatus;

    /** 连接状态中文描述 */
    private String connectionStatusLabel;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    /**
     * 实体转 VO（不含连接状态，需由调用方填充）
     *
     * @param entity 数据源实体
     * @return VO，entity 为 null 时返回 null
     */
    public static DataSourceVO from(DataSourceEntity entity) {
        if (entity == null) {
            return null;
        }
        DataSourceVO vo = new DataSourceVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setUrl(entity.getUrl());
        vo.setUsername(entity.getUsername());
        vo.setDriverClassName(entity.getDriverClassName());
        vo.setMaximumPoolSize(entity.getMaximumPoolSize());
        vo.setMinimumIdle(entity.getMinimumIdle());
        vo.setConnectionTimeout(entity.getConnectionTimeout());
        vo.setIdleTimeout(entity.getIdleTimeout());
        vo.setCreatedTime(entity.getCreatedTime());
        vo.setUpdatedTime(entity.getUpdatedTime());
        return vo;
    }

    /**
     * 填充连接状态
     *
     * @param status 连接状态
     * @return 当前对象，便于链式调用
     */
    public DataSourceVO withConnectionStatus(ConnectionStatus status) {
        this.connectionStatus = status == null ? ConnectionStatus.DISCONNECTED : status;
        this.connectionStatusLabel = this.connectionStatus.getLabel();
        return this;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(Integer maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public Integer getMinimumIdle() {
        return minimumIdle;
    }

    public void setMinimumIdle(Integer minimumIdle) {
        this.minimumIdle = minimumIdle;
    }

    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getConnectionStatusLabel() {
        return connectionStatusLabel;
    }

    public void setConnectionStatusLabel(String connectionStatusLabel) {
        this.connectionStatusLabel = connectionStatusLabel;
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
