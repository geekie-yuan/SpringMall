<template>
  <div class="profile-page">
    <div class="container">

      <h1 class="page-title">个人中心</h1>

      <div class="profile-layout">

        <!-- Basic Info Section -->
        <section class="profile-section">
          <h2 class="section-title">基本信息</h2>
          <el-form
            ref="profileFormRef"
            :model="userForm"
            :rules="profileRules"
            label-position="top"
            class="custom-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="userForm.username" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="角色">
              <span class="role-badge" :class="userForm.role === 'ADMIN' ? 'role-admin' : 'role-user'">
                {{ userForm.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </span>
            </el-form-item>
            <el-form-item label="注册时间">
              <span class="info-text">{{ formatDate(userForm.createdAt) }}</span>
            </el-form-item>
            <el-form-item>
              <button
                class="btn-save"
                :disabled="profileSubmitting"
                @click="handleUpdateProfile"
              >
                {{ profileSubmitting ? '保存中…' : '保存修改' }}
              </button>
            </el-form-item>
          </el-form>
        </section>

        <!-- Change Password Section -->
        <section class="profile-section">
          <h2 class="section-title">修改密码</h2>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-position="top"
            class="custom-form"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（至少6位）"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <button
                class="btn-save"
                :disabled="submitting"
                @click="handleUpdatePassword"
              >
                {{ submitting ? '提交中…' : '修改密码' }}
              </button>
            </el-form-item>
          </el-form>
        </section>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import { useUserStore } from '@/store/user'
import { updatePassword } from '@/api/user'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'
import { ValidationError } from '@/api/request'

const authStore = useAuthStore()
const userStore = useUserStore()
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const submitting = ref(false)
const profileSubmitting = ref(false)

const userForm = reactive({
  username: '',
  email: '',
  phone: '',
  role: '',
  createdAt: ''
})

const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '用户名长度为2-30个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { max: 20, message: '手机号不能超过20个字符', trigger: 'blur' }
  ]
}

const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return
  const originalUsername = authStore.user?.username
  try {
    await profileFormRef.value.validate()
    profileSubmitting.value = true
    await userStore.updateUserInfo({
      username: userForm.username,
      email: userForm.email,
      phone: userForm.phone
    })
    if (userForm.username !== originalUsername) {
      ElMessage.success('用户名已修改，请重新登录')
      setTimeout(() => { authStore.logout() }, 1500)
    }
  } catch (error) {
    if (error !== false) {
      console.error('更新个人信息失败:', error)
    }
  } finally {
    profileSubmitting.value = false
  }
}

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const applyServerErrors = (fields) => {
  if (!passwordFormRef.value) return
  const unmatchedErrors = []
  Object.entries(fields).forEach(([field, message]) => {
    const formItem = passwordFormRef.value.fields?.find((item) => item.prop === field)
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

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return
  passwordFormRef.value.clearValidate()
  try {
    await passwordFormRef.value.validate()
    submitting.value = true
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value.resetFields()
    setTimeout(() => { authStore.logout() }, 1500)
  } catch (error) {
    if (error instanceof ValidationError && error.fields) {
      applyServerErrors(error.fields)
    } else if (error !== false) {
      console.error('修改密码失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  const user = authStore.user
  if (user) {
    userForm.username = user.username || ''
    userForm.email = user.email || ''
    userForm.phone = user.phone || ''
    userForm.role = user.role || ''
    userForm.createdAt = user.createdAt || ''
  }
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.profile-page {
  background: $bg-color;
  min-height: calc(100vh - 68px);
  padding: $spacing-lg 0 $spacing-xxl;
}

.page-title {
  font-size: $font-size-xxl;
  font-weight: $font-weight-bold;
  letter-spacing: -0.03em;
  margin-bottom: $spacing-xl;
}

.profile-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-xl;
  align-items: start;
  max-width: 880px;

  @include mobile {
    grid-template-columns: 1fr;
  }
}

.profile-section {
  border: 1px solid $border-light;
  padding: $spacing-xl;
}

.section-title {
  font-size: $font-size-md;
  font-weight: $font-weight-bold;
  letter-spacing: -0.02em;
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-md;
  border-bottom: 1px solid $border-lighter;
}

.custom-form {
  :deep(.el-form-item__label) {
    font-size: $font-size-xs;
    font-weight: $font-weight-bold;
    letter-spacing: 0.06em;
    text-transform: uppercase;
    color: $text-secondary;
    padding-bottom: 4px;
    line-height: 1;
  }

  :deep(.el-input__wrapper) {
    border-radius: 0;
    box-shadow: 0 0 0 1px $border-base;

    &:hover { box-shadow: 0 0 0 1px $text-secondary; }

    &.is-focus {
      box-shadow: 0 0 0 1px $primary-color !important;
    }
  }
}

.role-badge {
  display: inline-block;
  padding: 2px 8px;
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  letter-spacing: 0.06em;
  text-transform: uppercase;

  &.role-admin {
    background: #fee2e2;
    color: #b91c1c;
  }

  &.role-user {
    background: $bg-gray;
    color: $text-secondary;
  }
}

.info-text {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.btn-save {
  padding: 10px 28px;
  background: $primary-color;
  color: #fff;
  border: none;
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
  font-family: $font-family;
  transition: background $transition-base;

  &:hover:not(:disabled) { background: #333; }
  &:disabled { opacity: 0.4; cursor: not-allowed; }
}
</style>
