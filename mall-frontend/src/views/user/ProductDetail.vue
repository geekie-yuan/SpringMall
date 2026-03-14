<template>
  <div class="product-detail-page">
    <div class="container">

      <Loading v-if="loading" />

      <template v-else-if="product">
        <!-- Breadcrumb -->
        <nav class="breadcrumb">
          <router-link to="/" class="crumb">首页</router-link>
          <span class="crumb-sep">/</span>
          <router-link to="/products" class="crumb">全部商品</router-link>
          <span class="crumb-sep">/</span>
          <span class="crumb crumb--current">{{ product.name }}</span>
        </nav>

        <!-- Main Layout -->
        <div class="detail-grid">

          <!-- Left: Image -->
          <div class="image-panel">
            <div class="image-frame">
              <img
                :src="product.mainImage || '/placeholder.png'"
                :alt="product.name"
              />
            </div>
          </div>

          <!-- Right: Info -->
          <div class="info-panel">
            <h1 class="product-name">{{ product.name }}</h1>
            <div class="product-price">¥{{ formatPrice(product.price) }}</div>

            <p v-if="product.description" class="product-description">
              {{ product.description }}
            </p>

            <div class="divider"></div>

            <div class="stock-row">
              <span class="meta-label">库存</span>
              <span v-if="product.stock > 0" class="stock-count">{{ product.stock }} 件</span>
              <span v-else class="out-of-stock">缺货</span>
            </div>

            <div class="quantity-row">
              <span class="meta-label">数量</span>
              <div class="quantity-control">
                <button
                  class="qty-btn"
                  @click="quantity > 1 && quantity--"
                  :disabled="quantity <= 1 || product.stock === 0"
                >−</button>
                <span class="qty-value">{{ quantity }}</span>
                <button
                  class="qty-btn"
                  @click="quantity < product.stock && quantity++"
                  :disabled="quantity >= product.stock || product.stock === 0"
                >+</button>
              </div>
            </div>

            <div class="action-group">
              <button
                class="btn-primary-full"
                :disabled="product.stock === 0"
                @click="handleAddToCart"
              >
                {{ product.stock === 0 ? '暂时缺货' : '加入购物车' }}
              </button>
              <button
                class="btn-buy-now"
                :disabled="product.stock === 0"
                @click="handleBuyNow"
              >
                立即购买
              </button>
            </div>
          </div>
        </div>

        <!-- Product Detail Section -->
        <div class="detail-section">
          <h2 class="section-heading">商品详情</h2>
          <div class="detail-body">{{ product.detail || '暂无详细信息' }}</div>
        </div>
      </template>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail } from '@/api/product'
import { useCartStore } from '@/store/cart'
import { useAuthStore } from '@/store/auth'
import { formatPrice } from '@/utils/format'
import { ElMessage } from 'element-plus'
import Loading from '@/components/common/Loading.vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

const product = ref(null)
const loading = ref(false)
const quantity = ref(1)

const fetchProductDetail = async () => {
  loading.value = true
  try {
    product.value = await getProductDetail(route.params.id)
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('商品不存在')
    router.push('/products')
  } finally {
    loading.value = false
  }
}

const handleAddToCart = async () => {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await cartStore.addItem(product.value.id, quantity.value)
    ElMessage.success('已加入购物车')
  } catch (error) {
    console.error('加入购物车失败:', error)
  }
}

const handleBuyNow = async () => {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await cartStore.addItem(product.value.id, quantity.value)
    router.push('/cart')
  } catch (error) {
    console.error('加入购物车失败:', error)
  }
}

onMounted(fetchProductDetail)
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.product-detail-page {
  background: $bg-color;
  min-height: calc(100vh - 68px);
  padding: $spacing-lg 0 $spacing-xxl;
}

// Breadcrumb
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: $spacing-xl;
  font-size: $font-size-sm;
}

.crumb {
  color: $text-secondary;
  text-decoration: none;
  transition: color $transition-base;

  &:hover { color: $text-primary; }
  &--current { color: $text-primary; }
}

.crumb-sep { color: $border-base; }

// Two-column Detail Grid
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-xxl;
  margin-bottom: $spacing-xxl;

  @include mobile {
    grid-template-columns: 1fr;
    gap: $spacing-xl;
  }
}

// Image Panel
.image-frame {
  background: $bg-gray;
  aspect-ratio: 1 / 1;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

// Info Panel
.info-panel {
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: clamp(22px, 3vw, 32px);
  font-weight: $font-weight-bold;
  letter-spacing: -0.03em;
  color: $text-primary;
  margin-bottom: $spacing-md;
  line-height: 1.2;
}

.product-price {
  font-size: 28px;
  font-weight: $font-weight-medium;
  color: $text-primary;
  margin-bottom: $spacing-lg;
}

.product-description {
  font-size: $font-size-base;
  color: $text-secondary;
  line-height: 1.7;
  margin-bottom: $spacing-lg;
}

.divider {
  height: 1px;
  background: $border-lighter;
  margin-bottom: $spacing-lg;
}

.meta-label {
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: $text-secondary;
  min-width: 60px;
}

// Stock Row
.stock-row {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.stock-count { font-size: $font-size-sm; color: $text-regular; }
.out-of-stock { font-size: $font-size-sm; color: $danger-color; }

// Quantity Control
.quantity-row {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-xl;
}

.quantity-control {
  display: flex;
  align-items: center;
  border: 1px solid $border-base;
}

.qty-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  cursor: pointer;
  background: none;
  border: none;
  font-family: $font-family;
  color: $text-regular;
  transition: background $transition-base, color $transition-base;

  &:hover:not(:disabled) { background: $bg-gray; color: $text-primary; }
  &:disabled { opacity: 0.35; cursor: not-allowed; }
}

.qty-value {
  width: 48px;
  text-align: center;
  font-size: $font-size-base;
  border-left: 1px solid $border-base;
  border-right: 1px solid $border-base;
  line-height: 40px;
}

// Action Buttons
.action-group {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  margin-top: auto;
  padding-top: $spacing-lg;
}

.btn-primary-full {
  width: 100%;
  padding: 16px;
  background: $primary-color;
  color: #fff;
  border: 1px solid $primary-color;
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
  font-family: $font-family;
  transition: background $transition-base, color $transition-base;

  &:hover:not(:disabled) { background: #333; border-color: #333; }
  &:disabled {
    background: $border-base;
    border-color: $border-base;
    cursor: not-allowed;
  }
}

.btn-buy-now {
  width: 100%;
  padding: 16px;
  background: transparent;
  color: $text-primary;
  border: 1px solid $text-primary;
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
  font-family: $font-family;
  transition: background $transition-base, color $transition-base;

  &:hover:not(:disabled) { background: $text-primary; color: #fff; }
  &:disabled { opacity: 0.35; cursor: not-allowed; }
}

// Detail Section
.detail-section {
  border-top: 1px solid $border-light;
  padding-top: $spacing-xl;
}

.section-heading {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  letter-spacing: -0.02em;
  margin-bottom: $spacing-lg;
}

.detail-body {
  font-size: $font-size-base;
  color: $text-regular;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
