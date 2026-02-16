-- ========================================
-- 添加支付功能支持
-- V1.1 - 2026-02-14
-- ========================================

USE mall;

-- 1. 在订单表中添加支付方式字段
ALTER TABLE `mall_order`
ADD COLUMN `payment_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式：ALIPAY/WECHAT' AFTER `status`;

-- 2. 创建支付记录表
CREATE TABLE IF NOT EXISTS `mall_payment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付ID',
    `payment_no` VARCHAR(50) NOT NULL COMMENT '支付流水号（唯一）',
    `order_no` VARCHAR(50) NOT NULL COMMENT '关联订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式：ALIPAY/WECHAT',
    `payment_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '支付状态：PENDING-待支付/SUCCESS-成功/FAILED-失败/CLOSED-已关闭',
    `trade_no` VARCHAR(100) DEFAULT NULL COMMENT '第三方交易号',
    `notify_time` DATETIME DEFAULT NULL COMMENT '异步通知时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_payment_status` (`payment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';
