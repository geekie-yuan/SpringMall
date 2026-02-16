-- V1.2: 添加退款功能支持
-- 创建退款记录表

CREATE TABLE IF NOT EXISTS mall_refund (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '退款ID',
    refund_no VARCHAR(64) NOT NULL COMMENT '退款流水号',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    payment_no VARCHAR(64) NOT NULL COMMENT '支付流水号',
    trade_no VARCHAR(64) COMMENT '支付宝交易号',
    refund_amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    refund_reason VARCHAR(255) COMMENT '退款原因',
    refund_status VARCHAR(32) NOT NULL COMMENT '退款状态：PROCESSING-处理中/SUCCESS-成功/FAILED-失败',
    refund_time DATETIME COMMENT '退款成功时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_refund_no (refund_no),
    KEY idx_order_no (order_no),
    KEY idx_payment_no (payment_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';
