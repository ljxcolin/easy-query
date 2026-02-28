package com.easyquery.core.result;

import com.easyquery.core.result.impl.DefaultResultHandler;
import java.util.Map;

/**
 * 结果处理器工厂
 */
public class ResultHandlerFactory {
    
    /**
     * 创建结果处理器实例
     * @param config 结果处理器配置
     * @return 结果处理器实例
     */
    public static ResultHandler createResultHandler(Map<String, Object> config) {
        ResultHandler resultHandler;
        
        // 检查是否使用自定义结果处理器
        if (config.containsKey("customClassName")) {
            try {
                String className = (String) config.get("customClassName");
                Class<?> clazz = Class.forName(className);
                resultHandler = (ResultHandler) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create custom result handler", e);
            }
        } else {
            // 使用默认结果处理器
            resultHandler = new DefaultResultHandler();
        }
        
        // 初始化结果处理器
        resultHandler.init(config);
        
        return resultHandler;
    }
    
    /**
     * 创建默认的结果处理器实例
     * @return 默认结果处理器实例
     */
    public static ResultHandler createDefaultResultHandler() {
        ResultHandler resultHandler = new DefaultResultHandler();
        resultHandler.init(java.util.Collections.emptyMap());
        return resultHandler;
    }
}
