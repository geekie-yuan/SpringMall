# Spring Mall 前端开发指南

## 目录
1. [项目概述](#项目概述)
2. [技术栈](#技术栈)
3. [开发环境准备](#开发环境准备)
4. [项目初始化](#项目初始化)
5. [项目结构](#项目结构)
6. [核心功能实现](#核心功能实现)
7. [开发规范](#开发规范)
8. [调试与测试](#调试与测试)
9. [部署上线](#部署上线)
10. [常见问题](#常见问题)

---

## 项目概述

Spring Mall 是一个完整的电商系统，包含用户端和管理端两个功能模块。本前端项目采用**单项目双端架构**，即在同一个 Vue 3 项目中实现用户端和管理端的所有功能。

### 核心功能模块

**用户端：**
- 用户注册/登录/登出
- 商品浏览（列表、详情、搜索）
- 购物车管理
- 下单结算
- 订单管理（查看、取消、确认收货）
- 个人信息管理
- 收货地址管理

**管理端：**
- 商品管理（CRUD、上下架、库存调整）
- 分类管理（CRUD）
- 订单管理（查看、发货）
- 用户管理（状态管理、角色管理）

---

## 技术栈

### 核心框架
- **Vue 3.4+** - 渐进式 JavaScript 框架
- **Vite 5.0+** - 下一代前端构建工具
- **Vue Router 4.2+** - 官方路由管理器
- **Pinia 2.1+** - 轻量级状态管理

### UI 组件库
- **Element Plus 2.5+** - 基于 Vue 3 的组件库
- **@element-plus/icons-vue** - Element Plus 图标库

### HTTP 客户端
- **Axios 1.6+** - Promise 基于的 HTTP 客户端

### 工具库
- **dayjs 1.11+** - 轻量级日期处理库
- **vue3-lazyload 0.3+** - 图片懒加载

### 开发工具
- **Sass** - CSS 预处理器
- **ESLint** - 代码检查工具
- **Prettier** - 代码格式化工具

---

## 开发环境准备

### 1. 软件安装

确保已安装以下软件：

```bash
# Node.js（推荐 v18.0+）
node --version
# v18.0.0 或更高

# npm（通常随 Node.js 安装）
npm --version
# 9.0.0 或更高
```

### 2. 推荐的 IDE 配置

**VS Code**（推荐）+ 以下插件：
- Vue Language Features (Volar)
- ESLint
- Prettier - Code formatter
- Vue VSCode Snippets

**WebStorm**（备选）：
- 内置 Vue 支持

---

## 项目初始化

### 第一步：创建 Vite + Vue 3 项目

```bash
# 进入工作目录
cd C:\Users\YuanS\Documents\project\springMall

# 使用 Vite 创建项目
npm create vite@latest mall-frontend -- --template vue

# 进入项目目录
cd mall-frontend

# 安装依赖
npm install
```

### 第二步：安装核心依赖

```bash
# 安装路由和状态管理
npm install vue-router@4 pinia

# 安装 Element Plus
npm install element-plus @element-plus/icons-vue

# 安装 HTTP 客户端
npm install axios

# 安装工具库
npm install dayjs vue3-lazyload

# 安装开发依赖
npm install -D sass eslint prettier
```

### 第三步：配置 Vite

编辑 `vite.config.js`：

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

### 第四步：创建环境变量文件

**.env.development**（开发环境）：
```
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

**.env.production**（生产环境）：
```
VITE_API_BASE_URL=https://api.yourdomain.com/api/v1
```

---

## 项目结构

```
mall-frontend/
├── public/                     # 静态资源目录
│   └── favicon.ico
├── src/
│   ├── api/                   # API 接口封装
│   │   ├── request.js         # Axios 实例配置
│   │   ├── auth.js            # 认证 API
│   │   ├── user.js            # 用户 API
│   │   ├── product.js         # 商品 API
│   │   ├── category.js        # 分类 API
│   │   ├── cart.js            # 购物车 API
│   │   ├── order.js           # 订单 API
│   │   ├── address.js         # 地址 API
│   │   ├── payment.js         # 支付 API
│   │   └── admin/             # 管理端 API
│   │       ├── product.js
│   │       ├── category.js
│   │       ├── order.js
│   │       └── user.js
│   │
│   ├── assets/                # 资源文件
│   │   ├── images/
│   │   ├── icons/
│   │   └── styles/
│   │       ├── variables.scss  # SCSS 变量
│   │       ├── common.scss     # 公共样式
│   │       └── reset.scss      # 样式重置
│   │
│   ├── components/            # 公共组件
│   │   ├── common/            # 通用组件
│   │   │   ├── Header.vue
│   │   │   ├── Footer.vue
│   │   │   ├── Loading.vue
│   │   │   └── Empty.vue
│   │   ├── user/              # 用户端组件
│   │   │   ├── ProductCard.vue
│   │   │   ├── CartItem.vue
│   │   │   └── OrderItem.vue
│   │   └── admin/             # 管理端组件
│   │       ├── Sidebar.vue
│   │       └── DataTable.vue
│   │
│   ├── layouts/               # 布局组件
│   │   ├── UserLayout.vue     # 用户端布局
│   │   └── AdminLayout.vue    # 管理端布局
│   │
│   ├── views/                 # 页面组件
│   │   ├── user/              # 用户端页面
│   │   │   ├── Home.vue
│   │   │   ├── ProductList.vue
│   │   │   ├── ProductDetail.vue
│   │   │   ├── Cart.vue
│   │   │   ├── Checkout.vue
│   │   │   ├── OrderList.vue
│   │   │   ├── OrderDetail.vue
│   │   │   ├── Profile.vue
│   │   │   └── Address.vue
│   │   ├── admin/             # 管理端页面
│   │   │   ├── Dashboard.vue
│   │   │   ├── ProductManage.vue
│   │   │   ├── CategoryManage.vue
│   │   │   ├── OrderManage.vue
│   │   │   └── UserManage.vue
│   │   └── auth/              # 认证页面
│   │       ├── Login.vue
│   │       └── Register.vue
│   │
│   ├── router/                # 路由配置
│   │   └── index.js
│   │
│   ├── store/                 # Pinia 状态管理
│   │   ├── index.js
│   │   ├── auth.js            # 认证状态
│   │   ├── cart.js            # 购物车状态
│   │   ├── user.js            # 用户信息状态
│   │   └── app.js             # 应用全局状态
│   │
│   ├── utils/                 # 工具函数
│   │   ├── storage.js         # LocalStorage 封装
│   │   ├── validate.js        # 表单验证
│   │   ├── format.js          # 数据格式化
│   │   └── constants.js       # 常量定义
│   │
│   ├── App.vue                # 根组件
│   └── main.js                # 入口文件
│
├── .env.development           # 开发环境变量
├── .env.production            # 生产环境变量
├── .gitignore
├── index.html
├── package.json
├── vite.config.js
└── README.md
```

---

## 核心功能实现

### 1. 入口文件配置（main.js）

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import VueLazyload from 'vue3-lazyload'

import App from './App.vue'
import router from './router'

// 导入全局样式
import '@/assets/styles/reset.scss'
import '@/assets/styles/common.scss'

const app = createApp(App)
const pinia = createPinia()

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.use(VueLazyload, {
  loading: '/loading.png',
  error: '/error.png'
})

app.mount('#app')
```

### 2. Axios 请求封装（api/request.js）

```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getToken, removeToken } from '@/utils/storage'

// 创建 Axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

// 请求拦截器 - 添加 Token
request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一错误处理
request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data

    if (code === 200) {
      return data
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message))
    }
  },
  error => {
    // Token 过期或未授权
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      removeToken()
      router.push('/login')
    }
    // 无权限
    else if (error.response?.status === 403) {
      ElMessage.error('无权限访问')
    }
    // 其他错误
    else {
      ElMessage.error(error.message || '请求失败')
    }

    return Promise.reject(error)
  }
)

export default request
```

### 3. Token 管理（utils/storage.js）

```javascript
// Token 和用户信息的存储 key
export const TOKEN_KEY = 'mall_token'
export const USER_KEY = 'mall_user'

// Token 操作
export const getToken = () => localStorage.getItem(TOKEN_KEY)
export const setToken = (token) => localStorage.setItem(TOKEN_KEY, token)
export const removeToken = () => localStorage.removeItem(TOKEN_KEY)

// 用户信息操作
export const getUser = () => {
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}
export const setUser = (user) => localStorage.setItem(USER_KEY, JSON.stringify(user))
export const removeUser = () => localStorage.removeItem(USER_KEY)

// 清除所有本地存储
export const clearStorage = () => {
  removeToken()
  removeUser()
}
```

### 4. 路由配置（router/index.js）

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/storage'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'

// 用户端布局
import UserLayout from '@/layouts/UserLayout.vue'
// 管理端布局
import AdminLayout from '@/layouts/AdminLayout.vue'

const routes = [
  // 认证路由
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },

  // 用户端路由
  {
    path: '/',
    component: UserLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/user/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('@/views/user/ProductList.vue'),
        meta: { title: '商品列表' }
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('@/views/user/ProductDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/user/Cart.vue'),
        meta: { title: '购物车', requiresAuth: true }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/user/Checkout.vue'),
        meta: { title: '结算', requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/user/OrderList.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'orders/:orderNo',
        name: 'OrderDetail',
        component: () => import('@/views/user/OrderDetail.vue'),
        meta: { title: '订单详情', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'addresses',
        name: 'Address',
        component: () => import('@/views/user/Address.vue'),
        meta: { title: '地址管理', requiresAuth: true }
      }
    ]
  },

  // 管理端路由
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理后台首页' }
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/ProductManage.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/CategoryManage.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/OrderManage.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken()
  const authStore = useAuthStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - Spring Mall` : 'Spring Mall'

  // 检查是否需要登录
  if (to.meta.requiresAuth && !token) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }

  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin) {
    if (!authStore.isAdmin) {
      ElMessage.error('无权访问管理后台')
      next('/')
      return
    }
  }

  next()
})

export default router
```

### 5. Pinia 状态管理

#### 认证状态（store/auth.js）

```javascript
import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, logout as logoutApi } from '@/api/auth'
import { setToken, removeToken, setUser, removeUser, getUser, clearStorage } from '@/utils/storage'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: getUser(),
    isLoggedIn: false
  }),

  getters: {
    // 是否是管理员
    isAdmin: (state) => state.user?.role === 'ADMIN',
    // 用户名
    username: (state) => state.user?.username || '',
    // 用户角色
    userRole: (state) => state.user?.role || 'USER'
  },

  actions: {
    // 登录
    async login(credentials) {
      try {
        const data = await loginApi(credentials)
        setToken(data.token)
        setUser(data.user)
        this.user = data.user
        this.isLoggedIn = true
        return data
      } catch (error) {
        throw error
      }
    },

    // 注册
    async register(userInfo) {
      try {
        const data = await registerApi(userInfo)
        setToken(data.token)
        setUser(data.user)
        this.user = data.user
        this.isLoggedIn = true
        return data
      } catch (error) {
        throw error
      }
    },

    // 登出
    async logout() {
      try {
        await logoutApi()
      } catch (error) {
        console.error('登出失败:', error)
      } finally {
        clearStorage()
        this.user = null
        this.isLoggedIn = false
      }
    },

    // 初始化用户状态
    initAuth() {
      const user = getUser()
      if (user) {
        this.user = user
        this.isLoggedIn = true
      }
    }
  }
})
```

#### 购物车状态（store/cart.js）

```javascript
import { defineStore } from 'pinia'
import { getCart, addToCart, updateCartItem, deleteCartItem, checkCartItem, checkAllCart } from '@/api/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    loading: false
  }),

  getters: {
    // 购物车商品数量
    cartCount: (state) => state.items.length,

    // 已选中的商品
    checkedItems: (state) => state.items.filter(item => item.checked),

    // 已选中商品的总价
    checkedTotal: (state) => {
      return state.checkedItems.reduce((sum, item) => {
        return sum + item.price * item.quantity
      }, 0)
    },

    // 是否全选
    isAllChecked: (state) => {
      return state.items.length > 0 && state.items.every(item => item.checked)
    }
  },

  actions: {
    // 获取购物车列表
    async fetchCart() {
      try {
        this.loading = true
        this.items = await getCart()
      } catch (error) {
        console.error('获取购物车失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 添加到购物车
    async addItem(productId, quantity = 1) {
      try {
        await addToCart({ productId, quantity })
        await this.fetchCart()
      } catch (error) {
        console.error('添加购物车失败:', error)
        throw error
      }
    },

    // 更新购物车商品数量
    async updateQuantity(id, quantity) {
      try {
        await updateCartItem(id, { quantity })
        await this.fetchCart()
      } catch (error) {
        console.error('更新数量失败:', error)
        throw error
      }
    },

    // 删除购物车商品
    async removeItem(id) {
      try {
        await deleteCartItem(id)
        await this.fetchCart()
      } catch (error) {
        console.error('删除商品失败:', error)
        throw error
      }
    },

    // 选中/取消选中商品
    async toggleCheck(id, checked) {
      try {
        await checkCartItem(id, { checked })
        await this.fetchCart()
      } catch (error) {
        console.error('更新选中状态失败:', error)
        throw error
      }
    },

    // 全选/取消全选
    async toggleCheckAll(checked) {
      try {
        await checkAllCart({ checked })
        await this.fetchCart()
      } catch (error) {
        console.error('全选操作失败:', error)
        throw error
      }
    }
  }
})
```

### 6. API 接口封装示例

#### 认证 API（api/auth.js）

```javascript
import request from './request'

// 注册
export const register = (data) => {
  return request({
    url: '/auth/register',
    method: 'POST',
    data
  })
}

// 登录
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'POST',
    data
  })
}

// 登出
export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'POST'
  })
}
```

#### 商品 API（api/product.js）

```javascript
import request from './request'

// 获取所有商品
export const getAllProducts = () => {
  return request({
    url: '/products',
    method: 'GET'
  })
}

// 根据分类获取商品
export const getProductsByCategory = (categoryId) => {
  return request({
    url: '/products/category/' + categoryId,
    method: 'GET'
  })
}

// 搜索商品
export const searchProducts = (keyword) => {
  return request({
    url: '/products/search',
    method: 'GET',
    params: { keyword }
  })
}

// 获取商品详情
export const getProductDetail = (id) => {
  return request({
    url: '/products/' + id,
    method: 'GET'
  })
}
```

### 7. 响应式设计

#### SCSS 变量和 Mixin（assets/styles/variables.scss）

```scss
// 断点定义
$breakpoint-mobile: 768px;
$breakpoint-tablet: 1024px;
$breakpoint-desktop: 1280px;

// 颜色定义
$primary-color: #409eff;
$success-color: #67c23a;
$warning-color: #e6a23c;
$danger-color: #f56c6c;
$info-color: #909399;

// 响应式 Mixins
@mixin mobile {
  @media (max-width: $breakpoint-mobile) {
    @content;
  }
}

@mixin tablet {
  @media (min-width: $breakpoint-mobile) and (max-width: $breakpoint-tablet) {
    @content;
  }
}

@mixin desktop {
  @media (min-width: $breakpoint-desktop) {
    @content;
  }
}
```

#### 响应式组件示例

```vue
<template>
  <div class="product-card">
    <el-card :body-style="{ padding: '0px' }">
      <img v-lazy="product.mainImage" :alt="product.name" class="product-image" />
      <div class="product-info">
        <h3 class="product-name">{{ product.name }}</h3>
        <p class="product-subtitle">{{ product.subtitle }}</p>
        <div class="product-footer">
          <span class="product-price">¥{{ product.price }}</span>
          <el-button type="primary" size="small" @click="addToCart">
            加入购物车
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useCartStore } from '@/store/cart'
import { ElMessage } from 'element-plus'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const cartStore = useCartStore()

const addToCart = async () => {
  try {
    await cartStore.addItem(props.product.id, 1)
    ElMessage.success('已添加到购物车')
  } catch (error) {
    ElMessage.error('添加失败')
  }
}
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.product-card {
  // PC 端默认样式
  width: 100%;
  padding: 10px;

  @include mobile {
    padding: 5px;
  }

  .product-image {
    width: 100%;
    height: 300px;
    object-fit: cover;

    @include mobile {
      height: 200px;
    }
  }

  .product-info {
    padding: 15px;

    @include mobile {
      padding: 10px;
    }
  }

  .product-name {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 8px;

    @include mobile {
      font-size: 14px;
    }
  }

  .product-subtitle {
    font-size: 14px;
    color: #909399;
    margin-bottom: 10px;

    @include mobile {
      font-size: 12px;
    }
  }

  .product-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .product-price {
    font-size: 20px;
    color: $danger-color;
    font-weight: bold;

    @include mobile {
      font-size: 18px;
    }
  }
}
</style>
```

---

## 开发规范

### 1. 命名规范

- **组件名**：PascalCase（如 `UserProfile.vue`）
- **方法名**：camelCase（如 `getUserInfo`）
- **常量名**：UPPER_SNAKE_CASE（如 `API_BASE_URL`）
- **CSS 类名**：kebab-case（如 `user-profile`）

### 2. 文件组织

- 每个组件一个文件
- 组件文件名使用 PascalCase
- 工具函数按功能模块分类

### 3. 代码风格

- 使用 Composition API
- 使用 `<script setup>` 语法
- Props 必须定义类型
- 合理使用 computed 和 reactive

### 4. Git 提交规范

```
feat: 新增功能
fix: 修复 bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试
chore: 构建/工具变动
```

示例：
```bash
git commit -m "feat: 实现商品列表页面"
git commit -m "fix: 修复购物车数量更新问题"
```

---

## 调试与测试

### 1. 开发模式运行

```bash
npm run dev
```

访问 `http://localhost:3000`

### 2. 使用 Vue DevTools

安装浏览器扩展：
- Chrome: Vue.js devtools
- Firefox: Vue.js devtools

可以查看：
- 组件树
- Pinia 状态
- 路由信息
- 性能分析

### 3. 网络请求调试

在浏览器开发者工具的 Network 面板：
- 查看请求参数
- 查看响应数据
- 检查请求头（Token 是否正确）

### 4. 常见问题调试

**Token 过期：**
- 检查 LocalStorage 中的 token
- 查看 401 响应
- 确认路由守卫是否正确跳转

**跨域问题：**
- 检查 Vite 代理配置
- 确认后端 CORS 配置

**状态更新不生效：**
- 检查 Pinia action 是否正确调用
- 确认组件是否使用了正确的 getter

---

## 部署上线

### 1. 构建生产版本

```bash
npm run build
```

生成的文件在 `dist/` 目录

### 2. Nginx 部署

#### nginx.conf 配置

```nginx
server {
    listen 80;
    server_name yourdomain.com;
    root /var/www/mall-frontend/dist;
    index index.html;

    # 单页应用路由配置
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico)$ {
        expires 7d;
        add_header Cache-Control "public, immutable";
    }
}
```

### 3. Docker 部署

#### Dockerfile

```dockerfile
# 构建阶段
FROM node:18-alpine as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 生产阶段
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### 构建和运行

```bash
# 构建镜像
docker build -t mall-frontend .

# 运行容器
docker run -d -p 80:80 --name mall-frontend mall-frontend
```

### 4. Vercel 部署（推荐用于演示）

```bash
# 安装 Vercel CLI
npm i -g vercel

# 部署
vercel
```

---

## 常见问题

### Q1: Token 过期后如何自动刷新？

**方案 1：后端返回新 Token**
后端在每次响应时返回新的 Token，前端更新 LocalStorage。

**方案 2：刷新 Token 机制**
使用 Refresh Token，在 Access Token 过期前请求新 Token。

### Q2: 如何处理图片上传？

使用 FormData：
```javascript
const uploadImage = async (file) => {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/upload',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
```

### Q3: 如何实现路由懒加载？

```javascript
{
  path: '/products',
  component: () => import('@/views/user/ProductList.vue')
}
```

### Q4: 如何优化首屏加载速度？

1. 使用路由懒加载
2. 图片懒加载
3. Element Plus 按需引入
4. 启用 Gzip 压缩
5. CDN 加速

### Q5: 如何处理移动端适配？

1. 使用 viewport meta 标签
2. 使用 SCSS 响应式 Mixin
3. 使用 Element Plus 的响应式布局组件
4. 测试不同屏幕尺寸

---

## 附录

### A. 完整的 package.json

```json
{
  "name": "mall-frontend",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.0",
    "pinia": "^2.1.0",
    "element-plus": "^2.5.0",
    "@element-plus/icons-vue": "^2.3.0",
    "axios": "^1.6.0",
    "dayjs": "^1.11.0",
    "vue3-lazyload": "^0.3.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0",
    "sass": "^1.70.0",
    "eslint": "^8.50.0",
    "prettier": "^3.1.0"
  }
}
```

### B. 后端 API 参考

详见项目根目录的 `API-GUIDE.md` 文件。

### C. 开发周期估算

- **第一阶段**（1-2天）：基础搭建 + 登录注册
- **第二阶段**（3-4天）：用户端核心功能
- **第三阶段**（2-3天）：管理端功能
- **第四阶段**（1-2天）：优化和测试

**总计**：7-11 天（单人开发）

---

## 联系与支持

如有问题，请参考：
- 后端 API 文档：`API-GUIDE.md`
- 前端实现方案：`C:\Users\YuanS\.claude\plans\jazzy-bouncing-swan.md`
- Spring Boot 后端代码：`src/main/java/site/geekie/shop/shoppingmall/`

祝开发顺利！
