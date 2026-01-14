/**
 * 本地存储工具函数
 * 用于管理 Token 和用户信息
 */

// 存储键名常量
export const TOKEN_KEY = 'mall_token'
export const USER_KEY = 'mall_user'

/**
 * Token 管理
 */
export const getToken = () => {
  return localStorage.getItem(TOKEN_KEY)
}

export const setToken = (token) => {
  localStorage.setItem(TOKEN_KEY, token)
}

export const removeToken = () => {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 用户信息管理
 */
export const getUser = () => {
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}

export const setUser = (user) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const removeUser = () => {
  localStorage.removeItem(USER_KEY)
}

/**
 * 清除所有存储
 */
export const clearStorage = () => {
  removeToken()
  removeUser()
}
