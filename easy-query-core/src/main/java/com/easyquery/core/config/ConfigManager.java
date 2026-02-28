package com.easyquery.core.config;

import java.util.Map;

/**
 * 配置管理器接口
 */
public interface ConfigManager {
    
    /**
     * 获取配置
     * @param key 配置键
     * @param <T> 配置类型
     * @return 配置值
     */
    <T> T getConfig(String key);
    
    /**
     * 获取配置，带默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @param <T> 配置类型
     * @return 配置值
     */
    <T> T getConfig(String key, T defaultValue);
    
    /**
     * 设置配置
     * @param key 配置键
     * @param value 配置值
     */
    void setConfig(String key, Object value);
    
    /**
     * 加载配置
     * @param configPath 配置文件路径
     */
    void loadConfig(String configPath);
    
    /**
     * 加载配置
     * @param configMap 配置映射
     */
    void loadConfig(Map<String, Object> configMap);
    
    /**
     * 热更新配置
     * @param key 配置键
     * @param value 配置值
     */
    void updateConfig(String key, Object value);
    
    /**
     * 初始化配置管理器
     * @param config 配置管理器配置
     */
    void init(Map<String, Object> config);
}
