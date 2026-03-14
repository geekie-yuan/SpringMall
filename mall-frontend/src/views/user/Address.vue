<template>
  <div class="address-page">
    <div class="container">

      <div class="page-header">
        <h1 class="page-title">地址管理</h1>
        <button class="btn-add" @click="handleAdd">+ 添加新地址</button>
      </div>

      <Loading v-if="loading" />
      <Empty v-else-if="!addresses.length" text="暂无收货地址">
        <template #action>
          <button class="btn-add" @click="handleAdd">添加新地址</button>
        </template>
      </Empty>

      <div v-else class="address-list">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          class="address-card"
          :class="{ 'is-default': addr.isDefault === 1 }"
        >
          <div class="addr-info">
            <div class="addr-head">
              <span class="addr-name">{{ addr.receiverName }}</span>
              <span class="addr-phone">{{ addr.phone }}</span>
              <span v-if="addr.isDefault === 1" class="default-tag">默认</span>
            </div>
            <p class="addr-detail">
              {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detailAddress }}
            </p>
          </div>

          <div class="addr-actions">
            <button
              v-if="addr.isDefault !== 1"
              class="action-link"
              @click="handleSetDefault(addr.id)"
            >设为默认</button>
            <button class="action-link" @click="handleEdit(addr)">编辑</button>
            <button class="action-link action-link--danger" @click="handleDelete(addr.id)">删除</button>
          </div>
        </div>
      </div>

      <!-- Add/Edit Dialog -->
      <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑地址' : '添加地址'"
        width="480px"
        :close-on-click-modal="false"
        class="address-dialog"
      >
        <el-form
          ref="formRef"
          :model="addressForm"
          :rules="rules"
          label-position="top"
          class="dialog-form"
        >
          <div class="form-row">
            <el-form-item label="收货人" prop="receiverName">
              <el-input v-model="addressForm.receiverName" placeholder="姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="receiverPhone">
              <el-input v-model="addressForm.receiverPhone" placeholder="手机号" />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="省" prop="province">
              <el-input v-model="addressForm.province" placeholder="省" />
            </el-form-item>
            <el-form-item label="市" prop="city">
              <el-input v-model="addressForm.city" placeholder="市" />
            </el-form-item>
            <el-form-item label="区/县" prop="district">
              <el-input v-model="addressForm.district" placeholder="区/县" />
            </el-form-item>
          </div>
          <el-form-item label="详细地址" prop="detail">
            <el-input
              v-model="addressForm.detail"
              type="textarea"
              :rows="3"
              placeholder="详细地址"
            />
          </el-form-item>
          <el-form-item label="设为默认地址">
            <el-switch v-model="addressForm.isDefault" />
          </el-form-item>
        </el-form>

        <template #footer>
          <button class="dialog-btn dialog-btn--cancel" @click="dialogVisible = false">取消</button>
          <button
            class="dialog-btn dialog-btn--confirm"
            :disabled="submitting"
            @click="handleSubmit"
          >{{ submitting ? '提交中…' : '确定' }}</button>
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

const rules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省', trigger: 'blur' }],
  city: [{ required: true, message: '请输入市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区/县', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

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

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (addr) => {
  isEdit.value = true
  addressForm.id = addr.id
  addressForm.receiverName = addr.receiverName
  addressForm.receiverPhone = addr.phone
  addressForm.province = addr.province
  addressForm.city = addr.city
  addressForm.district = addr.district
  addressForm.detail = addr.detailAddress
  addressForm.isDefault = addr.isDefault === 1
  dialogVisible.value = true
}

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
  } catch {}
}

const handleSetDefault = async (id) => {
  try {
    await setDefaultAddress(id)
    ElMessage.success('设置成功')
    fetchAddresses()
  } catch (error) {
    console.error('设置默认地址失败:', error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    const data = {
      receiverName: addressForm.receiverName,
      phone: addressForm.receiverPhone,
      province: addressForm.province,
      city: addressForm.city,
      district: addressForm.district,
      detailAddress: addressForm.detail,
      isDefault: addressForm.isDefault ? 1 : 0
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

const resetForm = () => {
  addressForm.id = null
  addressForm.receiverName = ''
  addressForm.receiverPhone = ''
  addressForm.province = ''
  addressForm.city = ''
  addressForm.district = ''
  addressForm.detail = ''
  addressForm.isDefault = false
  if (formRef.value) formRef.value.resetFields()
}

onMounted(fetchAddresses)
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.address-page {
  background: $bg-color;
  min-height: calc(100vh - 68px);
  padding: $spacing-lg 0 $spacing-xxl;
}

.page-header {
  @include flex-between;
  margin-bottom: $spacing-xl;
  padding-bottom: $spacing-md;
  border-bottom: 1px solid $border-light;
}

.page-title {
  font-size: $font-size-xxl;
  font-weight: $font-weight-bold;
  letter-spacing: -0.03em;
  margin: 0;
}

.btn-add {
  padding: 10px 20px;
  background: $primary-color;
  color: #fff;
  border: none;
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  cursor: pointer;
  font-family: $font-family;
  transition: background $transition-base;

  &:hover { background: #333; }
}

// Address list
.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: $spacing-lg;
}

.address-card {
  border: 1px solid $border-light;
  padding: $spacing-lg;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  transition: border-color $transition-base;

  &:hover { border-color: $text-secondary; }

  &.is-default {
    border-color: $primary-color;
  }
}

.addr-info { flex: 1; }

.addr-head {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.addr-name {
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
  color: $text-primary;
}

.addr-phone {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.default-tag {
  padding: 1px 6px;
  background: $primary-color;
  color: #fff;
  font-size: 10px;
  font-weight: $font-weight-bold;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.addr-detail {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin: 0;
  line-height: 1.6;
}

.addr-actions {
  display: flex;
  gap: $spacing-md;
  border-top: 1px solid $border-lighter;
  padding-top: $spacing-sm;
}

.action-link {
  background: none;
  border: none;
  font-size: $font-size-xs;
  color: $text-secondary;
  cursor: pointer;
  font-family: $font-family;
  padding: 0;
  transition: color $transition-base;

  &:hover { color: $text-primary; }

  &--danger:hover { color: $danger-color; }
}

// Dialog overrides
.address-dialog {
  :deep(.el-dialog) { border-radius: 0; }
  :deep(.el-dialog__header) { border-bottom: 1px solid $border-lighter; padding: $spacing-md $spacing-lg; }
  :deep(.el-dialog__title) {
    font-size: $font-size-md;
    font-weight: $font-weight-bold;
  }
  :deep(.el-dialog__footer) {
    border-top: 1px solid $border-lighter;
    padding: $spacing-md $spacing-lg;
    display: flex;
    justify-content: flex-end;
    gap: $spacing-sm;
  }
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
  gap: $spacing-md;
}

.dialog-form {
  :deep(.el-form-item__label) {
    font-size: $font-size-xs;
    font-weight: $font-weight-bold;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: $text-secondary;
    padding-bottom: 4px;
    line-height: 1;
  }

  :deep(.el-input__wrapper) {
    border-radius: 0;
    box-shadow: 0 0 0 1px $border-base;
    &:hover { box-shadow: 0 0 0 1px $text-secondary; }
    &.is-focus { box-shadow: 0 0 0 1px $primary-color !important; }
  }

  :deep(.el-textarea__inner) {
    border-radius: 0;
    box-shadow: 0 0 0 1px $border-base;
    &:hover { box-shadow: 0 0 0 1px $text-secondary; }
    &:focus { box-shadow: 0 0 0 1px $primary-color !important; }
  }
}

.dialog-btn {
  padding: 9px 24px;
  border: 1px solid transparent;
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  letter-spacing: 0.05em;
  cursor: pointer;
  font-family: $font-family;
  transition: background $transition-base, border-color $transition-base;

  &--cancel {
    background: transparent;
    color: $text-secondary;
    border-color: $border-base;
    &:hover { border-color: $primary-color; color: $text-primary; }
  }

  &--confirm {
    background: $primary-color;
    color: #fff;
    &:hover:not(:disabled) { background: #333; }
    &:disabled { opacity: 0.4; cursor: not-allowed; }
  }
}
</style>
