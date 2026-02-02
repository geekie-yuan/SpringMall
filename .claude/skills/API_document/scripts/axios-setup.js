/**
 * Spring Mall API - Axios 封装
 * 
 * 功能:
 * - 统一的请求/响应拦截
 * - 自动添加JWT Token
 * - Token过期自动处理
 * - 统一错误处理
 */

import axios from 'axios';

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

/**
 * 请求拦截器
 * 自动为需要认证的请求添加Token
 */
request.interceptors.request.use(
  config => {
    // 从localStorage获取Token
    const token = localStorage.getItem('accessToken');
    
    // 如果存在Token，添加到请求头
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    // 开发环境日志
    if (process.env.NODE_ENV === 'development') {
      console.log('📤 请求:', config.method?.toUpperCase(), config.url);
      if (config.data) {
        console.log('  数据:', config.data);
      }
    }
    
    return config;
  },
  error => {
    console.error('❌ 请求错误:', error);
    return Promise.reject(error);
  }
);

/**
 * 响应拦截器
 * 统一处理响应和错误
 */
request.interceptors.response.use(
  response => {
    const res = response.data;
    
    // 开发环境日志
    if (process.env.NODE_ENV === 'development') {
      console.log('📥 响应:', response.config.url);
      console.log('  结果:', res);
    }
    
    // 检查业务状态码
    if (res.code !== 200) {
      // 处理业务错误
      handleBusinessError(res.code, res.message);
      return Promise.reject(new Error(res.message || '请求失败'));
    }
    
    return res;
  },
  error => {
    console.error('❌ 响应错误:', error);
    
    // 处理HTTP错误
    if (error.response) {
      const { status, data } = error.response;
      
      switch (status) {
        case 401:
          handleUnauthorized();
          break;
        case 403:
          showMessage('权限不足，无法访问', 'error');
          break;
        case 404:
          showMessage('请求的资源不存在', 'error');
          break;
        case 500:
          showMessage('服务器错误，请稍后重试', 'error');
          break;
        default:
          showMessage(data?.message || '网络请求失败', 'error');
      }
    } else if (error.request) {
      // 请求发送了但没有收到响应
      showMessage('网络连接失败，请检查网络', 'error');
    } else {
      // 请求配置出错
      showMessage('请求配置错误', 'error');
    }
    
    return Promise.reject(error);
  }
);

/**
 * 处理业务错误码
 * @param {number} code - 错误码
 * @param {string} message - 错误信息
 */
function handleBusinessError(code, message) {
  // Token相关错误: 401, 40701, 40702
  if (code === 401 || code === 40701 || code === 40702) {
    handleUnauthorized();
    return;
  }
  
  // 权限错误
  if (code === 403) {
    showMessage('权限不足，请联系管理员', 'error');
    return;
  }
  
  // 显示具体业务错误
  showMessage(message || '操作失败', 'error');
}

/**
 * 处理未授权错误
 * 清除Token并跳转登录页
 */
function handleUnauthorized() {
  // 清除Token
  localStorage.removeItem('accessToken');
  
  // 提示用户
  showMessage('登录已过期，请重新登录', 'warning');
  
  // 跳转登录页
  // 根据你的路由库选择合适的方法
  // Vue Router:
  // router.push('/login');
  
  // React Router:
  // window.location.href = '/login';
  
  // 或简单的跳转:
  setTimeout(() => {
    window.location.href = '/login';
  }, 1500);
}

/**
 * 显示消息提示
 * @param {string} msg - 消息内容
 * @param {string} type - 消息类型: success, error, warning, info
 */
function showMessage(msg, type = 'info') {
  // 根据你使用的UI库选择合适的方法
  
  // Ant Design:
  // import { message } from 'antd';
  // message[type](msg);
  
  // Element Plus:
  // import { ElMessage } from 'element-plus';
  // ElMessage({ message: msg, type });
  
  // 原生alert (开发时使用):
  if (process.env.NODE_ENV === 'development') {
    console.log(`[${type.toUpperCase()}] ${msg}`);
  }
  alert(msg);
}

/**
 * API调用示例
 */

// 认证相关
export const authAPI = {
  // 注册
  register: (data) => request.post('/auth/register', data),
  
  // 登录
  login: (data) => request.post('/auth/login', data),
  
  // 登出
  logout: () => request.post('/auth/logout')
};

// 用户相关
export const userAPI = {
  // 获取个人信息
  getProfile: () => request.get('/user/profile'),
  
  // 修改个人信息
  updateProfile: (data) => request.put('/user/profile', data),
  
  // 修改密码
  changePassword: (data) => request.put('/user/password', data)
};

// 商品相关
export const productAPI = {
  // 获取商品列表
  getList: () => request.get('/products'),
  
  // 获取商品详情
  getDetail: (id) => request.get(`/products/${id}`),
  
  // 搜索商品
  search: (keyword) => request.get('/products/search', { 
    params: { keyword } 
  }),
  
  // 按分类查询
  getByCategory: (categoryId) => request.get(`/products/category/${categoryId}`),
  
  // 按状态查询
  getByStatus: (status) => request.get(`/products/status/${status}`)
};

// 分类相关
export const categoryAPI = {
  // 获取分类列表
  getList: () => request.get('/categories'),
  
  // 获取分类树
  getTree: () => request.get('/categories/tree'),
  
  // 获取子分类
  getChildren: (parentId) => request.get(`/categories/parent/${parentId}`)
};

// 购物车相关
export const cartAPI = {
  // 获取购物车
  getCart: () => request.get('/cart'),
  
  // 添加到购物车
  addItem: (data) => request.post('/cart', data),
  
  // 更新数量
  updateQuantity: (id, quantity) => request.put(`/cart/${id}`, { quantity }),
  
  // 删除商品
  deleteItem: (id) => request.delete(`/cart/${id}`),
  
  // 清空购物车
  clearCart: () => request.delete('/cart'),
  
  // 选中/取消选中
  checkItem: (id, checked) => request.put(`/cart/check/${id}`, null, {
    params: { checked }
  }),
  
  // 全选/取消全选
  checkAll: (checked) => request.put('/cart/check-all', null, {
    params: { checked }
  })
};

// 地址相关
export const addressAPI = {
  // 获取地址列表
  getList: () => request.get('/addresses'),
  
  // 获取地址详情
  getDetail: (id) => request.get(`/addresses/${id}`),
  
  // 新增地址
  create: (data) => request.post('/addresses', data),
  
  // 修改地址
  update: (id, data) => request.put(`/addresses/${id}`, data),
  
  // 删除地址
  delete: (id) => request.delete(`/addresses/${id}`),
  
  // 设为默认地址
  setDefault: (id) => request.put(`/addresses/${id}/default`)
};

// 订单相关
export const orderAPI = {
  // 创建订单
  create: (data) => request.post('/orders', data),
  
  // 获取订单列表
  getList: () => request.get('/orders'),
  
  // 按状态查询
  getByStatus: (status) => request.get(`/orders/status/${status}`),
  
  // 获取订单详情
  getDetail: (orderNo) => request.get(`/orders/${orderNo}`),
  
  // 取消订单
  cancel: (orderNo) => request.put(`/orders/${orderNo}/cancel`),
  
  // 确认收货
  confirm: (orderNo) => request.put(`/orders/${orderNo}/confirm`)
};

// 支付相关
export const paymentAPI = {
  // 发起支付
  pay: (data) => request.post('/payment/pay', data)
};

// 管理员 - 商品管理
export const adminProductAPI = {
  // 获取商品列表
  getList: () => request.get('/admin/products'),
  
  // 获取商品详情
  getDetail: (id) => request.get(`/admin/products/${id}`),
  
  // 新增商品
  create: (data) => request.post('/admin/products', data),
  
  // 修改商品
  update: (id, data) => request.put(`/admin/products/${id}`, data),
  
  // 删除商品
  delete: (id) => request.delete(`/admin/products/${id}`),
  
  // 修改商品状态
  updateStatus: (id, status) => request.put(`/admin/products/${id}/status`, null, {
    params: { status }
  }),
  
  // 修改商品库存
  updateStock: (id, stock) => request.put(`/admin/products/${id}/stock`, null, {
    params: { stock }
  })
};

// 管理员 - 分类管理
export const adminCategoryAPI = {
  // 获取分类列表
  getList: () => request.get('/admin/categories'),
  
  // 获取分类详情
  getDetail: (id) => request.get(`/admin/categories/${id}`),
  
  // 新增分类
  create: (data) => request.post('/admin/categories', data),
  
  // 修改分类
  update: (id, data) => request.put(`/admin/categories/${id}`, data),
  
  // 删除分类
  delete: (id) => request.delete(`/admin/categories/${id}`)
};

// 管理员 - 订单管理
export const adminOrderAPI = {
  // 获取所有订单
  getList: () => request.get('/admin/orders'),
  
  // 按状态查询
  getByStatus: (status) => request.get(`/admin/orders/status/${status}`),
  
  // 获取订单详情
  getDetail: (orderNo) => request.get(`/admin/orders/${orderNo}`),
  
  // 订单发货
  ship: (orderNo) => request.put(`/admin/orders/${orderNo}/ship`)
};

// 管理员 - 用户管理
export const adminUserAPI = {
  // 获取用户列表
  getList: () => request.get('/admin/users'),
  
  // 获取用户详情
  getDetail: (id) => request.get(`/admin/users/${id}`),
  
  // 修改用户状态
  updateStatus: (id, status) => request.put(`/admin/users/${id}/status`, null, {
    params: { status }
  }),
  
  // 修改用户角色
  updateRole: (id, role) => request.put(`/admin/users/${id}/role`, null, {
    params: { role }
  })
};

// 导出axios实例（如果需要自定义请求）
export default request;

/**
 * 使用示例:
 * 
 * import { authAPI, productAPI, cartAPI } from './axios-setup';
 * 
 * // 登录
 * const loginUser = async () => {
 *   try {
 *     const response = await authAPI.login({
 *       username: 'testuser',
 *       password: '123456'
 *     });
 *     localStorage.setItem('accessToken', response.data.accessToken);
 *     router.push('/');
 *   } catch (error) {
 *     // 错误已在拦截器中处理
 *   }
 * };
 * 
 * // 获取商品列表
 * const fetchProducts = async () => {
 *   try {
 *     const response = await productAPI.getList();
 *     return response.data;
 *   } catch (error) {
 *     // 错误已在拦截器中处理
 *   }
 * };
 * 
 * // 添加到购物车
 * const addToCart = async (productId, quantity) => {
 *   try {
 *     await cartAPI.addItem({ productId, quantity });
 *     message.success('已加入购物车');
 *   } catch (error) {
 *     // 错误已在拦截器中处理
 *   }
 * };
 */