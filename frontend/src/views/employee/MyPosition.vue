<template>
  <div class="my-position">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="岗位名称">
          <el-input v-model="searchForm.positionName" placeholder="筛选岗位" clearable @change="handleSearch" />
        </el-form-item>
        <el-form-item label="在岗状态">
          <el-select v-model="searchForm.workerStatus" placeholder="全部状态" clearable @change="handleSearch">
            <el-option label="在岗" value="在岗" />
            <el-option label="已结束" value="已结束" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 岗位列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>我的岗位记录</h3>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="onDutyWorkerId" label="记录ID" width="80" />
        <el-table-column prop="positionName" label="岗位名称" show-overflow-tooltip />
        <el-table-column prop="checkInTime" label="上班时间" width="110" />
        <el-table-column prop="checkOutTime" label="下班时间" width="110" />
        <el-table-column prop="hireDate" label="入职日期" width="110" />
        <el-table-column prop="leaveDate" label="离职日期" width="110" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="!row.leaveDate" type="success">在岗</el-tag>
            <el-tag v-else type="info">已结束</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.leaveDate" size="small" type="primary" @click="handleLeave(row)">请假</el-button>
            <el-button size="small" @click="handleViewSchedule(row)">排班</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />

      <el-empty v-if="tableData.length === 0 && !loading" description="暂无在岗记录" />
    </el-card>

    <!-- 排班详情对话框 -->
    <el-dialog v-model="scheduleVisible" title="排班信息" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位名称">{{ currentRow.positionName }}</el-descriptions-item>
        <el-descriptions-item label="岗位状态">{{ currentRow.workerStatus }}</el-descriptions-item>
        <el-descriptions-item label="打卡上班时间">{{ currentRow.checkInTime }}</el-descriptions-item>
        <el-descriptions-item label="打卡下班时间">{{ currentRow.checkOutTime }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ currentRow.hireDate }}</el-descriptions-item>
        <el-descriptions-item label="离职日期">{{ currentRow.leaveDate || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="计费方式">
          <el-tag size="small" :type="currentRow.billingMethod === 1 ? 'warning' : 'success'">
            {{ currentRow.billingMethod === 1 ? '按小时计费' : '按天计费' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="加班费">
          {{ currentRow.overtimePay ? `¥${currentRow.overtimePay}/时` : '无加班费' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">工作时间说明</el-divider>
      <p style="color: #606266;">
        您的排班时间为每天 {{ currentRow.checkInTime }} 至 {{ currentRow.checkOutTime }}，
        请按时打卡上下班。
      </p>
    </el-dialog>

    <!-- 请假申请对话框 -->
    <el-dialog v-model="leaveDialogVisible" title="申请请假" width="600px" @close="handleLeaveDialogClose">
      <el-form ref="leaveFormRef" :model="leaveForm" :rules="leaveRules" label-width="100px">
        <el-form-item label="岗位ID" prop="positionId">
          <el-input-number v-model="leaveForm.positionId" :min="1" style="width: 100%" disabled />
        </el-form-item>

        <el-form-item label="岗位名称">
          <el-input v-model="currentLeavingPosition" disabled />
        </el-form-item>

        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="leaveForm.leaveType" placeholder="请选择请假类型" style="width: 100%">
            <el-option label="事假" value="事假" />
            <el-option label="病假" value="病假" />
            <el-option label="年假" value="年假" />
            <el-option label="调休" value="调休" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="leaveForm.startDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            @change="calculateDays"
          />
        </el-form-item>

        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="leaveForm.endDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            @change="calculateDays"
          />
        </el-form-item>

        <el-form-item label="请假天数" prop="days">
          <el-input-number v-model="leaveForm.days" :min="0.5" :precision="1" style="width: 100%" disabled />
        </el-form-item>

        <el-form-item label="请假原因" prop="reason">
          <el-input
            v-model="leaveForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入请假原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="leaveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleLeaveSubmit" :loading="leaveSubmitLoading">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyWorkerRecord } from '../../api/worker'
import { createLeave } from '../../api/leave'

const tableData = ref([])
const loading = ref(false)
const scheduleVisible = ref(false)
const currentRow = ref({})

// 请假相关
const leaveDialogVisible = ref(false)
const leaveFormRef = ref(null)
const leaveSubmitLoading = ref(false)
const currentLeavingPosition = ref('')

const leaveForm = reactive({
  positionId: null,
  leaveType: '',
  startDate: '',
  endDate: '',
  days: 0,
  reason: ''
})

const leaveRules = {
  positionId: [{ required: true, message: '请输入岗位ID', trigger: 'blur' }],
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  days: [{ required: true, message: '请假天数不能为空', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

const searchForm = reactive({
  positionName: '',
  workerStatus: null
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const allData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      positionName: searchForm.positionName || undefined,
      workerStatus: searchForm.workerStatus || undefined
    }
    const res = await getMyWorkerRecord(params)
    const rawData = res.data || []
    allData.value = rawData
    total.value = rawData.length
    
    // 客户端分页
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    tableData.value = rawData.slice(start, end)
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
  searchForm.positionName = ''
  searchForm.workerStatus = null
  handleSearch()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

const handleViewSchedule = (row) => {
  currentRow.value = row
  scheduleVisible.value = true
}

// 请假功能
const handleLeave = (row) => {
  if (row.leaveDate) {
    ElMessage.warning('该岗位已离职，无法申请请假')
    return
  }
  
  Object.assign(leaveForm, {
    positionId: row.positionId,
    leaveType: '',
    startDate: '',
    endDate: '',
    days: 0,
    reason: ''
  })
  currentLeavingPosition.value = row.positionName || '未知岗位'
  leaveDialogVisible.value = true
}

// 计算请假天数
const calculateDays = () => {
  if (leaveForm.startDate && leaveForm.endDate) {
    const start = new Date(leaveForm.startDate)
    const end = new Date(leaveForm.endDate)
    const diffTime = Math.abs(end - start)
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1
    leaveForm.days = diffDays
  }
}

const handleLeaveSubmit = async () => {
  await leaveFormRef.value.validate(async (valid) => {
    if (valid) {
      leaveSubmitLoading.value = true
      try {
        await createLeave(leaveForm)
        ElMessage.success('请假申请提交成功')
        leaveDialogVisible.value = false
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error(error.response?.data?.message || '提交失败')
      } finally {
        leaveSubmitLoading.value = false
      }
    }
  })
}

const handleLeaveDialogClose = () => {
  leaveFormRef.value?.resetFields()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-position { width: 100%; box-sizing: border-box; }
.search-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header h3 { margin: 0; }
</style>
