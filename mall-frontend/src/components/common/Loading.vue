<template>
  <div class="loading-container" :class="{ fullscreen }">
    <el-icon class="loading-icon" :size="size">
      <Loading />
    </el-icon>
    <p v-if="text" class="loading-text">{{ text }}</p>
  </div>
</template>

<script setup>
defineProps({
  // 是否全屏显示
  fullscreen: {
    type: Boolean,
    default: false
  },
  // 图标大小
  size: {
    type: Number,
    default: 40
  },
  // 加载文本
  text: {
    type: String,
    default: '加载中...'
  }
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-xl;

  &.fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.9);
    z-index: 9999;
  }

  .loading-icon {
    color: $primary-color;
    animation: rotating 2s linear infinite;
  }

  .loading-text {
    margin-top: $spacing-md;
    color: $text-secondary;
    font-size: 14px;
  }
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
