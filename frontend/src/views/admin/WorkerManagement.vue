<template>
  <div class="worker-management">
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
        <el-form-item label="员工状态">
          <el-select 
            v-model="searchForm.workerStatus" 
            placeholder="员工状态" 
            clearable
            style="width: 150px"
          >
            <el-option label="在岗" value="在岗" />
            <el-option label="已结束" value="已结束" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增在岗员工
      </el-button>
    </div>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="onDutyWorkerId" label="记录ID" width="100" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="userName" label="用户名称" width="150" />
        <el-table-column prop="positionId" label="岗位ID" width="100" />
        <el-table-column prop="positionName" label="岗位名称" width="200" />
        <el-table-column prop="checkInTime" label="应签到时间" width="120" />
        <el-table-column prop="checkOutTime" label="应签退时间" width="120" />
        <el-table-column prop="hireDate" label="入职日期" width="120" />
        <el-table-column prop="leaveDate" label="离职日期" width="120" />
        <el-table-column label="员工状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="!row.leaveDate" type="success">在岗</el-tag>
            <el-tag v-else type="info">已结束</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="form.userId" :min="1" style="width: 100%" />
        </el-form-item>

        <el-form-item label="岗位ID" prop="positionId">
          <el-input-number v-model="form.positionId" :min="1" style="width: 100%" />
        </el-form-item>

        <el-form-item label="打卡上班时间" prop="checkInTime">
          <el-time-picker
            v-model="form.checkInTime"
            format="HH:mm:ss"
            value-format="HH:mm:ss"
            placeholder="选择时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="打卡下班时间" prop="checkOutTime">
          <el-time-picker
            v-model="form.checkOutTime"
            format="HH:mm:ss"
            value-format="HH:mm:ss"
            placeholder="选择时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="入职日期" prop="hireDate">
          <el-date-picker
            v-model="form.hireDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="离职日期">
          <el-date-picker
            v-model="form.leaveDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="员工状态" prop="workerStatus">
          <el-select v-model="form.workerStatus" placeholder="请选择" style="width: 100%">
            <el-option label="在岗" value="在岗" />
            <el-option label="已结束" value="已结束" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getWorkers, createWorker, updateWorker, deleteWorker } from '../../api/worker'

const searchForm = reactive({
  userName: '',
  positionName: '',
  workerStatus: null
})

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const submitLoading = ref(false)

const form = reactive({
  onDutyWorkerId: null,
  userId: null,
  positionId: null,
  checkInTime: '',
  checkOutTime: '',
  hireDate: '',
  leaveDate: '',
  workerStatus: '在岗'
})

const rules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  positionId: [{ required: true, message: '请输入岗位ID', trigger: 'blur' }],
  hireDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }],
  workerStatus: [{ required: true, message: '请选择员工状态', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      userName: searchForm.userName || undefined,
      positionName: searchForm.positionName || undefined,
      workerStatus: searchForm.workerStatus || undefined
    }
    
    const res = await getWorkers(params)
    
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
  searchForm.workerStatus = null
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增在岗员工'
  Object.assign(form, {
    onDutyWorkerId: null,
    userId: null,
    positionId: null,
    checkInTime: '',
    checkOutTime: '',
    hireDate: '',
    leaveDate: '',
    workerStatus: '在岗'
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑在岗员工'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该员工记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteWorker(row.onDutyWorkerId)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (form.onDutyWorkerId) {
          await updateWorker(form.onDutyWorkerId, form)
          ElMessage.success('更新成功')
        } else {
          await createWorker(form)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error(error.response?.data?.message || '提交失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
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
.worker-management {
  width: 100%;
}

.search-card {
  margin-bottom: 20px;
}

.action-buttons {
  margin-bottom: 20px;
}
</style>
