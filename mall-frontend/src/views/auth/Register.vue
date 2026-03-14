<template>
  <div class="auth-page">
    <!-- Left brand panel -->
    <div class="auth-brand">
      <router-link to="/" class="brand-logo">SPRING MALL</router-link>
      <div class="brand-content">
        <h2 class="brand-headline">加入我们<br />开启购物之旅</h2>
        <p class="brand-sub">注册即享专属优惠，每次购物都是一次发现</p>
      </div>
      <p class="brand-footer">© 2025 Spring Mall</p>
    </div>

    <!-- Right form panel -->
    <div class="auth-form-panel">
      <div class="form-wrap">
        <div class="form-header">
          <h1 class="form-title">创建账号</h1>
          <p class="form-sub">已有账号？<router-link to="/login" class="form-link">立即登录</router-link></p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="auth-form"
          @submit.prevent="handleRegister"
        >
          <el-form-item prop="username">
            <label class="field-label">用户名</label>
            <el-input
              v-model="form.username"
              placeholder="3-20位字母、数字或下划线"
              size="large"
              clearable
            />
          </el-form-item>

          <el-form-item prop="email">
            <label class="field-label">邮箱</label>
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
              size="large"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <label class="field-label">密码</label>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="至少6位"
              size="large"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <label class="field-label">确认密码</label>
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password
              clearable
              @keyup.enter="handleRegister"
            />
          </el-form-item>

          <el-form-item prop="agreement">
            <el-checkbox v-model="form.agreement">
              我已阅读并同意
              <span class="form-link">《用户协议》</span>
              和
              <span class="form-link">《隐私政策》</span>
            </el-checkbox>
          </el-form-item>

          <el-form-item>
            <button
              type="button"
              class="btn-submit"
              :disabled="loading"
              @click="handleRegister"
            >
              <span v-if="!loading">创建账号</span>
              <span v-else>注册中…</span>
            </button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'
import { ValidationError } from '@/api/request'

const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

// 表单数据
const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

// 自定义验证：确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 自定义验证：用户协议
const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请阅读并同意用户协议和隐私政策'))
  } else {
    callback()
  }
}

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agreement: [
    { validator: validateAgreement, trigger: 'change' }
  ]
}

// 将服务端字段错误应用到表单
const applyServerErrors = (fields) => {
  if (!formRef.value) return
  const unmatchedErrors = []
  Object.entries(fields).forEach(([field, message]) => {
    const formItem = formRef.value.fields?.find((item) => item.prop === field)
    if (formItem) {
      formItem.validateState = 'error'
      formItem.validateMessage = message
    } else {
      unmatchedErrors.push(message)
    }
  })
  if (unmatchedErrors.length > 0) {
    ElMessage.error(unmatchedErrors.join('；'))
  }
}

// 注册处理
const handleRegister = async () => {
  if (!formRef.value) return

  // 重置之前的服务端错误状态
  formRef.value.clearValidate()

  try {
    // 验证表单
    await formRef.value.validate()

    loading.value = true

    // 调用注册接口
    await authStore.register({
      username: form.username,
      email: form.email,
      password: form.password
    })

    // 注册成功后会显示成功提示并跳转登录页
  } catch (error) {
    if (error instanceof ValidationError && error.fields) {
      applyServerErrors(error.fields)
    } else if (error !== false) {
      // false 是表单验证失败，其他错误记录日志
      console.error('注册失败:', error)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.auth-page {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 100vh;

  @include mobile { grid-template-columns: 1fr; }
}

.auth-brand {
  background: $bg-dark;
  color: $text-inverse;
  display: flex;
  flex-direction: column;
  padding: $spacing-xl;

  @include mobile { display: none; }
}

.brand-logo {
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
  letter-spacing: 0.18em;
  color: $text-inverse;
  text-decoration: none;
}

.brand-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-headline {
  font-size: clamp(32px, 3.5vw, 52px);
  font-weight: $font-weight-bold;
  letter-spacing: -0.03em;
  line-height: 1.15;
  margin-bottom: $spacing-lg;
}

.brand-sub {
  font-size: $font-size-base;
  color: rgba(255, 255, 255, 0.6);
  line-height: $line-height-base;
  max-width: 320px;
}

.brand-footer {
  font-size: $font-size-xs;
  color: rgba(255, 255, 255, 0.35);
}

.auth-form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-xl $spacing-lg;
  background: $bg-color;
  overflow-y: auto;
}

.form-wrap {
  width: 100%;
  max-width: 380px;
  padding: $spacing-lg 0;
}

.form-header {
  margin-bottom: $spacing-xl;
}

.form-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  letter-spacing: -0.03em;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.form-sub {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.form-link {
  color: $text-primary;
  font-weight: $font-weight-medium;
  text-decoration: underline;
  text-underline-offset: 2px;
  cursor: pointer;
}

.field-label {
  display: block;
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: $text-secondary;
  margin-bottom: 6px;
}

.btn-submit {
  width: 100%;
  padding: 15px;
  background: $primary-color;
  color: #fff;
  border: none;
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
  font-family: $font-family;
  transition: background $transition-base;
  margin-top: $spacing-sm;

  &:hover:not(:disabled) { background: #333; }
  &:disabled { opacity: 0.5; cursor: not-allowed; }
}

:deep(.el-form-item) {
  margin-bottom: $spacing-lg;
  display: flex;
  flex-direction: column;

  .el-form-item__content { flex-direction: column; align-items: stretch; }
  .el-form-item__error { font-size: $font-size-xs; }
}

:deep(.el-input__wrapper) {
  border-radius: 0;
  box-shadow: 0 0 0 1px $border-base inset;

  &:hover { box-shadow: 0 0 0 1px $text-regular inset; }
  &.is-focus { box-shadow: 0 0 0 1px $text-primary inset !important; }
}

:deep(.el-checkbox__label) {
  font-size: $font-size-sm;
  color: $text-regular;
}

:deep(.el-checkbox__inner) {
  border-radius: 0;
}
</style>
