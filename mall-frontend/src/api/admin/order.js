/**
 * 管理端 - 订单管理 API
 */
import request from '../request'

/**
 * 获取所有订单
 * @param {Object} params - 查询参数 { page, size, status, keyword }
 */
export const getAllOrders = (params) => {
  return request({
    url: '/admin/orders',
    method: 'GET',
    params
  })
}

/**
 * 按状态获取订单（支持分页）
 * @param {string} status - 订单状态
 * @param {Object} params - 查询参数 { page, size }
 */
export const getOrdersByStatus = (status, params) => {
  return request({
    url: `/admin/orders/status/${status}`,
    method: 'GET',
    params
  })
}

/**
 * 获取订单详情
 * @param {string} orderNo - 订单号
 */
export const getOrderDetail = (orderNo) => {
  return request({
    url: `/admin/orders/${orderNo}`,
    method: 'GET'
  })
}

/**
 * 订单发货
 * @param {string} orderNo - 订单号
 */
export const shipOrder = (orderNo) => {
  return request({
    url: `/admin/orders/${orderNo}/ship`,
    method: 'PUT'
  })
}

/**
 * 取消订单（管理员）
 * @param {string} orderNo - 订单号
 */
export const cancelOrder = (orderNo) => {
  return request({
    url: `/admin/orders/${orderNo}/cancel`,
    method: 'PUT'
  })
}
