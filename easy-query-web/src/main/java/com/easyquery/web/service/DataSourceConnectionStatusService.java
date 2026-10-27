package com.easyquery.web.service;

import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.model.DataSourceEntry;
import com.easyquery.web.enums.ConnectionStatus;
import com.easyquery.web.loader.DataSourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源连接状态服务
 * <p>
 * 连接检测存在网络开销（受各数据源 connectTimeout 影响），因此结果缓存于内存：
 * <ul>
 *   <li>应用启动完成后统一预热一次</li>
 *   <li>数据源新增 / 修改 / 手动检测时刷新对应条目</li>
 *   <li>列表接口直接读缓存，避免每次请求都去打数据库</li>
 * </ul>
 * 缓存未命中时保守返回 {@link ConnectionStatus#DISCONNECTED}。
 * </p>
 *
 * @author easy-query
 */
@Service
public class DataSourceConnectionStatusService {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConnectionStatusService.class);

    private final Map<Long, ConnectionStatus> statusCache = new ConcurrentHashMap<>();

    private final DataSourceLoader dataSourceLoader;

    @Autowired
    public DataSourceConnectionStatusService(DataSourceLoader dataSourceLoader) {
        this.dataSourceLoader = dataSourceLoader;
    }

    /**
     * 应用启动完成后预热所有数据源的连接状态
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        Collection<Long> ids = dataSourceLoader.getLoadedDataSourceIds();
        if (ids.isEmpty()) {
            return;
        }
        logger.info("开始预热 {} 个数据源的连接状态", ids.size());
        refreshAll(ids);
        long connected = statusCache.values().stream()
                .filter(status -> status == ConnectionStatus.CONNECTED)
                .count();
        logger.info("连接状态预热完成：成功 {} 个，失败 {} 个", connected, ids.size() - connected);
    }

    /**
     * 批量刷新连接状态（并行检测，避免逐个串行等待连接超时）
     *
     * @param dataSourceIds 数据源 ID 集合
     */
    public void refreshAll(Collection<Long> dataSourceIds) {
        dataSourceIds.parallelStream().forEach(this::refresh);
    }

    /**
     * 刷新并返回指定数据源的连接状态
     *
     * @param dataSourceId 数据源 ID
     * @return 最新连接状态
     */
    public ConnectionStatus refresh(Long dataSourceId) {
        ConnectionStatus status = detect(dataSourceId);
        statusCache.put(dataSourceId, status);
        return status;
    }

    /**
     * 读取缓存的连接状态
     *
     * @param dataSourceId 数据源 ID
     * @return 连接状态，未检测过时返回 CONNECTED 之外的保守值 DISCONNECTED
     */
    public ConnectionStatus get(Long dataSourceId) {
        return statusCache.getOrDefault(dataSourceId, ConnectionStatus.DISCONNECTED);
    }

    /**
     * 清除缓存（数据源被删除时调用）
     *
     * @param dataSourceId 数据源 ID
     */
    public void evict(Long dataSourceId) {
        statusCache.remove(dataSourceId);
    }

    /**
     * 真实检测一次连接
     *
     * @param dataSourceId 数据源 ID
     * @return 连接状态
     */
    private ConnectionStatus detect(Long dataSourceId) {
        DataSourceEntry entry = dataSourceLoader.getDataSourceEntry(dataSourceId);
        if (entry == null) {
            return ConnectionStatus.DISCONNECTED;
        }
        DatabaseAdapter adapter = entry.getDatabaseAdapter();
        if (adapter == null) {
            return ConnectionStatus.DISCONNECTED;
        }
        try {
            return adapter.testConnection() ? ConnectionStatus.CONNECTED : ConnectionStatus.DISCONNECTED;
        } catch (Exception e) {
            logger.debug("数据源 [ID:{}] 连接检测失败：{}", dataSourceId, e.getMessage());
            return ConnectionStatus.DISCONNECTED;
        }
    }
}
