<template>
  <div class="application-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="申请状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="已申请" value="已申请" />
            <el-option label="已通过" value="已通过" />
            <el-option label="已拒绝" value="已拒绝" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="applicationId" label="申请ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="positionId" label="岗位ID" width="100" />
        <el-table-column prop="positionName" label="岗位名称" width="150" />
        <el-table-column prop="resumePdfPath" label="简历路径" width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="申请状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === '已申请'" type="warning">已申请</el-tag>
            <el-tag v-else-if="row.status === '已通过'" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column prop="approveTime" label="审批时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '已申请'" size="small" type="success" @click="handleApprove(row, '已通过')">
              通过
            </el-button>
            <el-button v-if="row.status === '已申请'" size="small" type="warning" @click="handleApprove(row, '已拒绝')">
              拒绝
            </el-button>
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="申请ID">{{ currentRow.applicationId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentRow.userId }}</el-descriptions-item>
        <el-descriptions-item label="岗位ID">{{ currentRow.positionId }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称">{{ currentRow.positionName }}</el-descriptions-item>
        <el-descriptions-item label="简历路径">{{ currentRow.resumePdfPath }}</el-descriptions-item>
        <el-descriptions-item label="申请状态">{{ currentRow.status }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ currentRow.approveTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApplications, approveApplication, deleteApplication } from '../../api/application'

const searchForm = reactive({
  status: null
})

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailVisible = ref(false)
const currentRow = ref({})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    const res = await getApplications(params)
    tableData.value = res.data.records || res.data
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.status = null
  handleSearch()
}

// 审批
const handleApprove = (row, status) => {
  const tip = status === '已通过' ? '通过' : '拒绝'
  ElMessageBox.confirm(`确定要${tip}该申请吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await approveApplication(row.applicationId, { status })
      ElMessage.success(`${tip}成功`)
      loadData()
    } catch (error) {
      console.error('审批失败:', error)
      ElMessage.error('审批失败')
    }
  }).catch(() => {})
}

// 查看
const handleView = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该申请记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteApplication(row.applicationId)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  loadData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.application-management {
  width: 100%;
}

.search-card {
  margin-bottom: 20px;
}
</style>
