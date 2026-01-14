# Spring Mall 前端项目 - 阶段一：基础搭建

## 📋 完成日期
2026-01-10

## 🎯 阶段目标
搭建 Spring Mall 前端项目的基础架构，配置开发环境，创建项目目录结构，集成核心依赖库。

---

## ✅ 完成清单

- [x] 初始化 Vite + Vue 3 项目
- [x] 安装并配置所有依赖
- [x] 创建项目目录结构
- [x] 配置 Vite（路径别名、代理）
- [x] 配置环境变量文件
- [x] 初始化 main.js（注册 Element Plus、Pinia、Router）
- [x] 创建全局样式文件（reset.scss, common.scss, variables.scss）
- [x] 验证项目可正常启动

---

## 📦 技术栈

### 核心框架
- **Vue 3.5.24** - 渐进式 JavaScript 框架（Composition API）
- **Vite 7.2.4** - 下一代前端构建工具

### UI 组件库
- **Element Plus 2.13.1** - Vue 3 组件库
- **@element-plus/icons-vue 2.3.2** - Element Plus 图标库

### 状态管理 & 路由
- **Vue Router 4.6.4** - Vue.js 官方路由
- **Pinia 3.0.4** - Vue 状态管理库

### HTTP 客户端 & 工具库
- **Axios 1.13.2** - HTTP 客户端
- **dayjs 1.11.19** - 日期处理库
- **vue3-lazyload 0.3.8** - 图片懒加载

### 开发工具
- **Sass 1.97.2** - CSS 预处理器
- **@vitejs/plugin-vue 6.0.1** - Vite Vue 插件

---

## 📁 项目目录结构

```
mall-frontend/
├── public/                     # 静态资源目录
├── src/
│   ├── api/                    # API 接口封装（按功能模块划分）
│   ├── assets/                 # 资源文件
│   │   ├── images/             # 图片资源
│   │   └── styles/             # 全局样式
│   │       ├── reset.scss      # CSS 重置样式
│   │       ├── variables.scss  # SCSS 变量和 Mixins
│   │       └── common.scss     # 通用样式类
│   ├── components/             # 公共组件
│   │   ├── common/             # 通用组件（Header, Footer, Loading, Empty）
│   │   ├── user/               # 用户端组件
│   │   └── admin/              # 管理端组件
│   ├── layouts/                # 布局组件
│   │   ├── UserLayout.vue      # 用户端布局（待创建）
│   │   └── AdminLayout.vue     # 管理端布局（待创建）
│   ├── views/                  # 页面组件
│   │   ├── user/               # 用户端页面（9个页面）
│   │   │   └── Home.vue        # 首页（已创建）
│   │   ├── admin/              # 管理端页面（5个页面）
│   │   └── auth/               # 认证页面（Login, Register）
│   ├── router/                 # 路由配置
│   │   └── index.js            # 路由入口文件
│   ├── store/                  # Pinia 状态管理
│   │   └── index.js            # Store 入口文件
│   ├── utils/                  # 工具函数
│   ├── App.vue                 # 根组件
│   └── main.js                 # 应用入口
├── .env.development            # 开发环境变量
├── .env.production             # 生产环境变量
├── vite.config.js              # Vite 配置文件
├── package.json                # 项目依赖配置
└── PHASE1-SETUP.md             # 本文档
```

---

## ⚙️ 配置详情

### 1. Vite 配置（vite.config.js）

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')  // 路径别名
    }
  },
  server: {
    port: 3000,                             // 开发服务器端口
    proxy: {
      '/api': {
        target: 'http://localhost:8080',    // 后端 API 代理
        changeOrigin: true
      }
    }
  }
})
```

**功能说明**：
- ✅ 配置 `@` 别名指向 `src` 目录，简化导入路径
- ✅ 开发服务器运行在端口 3000
- ✅ API 请求代理到后端服务器（localhost:8080）

### 2. 环境变量配置

#### .env.development（开发环境）
```
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

#### .env.production（生产环境）
```
VITE_API_BASE_URL=https://api.yourdomain.com/api/v1
```

**使用方式**：
```javascript
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL
```

### 3. 应用入口配置（src/main.js）

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import '@/assets/styles/reset.scss'
import '@/assets/styles/common.scss'

const app = createApp(App)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(pinia)
app.use(ElementPlus)
app.mount('#app')
```

**功能说明**：
- ✅ 注册 Vue Router 路由管理
- ✅ 注册 Pinia 状态管理
- ✅ 注册 Element Plus 组件库
- ✅ 全局注册所有 Element Plus 图标
- ✅ 导入全局样式文件

### 4. 路由配置（src/router/index.js）

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/user/Home.vue'),
    meta: { title: '首页' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

**当前状态**：
- ✅ 基础路由框架已搭建
- ⏳ 待添加所有页面路由（阶段二）
- ⏳ 待添加路由守卫（阶段二）

### 5. 状态管理配置（src/store/index.js）

```javascript
import { createPinia } from 'pinia'

const pinia = createPinia()

export default pinia
```

**当前状态**：
- ✅ Pinia 实例已创建
- ⏳ 待创建 auth.js、cart.js、user.js、app.js（阶段二）

---

## 🎨 全局样式系统

### 1. CSS 重置样式（reset.scss）

提供基础的样式重置，确保跨浏览器一致性：
- 清除默认 margin 和 padding
- 设置 box-sizing 为 border-box
- 统一字体和行高
- 清除列表样式、链接下划线
- 规范化图片、按钮、输入框样式

### 2. SCSS 变量和 Mixins（variables.scss）

#### 颜色变量
```scss
$primary-color: #409EFF;        // 主题色
$success-color: #67C23A;        // 成功色
$warning-color: #E6A23C;        // 警告色
$danger-color: #F56C6C;         // 危险色
$info-color: #909399;           // 信息色

$text-primary: #303133;         // 主要文本
$text-regular: #606266;         // 常规文本
$text-secondary: #909399;       // 次要文本
$text-placeholder: #C0C4CC;     // 占位文本

$border-base: #DCDFE6;          // 边框色
$bg-color: #FFFFFF;             // 背景色
$bg-page: #F5F7FA;              // 页面背景色
```

#### 间距变量
```scss
$spacing-xs: 4px;
$spacing-sm: 8px;
$spacing-md: 16px;
$spacing-lg: 24px;
$spacing-xl: 32px;
```

#### 响应式断点
```scss
$breakpoint-mobile: 768px;      // 移动端
$breakpoint-tablet: 1024px;     // 平板
$breakpoint-desktop: 1280px;    // 桌面
```

#### 常用 Mixins
```scss
@mixin mobile { ... }           // 移动端样式
@mixin tablet { ... }           // 平板样式
@mixin desktop { ... }          // 桌面样式
@mixin flex-center { ... }      // Flex 居中
@mixin flex-between { ... }     // Flex 两端对齐
@mixin text-ellipsis { ... }    // 单行文本省略
@mixin multi-line-ellipsis($lines) { ... }  // 多行文本省略
```

### 3. 通用样式类（common.scss）

提供常用的工具类：
- **容器类**：`.container`, `.page-container`
- **文本类**：`.text-primary`, `.text-regular`, `.text-secondary`
- **Flex 类**：`.flex`, `.flex-center`, `.flex-between`, `.flex-wrap`
- **间距类**：`.mt-md`, `.mb-lg`, `.pt-sm`, `.pr-xl` 等
- **文本省略**：`.text-ellipsis`, `.text-ellipsis-2`, `.text-ellipsis-3`
- **卡片样式**：`.card`

---

## 🚀 快速开始

### 1. 安装依赖（已完成）
```bash
cd mall-frontend
npm install
```

### 2. 启动开发服务器
```bash
npm run dev
```

服务器将在 http://localhost:3000 启动

### 3. 构建生产版本
```bash
npm run build
```

### 4. 预览生产构建
```bash
npm run preview
```

---

## 📊 项目验证

### 启动测试结果
```
VITE v7.3.1  ready in 4572 ms

➜  Local:   http://localhost:3000/
```

**验证状态**：
- ✅ Vite 开发服务器成功启动
- ✅ 所有依赖正确加载
- ✅ 编译时间：4.5 秒
- ✅ 监听端口：3000
- ✅ 首页正常访问

---

## 📝 开发规范

### 命名规范
- **组件名**：PascalCase（如 `UserProfile.vue`）
- **方法名**：camelCase（如 `getUserInfo`）
- **常量名**：UPPER_SNAKE_CASE（如 `API_BASE_URL`）
- **CSS 类名**：kebab-case（如 `user-profile`）

### 代码风格
1. 使用 **Composition API**
2. 使用 **`<script setup>`** 语法
3. Props 必须定义类型
4. 合理使用 computed 和 reactive
5. 组件保持单一职责

### 导入路径
使用 `@` 别名简化导入：
```javascript
// ✅ 推荐
import Header from '@/components/common/Header.vue'
import { getToken } from '@/utils/storage'

// ❌ 不推荐
import Header from '../../../components/common/Header.vue'
```

---

## 📈 下一步计划：阶段二

### 核心工具和配置（优先级：高）

1. **Token 管理**（`utils/storage.js`）
   - getToken, setToken, removeToken
   - getUser, setUser, removeUser
   - clearStorage

2. **Axios 请求封装**（`api/request.js`）
   - 创建 Axios 实例
   - 请求拦截器（自动添加 Token）
   - 响应拦截器（统一错误处理）

3. **API 接口封装**
   - `api/auth.js` - 认证接口
   - `api/user.js` - 用户接口
   - `api/product.js` - 商品接口
   - `api/category.js` - 分类接口
   - `api/cart.js` - 购物车接口
   - `api/order.js` - 订单接口
   - `api/address.js` - 地址接口
   - `api/payment.js` - 支付接口
   - `api/admin/` - 管理端接口

4. **Pinia Store 实现**
   - `store/auth.js` - 认证状态
   - `store/cart.js` - 购物车状态
   - `store/user.js` - 用户状态
   - `store/app.js` - 应用状态

5. **路由完善**
   - 添加所有页面路由
   - 实现路由守卫（认证检查、权限检查）
   - 配置页面标题

6. **布局组件**
   - `layouts/UserLayout.vue` - 用户端布局
   - `layouts/AdminLayout.vue` - 管理端布局

---

## 🔍 关键文件说明

| 文件路径 | 作用 | 状态 |
|---------|------|------|
| `vite.config.js` | Vite 配置（别名、代理、服务器） | ✅ 完成 |
| `src/main.js` | 应用入口，注册插件和全局组件 | ✅ 完成 |
| `src/App.vue` | 根组件 | ✅ 完成 |
| `src/router/index.js` | 路由配置 | ⚠️ 基础框架 |
| `src/store/index.js` | 状态管理入口 | ⚠️ 基础框架 |
| `src/assets/styles/reset.scss` | CSS 重置样式 | ✅ 完成 |
| `src/assets/styles/variables.scss` | SCSS 变量和 Mixins | ✅ 完成 |
| `src/assets/styles/common.scss` | 通用样式类 | ✅ 完成 |
| `.env.development` | 开发环境变量 | ✅ 完成 |
| `.env.production` | 生产环境变量 | ✅ 完成 |

---

## 💡 使用建议

### 1. 使用 SCSS 变量和 Mixins

```vue
<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.product-card {
  padding: $spacing-md;
  border-radius: $border-radius-base;
  background: $bg-color;

  @include mobile {
    padding: $spacing-sm;
  }

  .title {
    @include text-ellipsis;
    color: $text-primary;
  }
}
</style>
```

### 2. 使用通用样式类

```vue
<template>
  <div class="container">
    <div class="card mt-lg">
      <h2 class="text-primary mb-md">标题</h2>
      <p class="text-regular text-ellipsis-2">内容...</p>
    </div>
  </div>
</template>
```

### 3. 使用 Element Plus 图标

```vue
<template>
  <el-button>
    <el-icon><Search /></el-icon>
    搜索
  </el-button>
</template>

<script setup>
// 图标已全局注册，无需导入
</script>
```

---

## 🐛 已知问题

无

---

## 📞 联系信息

- **项目位置**：`C:\Users\YuanS\Documents\project\springMall\mall-frontend`
- **后端 API**：http://localhost:8080/api/v1
- **前端开发服务器**：http://localhost:3000

---

## 📜 更新日志

### v1.0.0 - 2026-01-10
- ✅ 初始化项目结构
- ✅ 配置开发环境
- ✅ 集成核心依赖
- ✅ 创建全局样式系统
- ✅ 完成阶段一所有任务

---

**阶段一完成进度：100%**

**下一阶段**：阶段二 - 核心工具和配置
