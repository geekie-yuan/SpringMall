<template>
  <div class="cart-page">
    <div class="container">
      <h2 class="page-title">购物车</h2>

      <Loading v-if="loading" />
      <Empty v-else-if="!cartItems.length" type="cart" text="购物车空空如也">
        <template #action>
          <el-button type="primary" @click="$router.push('/products')">
            去逛逛
          </el-button>
        </template>
      </Empty>

      <div v-else class="cart-content">
        <!-- 购物车列表 -->
        <div class="cart-list">
          <div class="cart-header">
            <el-checkbox
              :model-value="isAllChecked"
              @change="handleCheckAll"
            >
              全选
            </el-checkbox>
            <span>商品信息</span>
            <span>单价</span>
            <span>数量</span>
            <span>小计</span>
            <span>操作</span>
          </div>

          <div
            v-for="item in cartItems"
            :key="item.id"
            class="cart-item"
          >
            <el-checkbox
              :model-value="item.checked"
              @change="(val) => handleCheck(item.id, val)"
            />

            <div class="item-info" @click="goToDetail(item.productId)">
              <img :src="item.productImage || '/placeholder.png'" :alt="item.productName" />
              <div class="item-detail">
                <h4>{{ item.productName }}</h4>
                <p>{{ item.productSubtitle }}</p>
              </div>
            </div>

            <span class="item-price">¥{{ formatPrice(item.productPrice) }}</span>

            <el-input-number
              :model-value="item.quantity"
              :min="1"
              :max="item.productStock"
              size="small"
              @change="(val) => handleQuantityChange(item.id, val)"
            />

            <span class="item-subtotal">¥{{ formatPrice(item.productPrice * item.quantity) }}</span>

            <el-button
              type="danger"
              text
              @click="handleRemove(item.id)"
            >
              删除
            </el-button>
          </div>
        </div>

        <!-- 结算栏 -->
        <div class="cart-footer">
          <div class="footer-left">
            <el-checkbox
              :model-value="isAllChecked"
              @change="handleCheckAll"
            >
              全选
            </el-checkbox>
            <el-button text @click="handleRemoveSelected">删除选中商品</el-button>
          </div>

          <div class="footer-right">
            <div class="total-info">
              <span>已选择 {{ checkedCount }} 件商品</span>
              <span class="total-label">总计：</span>
              <span class="total-price">¥{{ formatPrice(checkedTotal) }}</span>
            </div>
            <el-button
              type="primary"
              size="large"
              :disabled="checkedCount === 0"
              @click="handleCheckout"
            >
              结算 ({{ checkedCount }})
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/store/cart'
import { formatPrice } from '@/utils/format'
import { ElMessageBox } from 'element-plus'
import Loading from '@/components/common/Loading.vue'
import Empty from '@/components/common/Empty.vue'

const router = useRouter()
const cartStore = useCartStore()

const loading = ref(false)

// 计算属性
const cartItems = computed(() => cartStore.items)
const isAllChecked = computed(() => cartStore.isAllChecked)
const checkedCount = computed(() => cartStore.checkedCount)
const checkedTotal = computed(() => cartStore.checkedTotal)

// 获取购物车
const fetchCart = async () => {
  loading.value = true
  try {
    await cartStore.fetchCart()
  } finally {
    loading.value = false
  }
}

// 全选/取消全选
const handleCheckAll = (checked) => {
  cartStore.toggleCheckAll(checked)
}

// 选中/取消选中
const handleCheck = (id, checked) => {
  cartStore.toggleCheck(id, checked)
}

// 修改数量
const handleQuantityChange = (id, quantity) => {
  cartStore.updateQuantity(id, quantity)
}

// 删除商品
const handleRemove = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.removeItem(id)
  } catch (error) {
    // 用户取消
  }
}

// 删除选中商品
const handleRemoveSelected = async () => {
  const checkedItems = cartStore.checkedItems
  if (checkedItems.length === 0) {
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${checkedItems.length} 件商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    for (const item of checkedItems) {
      await cartStore.removeItem(item.id)
    }
  } catch (error) {
    // 用户取消
  }
}

// 跳转详情
const goToDetail = (productId) => {
  router.push(`/products/${productId}`)
}

// 去结算
const handleCheckout = () => {
  if (checkedCount.value === 0) {
    return
  }
  router.push('/checkout')
}

onMounted(() => {
  fetchCart()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.cart-page {
  padding: $spacing-xl 0;
  min-height: calc(100vh - 60px);
}

.page-title {
  font-size: 24px;
  color: $text-primary;
  margin-bottom: $spacing-xl;
}

.cart-content {
  .cart-list {
    background: $bg-color;
    border-radius: $border-radius-base;
    overflow: hidden;
    margin-bottom: $spacing-lg;

    .cart-header {
      display: grid;
      grid-template-columns: 50px 1fr 120px 150px 120px 80px;
      gap: $spacing-md;
      padding: $spacing-md $spacing-lg;
      background: $bg-page;
      border-bottom: 1px solid $border-light;
      font-weight: 500;
      color: $text-regular;

      @include mobile {
        display: none;
      }
    }

    .cart-item {
      display: grid;
      grid-template-columns: 50px 1fr 120px 150px 120px 80px;
      gap: $spacing-md;
      padding: $spacing-lg;
      border-bottom: 1px solid $border-lighter;
      align-items: center;

      &:last-child {
        border-bottom: none;
      }

      .item-info {
        display: flex;
        gap: $spacing-md;
        cursor: pointer;

        img {
          width: 80px;
          height: 80px;
          object-fit: cover;
          border-radius: $border-radius-base;
          flex-shrink: 0;
        }

        .item-detail {
          flex: 1;
          min-width: 0;

          h4 {
            @include text-ellipsis;
            font-size: 16px;
            color: $text-primary;
            margin: 0 0 $spacing-sm 0;
          }

          p {
            @include text-ellipsis;
            font-size: 14px;
            color: $text-secondary;
            margin: 0;
          }
        }
      }

      .item-price,
      .item-subtotal {
        color: $danger-color;
        font-weight: 500;
      }

      @include mobile {
        grid-template-columns: 40px 1fr;
        gap: $spacing-sm;

        .item-info {
          grid-column: 1 / -1;

          img {
            width: 60px;
            height: 60px;
          }
        }

        .item-price {
          grid-column: 2;
        }

        .item-subtotal {
          grid-column: 2;
          text-align: right;
        }
      }
    }
  }

  .cart-footer {
    @include flex-between;
    background: $bg-color;
    padding: $spacing-lg;
    border-radius: $border-radius-base;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);

    .footer-left {
      display: flex;
      align-items: center;
      gap: $spacing-lg;
    }

    .footer-right {
      display: flex;
      align-items: center;
      gap: $spacing-xl;

      .total-info {
        display: flex;
        align-items: center;
        gap: $spacing-md;

        .total-label {
          color: $text-regular;
        }

        .total-price {
          font-size: 24px;
          color: $danger-color;
          font-weight: bold;
        }
      }
    }

    @include mobile {
      flex-direction: column;
      gap: $spacing-md;

      .footer-left,
      .footer-right {
        width: 100%;
      }

      .footer-right {
        flex-direction: column;
        gap: $spacing-sm;

        .total-info {
          width: 100%;
          justify-content: space-between;
        }

        .el-button {
          width: 100%;
        }
      }
    }
  }
}
</style>
