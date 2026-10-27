package com.easyquery.web.dto;

/**
 * 数据源创建/更新请求（入参 DTO）
 * <p>
 * 与实体分离，避免客户端直接构造实体字段（如 userId、id、状态等）。
 * 更新场景下 password 允许为空，表示"保持原密码不变"。
 * </p>
 *
 * @author easy-query
 */
public class DataSourceRequest {

    /** 数据源名称（创建后不可修改） */
    private String name;

    /** JDBC 连接 URL */
    private String url;

    /** 数据库用户名 */
    private String username;

    /** 数据库密码：创建时必填；更新时留空表示不修改 */
    private String password;

    /** 驱动类名 */
    private String driverClassName;

    /** 最大连接数 */
    private Integer maximumPoolSize;

    /** 最小空闲连接数 */
    private Integer minimumIdle;

    /** 连接超时时间（ms） */
    private Long connectionTimeout;

    /** 空闲超时时间（ms） */
    private Long idleTimeout;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
