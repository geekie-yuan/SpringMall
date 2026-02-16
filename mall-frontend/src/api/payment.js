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
