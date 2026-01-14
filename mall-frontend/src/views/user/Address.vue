<template>
  <div class="address-page">
    <div class="container">
      <div class="page-header">
        <h2 class="page-title">地址管理</h2>
        <el-button type="primary" @click="handleAdd">
          添加新地址
        </el-button>
      </div>

      <Loading v-if="loading" />
      <Empty v-else-if="!addresses.length" text="暂无地址">
        <template #action>
          <el-button type="primary" @click="handleAdd">
            添加新地址
          </el-button>
        </template>
      </Empty>
      <div v-else class="address-list">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          class="address-item"
        >
          <div class="address-info">
            <div class="info-header">
              <span class="name">{{ addr.receiverName }}</span>
              <span class="phone">{{ addr.phone }}</span>
              <el-tag v-if="addr.isDefault === 1" type="primary" size="small">默认</el-tag>
            </div>
            <p class="detail">
              {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detailAddress }}
            </p>
          </div>

          <div class="address-actions">
            <el-button
              v-if="addr.isDefault !== 1"
              text
              type="primary"
              @click="handleSetDefault(addr.id)"
            >
              设为默认
            </el-button>
            <el-button text @click="handleEdit(addr)">编辑</el-button>
            <el-button text type="danger" @click="handleDelete(addr.id)">删除</el-button>
          </div>
        </div>
      </div>

      <!-- 添加/编辑地址对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑地址' : '添加地址'"
        width="500px"
      >
        <el-form
          ref="formRef"
          :model="addressForm"
          :rules="rules"
          label-width="100px"
        >
          <el-form-item label="收货人" prop="receiverName">
            <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="receiverPhone">
            <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="省" prop="province">
            <el-input v-model="addressForm.province" placeholder="请输入省" />
          </el-form-item>
          <el-form-item label="市" prop="city">
            <el-input v-model="addressForm.city" placeholder="请输入市" />
          </el-form-item>
          <el-form-item label="区/县" prop="district">
            <el-input v-model="addressForm.district" placeholder="请输入区/县" />
          </el-form-item>
          <el-form-item label="详细地址" prop="detail">
            <el-input
              v-model="addressForm.detail"
              type="textarea"
              :rows="3"
              placeholder="请输入详细地址"
            />
          </el-form-item>
          <el-form-item label="默认地址">
            <el-switch v-model="addressForm.isDefault" />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            确定
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  getAddresses,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress
} from '@/api/address'
import { ElMessage, ElMessageBox } from 'element-plus'
import Loading from '@/components/common/Loading.vue'
import Empty from '@/components/common/Empty.vue'

const addresses = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

// 地址表单
const addressForm = reactive({
  id: null,
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

// 验证规则
const rules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  province: [
    { required: true, message: '请输入省', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入市', trigger: 'blur' }
  ],
  district: [
    { required: true, message: '请输入区/县', trigger: 'blur' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 获取地址列表
const fetchAddresses = async () => {
  loading.value = true
  try {
    addresses.value = await getAddresses()
  } catch (error) {
    console.error('获取地址失败:', error)
  } finally {
    loading.value = false
  }
}

// 添加地址
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑地址
const handleEdit = (addr) => {
  isEdit.value = true
  // 字段映射：后端 -> 前端
  addressForm.id = addr.id
  addressForm.receiverName = addr.receiverName
  addressForm.receiverPhone = addr.phone              // phone -> receiverPhone
  addressForm.province = addr.province
  addressForm.city = addr.city
  addressForm.district = addr.district
  addressForm.detail = addr.detailAddress             // detailAddress -> detail
  addressForm.isDefault = addr.isDefault === 1        // 0/1 -> boolean
  dialogVisible.value = true
}

// 删除地址
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteAddress(id)
    ElMessage.success('删除成功')
    fetchAddresses()
  } catch (error) {
    // 用户取消
  }
}

// 设为默认
const handleSetDefault = async (id) => {
  try {
    await setDefaultAddress(id)
    ElMessage.success('设置成功')
    fetchAddresses()
  } catch (error) {
    console.error('设置默认地址失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitting.value = true

    // 字段映射：前端 -> 后端
    const data = {
      receiverName: addressForm.receiverName,
      phone: addressForm.receiverPhone,        // receiverPhone -> phone
      province: addressForm.province,
      city: addressForm.city,
      district: addressForm.district,
      detailAddress: addressForm.detail,        // detail -> detailAddress
      isDefault: addressForm.isDefault ? 1 : 0  // boolean -> 0/1
    }

    if (isEdit.value) {
      await updateAddress(addressForm.id, data)
      ElMessage.success('修改成功')
    } else {
      await addAddress(data)
      ElMessage.success('添加成功')
    }

    dialogVisible.value = false
    fetchAddresses()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  addressForm.id = null
  addressForm.receiverName = ''
  addressForm.receiverPhone = ''
  addressForm.province = ''
  addressForm.city = ''
  addressForm.district = ''
  addressForm.detail = ''
  addressForm.isDefault = false

  if (formRef.value) {
    formRef.value.resetFields()
  }
}

onMounted(() => {
  fetchAddresses()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.address-page {
  padding: $spacing-xl 0;
  min-height: calc(100vh - 60px);
}

.page-header {
  @include flex-between;
  margin-bottom: $spacing-xl;

  .page-title {
    font-size: 24px;
    color: $text-primary;
    margin: 0;
  }
}

.address-list {
  display: grid;
  gap: $spacing-lg;

  .address-item {
    @include flex-between;
    background: $bg-color;
    padding: $spacing-lg;
    border-radius: $border-radius-base;
    border: 2px solid $border-light;
    transition: border-color 0.3s;

    &:hover {
      border-color: $primary-color;
    }

    .address-info {
      flex: 1;

      .info-header {
        display: flex;
        align-items: center;
        gap: $spacing-md;
        margin-bottom: $spacing-sm;

        .name {
          font-weight: 500;
          color: $text-primary;
        }

        .phone {
          color: $text-regular;
        }
      }

      .detail {
        color: $text-secondary;
        margin: 0;
        line-height: 1.6;
      }
    }

    .address-actions {
      display: flex;
      flex-direction: column;
      gap: $spacing-xs;
    }

    @include mobile {
      flex-direction: column;
      gap: $spacing-md;

      .address-actions {
        flex-direction: row;
        justify-content: flex-end;
      }
    }
  }
}
</style>
