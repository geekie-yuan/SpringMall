<template>
  <div class="product-manage-page">
    <div class="page-header">
      <h2 class="page-title">商品管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加商品
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品名称"
            clearable
            @input="debouncedSearch"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="selectedCategory"
            placeholder="全部分类"
            clearable
            @change="handleSearch"
          >
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="selectedStatus"
            placeholder="全部状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="在售" value="ON_SALE" />
            <el-option label="已下架" value="OFF_SALE" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 商品列表 -->
    <el-card class="table-card">
      <Loading v-if="loading" />
      <el-table v-else :data="products" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <img
              :src="row.mainImage || '/placeholder.png'"
              :alt="row.name"
              class="product-image"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="200" />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            {{ row.category?.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120" align="right">
          <template #default="{ row }">
            <span class="price-text">¥{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.stock > 10 ? 'success' : row.stock > 0 ? 'warning' : 'danger'">
              {{ row.stock }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              active-text="在售"
              inactive-text="下架"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button text type="primary" @click="handleStock(row)">
              修改库存
            </el-button>
            <el-button text type="danger" @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑商品对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商品' : '添加商品'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="productForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number
            v-model="productForm.price"
            :min="0"
            :step="0.01"
            :precision="2"
            placeholder="请输入价格"
          />
        </el-form-item>
        <el-form-item label="商品库存" prop="stock">
          <el-input-number
            v-model="productForm.stock"
            :min="0"
            placeholder="请输入库存"
          />
        </el-form-item>
        <el-form-item label="商品图片" prop="imageUrl">
          <el-input
            v-model="productForm.imageUrl"
            placeholder="请输入图片URL"
          />
          <div v-if="productForm.imageUrl" class="image-preview">
            <img :src="productForm.imageUrl" alt="预览" />
          </div>
        </el-form-item>
        <el-form-item label="商品详情" prop="detail">
          <el-input
            v-model="productForm.detail"
            type="textarea"
            :rows="4"
            placeholder="请输入商品详情"
          />
        </el-form-item>
        <el-form-item label="商品状态" prop="status">
          <el-radio-group v-model="productForm.status">
            <el-radio value="ON_SALE">在售</el-radio>
            <el-radio value="OFF_SALE">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改库存对话框 -->
    <el-dialog v-model="stockDialogVisible" title="修改库存" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前库存">
          <span>{{ currentProduct?.stock || 0 }}</span>
        </el-form-item>
        <el-form-item label="新库存">
          <el-input-number
            v-model="newStock"
            :min="0"
            placeholder="请输入新库存"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleStockSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  createProduct,
  updateProduct,
  deleteProduct,
  updateProductStatus,
  updateProductStock
} from '@/api/admin/product'
import { getAllProducts, getProductsByCategory, searchProducts } from '@/api/product'
import { getAllCategories } from '@/api/category'
import { formatPrice } from '@/utils/format'
import { debounce } from '@/utils/helpers'
import { ElMessage, ElMessageBox } from 'element-plus'
import Loading from '@/components/common/Loading.vue'

const loading = ref(false)
const products = ref([])
const categories = ref([])
const dialogVisible = ref(false)
const stockDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const searchKeyword = ref('')
const selectedCategory = ref(null)
const selectedStatus = ref(null)

const currentProduct = ref(null)
const newStock = ref(0)

// 商品表单
const productForm = ref({
  id: null,
  name: '',
  categoryId: null,
  price: 0,
  stock: 0,
  imageUrl: '',
  detail: '',
  status: 1
})

// 验证规则
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入商品库存', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请输入商品图片URL', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入商品详情', trigger: 'blur' }]
}

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    products.value = await getAllProducts()
  } catch (error) {
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    categories.value = await getAllCategories()
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 搜索
const handleSearch = async () => {
  loading.value = true
  try {
    if (searchKeyword.value) {
      products.value = await searchProducts(searchKeyword.value)
    } else if (selectedCategory.value) {
      products.value = await getProductsByCategory(selectedCategory.value)
    } else {
      await fetchProducts()
      return
    }

    // 如果有状态筛选，再过滤
    if (selectedStatus.value) {
      products.value = products.value.filter(p => p.status === selectedStatus.value)
    }
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

// 防抖搜索
const debouncedSearch = debounce(handleSearch, 500)

// 添加商品
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑商品
const handleEdit = (product) => {
  isEdit.value = true
  productForm.value = {
    id: product.id,
    name: product.name,
    categoryId: product.category?.id,
    price: product.price,
    stock: product.stock,
    imageUrl: product.mainImage,  // 后端 mainImage -> 前端 imageUrl
    detail: product.detail,
    status: product.status
  }
  dialogVisible.value = true
}

// 删除商品
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteProduct(id)
    ElMessage.success('删除成功')
    fetchProducts()
  } catch (error) {
    // 用户取消
  }
}

// 切换上下架
const handleStatusChange = async (product) => {
  try {
    const newStatus = product.status === 1 ? 0 : 1
    await updateProductStatus(product.id, newStatus)
    ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
    fetchProducts()
  } catch (error) {
    console.error('修改状态失败:', error)
  }
}

// 修改库存
const handleStock = (product) => {
  currentProduct.value = product
  newStock.value = product.stock
  stockDialogVisible.value = true
}

// 提交库存修改
const handleStockSubmit = async () => {
  submitting.value = true
  try {
    await updateProductStock(currentProduct.value.id, newStock.value)
    ElMessage.success('库存修改成功')
    stockDialogVisible.value = false
    fetchProducts()
  } catch (error) {
    console.error('修改库存失败:', error)
  } finally {
    submitting.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitting.value = true

    // 字段映射：前端 imageUrl -> 后端 mainImage
    const data = { ...productForm.value }
    delete data.id
    data.mainImage = data.imageUrl
    delete data.imageUrl

    if (isEdit.value) {
      await updateProduct(productForm.value.id, data)
      ElMessage.success('修改成功')
    } else {
      await createProduct(data)
      ElMessage.success('添加成功')
    }

    dialogVisible.value = false
    fetchProducts()
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
  productForm.value = {
    id: null,
    name: '',
    categoryId: null,
    price: 0,
    stock: 0,
    imageUrl: '',
    detail: '',
    status: 1
  }

  if (formRef.value) {
    formRef.value.resetFields()
  }
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.product-manage-page {
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

  .filter-card {
    margin-bottom: $spacing-lg;
  }

  .table-card {
    .product-image {
      width: 60px;
      height: 60px;
      object-fit: cover;
      border-radius: $border-radius-base;
    }

    .price-text {
      color: $danger-color;
      font-weight: 500;
    }
  }

  .image-preview {
    margin-top: $spacing-sm;

    img {
      max-width: 200px;
      max-height: 200px;
      border-radius: $border-radius-base;
    }
  }
}
</style>
