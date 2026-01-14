/**
 * 认证相关 API
 */
import request from './request'

/**
 * 用户注册
 * @param {Object} data - 注册信息 { username, email, password }
 */
export const register = (data) => {
  return request({
    url: '/auth/register',
    method: 'POST',
    data
  })
}

/**
 * 用户登录
 * @param {Object} data - 登录信息 { username, password }
 */
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'POST',
    data
  })
}

/**
 * 用户登出
 */
export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'POST'
  })
}
