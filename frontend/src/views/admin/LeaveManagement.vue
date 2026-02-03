<template>
  <div class="leave-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="员工名称">
          <el-input v-model="searchForm.userName" placeholder="请输入员工名称" clearable @keyup.enter="handleSearch" style="width: 180px" />
        </el-form-item>
        <el-form-item label="岗位名称">
          <el-input v-model="searchForm.positionName" placeholder="请输入岗位名称" clearable @keyup.enter="handleSearch" style="width: 180px" />
        </el-form-item>
        <el-form-item label="请假状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请假状态" 
            clearable
            @change="handleSearch"
            @clear="handleSearch"
            style="width: 150px"
          >
            <el-option label="申请中" value="申请中" />
            <el-option label="已通过" value="已通过" />
            <el-option label="已拒绝" value="已拒绝" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="leaveRequestId" label="请假ID" width="80" />
        <el-table-column label="员工信息" width="150">
          <template #default="{ row }">
            <div>{{ row.userName || '-' }}</div>
            <div style="color: #909399; font-size: 12px;">ID: {{ row.userId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="岗位信息" width="180">
          <template #default="{ row }">
            <div>{{ row.positionName || '-' }}</div>
            <div style="color: #909399; font-size: 12px;">ID: {{ row.positionId }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="leaveType" label="请假类型" width="100" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="days" label="请假天数" width="100" />
        <el-table-column prop="reason" label="请假原因" show-overflow-tooltip />
        <el-table-column prop="status" label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === '申请中'" type="warning">申请中</el-tag>
            <el-tag v-else-if="row.status === '已通过' || row.status === '同意'" type="success">{{ row.status }}</el-tag>
            <el-tag v-else type="danger">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '申请中'" size="small" type="success" @click="handleApprove(row, '已通过')">
              通过
            </el-button>
            <el-button v-if="row.status === '申请中'" size="small" type="warning" @click="handleApprove(row, '已拒绝')">
              拒绝
            </el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLeaves, approveLeave, deleteLeave } from '../../api/leave'

const searchForm = reactive({
  userName: '',
  positionName: '',
  status: null
})

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      status: searchForm.status || undefined,
      userName: searchForm.userName || undefined,
      positionName: searchForm.positionName || undefined
    }
    
    const res = await getLeaves(params)
    
    // 客户端分页
    const allData = res.data || []
    total.value = allData.length
    
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    tableData.value = allData.slice(start, end)
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.userName = ''
  searchForm.positionName = ''
  searchForm.status = null
  handleSearch()
}

const handleApprove = (row, status) => {
  const tip = status === '已通过' ? '通过' : '拒绝'
  ElMessageBox.confirm(`确定要${tip}该请假申请吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await approveLeave(row.leaveRequestId, { status })
      ElMessage.success(`${tip}成功`)
      loadData()
    } catch (error) {
      console.error('审批失败:', error)
      ElMessage.error('审批失败')
    }
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该请假记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteLeave(row.leaveRequestId)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

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
.leave-management {
  width: 100%;
}

.search-card {
  margin-bottom: 20px;
}
</style>
