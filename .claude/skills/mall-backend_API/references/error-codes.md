# Spring Mall 错误码参考

## 错误码列表

| 错误码 | 说明                        | 前端处理建议                 |
| ------ | --------------------------- | ---------------------------- |
| 200    | 成功                        | 正常流程处理                 |
| 400    | 请求参数错误                | 提示用户检查输入格式         |
| 401    | 未授权（未登录或Token过期） | 清除Token，跳转登录页        |
| 403    | 禁止访问（无权限）          | 提示"权限不足，请联系管理员" |
| 404    | 资源未找到                  | 提示"请求的资源不存在"       |
| 500    | 服务器内部错误              | 提示"系统异常，请稍后重试"   |

## 业务错误码详细说明

### 用户相关 (400xx)

| 错误码 | 说明             | 处理建议                           |
| ------ | ---------------- | ---------------------------------- |
| 40001  | 用户名已存在     | "该用户名已被使用，请更换"         |
| 40002  | 邮箱已存在       | "该邮箱已被注册，请更换或直接登录" |
| 40003  | 手机号已存在     | "该手机号已被使用，请更换"         |
| 40004  | 用户不存在       | "用户不存在，请检查用户名"         |
| 40005  | 用户名或密码错误 | "用户名或密码错误，请重新输入"     |
| 40006  | 账户已被禁用     | "您的账户已被禁用，请联系管理员"   |

### 商品相关 (401xx)

| 错误码 | 说明       | 处理建议                 |
| ------ | ---------- | ------------------------ |
| 40101  | 商品不存在 | "商品不存在或已下架"     |
| 40103  | 库存不足   | "库存不足，当前仅剩XX件" |
| 40104  | 商品已下架 | "商品已下架，无法购买"   |

### 分类相关 (402xx)

| 错误码 | 说明       | 处理建议       |
| ------ | ---------- | -------------- |
| 40201  | 分类不存在 | "该分类不存在" |

### 购物车相关 (403xx)

| 错误码 | 说明                 | 处理建议                           |
| ------ | -------------------- | ---------------------------------- |
| 40301  | 购物车项不存在       | 刷新购物车列表                     |
| 40302  | 购物车为空           | "您的购物车是空的，快去挑选商品吧" |
| 40303  | 购物车中无已选中商品 | "请至少选择一件商品"               |

### 地址相关 (404xx)

| 错误码 | 说明       | 处理建议         |
| ------ | ---------- | ---------------- |
| 40401  | 地址不存在 | "收货地址不存在" |

### 订单相关 (405xx)

| 错误码 | 说明           | 处理建议                                 |
| ------ | -------------- | ---------------------------------------- |
| 40501  | 订单不存在     | "订单不存在，请检查订单号"               |
| 40502  | 无效的订单状态 | "订单状态异常，无法操作"                 |
| 40503  | 订单无法取消   | "该订单不允许取消（仅待支付订单可取消）" |

### 支付相关 (406xx)

| 错误码 | 说明     | 处理建议                         |
| ------ | -------- | -------------------------------- |
| 40601  | 支付失败 | "支付失败，请重试或更换支付方式" |

### Token相关 (407xx)

| 错误码 | 说明        | 处理建议                                            |
| ------ | ----------- | --------------------------------------------------- |
| 40701  | 无效的Token | 清除Token，跳转登录页                               |
| 40702  | Token已过期 | 清除Token，跳转登录页，提示"登录已过期，请重新登录" |

## 错误处理最佳实践

### 1. Axios响应拦截器统一处理

```javascript
// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data;
    if (res.code !== 200) {
      // 根据错误码分类处理
      handleError(res.code, res.message);
      return Promise.reject(new Error(res.message));
    }
    return res;
  },
  error => {
    console.error('请求错误:', error);
    message.error('网络错误，请稍后重试');
    return Promise.reject(error);
  }
);

// 错误处理函数
function handleError(code, message) {
  // Token相关错误
  if (code === 401 || code === 40701 || code === 40702) {
    localStorage.removeItem('accessToken');
    router.push('/login');
    if (code === 40702) {
      showMessage('登录已过期，请重新登录');
    }
    return;
  }
  
  // 权限错误
  if (code === 403) {
    showMessage('权限不足，请联系管理员');
    return;
  }
  
  // 显示具体错误信息
  showMessage(message || '操作失败');
}
```

### 2. 特定场景的错误处理

#### 购物车场景
```javascript
try {
  await request.post('/cart', { productId, quantity });
  message.success('已加入购物车');
} catch (error) {
  // 40103: 库存不足
  if (error.code === 40103) {
    message.warning('库存不足，请减少购买数量');
  }
  // 40104: 商品已下架
  else if (error.code === 40104) {
    message.error('商品已下架');
  }
  else {
    message.error(error.message);
  }
}
```

#### 订单场景
```javascript
try {
  await request.put(`/orders/${orderNo}/cancel`);
  message.success('订单已取消');
} catch (error) {
  // 40503: 订单无法取消
  if (error.code === 40503) {
    message.warning('该订单不允许取消');
  }
  else {
    message.error(error.message);
  }
}
```

### 3. 用户友好的错误提示

| 场景       | 系统错误 | 用户友好提示                |
| ---------- | -------- | --------------------------- |
| 登录失败   | 40005    | "用户名或密码错误"          |
| Token过期  | 40702    | "登录已过期，请重新登录"    |
| 库存不足   | 40103    | "抱歉，库存不足（仅剩5件）" |
| 权限不足   | 403      | "您没有权限访问此页面"      |
| 网络错误   | -        | "网络连接失败，请检查网络"  |
| 服务器错误 | 500      | "系统繁忙，请稍后重试"      |

### 4. 表单验证与错误码配合

```javascript
// 注册表单
const register = async (formData) => {
  try {
    await request.post('/auth/register', formData);
    message.success('注册成功');
    router.push('/login');
  } catch (error) {
    // 根据错误码显示对应字段的错误
    if (error.code === 40001) {
      form.setFieldError('username', '该用户名已被使用');
    } else if (error.code === 40002) {
      form.setFieldError('email', '该邮箱已被注册');
    } else if (error.code === 40003) {
      form.setFieldError('phone', '该手机号已被使用');
    } else {
      message.error(error.message);
    }
  }
};
```

## 错误日志记录建议

```javascript
// 生产环境错误日志
if (process.env.NODE_ENV === 'production') {
  // 记录错误到日志系统
  logError({
    code: error.code,
    message: error.message,
    url: window.location.href,
    timestamp: new Date().toISOString(),
    userAgent: navigator.userAgent
  });
}
```