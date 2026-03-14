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

