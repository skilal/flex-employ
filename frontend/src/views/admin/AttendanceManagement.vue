<template>
  <div class="attendance-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="员工名称">
          <el-input v-model="searchForm.userName" placeholder="请输入员工名称" clearable @keyup.enter="handleSearch" style="width: 180px" />
        </el-form-item>
        <el-form-item label="岗位名称">
          <el-input v-model="searchForm.positionName" placeholder="请输入岗位名称" clearable @keyup.enter="handleSearch" style="width: 180px" />
        </el-form-item>
        <el-form-item label="考勤日期">
          <el-date-picker
            v-model="searchForm.attendanceDate"
            type="date"
            placeholder="考勤日期"
            value-format="YYYY-MM-DD"
            clearable
            @change="handleSearch"
            @clear="handleSearch"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="考勤状态">
          <el-select 
            v-model="searchForm.attendanceStatus" 
            placeholder="考勤状态" 
            clearable
            @change="handleSearch"
            @clear="handleSearch"
            style="width: 120px"
          >
            <el-option label="正常" value="正常" />
            <el-option label="迟到" value="迟到" />
            <el-option label="早退" value="早退" />
            <el-option label="缺勤" value="缺勤" />
            <el-option label="请假" value="请假" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增考勤记录
      </el-button>
    </div>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="attendanceId" label="考勤ID" width="80" />
        <el-table-column label="员工信息" width="150">
          <template #default="{ row }">
            <div>{{ row.userName || '-' }}</div>
            <div style="color: #909399; font-size: 12px;">ID: {{ row.onDutyWorkerId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="岗位信息" width="180">
          <template #default="{ row }">
            <div>{{ row.positionName || '-' }}</div>
            <div style="color: #909399; font-size: 12px;">ID: {{ row.positionId }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="attendanceDate" label="考勤日期" width="120" />
        <el-table-column prop="actualCheckIn" label="实际签到时间" width="150" />
        <el-table-column prop="actualCheckOut" label="实际签退时间" width="150" />
        <el-table-column prop="attendanceStatus" label="考勤状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.attendanceStatus === '正常'" type="success">正常</el-tag>
            <el-tag v-else-if="row.attendanceStatus === '迟到'" type="warning">迟到</el-tag>
            <el-tag v-else-if="row.attendanceStatus === '早退'" type="warning">早退</el-tag>
            <el-tag v-else-if="row.attendanceStatus === '请假'" type="info">请假</el-tag>
            <el-tag v-else type="danger">缺勤</el-tag>
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
        <el-form-item label="在岗员工" prop="onDutyWorkerId">
          <el-select
            v-model="form.onDutyWorkerId"
            placeholder="请选择在岗员工"
            filterable
            style="width: 100%"
            @change="handleWorkerChange"
          >
            <el-option
              v-for="item in workers"
              :key="item.onDutyWorkerId"
              :label="`${item.userName} (${item.positionName})`"
              :value="item.onDutyWorkerId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="员工姓名">
          <el-input :value="form.userName" placeholder="自动填充" disabled style="width: 100%" />
        </el-form-item>

        <el-form-item label="入职日期">
          <el-input :value="form.hireDate" placeholder="自动填充" disabled style="width: 100%" />
        </el-form-item>

        <el-form-item label="岗位名称">
          <el-input :value="form.positionName" placeholder="自动填充" disabled style="width: 100%" />
        </el-form-item>

        <el-form-item label="应签到时间">
          <el-input :value="form.checkInTime" placeholder="自动填充" disabled style="width: 100%" />
        </el-form-item>

        <el-form-item label="应签退时间">
          <el-input :value="form.checkOutTime" placeholder="自动填充" disabled style="width: 100%" />
        </el-form-item>

        <el-form-item label="工作开始时间">
          <el-input :value="form.workStartTime" placeholder="自动填充" disabled style="width: 100%" />
        </el-form-item>

        <el-form-item label="工作结束时间">
          <el-input :value="form.workEndTime" placeholder="自动填充" disabled style="width: 100%" />
        </el-form-item>

        <el-form-item label="排班规则">
          <el-tag v-if="form.workingDays" type="info">{{ formatWorkingDays(form.workingDays) }}</el-tag>
          <el-input v-else placeholder="自动填充" disabled style="width: 100%" />
        </el-form-item>

        <el-form-item label="考勤日期" prop="attendanceDate">
          <el-date-picker
            v-model="form.attendanceDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="实际签到时间">
          <el-time-picker
            v-model="form.actualCheckIn"
            format="HH:mm:ss"
            value-format="HH:mm:ss"
            placeholder="选择时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="实际签退时间">
          <el-time-picker
            v-model="form.actualCheckOut"
            format="HH:mm:ss"
            value-format="HH:mm:ss"
            placeholder="选择时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="考勤状态" v-if="form.attendanceId">
          <el-tag :type="getStatusType(form.attendanceStatus)">{{ form.attendanceStatus }}</el-tag>
          <span style="font-size: 12px; color: #909399; margin-left: 10px;">(系统根据打卡时间自动判定)</span>
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
import { getAttendances, createAttendance, updateAttendance, deleteAttendance } from '../../api/attendance'
import { getWorkers } from '../../api/worker'

const workers = ref([])
const loadWorkers = async () => {
  try {
    const res = await getWorkers({ workerStatus: '在岗' })
    workers.value = res.data || []
  } catch (error) {
    console.error('加载在岗员工失败:', error)
  }
}

const searchForm = reactive({
  userName: '',
  positionName: '',
  attendanceDate: null,
  attendanceStatus: null
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
  attendanceId: null,
  onDutyWorkerId: null,
  positionId: null,
  userName: '',
  positionName: '',
  checkInTime: '',
  checkOutTime: '',
  hireDate: '',
  workStartTime: '',
  workEndTime: '',
  attendanceDate: '',
  actualCheckIn: '',
  actualCheckOut: '',
  attendanceStatus: '缺勤',
  workingDays: ''
})

const rules = {
  onDutyWorkerId: [{ required: true, message: '请输入在岗员工ID', trigger: 'blur' }],
  positionId: [{ required: true, message: '请输入岗位ID', trigger: 'blur' }],
  attendanceDate: [{ required: true, message: '请选择考勤日期', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      attendanceDate: searchForm.attendanceDate || undefined,
      attendanceStatus: searchForm.attendanceStatus || undefined,
      userName: searchForm.userName || undefined,
      positionName: searchForm.positionName || undefined
    }
    
    const res = await getAttendances(params)
    
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
  searchForm.attendanceDate = null
  searchForm.attendanceStatus = null
  handleSearch()
}

const formatWorkingDays = (daysStr) => {
  if (!daysStr) return '未设置'
  const dayMap = {
    '1': '周一', '2': '周二', '3': '周三', '4': '周四', '5': '周五', '6': '周六', '7': '周日'
  }
  return daysStr.split(',').map(d => dayMap[d]).join(', ')
}

const getStatusType = (status) => {
  const map = {
    '正常': 'success',
    '迟到': 'warning',
    '早退': 'warning',
    '迟到且早退': 'danger',
    '缺勤': 'danger',
    '请假': 'info'
  }
  return map[status] || 'info'
}

const handleAdd = () => {
  dialogTitle.value = '新增考勤记录'
  Object.assign(form, {
    attendanceId: null,
    onDutyWorkerId: null,
    positionId: null,
    userName: '',
    positionName: '',
    checkInTime: '',
    checkOutTime: '',
    hireDate: '',
    workStartTime: '',
    workEndTime: '',
    attendanceDate: '',
    actualCheckIn: '',
    actualCheckOut: '',
    attendanceStatus: '缺勤',
    workingDays: ''
  })
  dialogVisible.value = true
}

const handleWorkerChange = (val) => {
  const worker = workers.value.find(w => w.onDutyWorkerId === val)
  if (worker) {
    form.positionId = worker.positionId
    form.userName = worker.userName
    form.positionName = worker.positionName
    form.checkInTime = worker.checkInTime
    form.checkOutTime = worker.checkOutTime
    form.hireDate = worker.hireDate
    form.workStartTime = worker.workStartTime
    form.workEndTime = worker.workEndTime
    form.workingDays = worker.workingDays
  }
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑考勤记录'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该考勤记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAttendance(row.attendanceId)
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
        if (form.attendanceId) {
          await updateAttendance(form.attendanceId, form)
          ElMessage.success('更新成功')
        } else {
          await createAttendance(form)
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
  loadWorkers()
})
</script>

<style scoped>
.attendance-management {
  width: 100%;
}

.search-card {
  margin-bottom: 20px;
}

.action-buttons {
  margin-bottom: 20px;
}
</style>
