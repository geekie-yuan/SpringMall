<template>
  <div class="dashboard-page">
    <h2 class="page-title">管理后台</h2>

    <!-- 数据统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon sales">
            <el-icon :size="32"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">总销售额</p>
            <h3 class="stat-value">¥{{ formatPrice(stats.totalSales) }}</h3>
            <p class="stat-trend positive">
              <el-icon><TrendCharts /></el-icon>
              <span>较上月 +12.5%</span>
            </p>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon orders">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">订单总数</p>
            <h3 class="stat-value">{{ stats.totalOrders }}</h3>
            <p class="stat-trend positive">
              <el-icon><TrendCharts /></el-icon>
              <span>较上月 +8.3%</span>
            </p>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon users">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">用户总数</p>
            <h3 class="stat-value">{{ stats.totalUsers }}</h3>
            <p class="stat-trend positive">
              <el-icon><TrendCharts /></el-icon>
              <span>较上月 +15.2%</span>
            </p>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon products">
            <el-icon :size="32"><Box /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">商品总数</p>
            <h3 class="stat-value">{{ stats.totalProducts }}</h3>
            <p class="stat-trend">
              <span>在售商品 {{ stats.onSaleProducts }}</span>
            </p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 快捷入口 -->
    <el-card class="quick-actions">
      <template #header>
        <span>快捷操作</span>
      </template>
      <div class="actions-grid">
        <div class="action-item" @click="$router.push('/admin/products')">
          <el-icon :size="24" color="#409EFF"><Goods /></el-icon>
          <span>商品管理</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/categories')">
          <el-icon :size="24" color="#67C23A"><Menu /></el-icon>
          <span>分类管理</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/orders')">
          <el-icon :size="24" color="#E6A23C"><Tickets /></el-icon>
          <span>订单管理</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/users')">
          <el-icon :size="24" color="#F56C6C"><UserFilled /></el-icon>
          <span>用户管理</span>
        </div>
      </div>
    </el-card>

    <!-- 最近订单 -->
    <el-card class="recent-orders">
      <template #header>
        <div class="card-header">
          <span>最近订单</span>
          <el-button text @click="$router.push('/admin/orders')">
            查看全部
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>

      <Loading v-if="loading" />
      <el-table v-else :data="recentOrders" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="用户ID" width="100" align="center">
          <template #default="{ row }">
            {{ row.userId }}
          </template>
        </el-table-column>
        <el-table-column label="商品数量" width="100" align="center">
          <template #default="{ row }">
            {{ row.items.length }}
          </template>
        </el-table-column>
        <el-table-column label="总金额" width="120" align="right">
          <template #default="{ row }">
            <span class="price-text">¥{{ formatPrice(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType[row.status]" size="small">
              {{ statusText[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button
              text
              type="primary"
              @click="$router.push(`/admin/orders/${row.orderNo}`)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  Money,
  Document,
  User,
  Box,
  Goods,
  Menu,
  Tickets,
  UserFilled,
  TrendCharts,
  ArrowRight
} from '@element-plus/icons-vue'
import { getAllOrders } from '@/api/admin/order'
import { getAllProducts } from '@/api/product'
import { getAllUsers } from '@/api/admin/user'
import { formatPrice, formatDate } from '@/utils/format'
import { ORDER_STATUS_TEXT, ORDER_STATUS_TAG_TYPE } from '@/utils/constants'
import Loading from '@/components/common/Loading.vue'

const loading = ref(false)
const recentOrders = ref([])

const statusText = ORDER_STATUS_TEXT
const statusTagType = ORDER_STATUS_TAG_TYPE

// 统计数据（模拟数据，实际应从后端获取）
const stats = ref({
  totalSales: 0,
  totalOrders: 0,
  totalUsers: 0,
  totalProducts: 0,
  onSaleProducts: 0
})

// 获取统计数据
const fetchStats = async () => {
  try {
    // 获取订单统计
    const orders = await getAllOrders()
    stats.value.totalOrders = orders.content?.length || orders.length || 0

    // 计算总销售额
    const allOrders = orders.content || orders || []
    stats.value.totalSales = allOrders
      .filter(order => order.status !== 'CANCELLED')
      .reduce((sum, order) => sum + order.totalAmount, 0)

    // 获取商品统计
    const products = await getAllProducts()
    stats.value.totalProducts = products.length || 0
    stats.value.onSaleProducts = products.filter(p => p.status === 'ON_SALE').length || 0

    // 获取用户统计
    const users = await getAllUsers()
    stats.value.totalUsers = users.content?.length || users.length || 0
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取最近订单
const fetchRecentOrders = async () => {
  loading.value = true
  try {
    const data = await getAllOrders()
    const orders = data.content || data || []
    // 取最近5个订单
    recentOrders.value = orders.slice(0, 5)
  } catch (error) {
    console.error('获取最近订单失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchRecentOrders()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.dashboard-page {
  padding: $spacing-lg;

  .page-title {
    font-size: 24px;
    color: $text-primary;
    margin: 0 0 $spacing-lg 0;
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-lg;
    margin-bottom: $spacing-lg;

    @include tablet {
      grid-template-columns: repeat(2, 1fr);
    }

    @include mobile {
      grid-template-columns: 1fr;
    }

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: $spacing-lg;

        .stat-icon {
          width: 64px;
          height: 64px;
          border-radius: 12px;
          @include flex-center;
          color: white;

          &.sales {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.orders {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }

          &.users {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          }

          &.products {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
          }
        }

        .stat-info {
          flex: 1;

          .stat-label {
            font-size: 14px;
            color: $text-secondary;
            margin: 0 0 $spacing-xs 0;
          }

          .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: $text-primary;
            margin: 0 0 $spacing-xs 0;
          }

          .stat-trend {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 12px;
            color: $text-secondary;
            margin: 0;

            &.positive {
              color: $success-color;
            }

            &.negative {
              color: $danger-color;
            }
          }
        }
      }
    }
  }

  .quick-actions {
    margin-bottom: $spacing-lg;

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: $spacing-md;

      @include mobile {
        grid-template-columns: repeat(2, 1fr);
      }

      .action-item {
        @include flex-center;
        flex-direction: column;
        gap: $spacing-sm;
        padding: $spacing-lg;
        border: 2px solid $border-light;
        border-radius: $border-radius-base;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: $primary-color;
          background: rgba(64, 158, 255, 0.05);
          transform: translateY(-2px);
        }

        span {
          font-size: 14px;
          color: $text-primary;
        }
      }
    }
  }

  .recent-orders {
    .card-header {
      @include flex-between;
    }

    .price-text {
      color: $danger-color;
      font-weight: 500;
    }
  }
}
</style>
