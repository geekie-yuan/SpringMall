# Spring Mall 前端项目

一个基于 Vue 3 + Element Plus 的现代化电商平台前端应用，包含用户购物端和管理后台。

## 📋 项目简介

Spring Mall 是一个全栈电商项目的前端部分，采用最新的前端技术栈构建，提供完整的购物流程和管理功能。

### 主要特性

- 🛒 完整的购物流程（浏览商品、加入购物车、下单、订单管理）
- 👤 用户中心（个人信息、地址管理、订单查询）
- 🔐 JWT Token 认证
- 🎨 响应式设计（移动端/平板/桌面端适配）
- 📦 管理后台（商品/分类/订单/用户管理）
- ⚡ 性能优化（路由懒加载、图片懒加载、代码分割）
- 🎯 防抖节流优化（搜索功能）

## 🚀 技术栈

- **框架**: Vue 3.4+ (Composition API + `<script setup>`)
- **构建工具**: Vite 7.3+
- **UI 组件库**: Element Plus 2.13+
- **状态管理**: Pinia 3.0+
- **路由管理**: Vue Router 4.6+
- **HTTP 客户端**: Axios 1.13+
- **CSS 预处理**: Sass/SCSS
- **工具库**: dayjs, vue3-lazyload

## 📁 项目结构

```
mall-frontend/
├── src/
│   ├── api/              # API 接口封装
│   │   ├── request.js    # Axios 封装
│   │   ├── auth.js       # 认证 API
│   │   ├── user.js       # 用户 API
│   │   ├── product.js    # 商品 API
│   │   ├── cart.js       # 购物车 API
│   │   ├── order.js      # 订单 API
│   │   └── admin/        # 管理端 API
│   ├── assets/           # 静态资源
│   │   ├── images/       # 图片
│   │   └── styles/       # 全局样式
│   ├── components/       # 公共组件
│   │   ├── common/       # 通用组件
│   │   ├── user/         # 用户端组件
│   │   └── admin/        # 管理端组件
│   ├── layouts/          # 布局组件
│   │   ├── UserLayout.vue
│   │   └── AdminLayout.vue
│   ├── views/            # 页面组件
│   │   ├── user/         # 用户端页面（10个）
│   │   ├── admin/        # 管理端页面（5个）
│   │   └── auth/         # 认证页面（2个）
│   ├── router/           # 路由配置
│   ├── store/            # Pinia 状态管理
│   │   ├── auth.js       # 认证状态
│   │   ├── cart.js       # 购物车状态
│   │   ├── user.js       # 用户状态
│   │   └── app.js        # 应用状态
│   ├── utils/            # 工具函数
│   │   ├── storage.js    # LocalStorage 封装
│   │   ├── validate.js   # 表单验证
│   │   ├── format.js     # 格式化函数
│   │   ├── helpers.js    # 辅助函数（防抖/节流）
│   │   └── constants.js  # 常量定义
│   ├── App.vue
│   └── main.js
├── public/               # 静态资源
├── .env.development      # 开发环境变量
├── .env.production       # 生产环境变量
├── vite.config.js        # Vite 配置
└── package.json          # 项目依赖
```

## 🛠️ 快速开始

### 环境要求

- Node.js >= 16.0.0
- npm >= 7.0.0

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

项目将运行在 `http://localhost:3000`

### 生产构建

```bash
npm run build
```

构建产物将生成在 `dist` 目录

### 预览生产构建

```bash
npm run preview
```

## 📦 页面列表

### 用户端页面（10个）

| 页面 | 路由 | 描述 |
|------|------|------|
| 首页 | `/` | 轮播、分类导航、商品展示 |
| 商品列表 | `/products` | 搜索、筛选、分页 |
| 商品详情 | `/products/:id` | 商品信息、加入购物车 |
| 购物车 | `/cart` | 商品管理、价格计算 |
| 结算页面 | `/checkout` | 地址选择、订单确认 |
| 订单列表 | `/orders` | 订单查询、状态筛选 |
| 订单详情 | `/orders/:orderNo` | 订单详细信息 |
| 个人中心 | `/profile` | 个人信息、修改密码 |
| 地址管理 | `/address` | 地址 CRUD |
| 登录 | `/login` | 用户登录 |
| 注册 | `/register` | 用户注册 |

### 管理端页面（5个）

| 页面 | 路由 | 描述 | 权限 |
|------|------|------|------|
| 管理后台 | `/admin` | 数据统计、快捷入口 | 管理员 |
| 商品管理 | `/admin/products` | 商品 CRUD、上下架 | 管理员 |
| 分类管理 | `/admin/categories` | 分类 CRUD | 管理员 |
| 订单管理 | `/admin/orders` | 订单查询、发货 | 管理员 |
| 用户管理 | `/admin/users` | 用户管理、角色分配 | 管理员 |

## 🔒 权限控制

- **公开页面**: 首页、商品列表、商品详情、登录、注册
- **需要登录**: 购物车、订单相关、个人中心、地址管理
- **需要管理员**: 所有 `/admin` 路由

## 🎨 响应式断点

```scss
$breakpoint-mobile: 768px;
$breakpoint-tablet: 1024px;
$breakpoint-desktop: 1280px;
```

## 🔧 环境变量

### .env.development
```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

### .env.production
```env
VITE_API_BASE_URL=https://api.yourdomain.com/api/v1
```

## 📚 核心功能

### 1. 认证系统
- JWT Token 认证
- LocalStorage 存储 Token 和用户信息
- 路由守卫自动验证登录状态和权限

### 2. 购物车
- 实时同步到服务器
- 数量修改、删除商品
- 全选/取消全选
- 价格自动计算

### 3. 订单流程
1. 浏览商品 → 加入购物车
2. 购物车 → 结算
3. 选择地址 → 提交订单
4. 查看订单 → 确认收货

### 4. 管理后台
- 数据统计仪表板
- 商品管理（CRUD、上下架、库存修改）
- 分类管理
- 订单管理（发货）
- 用户管理（状态控制、角色分配）

## ⚡ 性能优化

### 已实现的优化

1. **路由懒加载**: 所有页面组件使用动态导入
2. **图片懒加载**: 使用 vue3-lazyload 插件
3. **代码分割**:
   - Vue 生态单独打包（vue, vue-router, pinia）
   - Element Plus 单独打包
   - 工具库单独打包（axios, dayjs）
4. **防抖优化**: 搜索功能使用 500ms 防抖
5. **Axios 拦截器**: 统一请求/响应处理
6. **全局错误处理**: Vue errorHandler + unhandledrejection

## 🔗 后端 API

后端 API 文档：`http://localhost:8080/api-docs`

### API 基础路径
```
http://localhost:8080/api/v1
```

### 主要接口
- `/auth/*` - 认证接口
- `/products/*` - 商品接口
- `/cart/*` - 购物车接口
- `/orders/*` - 订单接口
- `/addresses/*` - 地址接口
- `/admin/*` - 管理端接口

## 📝 开发规范

### 命名规范
- 组件名: PascalCase (`UserProfile.vue`)
- 方法名: camelCase (`getUserInfo`)
- 常量名: UPPER_SNAKE_CASE (`API_BASE_URL`)
- CSS 类名: kebab-case (`user-profile`)

### 代码风格
- 使用 Composition API
- 使用 `<script setup>` 语法
- Props 必须定义类型
- 所有 API 调用使用 try-catch
- 合理使用 computed 和 reactive

## 🐛 故障排除

### 构建失败

**问题**: Sass @import 弃用警告

**解决**: 这是 Sass 的弃用警告，不影响功能。未来可以迁移到 `@use`。

**问题**: Chunk 大小警告

**解决**: 已通过代码分割优化，将大型依赖单独打包。

### 开发模式

**问题**: 端口被占用

**解决**: Vite 会自动尝试其他端口（3001, 3002, 3003...）

## 📄 文档

- `PHASE1-SETUP.md` - 基础搭建文档
- `PHASE2-CORE.md` - 核心配置文档
- `PHASE3-AUTH.md` - 认证功能文档
- `PHASE4-USER.md` - 用户端功能文档
- `PHASE5-ADMIN.md` - 管理端功能文档
- `PHASE6-OPTIMIZATION.md` - 优化完善文档

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

## 📜 开源协议

MIT License

## 👨‍💻 开发者

Claude Sonnet 4.5 - 2026-01-10

---

**祝使用愉快！** 🎉
