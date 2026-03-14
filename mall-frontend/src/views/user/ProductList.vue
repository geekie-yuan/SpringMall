<template>
  <div class="product-list-page">

    <!-- Page Header -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">全部商品</h1>
        <p class="page-count" v-if="total > 0">{{ total }} 件商品</p>
      </div>
    </div>

    <div class="container">
      <div class="layout">

        <!-- Sidebar Filters -->
        <aside class="sidebar">
          <div class="filter-block">
            <h3 class="filter-heading">搜索</h3>
            <div class="search-box">
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="商品名称..."
                class="search-input"
                @keyup.enter="handleSearch"
              />
              <button class="search-btn" @click="handleSearch">
                <el-icon><Search /></el-icon>
              </button>
            </div>
          </div>

          <div class="filter-block">
            <h3 class="filter-heading">分类</h3>
            <ul class="category-list">
              <li>
                <button
                  class="category-item"
                  :class="{ active: !selectedCategory }"
                  @click="selectCategory(null)"
                >全部分类</button>
              </li>
              <li v-for="category in categories" :key="category.id">
                <button
                  class="category-item"
                  :class="{ active: selectedCategory === category.id }"
                  @click="selectCategory(category.id)"
                >{{ category.name }}</button>
              </li>
            </ul>
          </div>
        </aside>

        <!-- Product Content -->
        <div class="content">

          <!-- Toolbar -->
          <div class="toolbar">
            <span class="toolbar-label">
              <template v-if="selectedCategory">
                {{ getCategoryName(selectedCategory) }}
              </template>
              <template v-else-if="searchKeyword">
                "{{ searchKeyword }}" 的搜索结果
              </template>
            </span>
            <div class="sort-controls">
              <el-select
                v-model="sortBy"
                size="small"
                style="width: 140px"
                @change="fetchProducts"
              >
                <el-option label="默认排序" value="default" />
                <el-option label="价格：低到高" value="price_asc" />
                <el-option label="价格：高到低" value="price_desc" />
                <el-option label="最新上架" value="createdAt_desc" />
              </el-select>
            </div>
          </div>

          <!-- States -->
          <Loading v-if="loading" />
          <Empty v-else-if="!products.length" type="product" text="暂无商品" />

          <!-- Grid -->
          <div v-else class="product-grid">
            <div
              v-for="product in products"
              :key="product.id"
              class="product-card"
              @click="goToDetail(product.id)"
            >
              <div class="product-card__image">
                <img
                  :src="product.mainImage || '/placeholder.png'"
                  :alt="product.name"
                  loading="lazy"
                />
                <div class="product-card__overlay">
                  <button class="btn-overlay" @click.stop="addToCart(product)">
                    加入购物车
                  </button>
                </div>
              </div>
              <div class="product-card__body">
                <h3 class="product-card__name">{{ product.name }}</h3>
                <p class="product-card__price">¥{{ formatPrice(product.price) }}</p>
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div v-if="total > pageSize" class="pagination-wrap">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[12, 24, 48]"
              layout="total, prev, pager, next"
              @current-change="fetchProducts"
              @size-change="fetchProducts"
            />
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getAllProducts } from '@/api/product'
import { useAppStore } from '@/store/app'
import { useAuthStore } from '@/store/auth'
import { useCartStore } from '@/store/cart'
import { formatPrice } from '@/utils/format'
import { debounce } from '@/utils/helpers'
import { ElMessage } from 'element-plus'
import Loading from '@/components/common/Loading.vue'
import Empty from '@/components/common/Empty.vue'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const authStore = useAuthStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const selectedCategory = ref(null)
const sortBy = ref('default')
const categories = ref([])
const products = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const getCategoryName = (id) => categories.value.find(c => c.id === id)?.name || ''

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }

    if (sortBy.value === 'default') {
      params.sortBy = 'sales'
      params.sortDir = 'desc'
    } else {
      const [field, dir] = sortBy.value.split('_')
      params.sortBy = field
      params.sortDir = dir
    }

    const data = await getAllProducts(params)
    products.value = data.list || data || []
    total.value = data.total || products.value.length
  } catch (error) {
    console.error('获取商品失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { currentPage.value = 1; fetchProducts() }
const debouncedSearch = debounce(handleSearch, 500)

const selectCategory = (id) => {
  selectedCategory.value = id
  currentPage.value = 1
  fetchProducts()
}

const goToDetail = (productId) => router.push(`/products/${productId}`)

const addToCart = async (product) => {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await cartStore.addItem(product.id, 1)
    ElMessage.success('已加入购物车')
  } catch (error) {
    console.error('加入购物车失败:', error)
  }
}

const applyRouteQuery = () => {
  // Reset state before applying new query
  selectedCategory.value = null
  sortBy.value = 'default'
  currentPage.value = 1

  if (route.query.categoryId) {
    if (route.query.categoryId === 'new') {
      // "新品" button: sort by creation time (newest first)
      sortBy.value = 'createdAt_desc'
    } else {
      selectedCategory.value = Number(route.query.categoryId)
    }
  }
}

// Watch route query changes so navigation between "新品" / "全部商品" triggers a reload
watch(() => route.query, () => {
  applyRouteQuery()
  fetchProducts()
})

onMounted(async () => {
  await appStore.fetchCategories()
  categories.value = appStore.categories

  applyRouteQuery()
  fetchProducts()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

// ─── Page Header ─────────────────────────────────────
.product-list-page {
  background: $bg-color;
  min-height: calc(100vh - 68px);
}

.page-header {
  border-bottom: 1px solid $border-light;
  padding: $spacing-xl 0 $spacing-lg;
}

.page-title {
  font-size: $font-size-xxl;
  font-weight: $font-weight-bold;
  letter-spacing: -0.03em;
  color: $text-primary;
  margin-bottom: 4px;
}

.page-count {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-top: $spacing-sm;
}

// ─── Two-column Layout ───────────────────────────────
.layout {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: $spacing-xxl;
  padding: $spacing-xl 0 $spacing-xxl;

  @include tablet { grid-template-columns: 180px 1fr; gap: $spacing-xl; }
  @include mobile { grid-template-columns: 1fr; }
}

// ─── Sidebar ─────────────────────────────────────────
.sidebar {
  @include mobile { display: none; }
}

.filter-block {
  margin-bottom: $spacing-xl;

  &:last-child { margin-bottom: 0; }
}

.filter-heading {
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: $text-secondary;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid $border-lighter;
}

.search-box {
  display: flex;
  border: 1px solid $border-base;
  padding-right: 6px;

  &:focus-within { border-color: $text-primary; }
}

.search-input {
  flex: 1;
  padding: 9px 12px;
  border: none;
  outline: none;
  font-size: $font-size-sm;
  font-family: $font-family;
  color: $text-primary;
  background: transparent;

  &::placeholder { color: $text-placeholder; }
}

.search-btn {
  width: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-secondary;
  cursor: pointer;
  background: none;
  border: none;
  transition: color $transition-base;

  &:hover { color: $text-primary; }
}

.category-list {
  list-style: none;
  padding: 0;
}

.category-item {
  display: block;
  width: 100%;
  text-align: left;
  padding: 7px 0;
  font-size: $font-size-sm;
  color: $text-regular;
  background: none;
  border: none;
  cursor: pointer;
  font-family: $font-family;
  transition: color $transition-base;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    left: -12px;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 0;
    background: $primary-color;
    transition: height $transition-base;
  }

  &:hover { color: $text-primary; }

  &.active {
    color: $text-primary;
    font-weight: $font-weight-medium;
    &::before { height: 100%; }
  }
}

// ─── Toolbar ─────────────────────────────────────────
.toolbar {
  @include flex-between;
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-md;
  border-bottom: 1px solid $border-lighter;
}

.toolbar-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  font-style: italic;
}

// ─── Product Grid ────────────────────────────────────
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1px;
  background: $border-light;
  border: 1px solid $border-light;
  margin-bottom: $spacing-xl;

  @include tablet { grid-template-columns: repeat(2, 1fr); }
  @include mobile { grid-template-columns: repeat(2, 1fr); }
}

.product-card {
  background: $bg-color;
  cursor: pointer;
  position: relative;
  overflow: hidden;

  &__image {
    position: relative;
    aspect-ratio: 3 / 4;
    background: $bg-gray;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform $transition-slow;
    }
  }

  &__overlay {
    position: absolute;
    inset: 0;
    background: rgba(0,0,0,0.2);
    display: flex;
    align-items: flex-end;
    padding: $spacing-md;
    opacity: 0;
    transition: opacity $transition-base;
  }

  .btn-overlay {
    width: 100%;
    padding: 12px;
    background: #fff;
    border: none;
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    letter-spacing: 0.04em;
    cursor: pointer;
    font-family: $font-family;
    transition: background $transition-base;

    &:hover { background: $bg-gray; }
  }

  &:hover {
    .product-card__image img { transform: scale(1.04); }
    .product-card__overlay { opacity: 1; }
  }

  &__body {
    padding: $spacing-md $spacing-md $spacing-lg;
  }

  &__name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $text-primary;
    margin-bottom: 4px;
    @include text-ellipsis;
  }

  &__price {
    font-size: $font-size-base;
    color: $text-regular;
  }
}

// ─── Pagination ──────────────────────────────────────
.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: $spacing-lg 0;

  :deep(.el-pagination) {
    .el-pager li {
      border-radius: 0;
      &.is-active { background: $primary-color; }
    }
    button {
      border-radius: 0;
    }
  }
}
</style>
