/**
 * 管理端 - 分类管理 API
 */
import request from '../request'

/**
 * 创建分类
 * @param {Object} data - 分类信息
 */
export const createCategory = (data) => {
  return request({
    url: '/admin/categories',
    method: 'POST',
    data
  })
}

/**
 * 更新分类
 * @param {number} id - 分类 ID
 * @param {Object} data - 分类信息
 */
export const updateCategory = (id, data) => {
  return request({
    url: `/admin/categories/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除分类
 * @param {number} id - 分类 ID
 */
export const deleteCategory = (id) => {
  return request({
    url: `/admin/categories/${id}`,
    method: 'DELETE'
  })
}
