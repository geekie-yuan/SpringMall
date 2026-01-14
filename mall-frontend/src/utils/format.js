/**
 * 格式化工具函数
 */
import dayjs from 'dayjs'

/**
 * 格式化价格
 * @param {number} price - 价格
 * @param {number} decimals - 小数位数，默认 2 位
 * @returns {string} 格式化后的价格字符串
 */
export const formatPrice = (price, decimals = 2) => {
  if (price === null || price === undefined) return '0.00'
  return Number(price).toFixed(decimals)
}

/**
 * 格式化日期时间
 * @param {string|Date} date - 日期
 * @param {string} format - 格式，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期字符串
 */
export const formatDate = (date, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!date) return ''
  return dayjs(date).format(format)
}

/**
 * 格式化相对时间（多久之前）
 */
export const formatRelativeTime = (date) => {
  if (!date) return ''
  const now = dayjs()
  const target = dayjs(date)
  const diff = now.diff(target, 'second')

  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)} 小时前`
  if (diff < 2592000) return `${Math.floor(diff / 86400)} 天前`
  return formatDate(date, 'YYYY-MM-DD')
}

/**
 * 格式化文件大小
 */
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

/**
 * 格式化数字（千分位分隔）
 */
export const formatNumber = (num) => {
  if (num === null || num === undefined) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 隐藏手机号中间四位
 */
export const hideMobile = (mobile) => {
  if (!mobile) return ''
  return mobile.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 隐藏邮箱部分字符
 */
export const hideEmail = (email) => {
  if (!email) return ''
  const [name, domain] = email.split('@')
  const visibleLength = Math.max(1, Math.floor(name.length / 3))
  const hiddenName = name.slice(0, visibleLength) + '***' + name.slice(-1)
  return hiddenName + '@' + domain
}
