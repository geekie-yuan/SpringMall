<template>
  <div class="profile-page">
    <div class="container">
      <h2 class="page-title">个人中心</h2>

      <div class="profile-content">
        <!-- 用户信息 -->
        <el-card class="info-card">
          <template #header>
            <span>基本信息</span>
          </template>
          <el-form
            ref="profileFormRef"
            :model="userForm"
            :rules="profileRules"
            label-width="100px"
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
              <el-tag :type="userForm.role === 'ADMIN' ? 'danger' : 'primary'">
                {{ userForm.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="注册时间">
              <span>{{ formatDate(userForm.createdAt) }}</span>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="profileSubmitting"
                @click="handleUpdateProfile"
              >
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密码 -->
        <el-card class="password-card">
          <template #header>
            <span>修改密码</span>
          </template>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
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
              <el-button
                type="primary"
                :loading="submitting"
                @click="handleUpdatePassword"
              >
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
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

// 用户信息
const userForm = reactive({
  username: '',
  email: '',
  phone: '',
  role: '',
  createdAt: ''
})

// 个人信息验证规则
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

// 保存个人信息
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
    // 用户名变更后需要重新登录（身份验证字段已变）
    if (userForm.username !== originalUsername) {
      ElMessage.success('用户名已修改，请重新登录')
      setTimeout(() => {
        authStore.logout()
      }, 1500)
    }
  } catch (error) {
    if (error !== false) {
      console.error('更新个人信息失败:', error)
    }
  } finally {
    profileSubmitting.value = false
  }
}

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 确认密码验证
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 密码验证规则
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

// 将服务端字段错误应用到密码表单
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

// 修改密码
const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return

  // 重置之前的服务端错误状态
  passwordFormRef.value.clearValidate()

  try {
    await passwordFormRef.value.validate()

    submitting.value = true

    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    ElMessage.success('密码修改成功，请重新登录')

    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value.resetFields()

    // 退出登录
    setTimeout(() => {
      authStore.logout()
    }, 1500)
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
  // 加载用户信息
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
  padding: $spacing-xl 0;
  min-height: calc(100vh - 60px);
}

.page-title {
  font-size: 24px;
  color: $text-primary;
  margin-bottom: $spacing-xl;
}

.profile-content {
  max-width: 800px;

  .info-card,
  .password-card {
    margin-bottom: $spacing-lg;
  }

  :deep(.el-card__header) {
    background: $bg-page;
    font-weight: 500;
  }
}
</style>
