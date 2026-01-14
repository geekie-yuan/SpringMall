-- ========================================
-- Mall Database Schema
-- ========================================

CREATE DATABASE IF NOT EXISTS mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mall;

-- ----------------------------------------
-- 1. 用户表
-- ----------------------------------------
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------------------
-- 2. 商品分类表（支持多级）
-- ----------------------------------------
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID，0为顶级',
    `level` INT NOT NULL DEFAULT 1 COMMENT '层级：1-一级 2-二级 3-三级',
    `sort_order` INT DEFAULT 0 COMMENT '排序值',
    `icon` VARCHAR(500) DEFAULT NULL COMMENT '分类图标',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ----------------------------------------
-- 3. 商品表
-- ----------------------------------------
CREATE TABLE `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `subtitle` VARCHAR(500) DEFAULT NULL COMMENT '副标题',
    `main_image` VARCHAR(500) DEFAULT NULL COMMENT '主图URL',
    `images` TEXT DEFAULT NULL COMMENT '图片列表（JSON数组）',
    `detail` TEXT DEFAULT NULL COMMENT '商品详情（HTML）',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ----------------------------------------
-- 4. 购物车表
-- ----------------------------------------
CREATE TABLE `cart_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `checked` TINYINT NOT NULL DEFAULT 1 COMMENT '是否选中：0-否 1-是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ----------------------------------------
-- 5. 收货地址表
-- ----------------------------------------
CREATE TABLE `address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `province` VARCHAR(50) NOT NULL COMMENT '省份',
    `city` VARCHAR(50) NOT NULL COMMENT '城市',
    `district` VARCHAR(50) NOT NULL COMMENT '区县',
    `detail_address` VARCHAR(200) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认：0-否 1-是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- ----------------------------------------
-- 6. 订单主表
-- ----------------------------------------
CREATE TABLE `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `freight` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费',
    `status` VARCHAR(20) NOT NULL DEFAULT 'UNPAID' COMMENT '订单状态',
    `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `receiver_address` VARCHAR(500) NOT NULL COMMENT '收货地址',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '订单备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ----------------------------------------
-- 7. 订单明细表
-- ----------------------------------------
CREATE TABLE `order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称（冗余）',
    `product_image` VARCHAR(500) DEFAULT NULL COMMENT '商品图片（冗余）',
    `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `quantity` INT NOT NULL COMMENT '数量',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '总价',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ----------------------------------------
-- 初始数据
-- ----------------------------------------

-- 插入默认admin账户（密码: admin123）
INSERT INTO `user` (`username`, `password`, `email`, `role`, `status`) VALUES
('admin', '$2a$10$uISN1BbnQjhKwrx4twz31.X/8cdzxjO.4hvYMPfbWnpYdBlmWgX0G', 'admin@mall.com', 'ADMIN', 1);
-- 插入默认user账户（密码: 123456）
INSERT INTO `user` (`username`, `password`, `email`, `role`, `status`) VALUES
('user', '$2a$10$sVu5LD/7oO8OMJ1wyGy6gueaIwyXdizADClstPIxw5Ux7hbmYLlry', 'user@mall.com', 'USER', 1);

-- 插入示例分类
INSERT INTO `category` (`name`, `parent_id`, `level`, `sort_order`) VALUES
('实体商品', 0, 1, 1),
('虚拟商品', 1, 1, 2);

