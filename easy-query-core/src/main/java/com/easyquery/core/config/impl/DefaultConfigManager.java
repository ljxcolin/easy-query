package com.easyquery.core.config.impl;

import com.easyquery.core.config.ConfigManager;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import java.util.Map;

/**
 * 默认配置管理器实现
 */
public class DefaultConfigManager implements ConfigManager {
    
    /**
     * 配置对象
     */
    private CompositeConfiguration configuration;
    
    /**
     * 配置管理器配置
     */
    private Map<String, Object> config;
    
    @Override
    public <T> T getConfig(String key) {
        return (T) configuration.getProperty(key);
    }
    
    @Override
    public <T> T getConfig(String key, T defaultValue) {
        return configuration.containsKey(key) ? (T) configuration.getProperty(key) : defaultValue;
    }
    
    @Override
    public void setConfig(String key, Object value) {
        configuration.setProperty(key, value);
    }
    
    @Override
    public void loadConfig(String configPath) {
        try {
            Configurations configs = new Configurations();
            Configuration loadedConfig;
            
            // 根据文件扩展名选择配置类型
            if (configPath.endsWith(".properties")) {
                loadedConfig = configs.properties(configPath);
            } else {
                throw new IllegalArgumentException("Unsupported config file format: " + configPath);
            }
            
            // 添加到复合配置中
            configuration.addConfiguration(loadedConfig);
        } catch (ConfigurationException e) {
            throw new RuntimeException("Failed to load config file: " + configPath, e);
        }
    }
    
    @Override
    public void loadConfig(Map<String, Object> configMap) {
        // 创建一个新的配置对象
        PropertiesConfiguration mapConfig = new PropertiesConfiguration();
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            mapConfig.setProperty(entry.getKey(), entry.getValue());
        }
        
        // 添加到复合配置中
        configuration.addConfiguration(mapConfig);
    }
    
    @Override
    public void updateConfig(String key, Object value) {
        configuration.setProperty(key, value);
    }
    
    @Override
    public void init(Map<String, Object> config) {
        this.config = config;
        this.configuration = new CompositeConfiguration();
        
        // 初始化默认配置
        PropertiesConfiguration defaultConfig = new PropertiesConfiguration();
        defaultConfig.setProperty("sharding.defaultStrategy", "HASH");
        defaultConfig.setProperty("sharding.dataSourceCount", 2);
        defaultConfig.setProperty("sharding.tableCount", 4);
        defaultConfig.setProperty("database.defaultType", "MYSQL");
        defaultConfig.setProperty("connection.pool.maximumSize", 10);
        defaultConfig.setProperty("connection.pool.minimumIdle", 5);
        
        configuration.addConfiguration(defaultConfig);
        
        // 加载配置文件
        if (config.containsKey("configPath")) {
            String configPath = (String) config.get("configPath");
            loadConfig(configPath);
        }
        
        // 加载配置映射
        if (config.containsKey("configMap")) {
            Map<String, Object> configMap = (Map<String, Object>) config.get("configMap");
            loadConfig(configMap);
        }
    }
}
