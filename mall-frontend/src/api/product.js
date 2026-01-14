/**
 * 商品相关 API
 */
import request from './request'

/**
 * 获取所有商品
 * @param {Object} params - 查询参数 { page, size, keyword, categoryId, status }
 */
export const getAllProducts = (params) => {
  return request({
    url: '/products',
    method: 'GET',
    params
  })
}

/**
 * 根据分类获取商品
 * @param {number} categoryId - 分类 ID
 * @param {Object} params - 查询参数 { page, size }
 */
export const getProductsByCategory = (categoryId, params) => {
  return request({
    url: `/products/category/${categoryId}`,
    method: 'GET',
    params
  })
}

/**
 * 根据状态获取商品
 * @param {string} status - 商品状态
 * @param {Object} params - 查询参数 { page, size }
 */
export const getProductsByStatus = (status, params) => {
  return request({
    url: `/products/status/${status}`,
    method: 'GET',
    params
  })
}

/**
 * 搜索商品
 * @param {string} keyword - 搜索关键词
 * @param {Object} params - 查询参数 { page, size }
 */
export const searchProducts = (keyword, params) => {
  return request({
    url: '/products/search',
    method: 'GET',
    params: { keyword, ...params }
  })
}

/**
 * 获取商品详情
 * @param {number} id - 商品 ID
 */
export const getProductDetail = (id) => {
  return request({
    url: `/products/${id}`,
    method: 'GET'
  })
}
