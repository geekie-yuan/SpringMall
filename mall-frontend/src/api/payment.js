/**
 * 支付相关 API
 */
import request from './request'

/**
 * 创建支付宝支付
 * @param {string} orderNo - 订单号
 */
export const createAlipayPayment = (orderNo) => {
  return request({
    url: '/payment/alipay/create',
    method: 'POST',
    data: { orderNo }
  })
}

/**
 * 创建微信Native支付订单
 * @param {string} orderNo - 订单号
 * @param {number} amount - 支付金额（元）
 * @param {string} description - 商品描述
 */
export const createWxPayment = (orderNo, amount, description) => {
  return request({
    url: '/payment/wechat/native',
    method: 'POST',
    data: { orderNo, amount, description }
  })
}

/**
 * 查询微信支付状态
 * @param {string} paymentNo - 支付单号
 */
export const queryWxPayment = (paymentNo) => {
  return request({
    url: `/payment/wechat/${paymentNo}`,
    method: 'GET'
  })
}

/**
 * 申请微信退款
 * @param {string} paymentNo - 支付单号
 * @param {number} refundAmount - 退款金额（元）
 * @param {string} reason - 退款原因
 */
export const applyWxRefund = (paymentNo, refundAmount, reason) => {
  return request({
    url: '/payment/wechat/refund',
    method: 'POST',
    data: { paymentNo, refundAmount, reason }
  })
}

/**
 * 查询支付状态
 * @param {string} paymentNo - 支付单号
 */
export const getPaymentStatus = (paymentNo) => {
  return request({
    url: `/payment/${paymentNo}`,
    method: 'GET'
  })
}

/**
 * 发起支付（旧接口，用于模拟支付）
 * @param {Object} data - 支付信息 { orderNo, paymentMethod }
 */
export const pay = (data) => {
  return request({
    url: '/payment/pay',
    method: 'POST',
    data
  })
}

/**
 * 支付回调
 * @param {Object} data - 回调数据
 */
export const paymentCallback = (data) => {
  return request({
    url: '/payment/callback',
    method: 'POST',
    data
  })
}

/**
 * 创建 Stripe 支付（Checkout Session）
 * @param {string} orderNo - 订单编号
 * @returns {Promise} 支付信息（包含 sessionUrl）
 */
export const createStripePayment = (orderNo) => {
  return request({
    url: '/payment/stripe/create',
    method: 'POST',
    data: { orderNo }
  })
}

/**
 * 查询 Stripe 支付状态
 * @param {string} paymentNo - 支付编号
 * @returns {Promise} 支付信息
 */
export const queryStripePayment = (paymentNo) => {
  return request({
    url: `/payment/stripe/${paymentNo}`,
    method: 'GET'
  })
}

