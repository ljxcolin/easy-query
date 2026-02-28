package com.easyquery.core.config;

import com.easyquery.core.config.impl.DefaultConfigManager;
import java.util.Map;

/**
 * 配置管理器工厂
 */
public class ConfigManagerFactory {
    
    /**
     * 创建配置管理器实例
     * @param config 配置管理器配置
     * @return 配置管理器实例
     */
    public static ConfigManager createConfigManager(Map<String, Object> config) {
        ConfigManager configManager;
        
        // 检查是否使用自定义配置管理器
        if (config.containsKey("customClassName")) {
            try {
                String className = (String) config.get("customClassName");
                Class<?> clazz = Class.forName(className);
                configManager = (ConfigManager) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create custom config manager", e);
            }
        } else {
            // 使用默认配置管理器
            configManager = new DefaultConfigManager();
        }
        
        // 初始化配置管理器
        configManager.init(config);
        
        return configManager;
    }
    
    /**
     * 创建默认的配置管理器实例
     * @return 默认配置管理器实例
     */
    public static ConfigManager createDefaultConfigManager() {
        ConfigManager configManager = new DefaultConfigManager();
        configManager.init(java.util.Collections.emptyMap());
        return configManager;
    }
}
