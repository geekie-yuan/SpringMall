<template>
  <div class="user-layout">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="container">
        <div class="header-content">
          <!-- Logo -->
          <router-link to="/" class="logo">
            <h1>Spring Mall</h1>
          </router-link>

          <!-- 导航菜单 -->
          <nav class="nav">
            <router-link to="/" class="nav-item">首页</router-link>
            <router-link to="/products" class="nav-item">商品</router-link>
            <router-link to="/cart" class="nav-item">
              <el-badge :value="cartCount" :hidden="cartCount === 0">
                购物车
              </el-badge>
            </router-link>
            <router-link to="/orders" class="nav-item">订单</router-link>
          </nav>

          <!-- 用户信息 -->
          <div class="user-info">
            <template v-if="isLoggedIn">
              <el-dropdown @command="handleCommand">
                <span class="user-name">
                  {{ username }}
                  <el-icon><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                    <el-dropdown-item command="address">地址管理</el-dropdown-item>
                    <el-dropdown-item v-if="isAdmin" command="admin">管理后台</el-dropdown-item>
                    <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <router-link to="/login" class="btn-link">登录</router-link>
              <router-link to="/register" class="btn-link">注册</router-link>
            </template>
          </div>
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <div class="container">
        <p>&copy; 2026 Spring Mall. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useCartStore } from '@/store/cart'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

// 计算属性
const isLoggedIn = computed(() => authStore.isLoggedIn)
const username = computed(() => authStore.username)
const isAdmin = computed(() => authStore.isAdmin)
const cartCount = computed(() => cartStore.cartCount)

// 初始化
authStore.initAuth()
// 只有普通用户才加载购物车，管理员不需要
if (isLoggedIn.value && !isAdmin.value) {
  cartStore.fetchCart()
}

// 下拉菜单操作
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'address':
      router.push('/address')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      authStore.logout()
      break
  }
}
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.user-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.header {
  background: $bg-color;
  border-bottom: 1px solid $border-light;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .header-content {
    @include flex-between;
    height: 60px;
  }

  .logo {
    h1 {
      font-size: 24px;
      color: $primary-color;
      font-weight: bold;
      margin: 0;
    }
  }

  .nav {
    display: flex;
    align-items: center;
    gap: $spacing-lg;

    .nav-item {
      color: $text-regular;
      font-size: 16px;
      transition: color 0.3s;

      &:hover,
      &.router-link-active {
        color: $primary-color;
      }
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: $spacing-md;

    .user-name {
      cursor: pointer;
      color: $text-primary;
      display: flex;
      align-items: center;
      gap: 4px;

      &:hover {
        color: $primary-color;
      }
    }

    .btn-link {
      color: $primary-color;
      font-size: 14px;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  @include mobile {
    .nav {
      gap: $spacing-md;

      .nav-item {
        font-size: 14px;
      }
    }
  }
}

.main-content {
  flex: 1;
  background: $bg-page;
}

.footer {
  background: $bg-color;
  border-top: 1px solid $border-light;
  padding: $spacing-lg 0;
  text-align: center;
  color: $text-secondary;
  font-size: 14px;
}
</style>
