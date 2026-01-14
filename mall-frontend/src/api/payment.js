/**
 * 支付相关 API
 */
import request from './request'

/**
 * 发起支付
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
