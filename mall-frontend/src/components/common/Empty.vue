<template>
  <div class="empty-container">
    <el-icon class="empty-icon" :size="iconSize">
      <component :is="iconComponent" />
    </el-icon>
    <p class="empty-text">{{ text }}</p>
    <slot name="action"></slot>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Box, ShoppingCart, Document, Picture } from '@element-plus/icons-vue'

const props = defineProps({
  // 类型：product, cart, order, image
  type: {
    type: String,
    default: 'default'
  },
  // 提示文本
  text: {
    type: String,
    default: '暂无数据'
  },
  // 图标大小
  iconSize: {
    type: Number,
    default: 80
  }
})

// 根据类型选择图标
const iconComponent = computed(() => {
  const iconMap = {
    product: Box,
    cart: ShoppingCart,
    order: Document,
    image: Picture
  }
  return iconMap[props.type] || Box
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-xl * 2;
  min-height: 300px;

  .empty-icon {
    color: $text-placeholder;
    margin-bottom: $spacing-lg;
  }

  .empty-text {
    color: $text-secondary;
    font-size: 16px;
    margin-bottom: $spacing-lg;
  }
}
</style>
