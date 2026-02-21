<template>
  <div class="payment-result-page">
    <div class="container">
      <Loading v-if="loading" />

      <div v-else class="result-container">
        <el-result
          :icon="resultIcon"
          :title="resultTitle"
          :sub-title="resultMessage"
        >
          <template #icon>
            <el-icon :size="80" :color="iconColor">
              <component :is="resultIconComponent" />
            </el-icon>
          </template>

          <template #extra>
            <!-- 支付信息详情 -->
            <div v-if="paymentInfo" class="payment-info">
              <el-card>
                <div class="info-row">
                  <span class="label">订单号：</span>
                  <span class="value">{{ paymentInfo.orderNo }}</span>
                </div>
                <div class="info-row">
                  <span class="label">支付单号：</span>
                  <span class="value">{{ paymentInfo.paymentNo }}</span>
                </div>
                <div class="info-row">
                  <span class="label">订单金额：</span>
                  <span class="amount">
                    ¥{{ formatPrice(paymentInfo.amount) }}
                  </span>
                </div>
                <div class="info-row">
                  <span class="label">支付方式：</span>
                  <span class="value">Stripe (币种由 Stripe 自动选择)</span>
                </div>
                <div v-if="paymentInfo.status === 'SUCCESS'" class="info-row">
                  <span class="label">支付状态：</span>
                  <el-tag type="success">支付成功</el-tag>
                </div>
                <div v-else-if="paymentInfo.status === 'FAILED'" class="info-row">
                  <span class="label">支付状态：</span>
                  <el-tag type="danger">支付失败</el-tag>
                </div>
                <div v-else class="info-row">
                  <span class="label">支付状态：</span>
                  <el-tag type="warning">处理中</el-tag>
                </div>
              </el-card>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button
                v-if="paymentInfo?.status === 'SUCCESS'"
                type="primary"
                size="large"
                @click="handleGoToOrder"
              >
                查看订单
              </el-button>

              <el-button
                v-else-if="paymentInfo?.status === 'FAILED'"
                type="primary"
                size="large"
                @click="handleRetry"
              >
                重新支付
              </el-button>

              <el-button
                v-if="paymentInfo?.status !== 'PENDING'"
                size="large"
                @click="handleGoToOrderList"
              >
                返回订单列表
              </el-button>
            </div>

            <!-- 轮询提示 -->
            <div v-if="paymentInfo?.status === 'PENDING'" class="polling-tip">
              <el-icon class="is-loading">
                <Loading />
              </el-icon>
              <span>正在查询支付结果，请稍候... ({{ pollingCount }}/{{ maxPolling }})</span>
            </div>
          </template>
        </el-result>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { SuccessFilled, CircleCloseFilled, WarningFilled, Loading } from '@element-plus/icons-vue'
import { queryStripePayment } from '@/api/payment'
import { formatPrice } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const paymentInfo = ref(null)
const pollingTimer = ref(null)
const pollingCount = ref(0)
const maxPolling = 10

// 结果图标组件
const resultIconComponent = computed(() => {
  if (!paymentInfo.value) return WarningFilled

  switch (paymentInfo.value.status) {
    case 'SUCCESS':
      return SuccessFilled
    case 'FAILED':
      return CircleCloseFilled
    case 'PENDING':
      return WarningFilled
    default:
      return WarningFilled
  }
})

// 结果图标类型
const resultIcon = computed(() => {
  if (!paymentInfo.value) return 'warning'

  switch (paymentInfo.value.status) {
    case 'SUCCESS':
      return 'success'
    case 'FAILED':
      return 'error'
    case 'PENDING':
      return 'warning'
    default:
      return 'warning'
  }
})

// 图标颜色
const iconColor = computed(() => {
  if (!paymentInfo.value) return '#E6A23C'

  switch (paymentInfo.value.status) {
    case 'SUCCESS':
      return '#67C23A'
    case 'FAILED':
      return '#F56C6C'
    case 'PENDING':
      return '#E6A23C'
    default:
      return '#E6A23C'
  }
})

// 结果标题
const resultTitle = computed(() => {
  if (!paymentInfo.value) return '查询支付结果中...'

  switch (paymentInfo.value.status) {
    case 'SUCCESS':
      return '支付成功！'
    case 'FAILED':
      return '支付失败'
    case 'PENDING':
      return '支付处理中...'
    default:
      return '支付状态未知'
  }
})

// 结果消息
const resultMessage = computed(() => {
  if (!paymentInfo.value) return '正在查询您的支付结果，请稍候...'

  switch (paymentInfo.value.status) {
    case 'SUCCESS':
      return '您的订单已支付成功，我们将尽快为您发货'
    case 'FAILED':
      return '支付未能完成，您可以重新尝试支付或选择其他支付方式'
    case 'PENDING':
      return '您的支付正在处理中，请耐心等待...'
    default:
      return '无法确认支付状态，请联系客服'
  }
})

// 查询支付状态
const fetchPaymentStatus = async () => {
  const paymentNo = route.query.paymentNo

  if (!paymentNo) {
    ElMessage.error('支付单号不存在')
    router.push('/orders')
    return
  }

  try {
    loading.value = true
    const result = await queryStripePayment(paymentNo)
    paymentInfo.value = result

    // 如果状态是 PENDING，启动轮询
    if (result.status === 'PENDING' && pollingCount.value < maxPolling) {
      startPolling()
    } else if (result.status === 'PENDING' && pollingCount.value >= maxPolling) {
      // 达到最大轮询次数
      ElMessage.warning('支付处理超时，请稍后在订单列表中查看支付状态')
      stopPolling()
    } else {
      // 支付已完成（成功或失败）
      stopPolling()
    }
  } catch (error) {
    console.error('查询支付状态失败:', error)
    ElMessage.error('查询支付状态失败，请重试')
  } finally {
    loading.value = false
  }
}

// 启动轮询
const startPolling = () => {
  stopPolling() // 先清除可能存在的定时器

  pollingTimer.value = setInterval(() => {
    pollingCount.value++
    fetchPaymentStatus()
  }, 3000) // 每 3 秒查询一次
}

// 停止轮询
const stopPolling = () => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

// 查看订单
const handleGoToOrder = () => {
  if (paymentInfo.value?.orderNo) {
    router.push(`/orders/${paymentInfo.value.orderNo}`)
  }
}

// 重新支付
const handleRetry = () => {
  if (paymentInfo.value?.orderNo) {
    router.push(`/payment/${paymentInfo.value.orderNo}`)
  }
}

// 返回订单列表
const handleGoToOrderList = () => {
  router.push('/orders')
}

onMounted(() => {
  fetchPaymentStatus()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.payment-result-page {
  min-height: calc(100vh - 60px);
  background: $bg-page;
  padding: $spacing-xl 0;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 $spacing-md;
}

.result-container {
  background: $bg-color;
  border-radius: $border-radius-base;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: $spacing-xl;
}

// 支付信息卡片
.payment-info {
  margin: $spacing-xl auto;
  max-width: 500px;

  .el-card {
    :deep(.el-card__body) {
      padding: $spacing-lg;
    }
  }

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
      word-break: break-all;
    }

    .amount {
      flex: 1;
      color: $danger-color;
      font-size: 18px;
      font-weight: bold;
    }
  }
}

// 操作按钮
.action-buttons {
  display: flex;
  justify-content: center;
  gap: $spacing-lg;
  margin-top: $spacing-lg;

  .el-button {
    min-width: 150px;
  }
}

// 轮询提示
.polling-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  margin-top: $spacing-xl;
  color: $text-secondary;
  font-size: 14px;

  .el-icon {
    font-size: 16px;
  }
}

// 响应式
@include mobile {
  .container {
    padding: 0 $spacing-sm;
  }

  .result-container {
    padding: $spacing-lg;
  }

  .payment-info {
    .info-row {
      flex-direction: column;
      align-items: flex-start;
      gap: $spacing-xs;

      .label {
        width: 100%;
      }
    }
  }

  .action-buttons {
    flex-direction: column;

    .el-button {
      width: 100%;
    }
  }

  .polling-tip {
    flex-direction: column;
    text-align: center;
  }
}
</style>
