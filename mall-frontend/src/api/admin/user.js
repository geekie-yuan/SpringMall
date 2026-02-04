/**
 * 管理端 - 用户管理 API
 */
import request from '../request'

/**
 * 获取所有用户
 * @param {Object} params - 查询参数 { page, size, keyword, role, status }
 */
export const getAllUsers = (params) => {
  return request({
    url: '/admin/users',
    method: 'GET',
    params
  })
}

/**
 * 更新用户状态（启用/禁用）
 * @param {number} id - 用户 ID
 * @param {number} status - 状态（1-正常，0-禁用）
 */
export const updateUserStatus = (id, status) => {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'PUT',
    params: { status }
  })
}

/**
 * 更新用户角色
 * @param {number} id - 用户 ID
 * @param {string} role - 角色（USER/ADMIN）
 */
export const updateUserRole = (id, role) => {
  return request({
    url: `/admin/users/${id}/role`,
    method: 'PUT',
    params: { role }
  })
}
