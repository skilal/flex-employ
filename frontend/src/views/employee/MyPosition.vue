<template>
  <div class="my-position">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px">
        <el-row :gutter="10">
          <el-col :xs="24" :sm="10">
            <el-form-item label="岗位名称">
              <el-input v-model="searchForm.positionName" placeholder="筛选岗位" clearable @change="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="7">
            <el-form-item label="在岗状态">
              <el-select v-model="searchForm.workerStatus" placeholder="全部" clearable @change="handleSearch" style="width: 100%">
                <el-option label="在岗" value="在岗" />
                <el-option label="已结束" value="已结束" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="7" class="filter-btns">
            <el-button type="primary" @click="handleSearch" style="width: 100%">查询</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 岗位列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>我的岗位记录</h3>
        </div>
      </template>

      <!-- 桌面端表格 -->
      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%" class="hidden-xs">
        <el-table-column prop="positionName" label="岗位名称" min-width="150" />
        <el-table-column prop="laborCompanyName" label="用工单位" min-width="120" />
        <el-table-column label="薪资标准" width="140">
          <template #default="{ row }">
            <div style="font-weight: bold; color: #f56c6c">¥{{ row.baseRate }}/{{ row.billingMethod === 1 ? '时' : '天' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="!row.leaveDate ? 'success' : 'info'">{{ !row.leaveDate ? '在岗' : '已结束' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.leaveDate" size="small" type="primary" @click="handleLeave(row)">请假</el-button>
            <el-button size="small" @click="handleViewSchedule(row)">排班</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端卡片列表 -->
      <div class="mobile-list visible-xs">
        <div v-for="row in tableData" :key="row.onDutyWorkerId" class="position-card" @click="handleViewSchedule(row)">
          <div class="card-header">
            <span class="p-name">{{ row.positionName }}</span>
            <el-tag :type="!row.leaveDate ? 'success' : 'info'" size="small">{{ !row.leaveDate ? '在岗' : '已结束' }}</el-tag>
          </div>
          <div class="card-desc">单位：{{ row.laborCompanyName }}</div>
          <div class="card-meta">
            <div class="meta-item">
              <span class="label">薪资</span>
              <span class="val price">¥{{ row.baseRate }}/{{ row.billingMethod === 1 ? '时' : '天' }}</span>
            </div>
            <div class="meta-item">
              <span class="label">入职日期</span>
              <span class="val">{{ row.hireDate }}</span>
            </div>
          </div>
          <div class="card-actions" @click.stop v-if="!row.leaveDate">
            <el-button size="small" type="primary" style="width: 100%" @click="handleLeave(row)">提交请假申请</el-button>
          </div>
        </div>
        <el-empty v-if="tableData.length === 0 && !loading" description="暂无记录" />
      </div>

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
    <el-dialog v-model="scheduleVisible" title="岗位详情与排班" width="90%" style="max-width: 620px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位名称">{{ currentRow.positionName }}</el-descriptions-item>
        <el-descriptions-item label="岗位状态">
          <el-tag :type="!currentRow.leaveDate ? 'success' : 'info'">{{ !currentRow.leaveDate ? '在岗' : '已结束' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用工单位">{{ currentRow.laborCompanyName }}</el-descriptions-item>
        <el-descriptions-item label="联系责任方">{{ currentRow.salaryPayerName || '人力服务公司' }}</el-descriptions-item>
        <el-descriptions-item label="岗位负责人">{{ currentRow.responsibleName || '管理员' }}</el-descriptions-item>
        <el-descriptions-item label="负责人电话">{{ currentRow.responsiblePhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="打卡时间">{{ currentRow.checkInTime }} - {{ currentRow.checkOutTime }}</el-descriptions-item>
        <el-descriptions-item label="结算周期">{{ currentRow.payCycle }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ currentRow.hireDate }}</el-descriptions-item>
        <el-descriptions-item label="离职日期">{{ currentRow.leaveDate || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="计费标准">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ currentRow.baseRate }} / {{ currentRow.billingMethod === 1 ? '小时' : '天' }}</span>
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
.my-position {
  max-width: 1200px;
  margin: 0 auto;
}
.search-card {
  margin-bottom: 20px;
}

/* 移动端卡片式展示 */
.position-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
  cursor: pointer;
}
.position-card:active {
  background: #fdfdfd;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.card-header .p-name {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}
.card-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 15px;
}
.card-meta {
  display: flex;
  justify-content: space-between;
  background: #f8f9fa;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 15px;
}
.meta-item {
  display: flex;
  flex-direction: column;
}
.meta-item .label {
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}
.meta-item .val {
  font-size: 13px;
  color: #606266;
}
.meta-item .val.price {
  color: #f56c6c;
  font-weight: bold;
}
.card-actions {
  border-top: 1px dashed #ebeef5;
  padding-top: 12px;
}

@media (max-width: 768px) {
  .search-card :deep(.el-form-item) {
    display: flex;
    margin-right: 0;
    margin-bottom: 12px;
  }
  .search-card :deep(.el-form-item__label) {
    width: auto !important;
  }
}
</style>
