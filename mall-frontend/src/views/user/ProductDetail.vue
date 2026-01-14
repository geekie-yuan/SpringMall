<template>
  <div class="product-detail-page">
    <div class="container">
      <Loading v-if="loading" />
      <div v-else-if="product" class="product-detail">
        <!-- 商品图片 -->
        <div class="product-image">
          <img :src="product.mainImage || '/placeholder.png'" :alt="product.name" />
        </div>

        <!-- 商品信息 -->
        <div class="product-info">
          <h1 class="product-name">{{ product.name }}</h1>
          <p class="product-desc">{{ product.description }}</p>

          <div class="product-price-box">
            <span class="label">价格：</span>
            <span class="price">¥{{ formatPrice(product.price) }}</span>
          </div>

          <div class="product-stock">
            <span class="label">库存：</span>
            <span :class="{ 'out-of-stock': product.stock === 0 }">
              {{ product.stock > 0 ? `${product.stock} 件` : '缺货' }}
            </span>
          </div>

          <div class="product-quantity">
            <span class="label">数量：</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="product.stock"
              :disabled="product.stock === 0"
            />
          </div>

          <div class="product-actions">
            <el-button
              type="primary"
              size="large"
              :disabled="product.stock === 0"
              @click="handleAddToCart"
            >
              加入购物车
            </el-button>
            <el-button
              type="danger"
              size="large"
              :disabled="product.stock === 0"
              @click="handleBuyNow"
            >
              立即购买
            </el-button>
          </div>
        </div>
      </div>

      <!-- 商品详情 -->
      <div v-if="product" class="product-detail-content">
        <h3>商品详情</h3>
        <div class="detail-text">
          {{ product.detail || '暂无详细信息' }}
        </div>
      </div>
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

// 获取商品详情
const fetchProductDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    product.value = await getProductDetail(id)
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('商品不存在')
    router.push('/products')
  } finally {
    loading.value = false
  }
}

// 加入购物车
const handleAddToCart = async () => {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await cartStore.addItem(product.value.id, quantity.value)
  } catch (error) {
    console.error('加入购物车失败:', error)
  }
}

// 立即购买
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

onMounted(() => {
  fetchProductDetail()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.product-detail-page {
  padding: $spacing-xl 0;
  min-height: calc(100vh - 60px);
}

.product-detail {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-xl * 2;
  margin-bottom: $spacing-xl * 2;

  @include mobile {
    grid-template-columns: 1fr;
    gap: $spacing-lg;
  }

  .product-image {
    width: 100%;
    height: 500px;
    background: $bg-color;
    border-radius: $border-radius-base;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      max-width: 100%;
      max-height: 100%;
      object-fit: contain;
    }

    @include mobile {
      height: 300px;
    }
  }

  .product-info {
    .product-name {
      font-size: 28px;
      color: $text-primary;
      margin: 0 0 $spacing-md 0;
      font-weight: bold;
    }

    .product-desc {
      color: $text-secondary;
      margin-bottom: $spacing-xl;
      line-height: 1.6;
    }

    .product-price-box {
      background: $bg-page;
      padding: $spacing-lg;
      border-radius: $border-radius-base;
      margin-bottom: $spacing-lg;

      .label {
        color: $text-regular;
        margin-right: $spacing-sm;
      }

      .price {
        font-size: 32px;
        color: $danger-color;
        font-weight: bold;
      }
    }

    .product-stock,
    .product-quantity {
      margin-bottom: $spacing-lg;
      display: flex;
      align-items: center;

      .label {
        color: $text-regular;
        margin-right: $spacing-md;
        width: 80px;
      }

      .out-of-stock {
        color: $danger-color;
      }
    }

    .product-actions {
      display: flex;
      gap: $spacing-md;
      margin-top: $spacing-xl;

      .el-button {
        flex: 1;
      }

      @include mobile {
        flex-direction: column;
      }
    }
  }
}

.product-detail-content {
  background: $bg-color;
  padding: $spacing-xl;
  border-radius: $border-radius-base;

  h3 {
    font-size: 20px;
    color: $text-primary;
    margin: 0 0 $spacing-lg 0;
    padding-bottom: $spacing-md;
    border-bottom: 2px solid $border-light;
  }

  .detail-text {
    color: $text-regular;
    line-height: 1.8;
    white-space: pre-wrap;
  }
}
</style>
