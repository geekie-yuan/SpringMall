<template>
  <div class="stripe-payment-page">
    <div class="container">
      <h2 class="page-title">
        <el-icon class="payment-icon">
          <CreditCard />
        </el-icon>
        安全支付
      </h2>

      <Loading v-if="loading" />

      <div v-else class="payment-container">
        <!-- 左侧：商品清单 -->
        <div class="left-section">
          <div v-if="orderItems.length > 0" class="product-list-section">
            <h3 class="section-title">
              商品清单
              <span class="item-count">{{ orderItems.length }} 件</span>
            </h3>

            <div class="product-items">
              <div
                v-for="item in orderItems"
                :key="item.id"
                class="product-item"
              >
                <div class="product-image">
                  <img
                    :src="item.productImage || '/placeholder.png'"
                    :alt="item.productName"
                    @error="handleImageError"
                  />
                </div>
                <div class="product-info">
                  <div class="product-name">{{ item.productName }}</div>
                  <div class="product-meta">
                    <span class="quantity">× {{ item.quantity }}</span>
                    <span class="unit-price">¥{{ formatPrice(item.unitPrice) }}</span>
                  </div>
                </div>
                <div class="product-total">
                  ¥{{ formatPrice(item.totalPrice) }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：订单摘要 -->
        <div class="right-section">
          <el-card class="summary-card">
            <h3 class="card-title">订单摘要</h3>

            <div class="summary-row">
              <span class="label">订单编号</span>
              <span class="value order-no">{{ orderNo }}</span>
            </div>

            <div class="summary-row">
              <span class="label">商品数量</span>
              <span class="value">{{ orderItems.length }} 件</span>
            </div>

            <el-divider />

            <div class="summary-row total-row">
              <span class="label">应付金额</span>
              <span class="total-amount">¥{{ formatPrice(originalAmount) }}</span>
            </div>

            <!-- Stripe 提示 -->
            <div class="currency-tip">
              <el-icon class="tip-icon">
                <InfoFilled />
              </el-icon>
              <span>Stripe 将根据您的地区自动显示本地货币</span>
            </div>

            <!-- 支付按钮 -->
            <div class="payment-actions">
              <el-button
                type="primary"
                size="large"
                :loading="submitting"
                @click="handleSubmit"
                class="pay-button"
              >
                <el-icon v-if="!submitting" class="button-icon">
                  <Lock />
                </el-icon>
                {{ submitting ? '跳转中...' : '安全支付' }}
              </el-button>
              <el-button
                size="large"
                @click="handleCancel"
                class="cancel-button"
              >
                取消订单
              </el-button>
            </div>

            <!-- 安全标识 -->
            <div class="security-badges">
              <div class="badge">
                <el-icon><Lock /></el-icon>
                <span>SSL 加密</span>
              </div>
              <div class="badge">
                <el-icon><CircleCheck /></el-icon>
                <span>PCI 认证</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CreditCard, Lock, InfoFilled, CircleCheck } from '@element-plus/icons-vue'
import { createStripePayment } from '@/api/payment'
import { getOrderDetail } from '@/api/order'
import { formatPrice } from '@/utils/format'
import Loading from '@/components/common/Loading.vue'

const route = useRoute()
const router = useRouter()

const orderNo = ref(route.query.orderNo || '')
const loading = ref(false)
const submitting = ref(false)
const originalAmount = ref(0)
const orderItems = ref([])

const initPayment = async () => {
  if (!orderNo.value) {
    ElMessage.error('订单号不存在')
    router.push('/orders')
    return
  }

  loading.value = true
  try {
    // 查询订单详情（包含商品列表）
    const detail = await getOrderDetail(orderNo.value)
    originalAmount.value = detail.totalAmount
    orderItems.value = detail.items || []
  } catch (error) {
    console.error('初始化失败:', error)
    ElMessage.error('初始化支付失败，请重试')
    router.push(`/payment/${orderNo.value}`)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (submitting.value) return

  submitting.value = true
  try {
    const result = await createStripePayment(orderNo.value)

    // 直接重定向到 Stripe Checkout 页面
    window.location.href = result.sessionUrl
  } catch (error) {
    console.error('创建支付失败:', error)
    ElMessage.error('创建支付失败，请重试')
    submitting.value = false
  }
}

const handleCancel = () => {
  router.push(`/payment/${orderNo.value}`)
}

const handleImageError = (e) => {
  // 图片加载失败时使用默认图片
  e.target.src = '/placeholder.png'
}

onMounted(() => {
  initPayment()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.stripe-payment-page {
  min-height: calc(100vh - 60px);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: $spacing-xl 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 $spacing-md;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: $spacing-xl;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-md;

  .payment-icon {
    font-size: 36px;
  }
}

.payment-container {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: $spacing-xl;
  align-items: start;
}

// 左侧：商品清单
.left-section {
  background: #ffffff;
  border-radius: 16px;
  padding: $spacing-xl;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-lg;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .item-count {
    font-size: 14px;
    color: $text-secondary;
    font-weight: normal;
    background: #f3f4f6;
    padding: 4px 12px;
    border-radius: 12px;
  }
}

.product-items {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.product-item {
  display: flex;
  align-items: center;
  padding: $spacing-lg;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    border-color: $primary-color;
  }
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f9fafb;
  border: 1px solid #e5e7eb;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;
  }

  &:hover img {
    transform: scale(1.05);
  }
}

.product-info {
  flex: 1;
  margin-left: $spacing-lg;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  font-size: 14px;

  .quantity {
    color: $text-secondary;
    background: #f3f4f6;
    padding: 2px 8px;
    border-radius: 6px;
    font-weight: 500;
  }

  .unit-price {
    color: $text-secondary;
  }
}

.product-total {
  font-size: 20px;
  font-weight: 700;
  color: $primary-color;
  margin-left: $spacing-lg;
  flex-shrink: 0;
}

// 右侧：订单摘要
.right-section {
  position: sticky;
  top: 80px;
}

.summary-card {
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  border: none;

  :deep(.el-card__body) {
    padding: $spacing-xl;
  }
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-lg;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm 0;
  font-size: 14px;

  .label {
    color: $text-secondary;
  }

  .value {
    color: $text-primary;
    font-weight: 500;
  }

  .order-no {
    font-family: 'Courier New', monospace;
    font-size: 13px;
  }

  &.total-row {
    padding: $spacing-md 0;
    font-size: 16px;

    .label {
      color: $text-primary;
      font-weight: 600;
    }
  }
}

.total-amount {
  font-size: 28px;
  font-weight: 700;
  color: $primary-color;
}

.currency-tip {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-top: $spacing-lg;
  padding: $spacing-md;
  background: #f0f9ff;
  border-radius: 12px;
  font-size: 13px;
  color: $text-secondary;
  line-height: 1.6;

  .tip-icon {
    color: $primary-color;
    font-size: 16px;
    flex-shrink: 0;
  }
}

.payment-actions {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-top: $spacing-xl;

  .el-button {
    width: 100%;
    height: 50px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 12px;
  }

  .pay-button {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 24px rgba(102, 126, 234, 0.5);
    }

    .button-icon {
      margin-right: $spacing-xs;
    }
  }

  .cancel-button {
    color: $text-secondary;
    border-color: #e5e7eb;

    &:hover {
      color: $text-primary;
      border-color: #d1d5db;
      background: #f9fafb;
    }
  }
}

.security-badges {
  display: flex;
  justify-content: center;
  gap: $spacing-xl;
  margin-top: $spacing-xl;
  padding-top: $spacing-lg;
  border-top: 1px solid $border-lighter;

  .badge {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    font-size: 12px;
    color: $text-secondary;

    .el-icon {
      color: #10b981;
      font-size: 16px;
    }
  }
}

// 响应式
@include mobile {
  .stripe-payment-page {
    padding: $spacing-lg 0;
  }

  .container {
    padding: 0 $spacing-sm;
  }

  .page-title {
    font-size: 24px;
    margin-bottom: $spacing-lg;

    .payment-icon {
      font-size: 28px;
    }
  }

  .payment-container {
    grid-template-columns: 1fr;
    gap: $spacing-lg;
  }

  .left-section {
    padding: $spacing-lg;
    border-radius: 12px;
  }

  .right-section {
    position: static;
  }

  .summary-card {
    :deep(.el-card__body) {
      padding: $spacing-lg;
    }
  }

  .product-item {
    padding: $spacing-md;
  }

  .product-image {
    width: 80px;
    height: 80px;
  }

  .product-info {
    margin-left: $spacing-md;
  }

  .product-name {
    font-size: 15px;
  }

  .product-total {
    font-size: 18px;
    margin-left: $spacing-md;
  }

  .total-amount {
    font-size: 24px;
  }

  .payment-actions {
    .el-button {
      height: 48px;
      font-size: 15px;
    }
  }

  .security-badges {
    gap: $spacing-md;
  }
}
</style>
