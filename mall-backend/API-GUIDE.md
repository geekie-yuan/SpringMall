# Spring Mall API 接口文档

## 项目概述

Spring Mall 是一个完整的在线商城后端系统，基于 Spring Boot 3.3.6 + MyBatis 3.5.17 开发。

**技术栈**: Spring Boot, Spring Security, JWT, MyBatis, MySQL

**基础路径**: `http://localhost:8080/api/v1`

**openapi:** `http://localhost:8080/api-docs`

---

## 统一响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

### 错误响应
```json
{
  "code": 40001,
  "message": "用户名已存在",
  "data": null
}
```

### 分页响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "size": 10,
    "pages": 10
  }
}
```

---

## 认证说明

### Token 获取
登录成功后获得 JWT Token，在后续请求中携带：

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 角色权限
- **公开接口**: 无需认证
- **USER**: 普通用户权限
- **ADMIN**: 管理员权限

---

## API 接口清单

### 1. 认证模块 `/api/v1/auth`

#### 1.1 用户注册
```http
POST /api/v1/auth/register
```

**请求体**:
```json
{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com",
  "phone": "13800138000"
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

#### 1.2 用户登录
```http
POST /api/v1/auth/login
```

**请求体**:
```json
{
  "username": "testuser",
  "password": "123456"
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

#### 1.3 用户登出
```http
POST /api/v1/auth/logout
Authorization: Bearer <token>
```

---

### 2. 用户模块 `/api/v1/user`

#### 2.1 获取个人信息
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
    "status": 1
  }
}
```

#### 2.2 修改个人信息
```http
PUT /api/v1/user/profile
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "email": "newemail@example.com",
  "phone": "13900139000",
  "avatar": "https://example.com/avatar.jpg"
}
```

#### 2.3 修改密码
```http
PUT /api/v1/user/password
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

---

### 3. 商品模块 `/api/v1/products`

#### 3.1 商品列表
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

#### 3.2 商品详情
```http
GET /api/v1/products/{id}
```

#### 3.3 搜索商品
```http
GET /api/v1/products/search?keyword=iPhone
```

#### 3.4 按分类查询
```http
GET /api/v1/products/category/{categoryId}
```

#### 3.5 按状态查询
```http
GET /api/v1/products/status/{status}
```
- status: 0-下架, 1-上架

---

### 4. 分类模块 `/api/v1/categories`

#### 4.1 分类列表
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

#### 4.2 分类树结构
```http
GET /api/v1/categories/tree
```

**响应** (带子分类):
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

#### 4.3 获取子分类
```http
GET /api/v1/categories/parent/{parentId}
```

---

### 5. 购物车模块 `/api/v1/cart` 🔒 USER

#### 5.1 获取购物车
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

#### 5.2 添加商品
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

#### 5.3 修改数量
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

#### 5.4 删除商品
```http
DELETE /api/v1/cart/{id}
Authorization: Bearer <token>
```

#### 5.5 清空购物车
```http
DELETE /api/v1/cart
Authorization: Bearer <token>
```

#### 5.6 选中/取消选中
```http
PUT /api/v1/cart/check/{id}?checked=true
Authorization: Bearer <token>
```

#### 5.7 全选/取消全选
```http
PUT /api/v1/cart/check-all?checked=true
Authorization: Bearer <token>
```

---

### 6. 收货地址模块 `/api/v1/addresses` 🔒 USER

#### 6.1 地址列表
```http
GET /api/v1/addresses
Authorization: Bearer <token>
```

#### 6.2 地址详情
```http
GET /api/v1/addresses/{id}
Authorization: Bearer <token>
```

#### 6.3 新增地址
```http
POST /api/v1/addresses
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "receiverName": "张三",
  "phone": "13800138000",
  "province": "北京市",
  "city": "北京市",
  "district": "朝阳区",
  "detailAddress": "某某街道123号",
  "isDefault": false
}
```

#### 6.4 修改地址
```http
PUT /api/v1/addresses/{id}
Authorization: Bearer <token>
```

#### 6.5 删除地址
```http
DELETE /api/v1/addresses/{id}
Authorization: Bearer <token>
```

#### 6.6 设为默认地址
```http
PUT /api/v1/addresses/{id}/default
Authorization: Bearer <token>
```

---

### 7. 订单模块 `/api/v1/orders` 🔒 USER

#### 7.1 创建订单
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

#### 7.2 订单列表
```http
GET /api/v1/orders
Authorization: Bearer <token>
```

#### 7.3 按状态查询
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

#### 7.4 订单详情
```http
GET /api/v1/orders/{orderNo}
Authorization: Bearer <token>
```

#### 7.5 取消订单
```http
PUT /api/v1/orders/{orderNo}/cancel
Authorization: Bearer <token>
```

**限制**:
- 用户可以取消待支付（UNPAID）和待发货（PAID）状态的订单
- 取消后订单状态变更为 CANCELLED
- 自动恢复所有商品库存
- 已发货（SHIPPED）和已完成（COMPLETED）订单无法取消

#### 7.6 确认收货
```http
PUT /api/v1/orders/{orderNo}/confirm
Authorization: Bearer <token>
```

**限制**: 只能确认已发货订单

---

### 8. 支付模块 `/api/v1/payment`

#### 8.1 发起支付
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

#### 8.2 支付回调（内部）
```http
POST /api/v1/payment/notify
```

**请求体**:
```json
{
  "orderNo": "20260108123456789",
  "transactionNo": "TEST123456789",
  "paymentStatus": "SUCCESS",
  "timestamp": 1767846694000
}
```

---

### 9. 后台管理 - 商品 `/api/v1/admin/products` 🔒 ADMIN

#### 9.1 商品列表（管理员）
```http
GET /api/v1/admin/products
Authorization: Bearer <admin-token>
```

#### 9.2 商品详情（管理员）
```http
GET /api/v1/admin/products/{id}
Authorization: Bearer <admin-token>
```

#### 9.3 新增商品
```http
POST /api/v1/admin/products
Authorization: Bearer <admin-token>
```

**请求体**:
```json
{
  "categoryId": 1,
  "name": "iPhone 15 Pro",
  "subtitle": "A17 Pro芯片，钛金属设计",
  "mainImage": "/images/iphone15pro.jpg",
  "images": "['/images/1.jpg', '/images/2.jpg']",
  "detail": "<p>商品详情HTML</p>",
  "price": 7999.00,
  "stock": 100,
  "status": 1
}
```

#### 9.4 修改商品
```http
PUT /api/v1/admin/products/{id}
Authorization: Bearer <admin-token>
```

#### 9.5 删除商品
```http
DELETE /api/v1/admin/products/{id}
Authorization: Bearer <admin-token>
```

#### 9.6 修改商品状态
```http
PUT /api/v1/admin/products/{id}/status?status=1
Authorization: Bearer <admin-token>
```
- status: 0-下架, 1-上架

#### 9.7 修改商品库存
```http
PUT /api/v1/admin/products/{id}/stock?stock=200
Authorization: Bearer <admin-token>
```

---

### 10. 后台管理 - 分类 `/api/v1/admin/categories` 🔒 ADMIN

#### 10.1 分类列表（管理员）
```http
GET /api/v1/admin/categories
Authorization: Bearer <admin-token>
```

#### 10.2 分类详情（管理员）
```http
GET /api/v1/admin/categories/{id}
Authorization: Bearer <admin-token>
```

#### 10.3 新增分类
```http
POST /api/v1/admin/categories
Authorization: Bearer <admin-token>
```

**请求体**:
```json
{
  "name": "家用电器",
  "parentId": 0,
  "sortOrder": 3
}
```

#### 10.4 修改分类
```http
PUT /api/v1/admin/categories/{id}
Authorization: Bearer <admin-token>
```

#### 10.5 删除分类
```http
DELETE /api/v1/admin/categories/{id}
Authorization: Bearer <admin-token>
```

**限制**:
- 有子分类的分类不能删除
- 有商品的分类不能删除

---

### 11. 后台管理 - 订单 `/api/v1/admin/orders` 🔒 ADMIN

#### 11.1 所有订单列表
```http
GET /api/v1/admin/orders
Authorization: Bearer <admin-token>
```

#### 11.2 按状态查询订单
```http
GET /api/v1/admin/orders/status/{status}
Authorization: Bearer <admin-token>
```

#### 11.3 订单详情（管理员）
```http
GET /api/v1/admin/orders/{orderNo}
Authorization: Bearer <admin-token>
```

**说明**: 管理员可查看所有用户的订单

#### 11.4 订单发货
```http
PUT /api/v1/admin/orders/{orderNo}/ship
Authorization: Bearer <admin-token>
```

**限制**: 只能发货已支付订单

#### 11.5 取消订单（管理员）
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

### 12. 后台管理 - 用户 `/api/v1/admin/users` 🔒 ADMIN

#### 12.1 用户列表
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

#### 12.2 用户详情
```http
GET /api/v1/admin/users/{id}
Authorization: Bearer <admin-token>
```

#### 12.3 修改用户状态
```http
PUT /api/v1/admin/users/{id}/status?status=0
Authorization: Bearer <admin-token>
```
- status: 0-禁用, 1-启用

#### 12.4 修改用户角色
```http
PUT /api/v1/admin/users/{id}/role?role=ADMIN
Authorization: Bearer <admin-token>
```
- role: USER, ADMIN

---

## 常见错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（未登录或Token过期） |
| 403 | 禁止访问（无权限） |
| 404 | 资源未找到 |
| 500 | 服务器内部错误 |
| 40001 | 用户名已存在 |
| 40002 | 邮箱已存在 |
| 40003 | 手机号已存在 |
| 40004 | 用户不存在 |
| 40005 | 用户名或密码错误 |
| 40006 | 账户已被禁用 |
| 40101 | 商品不存在 |
| 40103 | 库存不足 |
| 40104 | 商品已下架 |
| 40201 | 分类不存在 |
| 40301 | 购物车项不存在 |
| 40302 | 购物车为空 |
| 40303 | 购物车中无已选中商品 |
| 40401 | 地址不存在 |
| 40501 | 订单不存在 |
| 40502 | 无效的订单状态 |
| 40503 | 订单无法取消 |
| 40601 | 支付失败 |
| 40701 | 无效的Token |
| 40702 | Token已过期 |

---

## 使用示例

### 完整购物流程

```bash
# 1. 注册用户
POST /api/v1/auth/register
{
  "username": "buyer",
  "password": "123456",
  "email": "buyer@example.com"
}

# 2. 登录获取Token
POST /api/v1/auth/login
{
  "username": "buyer",
  "password": "123456"
}
# 获得: {"accessToken": "xxx..."}

# 3. 添加收货地址
POST /api/v1/addresses
Authorization: Bearer xxx...
{
  "receiverName": "张三",
  "phone": "13800138000",
  "province": "北京市",
  "city": "北京市",
  "district": "朝阳区",
  "detailAddress": "某某街道123号"
}

# 4. 浏览商品
GET /api/v1/products

# 5. 添加到购物车
POST /api/v1/cart
Authorization: Bearer xxx...
{
  "productId": 1,
  "quantity": 2
}

# 6. 查看购物车
GET /api/v1/cart
Authorization: Bearer xxx...

# 7. 创建订单
POST /api/v1/orders
Authorization: Bearer xxx...
{
  "addressId": 1,
  "remark": "请尽快发货"
}
# 获得: {"orderNo": "20260108123456789"}

# 8. 支付订单
POST /api/v1/payment/pay
Authorization: Bearer xxx...
{
  "orderNo": "20260108123456789",
  "paymentMethod": "MOCK"
}

# 9. 查看订单
GET /api/v1/orders/20260108123456789
Authorization: Bearer xxx...

# 10. 确认收货（发货后）
PUT /api/v1/orders/20260108123456789/confirm
Authorization: Bearer xxx...
```

---

## 注意事项

### 1. Token管理
- Token有效期为24小时
- Token过期后需要重新登录
- Token需要在请求头中携带：`Authorization: Bearer <token>`

### 2. 权限控制
- 公开接口：商品查询、分类查询等
- USER权限：购物车、订单、地址等个人操作
- ADMIN权限：所有后台管理接口

### 3. 数据隔离
- 用户只能操作自己的数据（购物车、订单、地址）
- 管理员可以查看和管理所有数据

### 4. 库存管理
- 下单时扣减库存
- 取消订单时恢复库存
- 使用乐观锁防止超卖

### 5. 订单状态流转
```
UNPAID（待支付） → 支付 → PAID（已支付）
                              ↓
                            发货
                              ↓
                       SHIPPED（已发货）
                              ↓
                          确认收货
                              ↓
                      COMPLETED（已完成）

UNPAID（待支付） → 取消 → CANCELLED（已取消）
```

---

## 开发调试

### Swagger文档
```
http://localhost:8080/swagger-ui.html
```

### 健康检查
```
GET http://localhost:8080/api/v1/health
```

### 数据库
```
数据库: mall
默认管理员: admin / admin123
```

---

## 联系与反馈

如有问题或建议，请通过以下方式联系：
- GitHub Issues: 
- 邮箱: 

**版本**: v1.0.0
**最后更新**: 2026-01-08
