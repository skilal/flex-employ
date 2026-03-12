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
            @change="handleSearch"
            @clear="handleSearch"
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
    <div class="action-buttons" style="display: flex; gap: 10px;">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增在岗员工
      </el-button>
      <el-button type="success" :disabled="selectedWorkers.length === 0" @click="handleBatchSign">
        批量一键签到 ({{ selectedWorkers.length }}人)
      </el-button>
    </div>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="onDutyWorkerId" label="记录ID" width="100" />
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
        <el-table-column prop="checkInTime" label="应签到时间" width="120" />
        <el-table-column prop="checkOutTime" label="应签退时间" width="120" />
        <el-table-column prop="hireDate" label="入职日期" width="120" />
        <el-table-column label="社保基数" width="120">
          <template #default="{ row }">
            {{ row.socialSecurityBase ? `¥${row.socialSecurityBase}` : '未核定' }}
          </template>
        </el-table-column>
        <el-table-column prop="leaveDate" label="离职日期" width="120" />
        <el-table-column label="员工状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.workerStatus === '在岗'" type="success">在岗</el-tag>
            <el-tag v-else-if="row.workerStatus === '已结束' || row.leaveDate" type="info">已结束</el-tag>
            <el-tag v-else type="warning">{{ row.workerStatus || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.workerStatus === '在岗'" 
              size="small" 
              type="success" 
              @click="handleManualSign(row)"
            >
              一键签到
            </el-button>
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
        <el-form-item label="用户" prop="userId">
          <el-select
            v-model="form.userId"
            placeholder="请选择用户"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in users"
              :key="item.userId"
              :label="item.name ? `${item.name} (${item.account})` : item.account" 
              :value="item.userId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="岗位" prop="positionId">
          <el-select
            v-model="form.positionId"
            placeholder="请选择岗位"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in positions"
              :key="item.positionId"
              :label="`${item.positionName} (ID:${item.positionId})`"
              :value="item.positionId"
            />
          </el-select>
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

        <el-form-item label="社保基数" prop="socialSecurityBase">
          <el-input-number 
            v-model="form.socialSecurityBase" 
            :precision="2" 
            :step="100" 
            :min="0"
            placeholder="留空则首月自动核定" 
            style="width: 100%" 
          />
          <div style="font-size: 12px; color: #909399; margin-top: 4px;">
            💡 首次发薪时根据应发工资自动核准，也可手动预设。
          </div>
        </el-form-item>

        <el-form-item label="员工状态" prop="workerStatus">
          <el-select v-model="form.workerStatus" placeholder="自动判定" style="width: 100%" disabled>
            <el-option label="在岗" value="在岗" />
            <el-option label="已结束" value="已结束" />
          </el-select>
          <div style="font-size: 12px; color: #909399; margin-top: 4px;">
            💡 状态由日期自动判定：设置离职日期后自动变为“已结束”
          </div>
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
import { Search, Plus } from '@element-plus/icons-vue'
import { getWorkers, createWorker, updateWorker, deleteWorker } from '../../api/worker'
import { getUsers } from '../../api/user'
import { getPositions } from '../../api/position'
import request from '../../utils/request'

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
const users = ref([]) // 用户列表
const positions = ref([]) // 岗位列表
const selectedWorkers = ref([]) // 所有勾选的关联记录

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const submitLoading = ref(false)

const form = reactive({
  onDutyWorkerId: null,
  userId: null,
  positionId: null,
  hireDate: '',
  leaveDate: '',
  workerStatus: '在岗',
  socialSecurityBase: null
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

// 加载用户列表
const loadUsers = async () => {
  try {
    const res = await getUsers({})
    users.value = res.data || []
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

// 加载岗位列表
const loadPositions = async () => {
  try {
    const res = await getPositions({})
    positions.value = res.data || []
  } catch (error) {
    console.error('加载岗位列表失败:', error)
  }
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
    workerStatus: '在岗',
    socialSecurityBase: null
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

// 供之前保留的一键签到(单行)及新增的批量签到共同调用的核心逻辑
const executeSign = async (workerIds) => {
  loading.value = true
  try {
    const res = await request.post('/attendances/manual-sign', workerIds)
    if (res.code === 200) {
      ElMessage.success(res.message || '签到/过滤执行完成')
    } else {
      ElMessage.error(res.message || '批量签到执行失败')
    }
  } catch (error) {
    console.error('一键签到异常:', error)
    ElMessage.error(error.message || '网络异常')
  } finally {
    loading.value = false
  }
}

const handleManualSign = (row) => {
  ElMessageBox.confirm(`确定要为员工【${row.userName}】进行今日的一键打卡补签吗？`, '一键代签 (Redis Bitmap)', {
    confirmButtonText: '确定补签',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => executeSign([row.onDutyWorkerId])).catch(() => {})
}

const handleSelectionChange = (val) => {
  selectedWorkers.value = val
}

const handleBatchSign = () => {
  const ids = selectedWorkers.value.map(w => w.onDutyWorkerId)
  ElMessageBox.confirm(`确定要为选中的 ${ids.length} 名在岗员工批量一键打卡吗？`, '批量代签', {
    confirmButtonText: '确认执行',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => executeSign(ids)).catch(() => {})
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
  loadUsers()
  loadPositions()
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
