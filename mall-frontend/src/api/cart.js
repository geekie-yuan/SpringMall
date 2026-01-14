/**
 * 购物车相关 API
 */
import request from './request'

/**
 * 获取购物车列表
 */
export const getCart = () => {
  return request({
    url: '/cart',
    method: 'GET'
  })
}

/**
 * 添加商品到购物车
 * @param {Object} data - 购物车项 { productId, quantity }
 */
export const addToCart = (data) => {
  return request({
    url: '/cart',
    method: 'POST',
    data
  })
}

/**
 * 更新购物车商品数量
 * @param {number} id - 购物车项 ID
 * @param {number} quantity - 数量
 */
export const updateCartItem = (id, quantity) => {
  return request({
    url: `/cart/${id}/quantity`,
    method: 'PUT',
    params: { quantity }
  })
}

/**
 * 删除购物车商品
 * @param {number} id - 购物车项 ID
 */
export const deleteCartItem = (id) => {
  return request({
    url: `/cart/${id}`,
    method: 'DELETE'
  })
}

/**
 * 切换购物车商品选中状态
 * @param {number} id - 购物车项 ID
 * @param {number} checked - 选中状态（0-未选中，1-已选中）
 */
export const checkCartItem = (id, checked) => {
  return request({
    url: `/cart/${id}/checked`,
    method: 'PUT',
    params: { checked }
  })
}

/**
 * 全选/取消全选购物车
 * @param {number} checked - 选中状态（0-未选中，1-已选中）
 */
export const checkAllCart = (checked) => {
  return request({
    url: '/cart/checked',
    method: 'PUT',
    params: { checked }
  })
}
