<template>
  <div class="category-manage-page">
    <div class="page-header">
      <h2 class="page-title">分类管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加分类
      </el-button>
    </div>

    <!-- 分类列表 -->
    <el-card class="category-card">
      <Loading v-if="loading" />
      <Empty v-else-if="!categories.length" text="暂无分类" />
      <div v-else class="category-list">
        <div
          v-for="category in categories"
          :key="category.id"
          class="category-item"
        >
          <div class="category-info">
            <el-icon class="category-icon" :size="24"><Menu /></el-icon>
            <div class="category-content">
              <h4 class="category-name">{{ category.name }}</h4>
              <p class="category-desc">
                {{ category.level === 1 ? '一级分类' : category.level === 2 ? '二级分类' : '三级分类' }}
                {{ category.parentId === 0 ? ' (顶级)' : '' }}
              </p>
            </div>
          </div>

          <div class="category-actions">
            <el-tag size="small">
              商品数: {{ category.productCount || 0 }}
            </el-tag>
            <el-button text type="primary" @click="handleEdit(category)">
              编辑
            </el-button>
            <el-button text type="danger" @click="handleDelete(category.id)">
              删除
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '添加分类'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="categoryForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父分类" prop="parentId">
          <el-select
            v-model="categoryForm.parentId"
            placeholder="请选择父分类"
            style="width: 100%"
            @change="handleParentChange"
          >
            <el-option label="顶级分类（一级分类）" :value="0" />
            <el-option
              v-for="cat in categories.filter(c => c.level < 3)"
              :key="cat.id"
              :label="`${cat.name} (${getLevelText(cat.level)})`"
              :value="cat.id"
              :disabled="cat.level >= 3"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分类层级">
          <el-input :value="getLevelText(categoryForm.level)" disabled />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            层级由父分类自动决定
          </div>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="categoryForm.sortOrder"
            :min="0"
            placeholder="数字越小越靠前"
          />
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
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Menu } from '@element-plus/icons-vue'
import {
  createCategory,
  updateCategory,
  deleteCategory
} from '@/api/admin/category'
import { getAllCategories } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'
import Loading from '@/components/common/Loading.vue'
import Empty from '@/components/common/Empty.vue'

const loading = ref(false)
const categories = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

// 分类表单
const categoryForm = ref({
  id: null,
  name: '',
  parentId: 0,
  level: 1,
  sortOrder: 0
})

// 验证规则
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  parentId: [{ required: true, message: '请选择父分类', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

// 获取层级文本
const getLevelText = (level) => {
  const levelMap = {
    1: '一级分类',
    2: '二级分类',
    3: '三级分类'
  }
  return levelMap[level] || '未知'
}

// 父分类改变时，自动设置层级
const handleParentChange = (parentId) => {
  if (parentId === 0) {
    // 顶级分类，层级为1
    categoryForm.value.level = 1
  } else {
    // 子分类，层级为父分类层级+1
    const parentCategory = categories.value.find(c => c.id === parentId)
    if (parentCategory) {
      categoryForm.value.level = parentCategory.level + 1
    }
  }
}

// 获取分类列表
const fetchCategories = async () => {
  loading.value = true
  try {
    const data = await getAllCategories()
    // 按排序字段排序
    categories.value = data.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  } catch (error) {
    console.error('获取分类列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 添加分类
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (category) => {
  isEdit.value = true
  categoryForm.value = {
    id: category.id,
    name: category.name,
    parentId: category.parentId || 0,
    level: category.level || 1,
    sortOrder: category.sortOrder || 0
  }
  dialogVisible.value = true
}

// 删除分类
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(
      '删除分类后，该分类下的商品将无法显示分类信息。确定要删除吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteCategory(id)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch (error) {
    // 用户取消
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitting.value = true

    const data = { ...categoryForm.value }
    delete data.id

    if (isEdit.value) {
      await updateCategory(categoryForm.value.id, data)
      ElMessage.success('修改成功')
    } else {
      await createCategory(data)
      ElMessage.success('添加成功')
    }

    dialogVisible.value = false
    fetchCategories()
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
  categoryForm.value = {
    id: null,
    name: '',
    parentId: 0,
    level: 1,
    sortOrder: 0
  }

  if (formRef.value) {
    formRef.value.resetFields()
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.category-manage-page {
  padding: $spacing-lg;

  .page-header {
    @include flex-between;
    margin-bottom: $spacing-lg;

    .page-title {
      font-size: 24px;
      color: $text-primary;
      margin: 0;
    }
  }

  .category-card {
    .category-list {
      display: grid;
      gap: $spacing-md;

      .category-item {
        @include flex-between;
        padding: $spacing-lg;
        background: $bg-page;
        border-radius: $border-radius-base;
        border: 2px solid $border-light;
        transition: all 0.3s;

        &:hover {
          border-color: $primary-color;
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
        }

        .category-info {
          display: flex;
          align-items: center;
          gap: $spacing-md;

          .category-icon {
            color: $primary-color;
          }

          .category-content {
            .category-name {
              font-size: 18px;
              color: $text-primary;
              margin: 0 0 $spacing-xs 0;
            }

            .category-desc {
              font-size: 14px;
              color: $text-secondary;
              margin: 0;
            }
          }
        }

        .category-actions {
          display: flex;
          align-items: center;
          gap: $spacing-sm;
        }

        @include mobile {
          flex-direction: column;
          gap: $spacing-md;
          align-items: flex-start;

          .category-actions {
            width: 100%;
            justify-content: space-between;
          }
        }
      }
    }
  }
}
</style>
