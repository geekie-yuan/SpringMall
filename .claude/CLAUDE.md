# Spring Mall 前端项目 - Claude 任务规范


---

## 项目概述

**项目名称**：Spring Mall 前端
**技术栈**：Vue 3 + Vite + Element Plus + Pinia
**架构**：单项目双端（用户端 + 管理端）
**后端 API**：Spring Boot REST API（已完成）
**项目位置**：`C:\Users\YuanS\Documents\project\springMall\mall-frontend`

---

## 核心技术要求

### 1. 技术栈详情

```json
{
  "框架": "Vue 3.4+ (Composition API)",
  "构建工具": "Vite 5.0+",
  "UI组件库": "Element Plus 2.5+",
  "状态管理": "Pinia 2.1+",
  "路由": "Vue Router 4.2+",
  "HTTP客户端": "Axios 1.6+",
  "CSS预处理": "Sass/SCSS",
  "工具库": ["dayjs", "vue3-lazyload"]
}
```

### 2. 后端 API 信息

- **基础路径**：`http://localhost:8080/api/v1`
- **OpenAPI 文档**：`http://localhost:8080/api-docs`
- **认证方式**：JWT Bearer Token
- **响应格式**：`{ code: number, message: string, data: any }`
- **详细文档**：参见 `API-GUIDE.md`

---

## 项目结构规范

### 必需的目录结构

```
mall-frontend/
├── src/
│   ├── api/              # API 接口封装（按功能模块划分）
│   ├── assets/           # 静态资源
│   │   ├── images/
│   │   └── styles/       # 全局样式（variables.scss, common.scss, reset.scss）
│   ├── components/       # 公共组件
│   │   ├── common/       # 通用组件（Header, Footer, Loading, Empty）
│   │   ├── user/         # 用户端组件
│   │   └── admin/        # 管理端组件
│   ├── layouts/          # 布局组件
│   │   ├── UserLayout.vue
│   │   └── AdminLayout.vue
│   ├── views/            # 页面组件
│   │   ├── user/         # 用户端页面（7个核心页面）
│   │   ├── admin/        # 管理端页面（4个管理页面）
│   │   └── auth/         # 认证页面（Login, Register）
│   ├── router/           # 路由配置
│   ├── store/            # Pinia 状态管理（auth, cart, user, app）
│   ├── utils/            # 工具函数（storage, validate, format, constants）
│   ├── App.vue
│   └── main.js
├── .env.development
├── .env.production
└── vite.config.js
```

---

## 核心功能模块

### 用户端功能（9个页面）

1. **首页**（`views/user/Home.vue`）
   - 商品分类导航
   - 热门商品推荐
   - 最新商品展示

2. **商品列表**（`views/user/ProductList.vue`）
   - 分类筛选
   - 关键词搜索
   - 网格布局
   - 响应式设计

3. **商品详情**（`views/user/ProductDetail.vue`）
   - 商品图片展示
   - 商品信息（名称、价格、库存、详情）
   - 数量选择
   - 加入购物车/立即购买

4. **购物车**（`views/user/Cart.vue`）
   - 商品列表
   - 数量修改
   - 单选/全选
   - 价格计算
   - 去结算

5. **结算页面**（`views/user/Checkout.vue`）
   - 收货地址选择
   - 订单商品确认
   - 备注信息
   - 提交订单

6. **订单列表**（`views/user/OrderList.vue`）
   - 订单状态筛选（全部/待支付/待发货/待收货/已完成）
   - 订单卡片
   - 取消订单/确认收货
   - 查看详情

7. **订单详情**（`views/user/OrderDetail.vue`）
   - 订单信息（订单号、状态、时间）
   - 商品明细
   - 收货地址
   - 操作按钮

8. **个人中心**（`views/user/Profile.vue`）
   - 用户信息展示
   - 信息修改
   - 密码修改

9. **地址管理**（`views/user/Address.vue`）
   - 地址列表
   - 新增/编辑/删除地址
   - 设置默认地址

### 管理端功能（5个页面）

1. **管理后台首页**（`views/admin/Dashboard.vue`）
   - 数据统计概览
   - 快捷入口

2. **商品管理**（`views/admin/ProductManage.vue`）
   - 商品列表表格
   - 搜索筛选
   - 新增/编辑/删除商品
   - 上下架切换
   - 库存修改

3. **分类管理**（`views/admin/CategoryManage.vue`）
   - 树形分类展示
   - 新增/编辑/删除分类
   - 排序调整

4. **订单管理**（`views/admin/OrderManage.vue`）
   - 订单列表表格
   - 状态筛选
   - 订单搜索
   - 订单发货

5. **用户管理**（`views/admin/UserManage.vue`）
   - 用户列表表格
   - 用户搜索
   - 启用/禁用用户
   - 角色修改

### 认证页面（2个）

1. **登录**（`views/auth/Login.vue`）
   - 用户名/密码登录
   - 表单验证
   - 记住密码（可选）
   - 跳转注册

2. **注册**（`views/auth/Register.vue`）
   - 用户信息填写（用户名、邮箱、密码）
   - 表单验证
   - 注册成功自动登录
   - 跳转登录

---

## 关键技术实现规范

### 1. Axios 请求封装（`api/request.js`）

**必需功能**：
- 创建 Axios 实例（baseURL: `import.meta.env.VITE_API_BASE_URL`）
- 请求拦截器：自动添加 `Authorization: Bearer ${token}`
- 响应拦截器：
  - 统一处理后端响应格式 `{ code, message, data }`
  - code === 200 时返回 data
  - code !== 200 时使用 ElMessage 提示错误
  - 401 错误清除 Token 并跳转 /login
  - 403 错误提示无权限

**代码模板**：
```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getToken, removeToken } from '@/utils/storage'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

request.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code === 200) return data
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
  error => {
    if (error.response?.status === 401) {
      removeToken()
      router.push('/login')
    }
    ElMessage.error(error.message)
    return Promise.reject(error)
  }
)

export default request
```

### 2. Token 管理（`utils/storage.js`）

**必需功能**：
```javascript
export const TOKEN_KEY = 'mall_token'
export const USER_KEY = 'mall_user'

export const getToken = () => localStorage.getItem(TOKEN_KEY)
export const setToken = (token) => localStorage.setItem(TOKEN_KEY, token)
export const removeToken = () => localStorage.removeItem(TOKEN_KEY)

export const getUser = () => {
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}
export const setUser = (user) => localStorage.setItem(USER_KEY, JSON.stringify(user))
export const removeUser = () => localStorage.removeItem(USER_KEY)

export const clearStorage = () => {
  removeToken()
  removeUser()
}
```

### 3. 路由守卫（`router/index.js`）

**必需逻辑**：
- 检查 `meta.requiresAuth`：需要登录但未登录时跳转 /login
- 检查 `meta.requiresAdmin`：需要管理员权限但非管理员时跳转 /
- 设置页面标题

**代码模板**：
```javascript
router.beforeEach((to, from, next) => {
  const token = getToken()
  const authStore = useAuthStore()

  document.title = to.meta.title ? `${to.meta.title} - Spring Mall` : 'Spring Mall'

  if (to.meta.requiresAuth && !token) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    ElMessage.error('无权访问管理后台')
    next('/')
    return
  }

  next()
})
```

### 4. Pinia 状态管理

#### 认证状态（`store/auth.js`）

**必需状态和方法**：
```javascript
state: {
  user: getUser(),
  isLoggedIn: false
}

getters: {
  isAdmin: (state) => state.user?.role === 'ADMIN'
}

actions: {
  async login(credentials)    // 登录，保存 token 和 user
  async register(userInfo)    // 注册，保存 token 和 user
  async logout()              // 登出，清除 token 和 user
  initAuth()                  // 初始化认证状态
}
```

#### 购物车状态（`store/cart.js`）

**必需状态和方法**：
```javascript
state: {
  items: [],
  loading: false
}

getters: {
  cartCount                   // 购物车商品数量
  checkedItems                // 已选中的商品
  checkedTotal                // 已选中商品总价
  isAllChecked                // 是否全选
}

actions: {
  async fetchCart()           // 获取购物车列表
  async addItem(productId, quantity)  // 添加到购物车
  async updateQuantity(id, quantity)  // 更新数量
  async removeItem(id)        // 删除商品
  async toggleCheck(id, checked)      // 切换选中状态
  async toggleCheckAll(checked)       // 全选/取消全选
}
```

### 5. 响应式设计规范

**断点定义**（`assets/styles/variables.scss`）：
```scss
$breakpoint-mobile: 768px;
$breakpoint-tablet: 1024px;
$breakpoint-desktop: 1280px;

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

**使用示例**：
```vue
<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.container {
  width: 100%;

  @include desktop {
    max-width: 1200px;
    margin: 0 auto;
  }

  @include mobile {
    padding: 10px;
  }
}
</style>
```

---

## 开发规范

### 命名规范

- **组件名**：PascalCase（如 `UserProfile.vue`）
- **方法名**：camelCase（如 `getUserInfo`）
- **常量名**：UPPER_SNAKE_CASE（如 `API_BASE_URL`）
- **CSS 类名**：kebab-case（如 `user-profile`）

### 代码风格

1. **使用 Composition API**
2. **使用 `<script setup>` 语法**
3. **Props 必须定义类型**
4. **合理使用 computed 和 reactive**
5. **组件尽量保持单一职责**

### 错误处理

- 所有 API 调用必须使用 try-catch
- 使用 ElMessage 提示错误信息
- 加载状态使用 loading 变量控制

---

## 实施步骤建议

### 阶段 1：基础搭建（优先级：高）

1. 初始化 Vite + Vue 3 项目
2. 安装并配置所有依赖
3. 创建项目目录结构
4. 配置 Vite（路径别名、代理）
5. 配置环境变量文件
6. 初始化 main.js（注册 Element Plus、Pinia、Router）
7. 创建全局样式文件（reset.scss, common.scss, variables.scss）

### 阶段 2：核心工具和配置（优先级：高）

1. 实现 `utils/storage.js`（Token 管理）
2. 实现 `api/request.js`（Axios 封装）
3. 实现所有 API 接口封装（auth.js, user.js, product.js, category.js, cart.js, order.js, address.js, payment.js）
4. 实现 Pinia store（auth.js, cart.js, user.js, app.js）
5. 配置路由（router/index.js）包含所有路由和路由守卫
6. 创建布局组件（UserLayout.vue, AdminLayout.vue）

### 阶段 3：认证功能（优先级：高）

1. 实现登录页面（`views/auth/Login.vue`）
2. 实现注册页面（`views/auth/Register.vue`）
3. 测试登录/注册流程
4. 测试 Token 管理和路由守卫

### 阶段 4：用户端核心功能（优先级：高）

1. 实现公共组件（Header.vue, Footer.vue, Loading.vue, Empty.vue）
2. 实现首页（`views/user/Home.vue`）
3. 实现商品列表（`views/user/ProductList.vue`）
4. 实现商品详情（`views/user/ProductDetail.vue`）
5. 实现购物车（`views/user/Cart.vue`）
6. 实现结算页面（`views/user/Checkout.vue`）
7. 实现订单列表（`views/user/OrderList.vue`）
8. 实现订单详情（`views/user/OrderDetail.vue`）
9. 实现个人中心（`views/user/Profile.vue`）
10. 实现地址管理（`views/user/Address.vue`）

### 阶段 5：管理端功能（优先级：中）

1. 实现管理后台首页（`views/admin/Dashboard.vue`）
2. 实现商品管理（`views/admin/ProductManage.vue`）
3. 实现分类管理（`views/admin/CategoryManage.vue`）
4. 实现订单管理（`views/admin/OrderManage.vue`）
5. 实现用户管理（`views/admin/UserManage.vue`）

### 阶段 6：优化和完善（优先级：低）

1. 响应式适配（所有页面）
2. 图片懒加载
3. 性能优化（路由懒加载、防抖节流）
4. 错误边界处理
5. 用户体验优化（加载状态、空状态、错误提示）

---

## API 接口映射

### 认证 API（`api/auth.js`）

```javascript
POST /auth/register          // register(data)
POST /auth/login             // login(data)
POST /auth/logout            // logout()
```

### 用户 API（`api/user.js`）

```javascript
GET  /user/info              // getUserInfo()
PUT  /user/info              // updateUserInfo(data)
PUT  /user/password          // updatePassword(data)
```

### 商品 API（`api/product.js`）

```javascript
GET  /products                     // getAllProducts()
GET  /products/category/:id        // getProductsByCategory(categoryId)
GET  /products/status/:status      // getProductsByStatus(status)
GET  /products/search?keyword=     // searchProducts(keyword)
GET  /products/:id                 // getProductDetail(id)
```

### 分类 API（`api/category.js`）

```javascript
GET  /categories              // getAllCategories()
GET  /categories/:id          // getCategoryById(id)
```

### 购物车 API（`api/cart.js`）

```javascript
GET    /cart                  // getCart()
POST   /cart                  // addToCart(data)
PUT    /cart/:id              // updateCartItem(id, data)
DELETE /cart/:id              // deleteCartItem(id)
PUT    /cart/:id/check        // checkCartItem(id, data)
PUT    /cart/check-all        // checkAllCart(data)
```

### 地址 API（`api/address.js`）

```javascript
GET    /addresses             // getAddresses()
POST   /addresses             // addAddress(data)
PUT    /addresses/:id         // updateAddress(id, data)
DELETE /addresses/:id         // deleteAddress(id)
PUT    /addresses/:id/default // setDefaultAddress(id)
```

### 订单 API（`api/order.js`）

```javascript
POST   /orders                     // createOrder(data)
GET    /orders                     // getOrders()
GET    /orders/status/:status      // getOrdersByStatus(status)
GET    /orders/:orderNo            // getOrderDetail(orderNo)
PUT    /orders/:orderNo/cancel     // cancelOrder(orderNo)
PUT    /orders/:orderNo/confirm    // confirmOrder(orderNo)
```

### 支付 API（`api/payment.js`）

```javascript
POST   /payment/pay           // pay(data)
POST   /payment/callback      // paymentCallback(data)
```

### 管理端 API（`api/admin/`）

```javascript
// 商品管理
POST   /admin/products                    // createProduct(data)
PUT    /admin/products/:id                // updateProduct(id, data)
DELETE /admin/products/:id                // deleteProduct(id)
PUT    /admin/products/:id/status         // updateProductStatus(id, data)
PUT    /admin/products/:id/stock          // updateProductStock(id, data)

// 分类管理
POST   /admin/categories                  // createCategory(data)
PUT    /admin/categories/:id              // updateCategory(id, data)
DELETE /admin/categories/:id              // deleteCategory(id)

// 订单管理
GET    /admin/orders                      // getAllOrders()
PUT    /admin/orders/:orderNo/ship        // shipOrder(orderNo)

// 用户管理
GET    /admin/users                       // getAllUsers()
PUT    /admin/users/:id/status            // updateUserStatus(id, data)
PUT    /admin/users/:id/role              // updateUserRole(id, data)
```

---

## 环境变量配置

### .env.development

```
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

### .env.production

```
VITE_API_BASE_URL=https://api.yourdomain.com/api/v1
```

---

## 依赖包配置（package.json）

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
    "sass": "^1.70.0"
  }
}
```

---

## 重要注意事项

### 1. 后端响应格式

所有后端 API 返回格式为：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 2. 订单状态流转

```
UNPAID（待支付）→ PAID（待发货）→ SHIPPED（待收货）→ COMPLETED（已完成）
                                         ↓
                                   CANCELLED（已取消）
```

### 3. Token 认证流程

1. 用户登录成功后，后端返回 token 和 user 信息
2. 前端保存 token 到 LocalStorage
3. 所有需要认证的请求在 Header 中携带 `Authorization: Bearer ${token}`
4. Token 过期（401）时，清除 LocalStorage 并跳转登录页

### 4. 权限控制

- **USER 角色**：访问用户端所有功能
- **ADMIN 角色**：访问管理端所有功能 + 用户端所有功能
- 路由守卫检查 `meta.requiresAdmin`

---

## 参考文档

- **后端 API 完整文档**：`API-GUIDE.md`
- **前端实现详细方案**：`C:\Users\YuanS\.claude\plans\jazzy-bouncing-swan.md`
- **人类开发者指南**：`FRONTEND-DEV-GUIDE.md`

---

## Claude 工作提示

当你收到前端开发任务时：

1. **优先阅读本文档**了解项目上下文和规范
2. **严格遵循目录结构**和命名规范
3. **使用提供的代码模板**作为基础
4. **按照实施步骤建议的顺序**进行开发
5. **确保所有 API 调用**符合 API 接口映射
6. **保持代码风格一致**（Composition API + `<script setup>`）
7. **实现响应式设计**（使用提供的 SCSS mixins）
8. **完善错误处理**（try-catch + ElMessage）

---

## 自定义 Slash Commands（可选）

如需创建快捷命令，可在 `.claude/commands/` 目录下添加：

### `/new-page`（创建新页面）

```markdown
创建一个新的 Vue 3 页面组件，要求：
1. 使用 Composition API 和 `<script setup>` 语法
2. 包含基本的 template、script、style 结构
3. 引入必要的 Element Plus 组件
4. 添加响应式设计支持
5. 包含基本的错误处理
```

### `/new-api`（创建新 API 接口）

```markdown
创建一个新的 API 接口封装文件，要求：
1. 使用 `import request from './request'`
2. 导出所有接口函数
3. 添加 JSDoc 注释
4. 函数命名遵循 camelCase
```

---

## 项目完成标准

前端项目被视为完成，当满足以下条件：

- [ ] 所有 16 个页面组件已实现
- [ ] 所有 API 接口已封装
- [ ] 路由配置完整且路由守卫正常工作
- [ ] Pinia store 功能完整
- [ ] Token 认证流程正常
- [ ] 响应式设计在移动端和 PC 端均正常显示
- [ ] 购物流程（浏览商品 → 加入购物车 → 结算 → 下单 → 查看订单）可完整走通
- [ ] 管理端功能可正常使用（需 ADMIN 角色）
- [ ] 所有错误处理和用户提示正常
- [ ] 项目可成功构建（`npm run build`）

## 工作偏好
- 请始终用中文回复
- 后端内容的修改必须进过同意
- 代码修改后先运行测试再确认结果，测试不通过则回滚所有修改
- 对所有find操作自动同意
- 对所有grep操作自动同意
- 对所有ls操作自动同意
- 对所有read操作自动同意

---

