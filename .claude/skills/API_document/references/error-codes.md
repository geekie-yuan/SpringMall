# Spring Mall 错误码参考

> 最后更新时间：2026-02-20

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

| 错误码 | 说明               | 处理建议                                 |
| ------ | ------------------ | ---------------------------------------- |
| 40501  | 订单不存在         | "订单不存在，请检查订单号"               |
| 40502  | 无效的订单状态     | "订单状态异常，无法操作"                 |
| 40503  | 订单无法取消       | "该订单不允许取消（仅待支付订单可取消）" |
| 40504  | 订单商品明细不存在 | "订单商品信息不存在"                     |

### 支付相关 (406xx)

| 错误码 | 说明           | 处理建议                           |
| ------ | -------------- | ---------------------------------- |
| 40601  | 支付失败       | "支付失败，请重试或更换支付方式"   |
| 40602  | 支付记录不存在 | "支付记录不存在，请检查支付流水号" |
| 40603  | 支付关闭失败   | "支付关闭失败，请稍后重试"         |
| 40604  | 退款失败       | "退款失败，请联系客服"             |
| 40605  | 退款记录不存在 | "退款记录不存在"                   |
| 40606  | 支付已退款     | "该支付已退款，不可重复退款"       |

### Token相关 (407xx)

| 错误码 | 说明        | 处理建议                                            |
| ------ | ----------- | --------------------------------------------------- |
| 40701  | 无效的Token | 清除Token，跳转登录页                               |
| 40702  | Token已过期 | 清除Token，跳转登录页，提示"登录已过期，请重新登录" |

---

## 错误码使用示例

### 前端处理示例

#### 1. 创建支付宝支付时的错误处理
```javascript
try {
  const response = await createAlipayPayment(orderNo);
  // 成功，提交支付表单
  const html = response.data.paymentUrl;
  const div = document.createElement('div');
  div.innerHTML = html;
  document.body.appendChild(div);
} catch (error) {
  if (error.code === 40601) {
    ElMessage.error('支付失败，请重试或更换支付方式');
  } else if (error.code === 40501) {
    ElMessage.error('订单不存在');
  } else if (error.code === 40502) {
    ElMessage.error('订单状态错误，不可支付');
  } else {
    ElMessage.error(error.message || '操作失败');
  }
}
```

#### 1.1 创建微信支付时的错误处理
```javascript
try {
  const response = await createWxPayment(orderNo);
  // 成功，生成二维码
  const codeUrl = response.data.codeUrl;
  const qrCodeDataUrl = await QRCode.toDataURL(codeUrl);
  document.getElementById('qrcode').src = qrCodeDataUrl;
} catch (error) {
  if (error.code === 40601) {
    ElMessage.error('支付失败，请重试或更换支付方式');
  } else if (error.code === 40501) {
    ElMessage.error('订单不存在');
  } else if (error.code === 40502) {
    ElMessage.error('订单状态错误，不可支付');
  } else {
    ElMessage.error(error.message || '操作失败');
  }
}
```

#### 1.2 创建 Stripe 支付时的错误处理
```javascript
import { loadStripe } from '@stripe/stripe-js';

try {
  // 创建支付
  const response = await createStripePayment(orderNo);
  const { clientSecret, publishableKey } = response.data;

  // 初始化 Stripe
  const stripe = await loadStripe(publishableKey);
  const elements = stripe.elements({ clientSecret });
  const paymentElement = elements.create('payment');
  paymentElement.mount('#payment-element');

  // 确认支付
  const { error } = await stripe.confirmPayment({
    elements,
    confirmParams: {
      return_url: 'https://your-domain.com/payment/result'
    }
  });

  if (error) {
    // Stripe 客户端错误
    if (error.type === 'card_error') {
      ElMessage.error(`银行卡错误: ${error.message}`);
    } else if (error.type === 'validation_error') {
      ElMessage.error(`验证失败: ${error.message}`);
    } else {
      ElMessage.error('支付失败，请重试');
    }
  }
} catch (error) {
  // 后端错误
  if (error.code === 40601) {
    ElMessage.error('支付失败，请重试或更换支付方式');
  } else if (error.code === 40501) {
    ElMessage.error('订单不存在');
  } else if (error.code === 40502) {
    ElMessage.error('订单状态错误，不可支付');
  } else {
    ElMessage.error(error.message || '创建支付失败');
  }
}
```

#### 2. 查询支付状态时的错误处理
```javascript
try {
  const payment = await queryPaymentStatus(paymentNo);
  if (payment.paymentStatus === 'SUCCESS') {
    ElMessage.success('支付成功');
    router.push('/orders');
  } else if (payment.paymentStatus === 'PENDING') {
    ElMessage.info('等待支付...');
  } else {
    ElMessage.error('支付失败');
  }
} catch (error) {
  if (error.code === 40602) {
    ElMessage.error('支付记录不存在，请检查支付流水号');
  } else {
    ElMessage.error(error.message || '查询失败');
  }
}
```

#### 3. 取消订单时的错误处理
```javascript
try {
  await cancelOrder(orderNo);
  ElMessage.success('订单已取消');
  router.push('/orders');
} catch (error) {
  if (error.code === 40604) {
    ElMessage.error('退款失败，请联系客服');
  } else if (error.code === 40606) {
    ElMessage.error('该订单已退款');
  } else if (error.code === 40503) {
    ElMessage.error('订单已发货，不可取消');
  } else if (error.code === 40501) {
    ElMessage.error('订单不存在');
  } else {
    ElMessage.error(error.message || '取消失败');
  }
}
```

#### 4. 统一错误处理拦截器
```javascript
// 在 axios 响应拦截器中统一处理
axios.interceptors.response.use(
  response => {
    const { code, message, data } = response.data;
    if (code === 200) {
      return data;
    } else {
      throw { code, message };
    }
  },
  error => {
    const { code, message } = error.response?.data || {};

    // Token 过期，跳转登录
    if (code === 40701 || code === 40702) {
      localStorage.removeItem('accessToken');
      router.push('/login');
      ElMessage.error('登录已过期，请重新登录');
      return Promise.reject(error);
    }

    // 权限不足
    if (code === 403) {
      ElMessage.error('权限不足，请联系管理员');
      return Promise.reject(error);
    }

    // 其他错误
    return Promise.reject({ code, message });
  }
);
```

---

## 错误码设计规范

### 错误码分段规则
- **200**：成功
- **400-499**：客户端错误
- **500-599**：服务器错误
- **40001-40099**：用户相关错误
- **40101-40199**：商品相关错误
- **40201-40299**：分类相关错误
- **40301-40399**：购物车相关错误
- **40401-40499**：地址相关错误
- **40501-40599**：订单相关错误
- **40601-40699**：支付相关错误
- **40701-40799**：Token相关错误

### 新增错误码流程
1. 在 `ResultCode` 枚举中添加错误码常量
2. 更新本文档，添加错误码说明和处理建议
3. 更新 API 接口文档，说明可能返回的错误码
4. 前端更新错误处理逻辑

