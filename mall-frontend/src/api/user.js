/**
 * 用户相关 API
 */
import request from './request'

/**
 * 获取用户信息
 */
export const getUserInfo = () => {
  return request({
    url: '/user/info',
    method: 'GET'
  })
}

/**
 * 更新用户信息
 * @param {Object} data - 用户信息
 */
export const updateUserInfo = (data) => {
  return request({
    url: '/user/info',
    method: 'PUT',
    data
  })
}

/**
 * 修改密码
 * @param {Object} data - 密码信息 { oldPassword, newPassword }
 */
export const updatePassword = (data) => {
  return request({
    url: '/user/password',
    method: 'PUT',
    data
  })
}
