<template>
  <div class="order-list-page">
    <div class="container">
      <h2 class="page-title">我的订单</h2>

      <!-- 状态筛选 -->
      <el-tabs v-model="activeStatus" @tab-click="handleTabClick">
        <el-tab-pane label="全部" name="ALL" />
        <el-tab-pane label="待支付" name="UNPAID" />
        <el-tab-pane label="待发货" name="PAID" />
        <el-tab-pane label="待收货" name="SHIPPED" />
        <el-tab-pane label="已完成" name="COMPLETED" />
      </el-tabs>

      <!-- 订单列表 -->
      <Loading v-if="loading" />
      <Empty v-else-if="!orders.length" type="order" text="暂无订单" />
      <div v-else class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <span>订单号：{{ order.orderNo }}</span>
            <span>{{ formatDate(order.createdAt) }}</span>
            <el-tag :type="statusTagType[order.status]">
              {{ statusText[order.status] }}
            </el-tag>
          </div>

          <div class="order-items">
            <div
              v-for="item in order.items"
              :key="item.id"
              class="order-item"
            >
              <img :src="item.productImage || '/placeholder.png'" :alt="item.productName" />
              <div class="item-info">
                <h4>{{ item.productName }}</h4>
                <p>¥{{ formatPrice(item.unitPrice) }} × {{ item.quantity }}</p>
              </div>
            </div>
          </div>

          <div class="order-footer">
            <div class="order-total">
              <span>合计：</span>
              <span class="total-price">¥{{ formatPrice(order.totalAmount) }}</span>
            </div>

            <div class="order-actions">
              <el-button size="small" @click="goToDetail(order.orderNo)">
                查看详情
              </el-button>
              <el-button
                v-if="order.status === 'UNPAID'"
                type="primary"
                size="small"
                @click="goToPay(order.orderNo)"
              >
                去支付
              </el-button>
              <el-button
                v-if="order.status === 'UNPAID' || order.status === 'PAID'"
                type="danger"
                size="small"
                @click="handleCancel(order.orderNo)"
              >
                取消订单
              </el-button>
              <el-button
                v-if="order.status === 'SHIPPED'"
                type="primary"
                size="small"
                @click="handleConfirm(order.orderNo)"
              >
                确认收货
              </el-button>
              <el-button
                v-if="order.status === 'SHIPPED' || order.status === 'COMPLETED'"
                type="info"
                size="small"
                plain
                @click="handleCancelShipped"
              >
                取消订单
              </el-button>
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
import { getOrders, getOrdersByStatus, cancelOrder, confirmOrder } from '@/api/order'
import { formatPrice, formatDate } from '@/utils/format'
import { ORDER_STATUS_TEXT, ORDER_STATUS_TAG_TYPE } from '@/utils/constants'
import { ElMessage, ElMessageBox } from 'element-plus'
import Loading from '@/components/common/Loading.vue'
import Empty from '@/components/common/Empty.vue'

const router = useRouter()

const activeStatus = ref('ALL')
const orders = ref([])
const loading = ref(false)

const statusText = ORDER_STATUS_TEXT
const statusTagType = ORDER_STATUS_TAG_TYPE

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    let data
    if (activeStatus.value === 'ALL') {
      data = await getOrders()
    } else {
      data = await getOrdersByStatus(activeStatus.value)
    }
    orders.value = data.content || data || []
  } catch (error) {
    console.error('获取订单失败:', error)
  } finally {
    loading.value = false
  }
}

// 切换状态
const handleTabClick = () => {
  fetchOrders()
}

// 查看详情
const goToDetail = (orderNo) => {
  router.push(`/orders/${orderNo}`)
}

// 去支付
const goToPay = (orderNo) => {
  router.push(`/payment/${orderNo}`)
}

// 取消订单
const handleCancel = async (orderNo) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cancelOrder(orderNo)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (error) {
    // 用户取消
  }
}

// 确认收货
const handleConfirm = async (orderNo) => {
  try {
    await ElMessageBox.confirm('确认已收到货物吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    await confirmOrder(orderNo)
    ElMessage.success('确认收货成功')
    fetchOrders()
  } catch (error) {
    // 用户取消
  }
}

// 已发货订单取消提醒
const handleCancelShipped = () => {
  ElMessageBox.alert('商品已发货，无法取消订单。如需退货，请联系客服。', '无法取消', {
    confirmButtonText: '我知道了',
    type: 'warning'
  })
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.order-list-page {
  padding: $spacing-xl 0;
  min-height: calc(100vh - 60px);
}

.page-title {
  font-size: 24px;
  color: $text-primary;
  margin-bottom: $spacing-lg;
}

.order-list {
  .order-card {
    background: $bg-color;
    border-radius: $border-radius-base;
    overflow: hidden;
    margin-bottom: $spacing-lg;

    .order-header {
      @include flex-between;
      padding: $spacing-md $spacing-lg;
      background: $bg-page;
      border-bottom: 1px solid $border-light;

      span {
        color: $text-regular;
        font-size: 14px;
      }
    }

    .order-items {
      padding: $spacing-lg;

      .order-item {
        display: flex;
        gap: $spacing-md;
        margin-bottom: $spacing-md;

        &:last-child {
          margin-bottom: 0;
        }

        img {
          width: 80px;
          height: 80px;
          object-fit: cover;
          border-radius: $border-radius-base;
        }

        .item-info {
          flex: 1;

          h4 {
            font-size: 16px;
            color: $text-primary;
            margin: 0 0 $spacing-sm 0;
          }

          p {
            font-size: 14px;
            color: $text-secondary;
            margin: 0;
          }
        }
      }
    }

    .order-footer {
      @include flex-between;
      padding: $spacing-lg;
      border-top: 1px solid $border-lighter;

      .order-total {
        .total-price {
          font-size: 18px;
          color: $danger-color;
          font-weight: bold;
          margin-left: $spacing-sm;
        }
      }

      .order-actions {
        display: flex;
        gap: $spacing-sm;
      }
    }
  }
}
</style>
