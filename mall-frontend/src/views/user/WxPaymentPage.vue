<template>
  <div class="wxpay-page">
    <div class="container">
      <Loading v-if="loading" />

      <!-- 支付成功 -->
      <div v-else-if="paymentSuccess" class="payment-result">
        <el-result type="success" title="支付成功" sub-title="您的订单已支付成功！">
          <template #extra>
            <el-button type="primary" size="large" @click="goToOrderDetail">
              查看订单
            </el-button>
            <el-button size="large" @click="goToOrderList">
              返回订单列表
            </el-button>
          </template>
        </el-result>
      </div>

      <!-- 支付失败 -->
      <div v-else-if="paymentFailed" class="payment-result">
        <el-result type="error" :title="errorTitle" :sub-title="errorMessage">
          <template #extra>
            <el-button type="primary" size="large" @click="retryPayment">
              重新支付
            </el-button>
            <el-button size="large" @click="goToOrderList">
              返回订单列表
            </el-button>
          </template>
        </el-result>
      </div>

      <!-- 支付中 -->
      <div v-else-if="paymentInfo" class="payment-content">
        <h2 class="page-title">微信扫码支付</h2>

        <!-- 订单信息 -->
        <div class="order-info-card">
          <div class="info-row">
            <span class="label">订单号：</span>
            <span class="value">{{ paymentInfo.orderNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">支付金额：</span>
            <span class="amount">¥{{ formatPrice(paymentInfo.amount) }}</span>
          </div>
          <div class="info-row">
            <span class="label">商品描述：</span>
            <span class="value">{{ description }}</span>
          </div>
        </div>

        <!-- 二维码展示区 -->
        <div class="qrcode-section">
          <div v-if="qrcodeUrl" class="qrcode-container">
            <img :src="qrcodeUrl" alt="微信支付二维码" class="qrcode-img" />
            <p class="qrcode-tip">请使用微信扫码支付</p>
          </div>
          <div v-else class="qrcode-error">
            <el-icon :size="60" color="#f56c6c"><Warning /></el-icon>
            <p>二维码生成失败</p>
            <el-button type="primary" size="small" @click="regenerateQrcode">
              重新生成
            </el-button>
          </div>

          <!-- 倒计时 -->
          <div v-if="countdown > 0" class="countdown">
            <el-icon><Timer /></el-icon>
            <span>剩余时间：{{ formatCountdown }}</span>
          </div>
          <div v-else class="timeout-warning">
            <el-icon><WarningFilled /></el-icon>
            <span>支付二维码已过期</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="payment-actions">
          <el-button size="large" @click="cancelPayment">
            取消支付
          </el-button>
          <el-button
            type="primary"
            size="large"
            :disabled="countdown <= 0"
            @click="regenerateQrcode"
          >
            刷新二维码
          </el-button>
        </div>

        <!-- 支付说明 -->
        <div class="payment-notice">
          <el-alert
            title="支付提示"
            type="info"
            :closable="false"
            show-icon
          >
            <ul>
              <li>请在15分钟内完成支付，超时后二维码将失效</li>
              <li>支付成功后，页面将自动跳转到订单详情</li>
              <li>如遇问题，请点击"刷新二维码"重新生成</li>
            </ul>
          </el-alert>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail } from '@/api/order'
import { createWxPayment, queryWxPayment } from '@/api/payment'
import { formatPrice } from '@/utils/format'
import { ElMessage } from 'element-plus'
import { Warning, Timer, WarningFilled } from '@element-plus/icons-vue'
import QRCode from 'qrcode'
import Loading from '@/components/common/Loading.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const paymentInfo = ref(null)
const qrcodeUrl = ref('')
const countdown = ref(900) // 15分钟倒计时
const pollingTimer = ref(null)
const countdownTimer = ref(null)
const paymentSuccess = ref(false)
const paymentFailed = ref(false)
const errorTitle = ref('支付失败')
const errorMessage = ref('支付过程中出现错误，请重试')
const description = ref('')
const pollingCount = ref(0) // 轮询次数
const MAX_POLLING_COUNT = 300 // 最多轮询300次（15分钟）

// 格式化倒计时
const formatCountdown = computed(() => {
  const minutes = Math.floor(countdown.value / 60)
  const seconds = countdown.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

// 生成二维码
const generateQrcode = async (codeUrl) => {
  try {
    qrcodeUrl.value = await QRCode.toDataURL(codeUrl, {
      width: 300,
      margin: 2,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      }
    })
  } catch (error) {
    console.error('二维码生成失败:', error)
    ElMessage.error('二维码生成失败，请重试')
  }
}

// 创建支付订单
const createPayment = async () => {
  loading.value = true
  try {
    const orderNo = route.params.orderNo
    if (!orderNo) {
      throw new Error('订单号不存在')
    }

    // 获取订单详情
    const order = await getOrderDetail(orderNo)
    description.value = `订单 ${order.orderNo}`

    // 检查订单状态
    if (order.status !== 'UNPAID') {
      ElMessage.warning('订单已支付或已取消')
      router.push(`/orders/${orderNo}`)
      return
    }

    // 创建微信支付订单
    const payment = await createWxPayment(
      order.orderNo,
      order.totalAmount,
      description.value
    )

    paymentInfo.value = payment

    // 生成二维码
    if (payment.codeUrl) {
      await generateQrcode(payment.codeUrl)
    } else {
      throw new Error('未获取到支付二维码链接')
    }

    // 启动轮询和倒计时
    startPolling()
    startCountdown()
  } catch (error) {
    console.error('创建支付失败:', error)
    paymentFailed.value = true
    errorTitle.value = '创建支付失败'
    errorMessage.value = error.message || '无法创建支付订单，请返回重试'
  } finally {
    loading.value = false
  }
}

// 查询支付状态
const checkPaymentStatus = async () => {
  try {
    if (!paymentInfo.value?.paymentNo) {
      return
    }

    const result = await queryWxPayment(paymentInfo.value.paymentNo)

    if (result.paymentStatus === 'SUCCESS') {
      // 支付成功
      stopPolling()
      stopCountdown()
      paymentSuccess.value = true
      ElMessage.success('支付成功！')
    } else if (result.paymentStatus === 'FAILED' || result.paymentStatus === 'CLOSED') {
      // 支付失败或关闭
      stopPolling()
      stopCountdown()
      paymentFailed.value = true
      errorTitle.value = '支付失败'
      errorMessage.value = '支付已失败或已关闭，请重新发起支付'
    }

    pollingCount.value++

    // 超过最大轮询次数
    if (pollingCount.value >= MAX_POLLING_COUNT) {
      stopPolling()
      stopCountdown()
      paymentFailed.value = true
      errorTitle.value = '支付超时'
      errorMessage.value = '支付已超时，请重新发起支付'
    }
  } catch (error) {
    console.error('查询支付状态失败:', error)
    // 查询失败不中断轮询，继续尝试
  }
}

// 启动轮询
const startPolling = () => {
  pollingCount.value = 0
  pollingTimer.value = setInterval(() => {
    checkPaymentStatus()
  }, 3000) // 每3秒查询一次
}

// 停止轮询
const stopPolling = () => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

// 启动倒计时
const startCountdown = () => {
  countdown.value = 900
  countdownTimer.value = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      stopCountdown()
      stopPolling()
    }
  }, 1000)
}

// 停止倒计时
const stopCountdown = () => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
}

// 重新生成二维码
const regenerateQrcode = async () => {
  // 清除定时器
  stopPolling()
  stopCountdown()

  // 重置状态
  paymentInfo.value = null
  qrcodeUrl.value = ''
  paymentSuccess.value = false
  paymentFailed.value = false

  // 重新创建支付
  await createPayment()
}

// 重试支付
const retryPayment = () => {
  regenerateQrcode()
}

// 取消支付
const cancelPayment = () => {
  stopPolling()
  stopCountdown()
  router.push('/orders')
}

// 跳转到订单详情
const goToOrderDetail = () => {
  router.push(`/orders/${route.params.orderNo}`)
}

// 跳转到订单列表
const goToOrderList = () => {
  router.push('/orders')
}

onMounted(() => {
  createPayment()
})

onUnmounted(() => {
  stopPolling()
  stopCountdown()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.wxpay-page {
  padding: $spacing-xl 0;
  min-height: calc(100vh - 60px);
  background: $bg-page;
}

.page-title {
  font-size: 24px;
  color: $text-primary;
  margin-bottom: $spacing-xl;
  text-align: center;
}

.payment-content {
  max-width: 600px;
  margin: 0 auto;
}

.payment-result {
  max-width: 600px;
  margin: 0 auto;
  background: $bg-color;
  border-radius: $border-radius-base;
  padding: $spacing-xl;
}

.order-info-card {
  background: $bg-color;
  border-radius: $border-radius-base;
  padding: $spacing-xl;
  margin-bottom: $spacing-xl;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .info-row {
    display: flex;
    align-items: center;
    padding: $spacing-sm 0;

    &:not(:last-child) {
      border-bottom: 1px solid $border-lighter;
    }

    .label {
      width: 100px;
      color: $text-secondary;
      font-size: 14px;
    }

    .value {
      flex: 1;
      color: $text-primary;
      font-size: 14px;
    }

    .amount {
      flex: 1;
      color: $danger-color;
      font-size: 24px;
      font-weight: bold;
    }
  }
}

.qrcode-section {
  background: $bg-color;
  border-radius: $border-radius-base;
  padding: $spacing-xl;
  margin-bottom: $spacing-xl;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .qrcode-container {
    text-align: center;

    .qrcode-img {
      width: 300px;
      height: 300px;
      border: 2px solid $border-light;
      border-radius: $border-radius-base;
      margin-bottom: $spacing-md;
    }

    .qrcode-tip {
      font-size: 16px;
      color: $text-regular;
      margin: 0;
    }
  }

  .qrcode-error {
    text-align: center;
    padding: $spacing-xl 0;

    p {
      font-size: 16px;
      color: $text-secondary;
      margin: $spacing-md 0;
    }
  }

  .countdown {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-xs;
    margin-top: $spacing-lg;
    font-size: 16px;
    color: $primary-color;
    font-weight: 500;

    .el-icon {
      font-size: 20px;
    }
  }

  .timeout-warning {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-xs;
    margin-top: $spacing-lg;
    font-size: 16px;
    color: $danger-color;
    font-weight: 500;

    .el-icon {
      font-size: 20px;
    }
  }
}

.payment-actions {
  display: flex;
  justify-content: center;
  gap: $spacing-lg;
  margin-bottom: $spacing-xl;

  .el-button {
    min-width: 150px;
  }
}

.payment-notice {
  background: $bg-color;
  border-radius: $border-radius-base;
  padding: $spacing-lg;

  ul {
    margin: $spacing-sm 0 0 0;
    padding-left: $spacing-lg;
    color: $text-regular;
    font-size: 14px;
    line-height: 1.8;

    li {
      margin-bottom: $spacing-xs;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

@include mobile {
  .payment-content,
  .payment-result {
    padding: 0 $spacing-md;
  }

  .order-info-card,
  .qrcode-section,
  .payment-notice {
    padding: $spacing-lg;
  }

  .qrcode-section .qrcode-container .qrcode-img {
    width: 250px;
    height: 250px;
  }

  .payment-actions {
    flex-direction: column;

    .el-button {
      width: 100%;
    }
  }
}
</style>
