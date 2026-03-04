-- ============================================
-- easy-query 数据库初始化脚本
-- 版本：1.0.0
-- 数据库：MySQL 5.7+
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `easyquery` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `easyquery`;

-- ============================================
-- 1. 数据源配置表
-- ============================================
DROP TABLE IF EXISTS `data_source`;
CREATE TABLE `data_source` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `name` VARCHAR(64) NOT NULL COMMENT '数据源名称',
    `url` VARCHAR(256) NOT NULL COMMENT 'JDBC 连接 URL',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(128) NOT NULL COMMENT '密码',
    `driver_class_name` VARCHAR(128) NOT NULL DEFAULT 'com.mysql.cj.jdbc.Driver' COMMENT '驱动类名',
    `maximum_pool_size` INT(11) NOT NULL DEFAULT 10 COMMENT '最大连接数',
    `minimum_idle` INT(11) NOT NULL DEFAULT 5 COMMENT '最小空闲连接数',
    `connection_timeout` BIGINT(20) NOT NULL DEFAULT 30000 COMMENT '连接超时时间 (ms)',
    `idle_timeout` BIGINT(20) NOT NULL DEFAULT 600000 COMMENT '空闲超时时间 (ms)',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据源配置表';

-- ============================================
-- 2. 分片策略配置表
-- ============================================
DROP TABLE IF EXISTS `sharding_strategy`;
CREATE TABLE `sharding_strategy` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `name` VARCHAR(64) NOT NULL COMMENT '策略名称',
    `strategy_type` VARCHAR(32) NOT NULL COMMENT '策略类型：HASH, RANGE, LIST, CUSTOM',
    `table_name` VARCHAR(64) NOT NULL COMMENT '逻辑表名',
    `sharding_column` VARCHAR(64) NOT NULL COMMENT '分片字段',
    `config_json` TEXT COMMENT '策略配置 (JSON 格式)',
    `description` VARCHAR(256) DEFAULT NULL COMMENT '描述',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分片策略配置表';

-- ============================================
-- 3. 数据源分片映射表
-- ============================================
DROP TABLE IF EXISTS `data_source_sharding`;
CREATE TABLE `data_source_sharding` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `strategy_id` BIGINT(20) NOT NULL COMMENT '分片策略 ID',
    `data_source_name` VARCHAR(64) NOT NULL COMMENT '数据源名称',
    `sharding_value` VARCHAR(128) NOT NULL COMMENT '分片值 (范围或列表)',
    `sharding_value_type` VARCHAR(32) NOT NULL DEFAULT 'RANGE' COMMENT '分片值类型：RANGE, LIST, HASH',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_strategy_id` (`strategy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据源分片映射表';

-- ============================================
-- 4. 表分片映射表
-- ============================================
DROP TABLE IF EXISTS `table_sharding`;
CREATE TABLE `table_sharding` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `strategy_id` BIGINT(20) NOT NULL COMMENT '分片策略 ID',
    `table_name` VARCHAR(64) NOT NULL COMMENT '物理表名',
    `sharding_value` VARCHAR(128) NOT NULL COMMENT '分片值 (范围或列表)',
    `sharding_value_type` VARCHAR(32) NOT NULL DEFAULT 'RANGE' COMMENT '分片值类型：RANGE, LIST, HASH',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_strategy_id` (`strategy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表分片映射表';

-- ============================================
-- 5. SQL 执行日志表
-- ============================================
DROP TABLE IF EXISTS `sql_log`;
CREATE TABLE `sql_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `trace_id` VARCHAR(64) NOT NULL COMMENT '追踪 ID',
    `sql_text` TEXT NOT NULL COMMENT 'SQL 语句',
    `sql_type` VARCHAR(32) NOT NULL COMMENT 'SQL 类型：SELECT, INSERT, UPDATE, DELETE',
    `data_source_name` VARCHAR(64) NOT NULL COMMENT '数据源名称',
    `table_name` VARCHAR(64) NOT NULL COMMENT '表名',
    `sharding_key` VARCHAR(128) DEFAULT NULL COMMENT '分片键',
    `execute_time_ms` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '执行时间 (ms)',
    `affected_rows` INT(11) NOT NULL DEFAULT 0 COMMENT '影响行数',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    `error_message` TEXT COMMENT '错误信息',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_trace_id` (`trace_id`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL 执行日志表';

-- ============================================
-- 6. 监控指标表
-- ============================================
DROP TABLE IF EXISTS `monitor_metrics`;
CREATE TABLE `monitor_metrics` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `metric_name` VARCHAR(64) NOT NULL COMMENT '指标名称',
    `metric_type` VARCHAR(32) NOT NULL COMMENT '指标类型：QUERY, TRANSACTION, CONNECTION',
    `metric_value` DECIMAL(20,6) NOT NULL COMMENT '指标值',
    `metric_unit` VARCHAR(32) NOT NULL COMMENT '指标单位',
    `data_source_name` VARCHAR(64) DEFAULT NULL COMMENT '数据源名称',
    `labels` VARCHAR(512) DEFAULT NULL COMMENT '标签 (JSON 格式)',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_metric_name` (`metric_name`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监控指标表';

-- ============================================
-- 7. 事务日志表
-- ============================================
DROP TABLE IF EXISTS `transaction_log`;
CREATE TABLE `transaction_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `transaction_id` VARCHAR(64) NOT NULL COMMENT '事务 ID',
    `status` VARCHAR(32) NOT NULL COMMENT '事务状态：BEGIN, COMMIT, ROLLBACK',
    `data_source_name` VARCHAR(64) NOT NULL COMMENT '数据源名称',
    `branch_id` VARCHAR(64) DEFAULT NULL COMMENT '分支事务 ID',
    `operation` VARCHAR(32) NOT NULL COMMENT '操作类型',
    `table_name` VARCHAR(64) NOT NULL COMMENT '表名',
    `before_image` TEXT COMMENT '操作前镜像 (JSON)',
    `after_image` TEXT COMMENT '操作后镜像 (JSON)',
    `execute_time_ms` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '执行时间 (ms)',
    `error_message` TEXT COMMENT '错误信息',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_transaction_id` (`transaction_id`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事务日志表';

-- ============================================
-- 8. 配置历史表
-- ============================================
DROP TABLE IF EXISTS `config_history`;
CREATE TABLE `config_history` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `config_type` VARCHAR(32) NOT NULL COMMENT '配置类型：DATA_SOURCE, SHARDING, OTHER',
    `config_id` BIGINT(20) NOT NULL COMMENT '配置 ID',
    `operation` VARCHAR(32) NOT NULL COMMENT '操作类型：CREATE, UPDATE, DELETE',
    `old_value` TEXT COMMENT '旧值 (JSON)',
    `new_value` TEXT COMMENT '新值 (JSON)',
    `operator` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
    `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_config_type_id` (`config_type`, `config_id`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置历史表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入默认数据源配置
INSERT INTO `data_source` (`name`, `url`, `username`, `password`, `driver_class_name`, `maximum_pool_size`, `minimum_idle`, `status`) VALUES
('ds0', 'jdbc:mysql://localhost:3306/db0?useUnicode=true&characterEncoding=utf8&useSSL=false', 'root', 'password', 'com.mysql.cj.jdbc.Driver', 10, 5, 1),
('ds1', 'jdbc:mysql://localhost:3306/db1?useUnicode=true&characterEncoding=utf8&useSSL=false', 'root', 'password', 'com.mysql.cj.jdbc.Driver', 10, 5, 1);

-- 插入默认分片策略配置
INSERT INTO `sharding_strategy` (`name`, `strategy_type`, `table_name`, `sharding_column`, `config_json`, `description`) VALUES
('user_hash_sharding', 'HASH', 'user', 'id', '{"dataSourceCount": 2, "tableCount": 4}', '用户表哈希分片策略'),
('order_range_sharding', 'RANGE', 'order', 'order_id', '{"dataSourceRanges": {"0": "ds0", "10000": "ds1"}, "tableRanges": {"0": "t0", "2500": "t1", "5000": "t2", "7500": "t3"}}', '订单表范围分片策略');

-- ============================================
-- 示例业务表（用于测试分片）
-- ============================================

-- 用户表（分片表）
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT(20) NOT NULL COMMENT '用户 ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 订单表（分片表）
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `order_id` BIGINT(20) NOT NULL COMMENT '订单 ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户 ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单金额',
    `status` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================
-- 脚本结束
-- ============================================
