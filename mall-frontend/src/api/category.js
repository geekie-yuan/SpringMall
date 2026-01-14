/**
 * 分类相关 API
 */
import request from './request'

/**
 * 获取所有分类
 */
export const getAllCategories = () => {
  return request({
    url: '/categories',
    method: 'GET'
  })
}

/**
 * 根据 ID 获取分类
 * @param {number} id - 分类 ID
 */
export const getCategoryById = (id) => {
  return request({
    url: `/categories/${id}`,
    method: 'GET'
  })
}
