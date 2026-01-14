<template>
  <div class="checkout-page">
    <div class="container">
      <h2 class="page-title">确认订单</h2>

      <div class="checkout-content">
        <!-- 收货地址 -->
        <div class="section">
          <h3 class="section-title">收货地址</h3>
          <div v-if="addresses.length === 0" class="empty-address">
            <p>您还没有收货地址</p>
            <el-button type="primary" @click="$router.push('/address')">
              添加地址
            </el-button>
          </div>
          <div v-else class="address-list">
            <div
              v-for="addr in addresses"
              :key="addr.id"
              class="address-item"
              :class="{ active: selectedAddress === addr.id }"
              @click="selectedAddress = addr.id"
            >
              <div class="address-info">
                <span class="name">{{ addr.receiverName }}</span>
                <span class="phone">{{ addr.receiverPhone }}</span>
                <el-tag v-if="addr.isDefault" type="primary" size="small">默认</el-tag>
              </div>
              <p class="address-detail">
                {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}
              </p>
            </div>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="section">
          <h3 class="section-title">商品信息</h3>
          <div class="product-list">
            <div
              v-for="item in checkedItems"
              :key="item.id"
              class="product-item"
            >
              <img :src="item.productImage || '/placeholder.png'" :alt="item.productName" />
              <div class="product-info">
                <h4>{{ item.productName }}</h4>
                <p>¥{{ formatPrice(item.productPrice) }} × {{ item.quantity }}</p>
              </div>
              <span class="product-total">¥{{ formatPrice(item.productPrice * item.quantity) }}</span>
            </div>
          </div>
        </div>

        <!-- 备注 -->
        <div class="section">
          <h3 class="section-title">订单备注</h3>
          <el-input
            v-model="remark"
            type="textarea"
            :rows="3"
            placeholder="选填，可以告诉我们您的特殊需求"
            maxlength="200"
            show-word-limit
          />
        </div>

        <!-- 订单总计 -->
        <div class="section order-summary">
          <div class="summary-item">
            <span>商品总计：</span>
            <span>¥{{ formatPrice(totalAmount) }}</span>
          </div>
          <div class="summary-item">
            <span>运费：</span>
            <span>¥0.00</span>
          </div>
          <div class="summary-item total">
            <span>应付总额：</span>
            <span class="total-price">¥{{ formatPrice(totalAmount) }}</span>
          </div>
        </div>

        <!-- 提交订单 -->
        <div class="submit-section">
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            :disabled="!selectedAddress || checkedItems.length === 0"
            @click="handleSubmit"
          >
            提交订单
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/store/cart'
import { useUserStore } from '@/store/user'
import { createOrder } from '@/api/order'
import { formatPrice } from '@/utils/format'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const addresses = ref([])
const selectedAddress = ref(null)
const remark = ref('')
const submitting = ref(false)

// 计算属性
const checkedItems = computed(() => cartStore.checkedItems)
const totalAmount = computed(() => cartStore.checkedTotal)

// 获取地址列表
const fetchAddresses = async () => {
  try {
    await userStore.fetchAddresses()
    addresses.value = userStore.addresses

    // 默认选中默认地址
    const defaultAddr = addresses.value.find(addr => addr.isDefault)
    if (defaultAddr) {
      selectedAddress.value = defaultAddr.id
    } else if (addresses.value.length > 0) {
      selectedAddress.value = addresses.value[0].id
    }
  } catch (error) {
    console.error('获取地址失败:', error)
  }
}

// 提交订单
const handleSubmit = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  if (checkedItems.value.length === 0) {
    ElMessage.warning('购物车中没有选中的商品')
    return
  }

  submitting.value = true
  try {
    const orderData = {
      addressId: selectedAddress.value,
      remark: remark.value,
      items: checkedItems.value.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      }))
    }

    const order = await createOrder(orderData)
    ElMessage.success('订单提交成功，请完成支付')

    // 清空已选中的购物车商品
    cartStore.clearCart()

    // 跳转到支付页面
    router.push(`/payment/${order.orderNo}`)
  } catch (error) {
    console.error('提交订单失败:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 检查是否有选中的商品
  if (checkedItems.value.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    router.push('/cart')
    return
  }

  fetchAddresses()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.checkout-page {
  padding: $spacing-xl 0;
  min-height: calc(100vh - 60px);
}

.page-title {
  font-size: 24px;
  color: $text-primary;
  margin-bottom: $spacing-xl;
}

.checkout-content {
  max-width: 900px;

  .section {
    background: $bg-color;
    padding: $spacing-lg;
    border-radius: $border-radius-base;
    margin-bottom: $spacing-lg;

    .section-title {
      font-size: 18px;
      color: $text-primary;
      margin: 0 0 $spacing-md 0;
      padding-bottom: $spacing-sm;
      border-bottom: 2px solid $border-light;
    }

    .empty-address {
      text-align: center;
      padding: $spacing-xl;
      color: $text-secondary;

      p {
        margin-bottom: $spacing-md;
      }
    }

    .address-list {
      display: grid;
      gap: $spacing-md;

      .address-item {
        padding: $spacing-md;
        border: 2px solid $border-light;
        border-radius: $border-radius-base;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: $primary-color;
        }

        &.active {
          border-color: $primary-color;
          background: rgba(64, 158, 255, 0.05);
        }

        .address-info {
          display: flex;
          align-items: center;
          gap: $spacing-md;
          margin-bottom: $spacing-sm;

          .name {
            font-weight: 500;
            color: $text-primary;
          }

          .phone {
            color: $text-regular;
          }
        }

        .address-detail {
          color: $text-secondary;
          margin: 0;
        }
      }
    }

    .product-list {
      .product-item {
        display: flex;
        align-items: center;
        gap: $spacing-md;
        padding: $spacing-md 0;
        border-bottom: 1px solid $border-lighter;

        &:last-child {
          border-bottom: none;
        }

        img {
          width: 60px;
          height: 60px;
          object-fit: cover;
          border-radius: $border-radius-base;
        }

        .product-info {
          flex: 1;

          h4 {
            font-size: 14px;
            color: $text-primary;
            margin: 0 0 $spacing-xs 0;
          }

          p {
            font-size: 12px;
            color: $text-secondary;
            margin: 0;
          }
        }

        .product-total {
          color: $danger-color;
          font-weight: 500;
        }
      }
    }
  }

  .order-summary {
    .summary-item {
      @include flex-between;
      padding: $spacing-sm 0;
      color: $text-regular;

      &.total {
        padding-top: $spacing-md;
        border-top: 1px solid $border-light;
        font-size: 18px;
        color: $text-primary;

        .total-price {
          font-size: 24px;
          color: $danger-color;
          font-weight: bold;
        }
      }
    }
  }

  .submit-section {
    text-align: right;

    .el-button {
      min-width: 200px;
    }
  }
}
</style>
