package com.easyquery.web.enums;

/**
 * 数据源连接状态
 *
 * @author easy-query
 */
public enum ConnectionStatus {

    /** 连接成功 */
    CONNECTED("连接成功"),

    /** 连接失败 */
    DISCONNECTED("连接失败");

    private final String label;

    ConnectionStatus(String label) {
        this.label = label;
    }

    /**
     * 获取中文描述
     *
     * @return 中文描述
     */
    public String getLabel() {
        return label;
    }
}
