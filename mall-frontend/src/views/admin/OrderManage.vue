<template>
  <div class="order-manage-page">
    <h2 class="page-title">订单管理</h2>

    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="订单号">
          <el-input
            v-model="searchOrderNo"
            placeholder="请输入订单号"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select
            v-model="selectedStatus"
            placeholder="全部状态"
            clearable
            @change="handleStatusChange"
          >
            <el-option label="待支付" value="UNPAID" />
            <el-option label="待发货" value="PAID" />
            <el-option label="待收货" value="SHIPPED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单列表 -->
    <el-card class="table-card">
      <Loading v-if="loading" />
      <el-table v-else :data="orders" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="用户ID" width="100" align="center">
          <template #default="{ row }">
            {{ row.userId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="收货人" width="120">
          <template #default="{ row }">
            {{ row.receiverName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="商品数量" width="100" align="center">
          <template #default="{ row }">
            {{ row.items?.length || 0 }}
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
        <el-table-column label="支付时间" width="160">
          <template #default="{ row }">
            {{ row.paymentTime ? formatDate(row.paymentTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              text
              type="primary"
              @click="$router.push(`/admin/orders/${row.orderNo}`)"
            >
              查看详情
            </el-button>
            <el-button
              v-if="row.status === 'UNPAID' || row.status === 'PAID'"
              text
              type="danger"
              @click="handleCancel(row.orderNo)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.status === 'PAID'"
              text
              type="success"
              @click="handleShip(row.orderNo)"
            >
              发货
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchOrders"
          @current-change="fetchOrders"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getAllOrders, shipOrder, cancelOrder, getOrderDetail } from '@/api/admin/order'
import { formatPrice, formatDate } from '@/utils/format'
import { ORDER_STATUS_TEXT, ORDER_STATUS_TAG_TYPE } from '@/utils/constants'
import { ElMessage, ElMessageBox } from 'element-plus'
import Loading from '@/components/common/Loading.vue'

const loading = ref(false)
const orders = ref([])
const searchOrderNo = ref('')
const selectedStatus = ref(null)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusText = ORDER_STATUS_TEXT
const statusTagType = ORDER_STATUS_TAG_TYPE

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const data = await getAllOrders()
    let orderList = data.content || data || []

    // 状态筛选
    if (selectedStatus.value) {
      orderList = orderList.filter(o => o.status === selectedStatus.value)
    }

    total.value = orderList.length

    // 分页
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    orders.value = orderList.slice(start, end)
  } catch (error) {
    console.error('获取订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索订单
const handleSearch = async () => {
  if (!searchOrderNo.value.trim()) {
    fetchOrders()
    return
  }

  loading.value = true
  try {
    const order = await getOrderDetail(searchOrderNo.value)
    orders.value = [order]
    total.value = 1
  } catch (error) {
    ElMessage.error('订单不存在')
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 状态筛选
const handleStatusChange = () => {
  currentPage.value = 1
  fetchOrders()
}

// 发货
const handleShip = async (orderNo) => {
  try {
    await ElMessageBox.confirm('确定要发货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    await shipOrder(orderNo)
    ElMessage.success('发货成功')
    fetchOrders()
  } catch (error) {
    // 用户取消
  }
}

// 取消订单
const handleCancel = async (orderNo) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？取消后将恢复商品库存。', '提示', {
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

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.order-manage-page {
  padding: $spacing-lg;

  .page-title {
    font-size: 24px;
    color: $text-primary;
    margin: 0 0 $spacing-lg 0;
  }

  .filter-card {
    margin-bottom: $spacing-lg;
  }

  .table-card {
    .price-text {
      color: $danger-color;
      font-weight: 500;
    }

    .pagination {
      margin-top: $spacing-lg;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
