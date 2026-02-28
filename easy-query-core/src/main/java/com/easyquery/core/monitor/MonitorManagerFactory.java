package com.easyquery.core.monitor;

import com.easyquery.core.monitor.impl.DefaultMonitorManager;
import java.util.Map;

/**
 * 监控管理器工厂
 */
public class MonitorManagerFactory {
    
    /**
     * 创建监控管理器实例
     * @param config 监控管理器配置
     * @return 监控管理器实例
     */
    public static MonitorManager createMonitorManager(Map<String, Object> config) {
        MonitorManager monitorManager;
        
        // 检查是否使用自定义监控管理器
        if (config.containsKey("customClassName")) {
            try {
                String className = (String) config.get("customClassName");
                Class<?> clazz = Class.forName(className);
                monitorManager = (MonitorManager) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create custom monitor manager", e);
            }
        } else {
            // 使用默认监控管理器
            monitorManager = new DefaultMonitorManager();
        }
        
        // 初始化监控管理器
        monitorManager.init(config);
        
        return monitorManager;
    }
    
    /**
     * 创建默认的监控管理器实例
     * @return 默认监控管理器实例
     */
    public static MonitorManager createDefaultMonitorManager() {
        MonitorManager monitorManager = new DefaultMonitorManager();
        monitorManager.init(java.util.Collections.emptyMap());
        return monitorManager;
    }
}
