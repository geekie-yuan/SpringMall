/**
 * 认证状态管理
 */
import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, logout as logoutApi } from '@/api/auth'
import { getUser, setUser, getToken, setToken, clearStorage } from '@/utils/storage'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: getUser(),
    token: getToken(),
    isLoggedIn: !!getToken()
  }),

  getters: {
    /**
     * 判断是否为管理员
     */
    isAdmin: (state) => {
      return state.user?.role === 'ADMIN'
    },

    /**
     * 获取用户名
     */
    username: (state) => {
      return state.user?.username || ''
    },

    /**
     * 获取用户角色
     */
    userRole: (state) => {
      return state.user?.role || ''
    }
  },

  actions: {
    /**
     * 用户登录
     * @param {Object} credentials - 登录凭证 { username, password }
     */
    async login(credentials) {
      try {
        const data = await loginApi(credentials)

        // 保存 token 和用户信息
        this.token = data.token
        this.user = data.user
        this.isLoggedIn = true

        setToken(data.token)
        setUser(data.user)

        ElMessage.success('登录成功')

        // 根据角色跳转到不同页面
        if (this.isAdmin) {
          router.push('/admin')
        } else {
          router.push('/')
        }

        return data
      } catch (error) {
        console.error('登录失败:', error)
        throw error
      }
    },

    /**
     * 用户注册
     * @param {Object} userInfo - 注册信息 { username, email, password }
     */
    async register(userInfo) {
      try {
        await registerApi(userInfo)

        // 注册成功，显示提示并跳转登录页
        await ElMessageBox.alert(
          '注册成功！请使用您的账号登录。',
          '注册成功',
          {
            confirmButtonText: '前往登录',
            type: 'success'
          }
        )

        router.push('/login')

        return true
      } catch (error) {
        console.error('注册失败:', error)
        throw error
      }
    },

    /**
     * 用户登出
     */
    async logout() {
      try {
        await logoutApi()
      } catch (error) {
        console.error('登出请求失败:', error)
      } finally {
        // 无论请求是否成功，都清除本地状态
        this.token = null
        this.user = null
        this.isLoggedIn = false

        clearStorage()

        ElMessage.success('已退出登录')
        router.push('/login')
      }
    },

    /**
     * 初始化认证状态
     * 用于页面刷新后恢复登录状态
     */
    initAuth() {
      const token = getToken()
      const user = getUser()

      if (token && user) {
        this.token = token
        this.user = user
        this.isLoggedIn = true
      } else {
        this.token = null
        this.user = null
        this.isLoggedIn = false
      }
    },

    /**
     * 更新用户信息
     * @param {Object} user - 用户信息
     */
    updateUser(user) {
      this.user = user
      setUser(user)
    }
  }
})
