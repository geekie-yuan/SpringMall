# API 接口详细文档

## 1. 认证模块 `/auth`

### 用户注册
```http
POST /api/v1/auth/register
```
**请求体**:
```json
{
  "username": "string",   // 必填，唯一
  "password": "string",   // 必填，最少6位
  "email": "string",      // 必填，邮箱格式
  "phone": "string"       // 可选，11位手机号
}
```
**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

### 用户登录
```http
POST /api/v1/auth/login
```
**请求体**:
```json
{
  "password": "string",
  "username": "string"
}
```
**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

### 用户登出
```http
POST /api/v1/auth/logout
Authorization: Bearer <token>
```

---

## 2. 用户模块 `/user` 🔒 USER

### 获取个人信息
```http
GET /api/v1/user/profile
Authorization: Bearer <token>
```
**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "phone": "13800138000",
    "role": "USER",
    "status": 1,
    "avatar": "url"
  }
}
```

### 修改个人信息
```http
PUT /api/v1/user/profile
Authorization: Bearer <token>
```
**请求体**:
```json
{
  "email": "string",
  "phone": "string",
  "avatar": "string"
}
```

### 修改密码
```http
PUT /api/v1/user/password
Authorization: Bearer <token>
```
**请求体**:
```json
{
  "oldPassword": "string",
  "newPassword": "string"
}
```

---

## 3. 商品模块 `/products`

### 商品列表
```http
GET /api/v1/products
```
**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "categoryId": 1,
      "categoryName": "手机数码",
      "name": "iPhone 15 Pro",
      "subtitle": "A17 Pro芯片，钛金属设计",
      "mainImage": "/images/iphone15pro.jpg",
      "price": 7999.00,
      "stock": 100,
      "status": 1
    }
  ]
}
```

### 商品详情
```http
GET /api/v1/products/{id}
```
**响应** (包含额外字段):
```json
{
  "images": "['/images/1.jpg', '/images/2.jpg']",
  "detail": "<p>商品详情HTML</p>"
}
```

### 搜索商品
```http
GET /api/v1/products/search?keyword=iPhone
```

### 按分类查询
```http
GET /api/v1/products/category/{categoryId}
```

### 按状态查询
```http
GET /api/v1/products/status/{status}
```
- status: 0-下架, 1-上架

---

## 4. 分类模块 `/categories`

### 分类列表
```http
GET /api/v1/categories
```
**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "电子产品",
      "parentId": 0,
      "level": 1,
      "sortOrder": 1,
      "status": 1
    }
  ]
}
```

### 分类树结构
```http
GET /api/v1/categories/tree
```
**响应** (递归结构):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "电子产品",
      "children": [
        {
          "id": 4,
          "name": "手机",
          "children": []
        }
      ]
    }
  ]
}
```

### 获取子分类
```http
GET /api/v1/categories/parent/{parentId}
```

---

## 5. 购物车模块 `/cart` 🔒 USER

### 获取购物车
```http
GET /api/v1/cart
Authorization: Bearer <token>
```
**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "productId": 1,
      "productName": "iPhone 15 Pro",
      "productImage": "/images/iphone15pro.jpg",
      "price": 7999.00,
      "quantity": 2,
      "totalPrice": 15998.00,
      "checked": true,
      "stock": 100
    }
  ]
}
```

### 添加商品
```http
POST /api/v1/cart
Authorization: Bearer <token>
```
**请求体**:
```json
{
  "productId": 1,
  "quantity": 2
}
```

### 修改数量
```http
PUT /api/v1/cart/{id}
Authorization: Bearer <token>
```
**请求体**:
```json
{
  "quantity": 3
}
```

### 删除商品
```http
DELETE /api/v1/cart/{id}
Authorization: Bearer <token>
```

### 清空购物车
```http
DELETE /api/v1/cart
Authorization: Bearer <token>
```

### 选中/取消选中
```http
PUT /api/v1/cart/check/{id}?checked=true
Authorization: Bearer <token>
```

### 全选/取消全选
```http
PUT /api/v1/cart/check-all?checked=true
Authorization: Bearer <token>
```

---

## 6. 收货地址模块 `/addresses` 🔒 USER

### 地址列表
```http
GET /api/v1/addresses
Authorization: Bearer <token>
```
**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "receiverName": "张三",
      "phone": "13800138000",
      "province": "北京市",
      "city": "北京市",
      "district": "朝阳区",
      "detailAddress": "某某街道123号",
      "isDefault": true
    }
  ]
}
```

### 地址详情
```http
GET /api/v1/addresses/{id}
Authorization: Bearer <token>
```

### 新增地址
```http
POST /api/v1/addresses
Authorization: Bearer <token>
```
**请求体**:
```json
{
  "receiverName": "string",
  "phone": "string",
  "province": "string",
  "city": "string",
  "district": "string",
  "detailAddress": "string",
  "isDefault": false
}
```

### 修改地址
```http
PUT /api/v1/addresses/{id}
Authorization: Bearer <token>
```

### 删除地址
```http
DELETE /api/v1/addresses/{id}
Authorization: Bearer <token>
```

### 设为默认地址
```http
PUT /api/v1/addresses/{id}/default
Authorization: Bearer <token>
```

---

## 7. 订单模块 `/orders` 🔒 USER

### 创建订单
```http
POST /api/v1/orders
Authorization: Bearer <token>
```
**请求体**:
```json
{
  "addressId": 1,
  "remark": "请尽快发货"
}
```
**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "20260108123456789",
    "totalAmount": 15998.00,
    "payAmount": 15998.00,
    "freight": 0.00,
    "status": "UNPAID",
    "statusDesc": "待支付",
    "items": [
      {
        "productId": 1,
        "productName": "iPhone 15 Pro",
        "quantity": 2,
        "unitPrice": 7999.00,
        "totalPrice": 15998.00
      }
    ]
  }
}
```

### 订单列表
```http
GET /api/v1/orders
Authorization: Bearer <token>
```

### 按状态查询
```http
GET /api/v1/orders/status/{status}
Authorization: Bearer <token>
```
**订单状态**:
- `UNPAID` - 待支付
- `PAID` - 已支付
- `SHIPPED` - 已发货
- `COMPLETED` - 已完成
- `CANCELLED` - 已取消

### 订单详情
```http
GET /api/v1/orders/{orderNo}
Authorization: Bearer <token>
```

### 取消订单
```http
PUT /api/v1/orders/{orderNo}/cancel
Authorization: Bearer <token>
```
**限制**:
- 用户可以取消待支付（UNPAID）和待发货（PAID）状态的订单
- 取消后订单状态变更为 CANCELLED
- 自动恢复所有商品库存
- 已发货（SHIPPED）和已完成（COMPLETED）订单无法取消

### 确认收货
```http
PUT /api/v1/orders/{orderNo}/confirm
Authorization: Bearer <token>
```
**限制**: 只能确认已发货订单

---

## 8. 支付模块 `/payment` 🔒 USER

### 发起支付
```http
POST /api/v1/payment/pay
Authorization: Bearer <token>
```
**请求体**:
```json
{
  "orderNo": "20260108123456789",
  "paymentMethod": "MOCK"
}
```
**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderNo": "20260108123456789",
    "payAmount": 15998.00,
    "paymentMethod": "MOCK",
    "paymentStatus": "SUCCESS",
    "message": "支付成功",
    "transactionNo": "PAY1767846618551..."
  }
}
```

---

## 9. 后台 - 商品管理 `/admin/products` 🔒 ADMIN

### 商品列表（管理员）
```http
GET /api/v1/admin/products
Authorization: Bearer <admin-token>
```

### 商品详情（管理员）
```http
GET /api/v1/admin/products/{id}
Authorization: Bearer <admin-token>
```

### 新增商品
```http
POST /api/v1/admin/products
Authorization: Bearer <admin-token>
```
**请求体**:
```json
{
  "categoryId": 1,
  "name": "string",
  "subtitle": "string",
  "mainImage": "string",
  "images": "string",
  "detail": "string",
  "price": 7999.00,
  "stock": 100,
  "status": 1
}
```

### 修改商品
```http
PUT /api/v1/admin/products/{id}
Authorization: Bearer <admin-token>
```

### 删除商品
```http
DELETE /api/v1/admin/products/{id}
Authorization: Bearer <admin-token>
```

### 修改商品状态
```http
PUT /api/v1/admin/products/{id}/status?status=1
Authorization: Bearer <admin-token>
```

### 修改商品库存
```http
PUT /api/v1/admin/products/{id}/stock?stock=200
Authorization: Bearer <admin-token>
```

---

## 10. 后台 - 分类管理 `/admin/categories` 🔒 ADMIN

### 新增分类
```http
POST /api/v1/admin/categories
Authorization: Bearer <admin-token>
```
**请求体**:
```json
{
  "name": "string",
  "parentId": 0,
  "sortOrder": 1
}
```

### 修改分类
```http
PUT /api/v1/admin/categories/{id}
Authorization: Bearer <admin-token>
```

### 删除分类
```http
DELETE /api/v1/admin/categories/{id}
Authorization: Bearer <admin-token>
```
**限制**: 有子分类或有商品的分类不能删除

---

## 11. 后台 - 订单管理 `/admin/orders` 🔒 ADMIN

### 所有订单列表
```http
GET /api/v1/admin/orders
Authorization: Bearer <admin-token>
```

### 按状态查询订单
```http
GET /api/v1/admin/orders/status/{status}
Authorization: Bearer <admin-token>
```

### 订单详情（管理员）
```http
GET /api/v1/admin/orders/{orderNo}
Authorization: Bearer <admin-token>
```

### 订单发货
```http
PUT /api/v1/admin/orders/{orderNo}/ship
Authorization: Bearer <admin-token>
```
**限制**: 只能发货已支付订单

### 取消订单（管理员）
```http
PUT /api/v1/admin/orders/{orderNo}/cancel
Authorization: Bearer <admin-token>
```
**说明**: 管理员可取消待支付和待发货状态的订单，取消后自动恢复库存

**限制**:
- 只能取消 UNPAID（待支付）或 PAID（待发货）状态的订单
- 取消后订单状态变更为 CANCELLED
- 自动恢复所有商品库存

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 12. 后台 - 用户管理 `/admin/users` 🔒 ADMIN

### 用户列表
```http
GET /api/v1/admin/users
Authorization: Bearer <admin-token>
```
**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com",
      "phone": "13800138000",
      "role": "USER",
      "status": 1,
      "createdAt": "2026-01-08T10:00:00"
    }
  ]
}
```

### 用户详情
```http
GET /api/v1/admin/users/{id}
Authorization: Bearer <admin-token>
```

### 修改用户状态
```http
PUT /api/v1/admin/users/{id}/status?status=0
Authorization: Bearer <admin-token>
```
- status: 0-禁用, 1-启用

### 修改用户角色
```http
PUT /api/v1/admin/users/{id}/role?role=ADMIN
Authorization: Bearer <admin-token>
```
- role: USER, ADMIN

---

## 使用示例

### 完整购物流程代码
```javascript
// 1. 注册/登录
const auth = await request.post('/auth/login', {
  username: 'testuser',
  password: '123456'
});
localStorage.setItem('accessToken', auth.data.accessToken);

// 2. 浏览商品
const products = await request.get('/products');

// 3. 添加到购物车
await request.post('/cart', {
  productId: 1,
  quantity: 2
});

// 4. 查看购物车
const cart = await request.get('/cart');

// 5. 创建订单
const order = await request.post('/orders', {
  addressId: 1,
  remark: '请尽快发货'
});

// 6. 支付
const payment = await request.post('/payment/pay', {
  orderNo: order.data.orderNo,
  paymentMethod: 'MOCK'
});

// 7. 查看订单
const orderDetail = await request.get(`/orders/${order.data.orderNo}`);
```