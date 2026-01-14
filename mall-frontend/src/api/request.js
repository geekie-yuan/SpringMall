/**
 * Axios 请求封装
 * 统一处理请求和响应
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getToken, removeToken, clearStorage } from '@/utils/storage'

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 自动添加 Token
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data

    // 成功响应
    if (code === 200) {
      return data
    }

    // 业务错误
    ElMessage.error(message || '操作失败')
    return Promise.reject(new Error(message || '操作失败'))
  },
  (error) => {
    // HTTP 错误处理
    if (error.response) {
      const { status, data, config } = error.response

      switch (status) {
        case 401:
          // 区分登录请求失败和 Token 过期
          if (config.url.includes('/auth/login')) {
            // 登录请求失败，显示后端返回的错误信息
            ElMessage.error(data?.message || '用户名或密码错误')
          } else {
            // Token 过期或无效，清除 token 并跳转登录
            ElMessage.error('登录已过期，请重新登录')
            clearStorage()
            router.push('/login')
          }
          break

        case 403:
          // 无权限
          ElMessage.error(data?.message || '无权访问')
          break

        case 404:
          // 资源不存在
          ElMessage.error('请求的资源不存在')
          break

        case 500:
          // 服务器错误
          ElMessage.error('服务器错误，请稍后重试')
          break

        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      // 请求配置错误
      ElMessage.error(error.message || '请求失败')
    }

    return Promise.reject(error)
  }
)

export default request
