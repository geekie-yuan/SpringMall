<template>
  <div class="admin-order-detail-page">
    <h2 class="page-title">订单详情</h2>

    <Loading v-if="loading" />
    <div v-else-if="order" class="order-detail">
      <!-- 订单状态卡片 -->
      <el-card class="status-card">
        <div class="status-header">
          <el-tag :type="statusTagType[order.status]" size="large">
            {{ statusText[order.status] }}
          </el-tag>
          <div class="order-no">
            <span class="label">订单号：</span>
            <span>{{ order.orderNo }}</span>
          </div>
        </div>
      </el-card>

      <!-- 订单信息 -->
      <el-card class="info-card">
        <template #header>
          <span>订单信息</span>
        </template>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">用户ID：</span>
            <span>{{ order.userId }}</span>
          </div>
          <div class="info-item">
            <span class="label">下单时间：</span>
            <span>{{ formatDate(order.createdAt) }}</span>
          </div>
          <div v-if="order.paymentTime" class="info-item">
            <span class="label">支付时间：</span>
            <span>{{ formatDate(order.paymentTime) }}</span>
          </div>
          <div v-if="order.shipTime" class="info-item">
            <span class="label">发货时间：</span>
            <span>{{ formatDate(order.shipTime) }}</span>
          </div>
          <div v-if="order.completeTime" class="info-item">
            <span class="label">完成时间：</span>
            <span>{{ formatDate(order.completeTime) }}</span>
          </div>
          <div v-if="order.remark" class="info-item full-width">
            <span class="label">订单备注：</span>
            <span>{{ order.remark }}</span>
          </div>
        </div>
      </el-card>

      <!-- 收货信息 -->
      <el-card class="address-card">
        <template #header>
          <span>收货信息</span>
        </template>
        <div class="address-info">
          <p><strong>{{ order.receiverName }}</strong> {{ order.receiverPhone }}</p>
          <p>{{ order.receiverAddress }}</p>
        </div>
      </el-card>

      <!-- 商品清单 -->
      <el-card class="products-card">
        <template #header>
          <span>商品清单</span>
        </template>
        <el-table :data="order.items" border>
          <el-table-column label="商品" min-width="300">
            <template #default="{ row }">
              <div class="product-info">
                <img :src="row.productImage || '/placeholder.png'" :alt="row.productName" />
                <span>{{ row.productName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120" align="right">
            <template #default="{ row }">
              ¥{{ formatPrice(row.unitPrice) }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100" align="center" />
          <el-table-column label="小计" width="120" align="right">
            <template #default="{ row }">
              <span class="subtotal">¥{{ formatPrice(row.totalPrice) }}</span>
            </template>
          </el-table-column>
        </el-table>

        <div class="order-summary">
          <div class="summary-item">
            <span>商品总计：</span>
            <span>¥{{ formatPrice(order.totalAmount) }}</span>
          </div>
          <div class="summary-item">
            <span>运费：</span>
            <span>¥{{ formatPrice(order.freight || 0) }}</span>
          </div>
          <div class="summary-item total">
            <span>实付款：</span>
            <span class="total-price">¥{{ formatPrice(order.payAmount || order.totalAmount) }}</span>
          </div>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-section">
        <el-button size="large" @click="$router.push('/admin/orders')">
          返回订单列表
        </el-button>
        <el-button
          v-if="order.status === 'UNPAID' || order.status === 'PAID'"
          type="danger"
          size="large"
          @click="handleCancel"
        >
          取消订单
        </el-button>
        <el-button
          v-if="order.status === 'PAID'"
          type="primary"
          size="large"
          @click="handleShip"
        >
          发货
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, shipOrder, cancelOrder } from '@/api/admin/order'
import { formatPrice, formatDate } from '@/utils/format'
import { ORDER_STATUS_TEXT, ORDER_STATUS_TAG_TYPE } from '@/utils/constants'
import { ElMessage, ElMessageBox } from 'element-plus'
import Loading from '@/components/common/Loading.vue'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(false)

const statusText = ORDER_STATUS_TEXT
const statusTagType = ORDER_STATUS_TAG_TYPE

// 获取订单详情
const fetchOrderDetail = async () => {
  loading.value = true
  try {
    const orderNo = route.params.orderNo
    order.value = await getOrderDetail(orderNo)
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('订单不存在')
    router.push('/admin/orders')
  } finally {
    loading.value = false
  }
}

// 发货
const handleShip = async () => {
  try {
    await ElMessageBox.confirm('确定要发货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    await shipOrder(order.value.orderNo)
    ElMessage.success('发货成功')
    fetchOrderDetail()
  } catch (error) {
    // 用户取消
  }
}

// 取消订单
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？取消后将恢复商品库存。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cancelOrder(order.value.orderNo)
    ElMessage.success('订单已取消')
    fetchOrderDetail()
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.admin-order-detail-page {
  padding: $spacing-lg;

  .page-title {
    font-size: 24px;
    color: $text-primary;
    margin: 0 0 $spacing-lg 0;
  }

  .order-detail {
    max-width: 1200px;
    margin: 0 auto;

    .status-card {
      margin-bottom: $spacing-lg;

      .status-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .order-no {
          font-size: 16px;
          color: $text-secondary;

          .label {
            color: $text-regular;
          }
        }
      }
    }

    .info-card,
    .address-card,
    .products-card {
      margin-bottom: $spacing-lg;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: $spacing-md;

      @include mobile {
        grid-template-columns: 1fr;
      }

      .info-item {
        display: flex;
        line-height: 1.8;

        &.full-width {
          grid-column: 1 / -1;
        }

        .label {
          color: $text-secondary;
          min-width: 100px;
        }
      }
    }

    .address-info {
      p {
        margin: 0;
        line-height: 1.8;

        strong {
          margin-right: $spacing-sm;
        }
      }
    }

    .product-info {
      display: flex;
      align-items: center;
      gap: $spacing-md;

      img {
        width: 60px;
        height: 60px;
        object-fit: cover;
        border-radius: $border-radius-base;
      }
    }

    .subtotal {
      color: $danger-color;
      font-weight: 500;
    }

    .order-summary {
      margin-top: $spacing-lg;
      padding-top: $spacing-lg;
      border-top: 1px solid $border-light;
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: $spacing-sm;

      .summary-item {
        display: flex;
        gap: $spacing-xl;
        font-size: 14px;

        &.total {
          font-size: 16px;
          font-weight: bold;
          margin-top: $spacing-sm;

          .total-price {
            color: $danger-color;
            font-size: 20px;
          }
        }
      }
    }

    .action-section {
      display: flex;
      justify-content: center;
      gap: $spacing-md;
      margin-top: $spacing-xl;

      @include mobile {
        flex-direction: column;
      }
    }
  }
}
</style>
