<template>
  <div class="home">
    <!-- Banner 轮播 -->
    <div class="banner-section">
      <el-carousel height="400px" :interval="5000">
        <el-carousel-item v-for="item in banners" :key="item.id">
          <div class="banner-item" :style="{ background: item.bg }">
            <div class="container">
              <h2>{{ item.title }}</h2>
              <p>{{ item.desc }}</p>
              <el-button type="primary" size="large" @click="$router.push('/products')">
                立即购买
              </el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 分类导航 -->
    <div class="category-section">
      <div class="container">
        <h3 class="section-title">商品分类</h3>
        <div class="category-grid">
          <div
            v-for="category in categories"
            :key="category.id"
            class="category-item"
            @click="goToCategory(category.id)"
          >
            <el-icon :size="40"><Grid /></el-icon>
            <span>{{ category.name }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 热门商品 -->
    <div class="products-section">
      <div class="container">
        <h3 class="section-title">热门商品</h3>
        <Loading v-if="loading" />
        <Empty v-else-if="!products.length" type="product" text="暂无商品" />
        <div v-else class="product-grid">
          <div
            v-for="product in products"
            :key="product.id"
            class="product-card"
            @click="goToDetail(product.id)"
          >
            <div class="product-image">
              <img :src="product.mainImage || '/placeholder.png'" :alt="product.name" />
            </div>
            <div class="product-info">
              <h4 class="product-name">{{ product.name }}</h4>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-footer">
                <span class="product-price">¥{{ formatPrice(product.price) }}</span>
                <el-button type="primary" size="small" @click.stop="addToCart(product)">
                  加入购物车
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAllProducts } from '@/api/product'
import { useAppStore } from '@/store/app'
import { useCartStore } from '@/store/cart'
import { formatPrice } from '@/utils/format'
import { ElMessage } from 'element-plus'
import Loading from '@/components/common/Loading.vue'
import Empty from '@/components/common/Empty.vue'

const router = useRouter()
const appStore = useAppStore()
const cartStore = useCartStore()

// Banner 数据
const banners = ref([
  {
    id: 1,
    title: '欢迎来到 Spring Mall',
    desc: '精选好物，品质保证',
    bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    id: 2,
    title: '新品上市',
    desc: '最新商品，等你来选',
    bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    id: 3,
    title: '限时优惠',
    desc: '超值特惠，不容错过',
    bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  }
])

const categories = ref([])
const products = ref([])
const loading = ref(false)

// 获取分类列表
const fetchCategories = async () => {
  await appStore.fetchCategories()
  categories.value = appStore.categories.slice(0, 8) // 只显示前8个
}

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    const data = await getAllProducts({ page: 0, size: 8 })
    products.value = data.content || data || []
  } catch (error) {
    console.error('获取商品失败:', error)
  } finally {
    loading.value = false
  }
}

// 跳转到分类页面
const goToCategory = (categoryId) => {
  router.push({
    path: '/products',
    query: { categoryId }
  })
}

// 跳转到商品详情
const goToDetail = (productId) => {
  router.push(`/products/${productId}`)
}

// 加入购物车
const addToCart = async (product) => {
  try {
    await cartStore.addItem(product.id, 1)
  } catch (error) {
    console.error('加入购物车失败:', error)
  }
}

onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.home {
  background: $bg-page;
}

.banner-section {
  .banner-item {
    height: 400px;
    display: flex;
    align-items: center;
    color: #fff;

    h2 {
      font-size: 48px;
      margin-bottom: $spacing-md;
      font-weight: bold;
    }

    p {
      font-size: 20px;
      margin-bottom: $spacing-xl;
    }

    @include mobile {
      h2 {
        font-size: 32px;
      }

      p {
        font-size: 16px;
      }
    }
  }
}

.category-section,
.products-section {
  padding: $spacing-xl * 2 0;
}

.section-title {
  font-size: 28px;
  color: $text-primary;
  margin-bottom: $spacing-xl;
  text-align: center;
  font-weight: bold;

  @include mobile {
    font-size: 24px;
  }
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: $spacing-lg;

  @include tablet {
    grid-template-columns: repeat(4, 1fr);
  }

  @include mobile {
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-md;
  }

  .category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: $spacing-lg;
    background: $bg-color;
    border-radius: $border-radius-base;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      transform: translateY(-4px);
    }

    .el-icon {
      color: $primary-color;
      margin-bottom: $spacing-sm;
    }

    span {
      color: $text-regular;
      font-size: 14px;
    }

    @include mobile {
      padding: $spacing-md;

      .el-icon {
        font-size: 30px;
      }

      span {
        font-size: 12px;
      }
    }
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-lg;

  @include tablet {
    grid-template-columns: repeat(3, 1fr);
  }

  @include mobile {
    grid-template-columns: repeat(2, 1fr);
    gap: $spacing-md;
  }

  .product-card {
    background: $bg-color;
    border-radius: $border-radius-base;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transform: translateY(-4px);
    }

    .product-image {
      width: 100%;
      height: 200px;
      overflow: hidden;
      background: $bg-page;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      @include mobile {
        height: 150px;
      }
    }

    .product-info {
      padding: $spacing-md;

      .product-name {
        @include text-ellipsis;
        font-size: 16px;
        color: $text-primary;
        margin: 0 0 $spacing-sm 0;
        font-weight: 500;
      }

      .product-desc {
        @include text-ellipsis;
        font-size: 14px;
        color: $text-secondary;
        margin: 0 0 $spacing-md 0;
      }

      .product-footer {
        @include flex-between;

        .product-price {
          font-size: 20px;
          color: $danger-color;
          font-weight: bold;
        }
      }

      @include mobile {
        padding: $spacing-sm;

        .product-name {
          font-size: 14px;
        }

        .product-desc {
          display: none;
        }

        .product-price {
          font-size: 16px;
        }
      }
    }
  }
}
</style>
