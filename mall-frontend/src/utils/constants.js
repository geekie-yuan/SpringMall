/**
 * 常量定义
 */

/**
 * 订单状态
 */
export const ORDER_STATUS = {
  UNPAID: 'UNPAID',
  PAID: 'PAID',
  SHIPPED: 'SHIPPED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED'
}

/**
 * 订单状态文本映射
 */
export const ORDER_STATUS_TEXT = {
  UNPAID: '待支付',
  PAID: '待发货',
  SHIPPED: '待收货',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

/**
 * 订单状态标签类型映射（Element Plus Tag）
 */
export const ORDER_STATUS_TAG_TYPE = {
  UNPAID: 'warning',
  PAID: 'primary',
  SHIPPED: 'info',
  COMPLETED: 'success',
  CANCELLED: 'danger'
}

/**
 * 商品状态
 */
export const PRODUCT_STATUS = {
  ON_SALE: 'ON_SALE',
  OFF_SALE: 'OFF_SALE',
  OUT_OF_STOCK: 'OUT_OF_STOCK'
}

/**
 * 商品状态文本映射
 */
export const PRODUCT_STATUS_TEXT = {
  ON_SALE: '在售',
  OFF_SALE: '下架',
  OUT_OF_STOCK: '缺货'
}

/**
 * 用户角色
 */
export const USER_ROLE = {
  USER: 'USER',
  ADMIN: 'ADMIN'
}

/**
 * 用户角色文本映射
 */
export const USER_ROLE_TEXT = {
  USER: '普通用户',
  ADMIN: '管理员'
}

/**
 * 支付方式
 */
export const PAYMENT_METHOD = {
  ALIPAY: 'ALIPAY',
  WECHAT: 'WECHAT',
  BALANCE: 'BALANCE'
}

/**
 * 支付方式文本映射
 */
export const PAYMENT_METHOD_TEXT = {
  ALIPAY: '支付宝',
  WECHAT: '微信支付',
  BALANCE: '余额支付'
}

/**
 * 每页显示数量选项
 */
export const PAGE_SIZES = [10, 20, 50, 100]

/**
 * 默认每页显示数量
 */
export const DEFAULT_PAGE_SIZE = 20

/**
 * 图片上传最大尺寸（MB）
 */
export const MAX_IMAGE_SIZE = 5

/**
 * 允许的图片格式
 */
export const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
