<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>Spring Mall</h1>
        <p>欢迎登录</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            clearable
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="form.remember">记住我</el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="link">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

// 表单数据
const form = reactive({
  username: '',
  password: '',
  remember: false
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

// 登录处理
const handleLogin = async () => {
  if (!formRef.value) return

  try {
    // 验证表单
    await formRef.value.validate()

    loading.value = true

    // 调用登录接口
    await authStore.login({
      username: form.username,
      password: form.password
    })

    // 登录成功后会在 store 中自动跳转
  } catch (error) {
    if (error !== false) {
      // false 是表单验证失败
      console.error('登录失败:', error)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: $spacing-lg;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background: $bg-color;
  border-radius: $border-radius-lg;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  padding: $spacing-xl;

  @include mobile {
    padding: $spacing-lg;
  }
}

.login-header {
  text-align: center;
  margin-bottom: $spacing-xl;

  h1 {
    font-size: 32px;
    color: $primary-color;
    margin: 0 0 $spacing-sm 0;
    font-weight: bold;
  }

  p {
    font-size: 16px;
    color: $text-secondary;
    margin: 0;
  }
}

.login-form {
  .login-btn {
    width: 100%;
    margin-top: $spacing-md;
  }
}

.login-footer {
  text-align: center;
  margin-top: $spacing-md;
  font-size: 14px;
  color: $text-secondary;

  .link {
    color: $primary-color;
    margin-left: 4px;

    &:hover {
      text-decoration: underline;
    }
  }
}

:deep(.el-form-item) {
  margin-bottom: $spacing-lg;
}

:deep(.el-checkbox) {
  .el-checkbox__label {
    color: $text-regular;
  }
}
</style>
