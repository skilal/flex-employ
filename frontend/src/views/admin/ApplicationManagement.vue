<template>
  <div class="application-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名称">
          <el-input 
            v-model="searchForm.userName" 
            placeholder="请输入用户名称" 
            clearable 
            style="width: 200px"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="岗位名称">
          <el-input 
            v-model="searchForm.positionName" 
            placeholder="请输入岗位名称" 
            clearable 
            style="width: 200px"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="申请状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="申请状态" 
            clearable
            @change="handleSearch"
            @clear="handleSearch"
            style="width: 150px"
          >
            <el-option label="已申请" value="已申请" />
            <el-option label="已通过" value="已通过" />
            <el-option label="已拒绝" value="已拒绝" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="applicationId" label="申请ID" width="80" />
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
        <el-table-column prop="resumePdfPath" label="简历" width="120">
          <template #default="{ row }">
            <el-button 
              v-if="row.resumePdfPath" 
              type="primary" 
              link 
              @click="handleViewResume(row.resumePdfPath)"
            >
              查看简历
            </el-button>
            <span v-else style="color: #909399">未上传</span>
          </template>
        </el-table-column>
        <el-table-column prop="applicationNote" label="申请说明" min-width="200" show-overflow-tooltip />
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
        <el-descriptions-item label="申请人姓名">{{ currentRow.userName }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ ['M', '男'].includes(currentRow.gender) ? '男' : ['F', '女'].includes(currentRow.gender) ? '女' : '未知' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentRow.userId }}</el-descriptions-item>
        <el-descriptions-item label="岗位ID">{{ currentRow.positionId }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称">{{ currentRow.positionName }}</el-descriptions-item>
        <el-descriptions-item label="简历文件">
          <el-button 
            v-if="currentRow.resumePdfPath" 
            type="primary" 
            size="small" 
            @click="handleViewResume(currentRow.resumePdfPath)"
          >
            点击在新标签页预览 PDF
          </el-button>
          <span v-else>未上传简历</span>
        </el-descriptions-item>
        <el-descriptions-item label="申请说明">{{ currentRow.applicationNote || '无' }}</el-descriptions-item>
        <el-descriptions-item label="申请状态">{{ currentRow.status }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ currentRow.approveTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="approveVisible" title="审批通过" width="500px" @close="handleApproveDialogClose">
      <el-form ref="approveFormRef" :model="approveForm" :rules="approveRules" label-width="120px">
        <el-form-item label="入职日期" prop="hireDate">
          <el-date-picker
            v-model="approveForm.hireDate"
            type="date"
            placeholder="选择入职日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" @click="handleApproveSubmit" :loading="approveLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getApplications, approveApplication, deleteApplication } from '../../api/application'

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

const detailVisible = ref(false)
const currentRow = ref({})

// 审批对话框
const approveVisible = ref(false)
const approveFormRef = ref(null)
const approveLoading = ref(false)
const currentApproveRow = ref(null)
const approveForm = reactive({
  hireDate: ''
})

const approveRules = {
  hireDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      userName: searchForm.userName || undefined,
      positionName: searchForm.positionName || undefined,
      status: searchForm.status || undefined
    }
    
    const res = await getApplications(params)
    
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

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.userName = ''
  searchForm.positionName = ''
  searchForm.status = null
  handleSearch()
}

// 审批
const handleApprove = (row, status) => {
  if (status === '已通过') {
    // 通过时打开对话框填写入职信息
    currentApproveRow.value = row
    approveForm.hireDate = row.workStartTime || ''
    approveVisible.value = true
  } else {
    // 拒绝时直接确认
    ElMessageBox.confirm('确定要拒绝该申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await approveApplication(row.applicationId, { status })
        ElMessage.success('拒绝成功')
        loadData()
      } catch (error) {
        const msg = error.response?.data?.message || '拒绝操作失败'
        ElMessage.error(msg)
      }
    }).catch(() => {})
  }
}

// 提交审批
const handleApproveSubmit = () => {
  approveFormRef.value.validate(async (valid) => {
    if (valid) {
      approveLoading.value = true
      try {
        await approveApplication(currentApproveRow.value.applicationId, {
          status: '已通过',
          hireDate: approveForm.hireDate
        })
        ElMessage.success('审批通过成功')
        approveVisible.value = false
        loadData()
      } catch (error) {
        const msg = error.response?.data?.message || '审批通过操作失败'
        ElMessage.error(msg)
      } finally {
        approveLoading.value = false
      }
    }
  })
}

// 关闭审批对话框
const handleApproveDialogClose = () => {
  approveFormRef.value?.resetFields()
}

// 查看
const handleView = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

const handleViewResume = (path) => {
  if (path) {
    window.open(path, '_blank')
  }
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
