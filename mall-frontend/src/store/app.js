/**
 * 应用全局状态管理
 */
import { defineStore } from 'pinia'
import { getAllCategories } from '@/api/category'

export const useAppStore = defineStore('app', {
  state: () => ({
    // 全局加载状态
    globalLoading: false,

    // 分类列表
    categories: [],

    // 侧边栏折叠状态（管理端）
    sidebarCollapsed: false,

    // 设备类型
    device: 'desktop' // desktop | mobile
  }),

  getters: {
    /**
     * 是否为移动端
     */
    isMobile: (state) => {
      return state.device === 'mobile'
    }
  },

  actions: {
    /**
     * 设置全局加载状态
     * @param {boolean} loading - 加载状态
     */
    setGlobalLoading(loading) {
      this.globalLoading = loading
    },

    /**
     * 获取分类列表
     */
    async fetchCategories() {
      try {
        const data = await getAllCategories()
        this.categories = data || []
        return data
      } catch (error) {
        console.error('获取分类列表失败:', error)
        this.categories = []
      }
    },

    /**
     * 切换侧边栏折叠状态
     */
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },

    /**
     * 设置设备类型
     * @param {string} device - 设备类型
     */
    setDevice(device) {
      this.device = device
    },

    /**
     * 根据窗口宽度检测设备类型
     */
    detectDevice() {
      const width = window.innerWidth
      this.device = width < 768 ? 'mobile' : 'desktop'
    }
  }
})
