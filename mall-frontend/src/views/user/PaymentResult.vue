<template>
  <div class="payment-result-page">
    <div class="container">
      <Loading v-if="loading" text="正在查询支付结果..." />

      <div v-else class="result-content">
        <!-- 支付成功 -->
        <el-result
          v-if="paymentStatus === 'SUCCESS'"
          icon="success"
          title="支付成功！"
          :sub-title="`支付单号：${paymentNo}`"
        >
          <template #extra>
            <div class="payment-info">
              <div class="info-item">
                <span class="label">订单号：</span>
                <span class="value">{{ paymentData?.orderNo }}</span>
              </div>
              <div class="info-item">
                <span class="label">支付金额：</span>
                <span class="amount">¥{{ formatPrice(paymentData?.amount) }}</span>
              </div>
              <div class="info-item">
                <span class="label">支付时间：</span>
                <span class="value">{{ formatDate(paymentData?.createdAt) }}</span>
              </div>
              <div v-if="paymentData?.tradeNo" class="info-item">
                <span class="label">支付宝交易号：</span>
                <span class="value">{{ paymentData.tradeNo }}</span>
              </div>
            </div>

            <div class="actions">
              <el-button
                type="primary"
                size="large"
                @click="goToOrderDetail"
              >
                查看订单
              </el-button>
              <el-button
                size="large"
                @click="$router.push('/orders')"
              >
                返回订单列表
              </el-button>
            </div>
          </template>
        </el-result>

        <!-- 支付失败 -->
        <el-result
          v-else-if="paymentStatus === 'FAILED'"
          icon="error"
          title="支付失败"
          sub-title="很抱歉，您的支付未能成功完成"
        >
          <template #extra>
            <div class="payment-info">
              <div class="info-item">
                <span class="label">订单号：</span>
                <span class="value">{{ paymentData?.orderNo }}</span>
              </div>
              <div class="info-item">
                <span class="label">失败原因：</span>
                <span class="value error">{{ failureReason || '支付未完成' }}</span>
              </div>
            </div>

            <div class="actions">
              <el-button
                type="primary"
                size="large"
                @click="retryPayment"
              >
                重新支付
              </el-button>
              <el-button
                size="large"
                @click="$router.push('/orders')"
              >
                返回订单列表
              </el-button>
            </div>
          </template>
        </el-result>

        <!-- 支付处理中 -->
        <el-result
          v-else-if="paymentStatus === 'PENDING'"
          icon="info"
          title="支付处理中"
          :sub-title="`正在确认支付结果，已自动查询 ${pollCount} 次`"
        >
          <template #extra>
            <div class="payment-info">
              <div class="info-item">
                <span class="label">订单号：</span>
                <span class="value">{{ paymentData?.orderNo }}</span>
              </div>
              <div class="info-item">
                <span class="label">支付金额：</span>
                <span class="amount">¥{{ formatPrice(paymentData?.amount) }}</span>
              </div>
            </div>

            <el-alert
              type="info"
              :closable="false"
              show-icon
              class="pending-tip"
            >
              <template #title>
                <div>支付确认中，请稍候...</div>
                <div class="tip-text">如长时间未更新，请手动刷新页面</div>
              </template>
            </el-alert>

            <div class="actions">
              <el-button
                size="large"
                :loading="loading"
                @click="checkPaymentStatus"
              >
                立即查询
              </el-button>
              <el-button
                size="large"
                @click="$router.push('/orders')"
              >
                返回订单列表
              </el-button>
            </div>
          </template>
        </el-result>

        <!-- 支付已取消 -->
        <el-result
          v-else-if="paymentStatus === 'CANCELLED'"
          icon="warning"
          title="支付已取消"
          sub-title="您已取消本次支付"
        >
          <template #extra>
            <div class="actions">
              <el-button
                type="primary"
                size="large"
                @click="retryPayment"
              >
                重新支付
              </el-button>
              <el-button
                size="large"
                @click="$router.push('/orders')"
              >
                返回订单列表
              </el-button>
            </div>
          </template>
        </el-result>

        <!-- 无支付信息 -->
        <el-result
          v-else
          icon="error"
          title="未找到支付信息"
          sub-title="支付单号不存在或已失效"
        >
          <template #extra>
            <el-button
              type="primary"
              size="large"
              @click="$router.push('/orders')"
            >
              返回订单列表
            </el-button>
          </template>
        </el-result>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPaymentStatus } from '@/api/payment'
import { formatPrice, formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'
import Loading from '@/components/common/Loading.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const paymentNo = ref('')
const paymentStatus = ref('')
const paymentData = ref(null)
const failureReason = ref('')
const pollCount = ref(0)
const maxPollCount = 10 // 最多轮询 10 次
const pollInterval = 3000 // 轮询间隔 3 秒
let pollTimer = null

// 查询支付状态
const checkPaymentStatus = async () => {
  if (!paymentNo.value) {
    ElMessage.error('支付单号不存在')
    return
  }

  loading.value = true
  try {
    const data = await getPaymentStatus(paymentNo.value)
    paymentData.value = data
    paymentStatus.value = data.paymentStatus

    // 如果支付成功或失败，停止轮询
    if (data.paymentStatus === 'SUCCESS' || data.paymentStatus === 'FAILED') {
      stopPolling()

      if (data.paymentStatus === 'SUCCESS') {
        ElMessage.success('支付成功！')
      }
    }
  } catch (error) {
    console.error('查询支付状态失败:', error)
    ElMessage.error('查询支付状态失败')
    stopPolling()
  } finally {
    loading.value = false
  }
}

// 开始轮询
const startPolling = () => {
  stopPolling() // 先清除已有的定时器

  pollTimer = setInterval(async () => {
    pollCount.value++

    // 超过最大轮询次数，停止轮询
    if (pollCount.value >= maxPollCount) {
      stopPolling()
      ElMessage.warning('支付确认超时，请稍后手动刷新页面查看结果')
      return
    }

    await checkPaymentStatus()
  }, pollInterval)
}

// 停止轮询
const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// 跳转到订单详情
const goToOrderDetail = () => {
  if (paymentData.value?.orderNo) {
    router.push(`/orders/${paymentData.value.orderNo}`)
  } else {
    router.push('/orders')
  }
}

// 重新支付
const retryPayment = () => {
  if (paymentData.value?.orderNo) {
    router.push(`/payment/${paymentData.value.orderNo}`)
  } else {
    router.push('/orders')
  }
}

onMounted(async () => {
  // 从 URL 获取支付单号
  paymentNo.value = route.query.paymentNo

  if (!paymentNo.value) {
    ElMessage.error('支付单号不存在')
    router.push('/orders')
    return
  }

  // 首次查询
  await checkPaymentStatus()

  // 如果状态为 PENDING，开始轮询
  if (paymentStatus.value === 'PENDING') {
    startPolling()
  }
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.payment-result-page {
  padding: $spacing-xl 0;
  min-height: calc(100vh - 60px);
  background: $bg-page;
}

.result-content {
  max-width: 800px;
  margin: 0 auto;
  background: $bg-color;
  border-radius: $border-radius-base;
  padding: $spacing-xl;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .payment-info {
    background: $bg-page;
    border-radius: $border-radius-base;
    padding: $spacing-lg;
    margin: $spacing-lg 0;

    .info-item {
      display: flex;
      padding: $spacing-sm 0;
      line-height: 1.6;

      &:not(:last-child) {
        border-bottom: 1px solid $border-lighter;
      }

      .label {
        width: 120px;
        color: $text-secondary;
        font-size: 14px;
        flex-shrink: 0;
      }

      .value {
        flex: 1;
        color: $text-primary;
        font-size: 14px;
        word-break: break-all;

        &.error {
          color: $danger-color;
        }
      }

      .amount {
        flex: 1;
        color: $danger-color;
        font-size: 20px;
        font-weight: bold;
      }
    }
  }

  .pending-tip {
    margin: $spacing-lg 0;

    .tip-text {
      font-size: 13px;
      color: $text-secondary;
      margin-top: $spacing-xs;
    }
  }

  .actions {
    display: flex;
    justify-content: center;
    gap: $spacing-md;
    margin-top: $spacing-xl;

    @include mobile {
      flex-direction: column;

      .el-button {
        width: 100%;
      }
    }
  }
}

@include mobile {
  .payment-result-page {
    padding: $spacing-lg 0;
  }

  .result-content {
    margin: 0 $spacing-md;
    padding: $spacing-lg;

    .payment-info {
      padding: $spacing-md;

      .info-item {
        flex-direction: column;

        .label {
          width: auto;
          margin-bottom: $spacing-xs;
        }
      }
    }
  }
}
</style>
