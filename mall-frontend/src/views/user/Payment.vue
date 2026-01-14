<template>
  <div class="payment-page">
    <div class="container">
      <h2 class="page-title">订单支付</h2>

      <Loading v-if="loading" />

      <div v-else-if="order" class="payment-content">
        <!-- 订单信息 -->
        <div class="order-info-card">
          <div class="info-row">
            <span class="label">订单号：</span>
            <span class="value">{{ order.orderNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">订单金额：</span>
            <span class="amount">¥{{ formatPrice(order.totalAmount) }}</span>
          </div>
          <div class="info-row">
            <span class="label">收货人：</span>
            <span class="value">{{ order.receiverName }} {{ order.receiverPhone }}</span>
          </div>
        </div>

        <!-- 支付方式选择 -->
        <div class="payment-methods">
          <h3 class="section-title">选择支付方式</h3>

          <div class="methods-grid">
            <!-- 支付宝/微信 -->
            <div
              class="method-card disabled"
              @click="handleMethodClick('ALIPAY_WECHAT')"
            >
              <div class="method-icon">
                <el-icon :size="40"><Wallet /></el-icon>
              </div>
              <div class="method-info">
                <h4>支付宝/微信支付</h4>
                <p class="method-desc">使用支付宝或微信扫码支付</p>
                <el-tag type="info" size="small">暂未开通</el-tag>
              </div>
              <el-radio
                :model-value="selectedMethod"
                value="ALIPAY_WECHAT"
                disabled
              />
            </div>

            <!-- 信用卡/借记卡 -->
            <div
              class="method-card disabled"
              @click="handleMethodClick('CARD')"
            >
              <div class="method-icon">
                <el-icon :size="40"><CreditCard /></el-icon>
              </div>
              <div class="method-info">
                <h4>信用卡/借记卡</h4>
                <p class="method-desc">使用银行卡在线支付</p>
                <el-tag type="info" size="small">暂未开通</el-tag>
              </div>
              <el-radio
                :model-value="selectedMethod"
                value="CARD"
                disabled
              />
            </div>

            <!-- Mock 模拟支付 -->
            <div
              :class="['method-card', selectedMethod === 'MOCK' ? 'active' : '']"
              @click="handleMethodClick('MOCK')"
            >
              <div class="method-icon">
                <el-icon :size="40"><Tools /></el-icon>
              </div>
              <div class="method-info">
                <h4>模拟支付</h4>
                <p class="method-desc">测试环境模拟支付（立即成功）</p>
                <el-tag type="success" size="small">可用</el-tag>
              </div>
              <el-radio
                v-model="selectedMethod"
                value="MOCK"
              />
            </div>
          </div>
        </div>

        <!-- 支付按钮 -->
        <div class="payment-actions">
          <el-button
            size="large"
            @click="$router.back()"
          >
            返回
          </el-button>
          <el-button
            type="primary"
            size="large"
            :loading="paying"
            :disabled="!selectedMethod"
            @click="handlePay"
          >
            <span v-if="!paying">确认支付 ¥{{ formatPrice(order?.totalAmount || 0) }}</span>
            <span v-else>支付中...</span>
          </el-button>
        </div>
      </div>

      <!-- 订单不存在或已支付 -->
      <Empty
        v-else-if="!loading && !order"
        type="order"
        text="订单不存在或已支付"
      >
        <template #action>
          <el-button type="primary" @click="$router.push('/orders')">
            返回订单列表
          </el-button>
        </template>
      </Empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail } from '@/api/order'
import { pay } from '@/api/payment'
import { formatPrice } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Wallet, CreditCard, Tools } from '@element-plus/icons-vue'
import Loading from '@/components/common/Loading.vue'
import Empty from '@/components/common/Empty.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const paying = ref(false)
const order = ref(null)
const selectedMethod = ref('MOCK') // 默认选中 MOCK

// 获取订单详情
const fetchOrderDetail = async () => {
  const orderNo = route.params.orderNo
  if (!orderNo) {
    ElMessage.error('订单号不存在')
    router.push('/orders')
    return
  }

  loading.value = true
  try {
    order.value = await getOrderDetail(orderNo)

    // 检查订单状态
    if (order.value.status !== 'UNPAID') {
      ElMessage.warning('订单已支付或已取消')
      router.push(`/orders/${orderNo}`)
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
    router.push('/orders')
  } finally {
    loading.value = false
  }
}

// 支付方式点击处理
const handleMethodClick = (method) => {
  if (method === 'MOCK') {
    selectedMethod.value = method
  } else {
    ElMessage.info('该支付方式暂未开通，敬请期待')
  }
}

// 处理支付
const handlePay = async () => {
  if (!selectedMethod.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认使用${selectedMethod.value === 'MOCK' ? '模拟支付' : ''}支付 ¥${formatPrice(order.value.totalAmount)} 吗？`,
      '支付确认',
      {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    paying.value = true

    // 调用支付 API
    const result = await pay({
      orderNo: order.value.orderNo,
      paymentMethod: selectedMethod.value
    })

    if (result.paymentStatus === 'SUCCESS') {
      ElMessage.success('支付成功！')

      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push(`/orders/${order.value.orderNo}`)
      }, 1500)
    } else {
      ElMessage.error(result.message || '支付失败，请重试')
      paying.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
      ElMessage.error('支付失败，请重试')
    }
    paying.value = false
  }
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.payment-page {
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
  max-width: 800px;
  margin: 0 auto;
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

.payment-methods {
  background: $bg-color;
  border-radius: $border-radius-base;
  padding: $spacing-xl;
  margin-bottom: $spacing-xl;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .section-title {
    font-size: 18px;
    color: $text-primary;
    margin: 0 0 $spacing-lg 0;
  }

  .methods-grid {
    display: flex;
    flex-direction: column;
    gap: $spacing-md;
  }

  .method-card {
    display: flex;
    align-items: center;
    gap: $spacing-lg;
    padding: $spacing-lg;
    border: 2px solid $border-light;
    border-radius: $border-radius-base;
    cursor: pointer;
    transition: all 0.3s;
    background: $bg-color;

    &:hover:not(.disabled) {
      border-color: $primary-color;
      box-shadow: 0 4px 12px rgba($primary-color, 0.1);
    }

    &.active {
      border-color: $primary-color;
      background: rgba($primary-color, 0.02);
      box-shadow: 0 4px 12px rgba($primary-color, 0.15);
    }

    &.disabled {
      cursor: not-allowed;
      opacity: 0.6;

      .method-icon {
        color: $text-placeholder;
      }
    }

    .method-icon {
      color: $primary-color;
      flex-shrink: 0;
    }

    .method-info {
      flex: 1;

      h4 {
        font-size: 16px;
        color: $text-primary;
        margin: 0 0 $spacing-xs 0;
      }

      .method-desc {
        font-size: 13px;
        color: $text-secondary;
        margin: 0 0 $spacing-xs 0;
      }
    }

    .el-radio {
      flex-shrink: 0;
    }
  }
}

.payment-actions {
  display: flex;
  justify-content: center;
  gap: $spacing-lg;
  padding: $spacing-xl 0;

  .el-button {
    min-width: 150px;
  }
}

@include mobile {
  .payment-content {
    padding: 0 $spacing-md;
  }

  .order-info-card,
  .payment-methods {
    padding: $spacing-lg;
  }

  .methods-grid {
    .method-card {
      flex-direction: column;
      text-align: center;

      .method-info {
        text-align: center;
      }
    }
  }

  .payment-actions {
    flex-direction: column;

    .el-button {
      width: 100%;
    }
  }
}
</style>
