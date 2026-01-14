<template>
  <div class="product-list-page">
    <div class="container">
      <!-- 搜索和筛选 -->
      <div class="filter-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品"
          size="large"
          clearable
          @input="debouncedSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>

        <el-select
          v-model="selectedCategory"
          placeholder="选择分类"
          size="large"
          clearable
          @change="handleCategoryChange"
        >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
      </div>

      <!-- 商品列表 -->
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

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="fetchProducts"
        @size-change="fetchProducts"
        class="pagination"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getAllProducts, searchProducts, getProductsByCategory } from '@/api/product'
import { useAppStore } from '@/store/app'
import { useCartStore } from '@/store/cart'
import { formatPrice } from '@/utils/format'
import { debounce } from '@/utils/helpers'
import Loading from '@/components/common/Loading.vue'
import Empty from '@/components/common/Empty.vue'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const selectedCategory = ref(null)
const categories = ref([])
const products = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    let data
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }

    if (searchKeyword.value) {
      data = await searchProducts(searchKeyword.value, params)
    } else if (selectedCategory.value) {
      data = await getProductsByCategory(selectedCategory.value, params)
    } else {
      data = await getAllProducts(params)
    }

    products.value = data.content || data || []
    total.value = data.totalElements || products.value.length
  } catch (error) {
    console.error('获取商品失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

// 防抖搜索（用于实时输入）
const debouncedSearch = debounce(handleSearch, 500)

// 分类筛选
const handleCategoryChange = () => {
  currentPage.value = 1
  fetchProducts()
}

// 跳转详情
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

onMounted(async () => {
  // 获取分类
  await appStore.fetchCategories()
  categories.value = appStore.categories

  // 从路由获取分类ID
  if (route.query.categoryId) {
    selectedCategory.value = Number(route.query.categoryId)
  }

  fetchProducts()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.product-list-page {
  padding: $spacing-xl 0;
  min-height: calc(100vh - 60px);
}

.filter-section {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-xl;

  .el-input {
    flex: 1;
  }

  .el-select {
    width: 200px;
  }

  @include mobile {
    flex-direction: column;

    .el-select {
      width: 100%;
    }
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-lg;
  margin-bottom: $spacing-xl;

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

.pagination {
  display: flex;
  justify-content: center;

  @include mobile {
    :deep(.el-pagination__sizes) {
      display: none;
    }

    :deep(.el-pagination__jump) {
      display: none;
    }
  }
}
</style>
