<template>
  <div class="user-manage-page">
    <h2 class="page-title">用户管理</h2>

    <!-- 搜索栏 -->
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="搜索用户">
          <el-input
            v-model="searchKeyword"
            placeholder="用户名或邮箱"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="selectedRole"
            placeholder="全部角色"
            clearable
            @change="handleRoleFilter"
          >
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="selectedStatus"
            placeholder="全部状态"
            clearable
            @change="handleStatusChange"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <Loading v-if="loading" />
      <el-table v-else :data="users" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              active-text="启用"
              inactive-text="禁用"
              @change="handleStatusToggle(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== 'ADMIN'"
              text
              type="warning"
              @click="handleRoleChange(row, 'ADMIN')"
            >
              设为管理员
            </el-button>
            <el-button
              v-else
              text
              type="primary"
              @click="handleRoleChange(row, 'USER')"
            >
              设为用户
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchUsers"
          @current-change="fetchUsers"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getAllUsers, updateUserStatus, updateUserRole } from '@/api/admin/user'
import { formatDate } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import Loading from '@/components/common/Loading.vue'

const loading = ref(false)
const users = ref([])
const allUsers = ref([])

const searchKeyword = ref('')
const selectedRole = ref(null)
const selectedStatus = ref(null)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const data = await getAllUsers()
    allUsers.value = data.content || data || []
    filterUsers()
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 筛选用户
const filterUsers = () => {
  let filtered = [...allUsers.value]

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(
      u =>
        u.username.toLowerCase().includes(keyword) ||
        u.email.toLowerCase().includes(keyword)
    )
  }

  // 角色筛选
  if (selectedRole.value) {
    filtered = filtered.filter(u => u.role === selectedRole.value)
  }

  // 状态筛选
  if (selectedStatus.value) {
    filtered = filtered.filter(u => u.status === selectedStatus.value)
  }

  total.value = filtered.length

  // 分页
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  users.value = filtered.slice(start, end)
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  filterUsers()
}

// 角色筛选
const handleRoleFilter = () => {
  currentPage.value = 1
  filterUsers()
}

// 状态筛选
const handleStatusChange = () => {
  currentPage.value = 1
  filterUsers()
}

// 切换状态
const handleStatusToggle = async (user) => {
  try {
    const newStatus = user.status === 1 ? 0 : 1
    const action = newStatus === 1 ? '启用' : '禁用'

    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await updateUserStatus(user.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchUsers()
  } catch (error) {
    // 用户取消
  }
}

// 修改角色
const handleRoleChange = async (user, newRole) => {
  try {
    const roleName = newRole === 'ADMIN' ? '管理员' : '普通用户'

    await ElMessageBox.confirm(`确定要将该用户设为${roleName}吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await updateUserRole(user.id, newRole)
    ElMessage.success('修改成功')
    fetchUsers()
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.user-manage-page {
  padding: $spacing-lg;

  .page-title {
    font-size: 24px;
    color: $text-primary;
    margin: 0 0 $spacing-lg 0;
  }

  .filter-card {
    margin-bottom: $spacing-lg;
  }

  .table-card {
    .pagination {
      margin-top: $spacing-lg;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
