/**
 * 地址相关 API
 */
import request from './request'

/**
 * 获取地址列表
 */
export const getAddresses = () => {
  return request({
    url: '/addresses',
    method: 'GET'
  })
}

/**
 * 添加地址
 * @param {Object} data - 地址信息
 */
export const addAddress = (data) => {
  return request({
    url: '/addresses',
    method: 'POST',
    data
  })
}

/**
 * 更新地址
 * @param {number} id - 地址 ID
 * @param {Object} data - 地址信息
 */
export const updateAddress = (id, data) => {
  return request({
    url: `/addresses/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除地址
 * @param {number} id - 地址 ID
 */
export const deleteAddress = (id) => {
  return request({
    url: `/addresses/${id}`,
    method: 'DELETE'
  })
}

/**
 * 设置默认地址
 * @param {number} id - 地址 ID
 */
export const setDefaultAddress = (id) => {
  return request({
    url: `/addresses/${id}/default`,
    method: 'PUT'
  })
}
