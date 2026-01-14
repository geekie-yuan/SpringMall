/**
 * 用户状态管理
 */
import { defineStore } from 'pinia'
import { getUserInfo as getUserInfoApi, updateUserInfo as updateUserInfoApi } from '@/api/user'
import { getAddresses as getAddressesApi } from '@/api/address'
import { ElMessage } from 'element-plus'
import { useAuthStore } from './auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    addresses: [],
    loading: false
  }),

  getters: {
    /**
     * 默认地址
     */
    defaultAddress: (state) => {
      return state.addresses.find(addr => addr.isDefault) || state.addresses[0]
    }
  },

  actions: {
    /**
     * 获取用户详细信息
     */
    async fetchUserInfo() {
      try {
        this.loading = true
        const data = await getUserInfoApi()
        this.userInfo = data

        // 同步更新 authStore 中的用户信息
        const authStore = useAuthStore()
        authStore.updateUser(data)

        return data
      } catch (error) {
        console.error('获取用户信息失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 更新用户信息
     * @param {Object} data - 用户信息
     */
    async updateUserInfo(data) {
      try {
        const updatedUser = await updateUserInfoApi(data)
        this.userInfo = updatedUser

        // 同步更新 authStore
        const authStore = useAuthStore()
        authStore.updateUser(updatedUser)

        ElMessage.success('信息更新成功')
        return updatedUser
      } catch (error) {
        console.error('更新用户信息失败:', error)
        throw error
      }
    },

    /**
     * 获取地址列表
     */
    async fetchAddresses() {
      try {
        const data = await getAddressesApi()
        this.addresses = data || []
        return data
      } catch (error) {
        console.error('获取地址列表失败:', error)
        this.addresses = []
        throw error
      }
    }
  }
})
