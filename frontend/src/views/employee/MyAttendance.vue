<template>
  <div class="my-attendance">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="考勤日期">
          <el-date-picker
            v-model="searchForm.attendanceDate"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="考勤状态">
          <el-select v-model="searchForm.attendanceStatus" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="正常" value="正常" />
            <el-option label="迟到" value="迟到" />
            <el-option label="早退" value="早退" />
            <el-option label="迟到且早退" value="迟到且早退" />
            <el-option label="缺勤" value="缺勤" />
            <el-option label="旷工" value="旷工" />
            <el-option label="请假" value="请假" />
            <el-option label="假日" value="假日" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleQuickMonth">本月</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="10" class="stat-row">
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="never">
          <el-statistic title="出勤" :value="statistics.normalDays">
            <template #suffix><span class="unit">天</span></template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="never">
          <el-statistic title="缺勤" :value="statistics.absentDays" value-style="color: #E6A23C">
            <template #suffix><span class="unit">天</span></template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="never">
          <el-statistic title="旷工" :value="statistics.absenteeismDays" value-style="color: #F56C6C">
            <template #suffix><span class="unit">天</span></template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="never">
          <el-statistic title="迟到" :value="statistics.lateTimes">
            <template #suffix><span class="unit">次</span></template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="never">
          <el-statistic title="早退" :value="statistics.earlyTimes">
            <template #suffix><span class="unit">次</span></template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="never">
          <el-statistic title="请假" :value="statistics.leaveDays" value-style="color: #409EFF">
            <template #suffix><span class="unit">天</span></template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <div class="data-section">
      <!-- 桌面端表格 -->
      <el-table :data="pagedData" border stripe v-loading="loading" class="hidden-xs">
        <el-table-column prop="attendanceId" label="ID" width="80" />
        <el-table-column prop="attendanceDate" label="考勤日期" width="110" />
        <el-table-column label="所属岗位" width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="pro-pos-name">{{ row.positionName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="标准时段" width="130">
          <template #default="{ row }">
            <div class="pro-time-text">
              <el-icon><Timer /></el-icon> {{ formatTime(row.checkInTime) }}-{{ formatTime(row.checkOutTime) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="actualCheckIn" label="签到时间" width="100" />
        <el-table-column prop="actualCheckOut" label="签退时间" width="100" />
        <el-table-column prop="attendanceStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.attendanceStatus)">{{ row.attendanceStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="合计" width="100">
          <template #default="{ row }">
            {{ calculateWorkHours(row) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端卡片列表 -->
      <div class="mobile-list visible-xs" v-loading="loading">
        <div v-for="row in pagedData" :key="row.attendanceId" class="attendance-card">
          <div class="card-top">
            <span class="card-date">{{ row.attendanceDate }}</span>
            <el-tag :type="getStatusType(row.attendanceStatus)" size="small">{{ row.attendanceStatus }}</el-tag>
          </div>
          <div class="card-body">
            <div class="card-pos">{{ row.positionName || '未关联岗位' }}</div>
            <div class="card-time-grid">
              <div class="time-item">
                <span class="label">签到</span>
                <span class="val">{{ row.actualCheckIn || '--:--' }}</span>
              </div>
              <div class="time-item">
                <span class="label">签退</span>
                <span class="val">{{ row.actualCheckOut || '--:--' }}</span>
              </div>
              <div class="time-item">
                <span class="label">标准</span>
                <span class="val small">{{ formatTime(row.checkInTime) }}-{{ formatTime(row.checkOutTime) }}</span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-if="tableData.length === 0 && !loading" description="暂无记录" />
      </div>

      <div class="pagination-footer">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :layout="mobileLayout"
          @current-change="handlePageChange"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Calendar, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMyAttendances } from '../../api/attendance'

const searchForm = reactive({
  attendanceDate: [],
  attendanceStatus: ''
})

const tableData = ref([])
const pagedData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statistics = reactive({
  normalDays: 0,
  absentDays: 0,
  lateTimes: 0,
  earlyTimes: 0,
  leaveDays: 0,
  holidayDays: 0,
  absenteeismDays: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchForm.attendanceDate && searchForm.attendanceDate.length === 2) {
      params.startDate = searchForm.attendanceDate[0]
      params.endDate = searchForm.attendanceDate[1]
    }
    
    const res = await getMyAttendances(params)
    let allLoaded = res.data.records || res.data || []
    
    // 客户端考勤状态筛选
    if (searchForm.attendanceStatus) {
      allLoaded = allLoaded.filter(item => item.attendanceStatus === searchForm.attendanceStatus)
    }
    
    tableData.value = allLoaded
    total.value = allLoaded.length
    
    updatePagedData()
    // 统计基于当前筛选结果
    calculateStatistics(allLoaded)
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const updatePagedData = () => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  pagedData.value = tableData.value.slice(start, end)
}

const handlePageChange = (val) => {
  currentPage.value = val
  updatePagedData()
}

const calculateStatistics = (data) => {
  const src = data || tableData.value
  statistics.normalDays = src.filter(item => item.attendanceStatus === '正常').length
  statistics.absentDays = src.filter(item => item.attendanceStatus === '缺勤').length
  statistics.lateTimes = src.filter(item => 
    item.attendanceStatus === '迟到' || item.attendanceStatus === '迟到且早退'
  ).length
  statistics.earlyTimes = src.filter(item => 
    item.attendanceStatus === '早退' || item.attendanceStatus === '迟到且早退'
  ).length
  statistics.leaveDays = src.filter(item => item.attendanceStatus === '请假').length
  statistics.holidayDays = src.filter(item => item.attendanceStatus === '假日').length
  statistics.absenteeismDays = src.filter(item => item.attendanceStatus === '旷工').length
}

const formatWorkingDays = (daysStr) => {
  if (!daysStr) return '灵活安排'
  const dayMap = { '1': '周一', '2': '周二', '3': '周三', '4': '周四', '5': '周五', '6': '周六', '7': '周日' }
  const days = daysStr.split(',').sort().map(d => dayMap[d])
  if (daysStr === '1,2,3,4,5') return '周一至周五'
  if (daysStr === '1,2,3,4,5,6,7') return '全周'
  return days.join(', ')
}

const getStatusType = (status) => {
  const map = {
    '正常': 'success',
    '迟到': 'warning',
    '早退': 'warning',
    '迟到且早退': 'danger',
    '缺勤': 'danger',
    '旷工': 'danger',
    '请假': 'info',
    '假日': 'info'
  }
  return map[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return '--:--'
  return time.length > 5 ? time.substring(0, 5) : time
}

const calculateWorkHours = (row) => {
  if (!row.actualCheckIn || !row.actualCheckOut) {
    return '-'
  }
  
  const checkIn = new Date(`2000-01-01 ${row.actualCheckIn}`)
  const checkOut = new Date(`2000-01-01 ${row.actualCheckOut}`)
  const diff = (checkOut - checkIn) / 1000 / 60 / 60
  
  return diff > 0 ? `${diff.toFixed(1)}小时` : '-'
}

const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  searchForm.attendanceDate = []
  searchForm.attendanceStatus = ''
  loadData()
}

// 移动端分页布局适配
const mobileLayout = computed(() => {
  return window.innerWidth < 768 ? 'prev, pager, next' : 'total, prev, pager, next, jumper'
})

// 便捷：快速跳转本月
const handleQuickMonth = () => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const lastDay = new Date(y, now.getMonth() + 1, 0).getDate()
  searchForm.attendanceDate = [`${y}-${m}-01`, `${y}-${m}-${lastDay}`]
  searchForm.attendanceStatus = ''
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-attendance {
  max-width: 1200px;
  margin: 0 auto;
}

.search-card {
  margin-bottom: 20px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-row :deep(.el-card__body) {
  padding: 15px;
}

.unit {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}

.data-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.pro-pos-name {
  font-weight: 500;
  color: #303133;
}

/* 移动端列表 */
.attendance-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  transition: all 0.3s;
}

.attendance-card:active {
  background: #f5f7fa;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #f0f0f0;
}

.card-date {
  font-weight: bold;
  color: #303133;
}

.card-pos {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.card-time-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.time-item {
  display: flex;
  flex-direction: column;
}

.time-item .label {
  font-size: 11px;
  color: #909399;
  margin-bottom: 2px;
}

.time-item .val {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.time-item .val.small {
  font-size: 12px;
}

.pagination-footer {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .my-attendance {
    padding: 4px;
  }
  .search-card :deep(.el-form-item) {
    display: flex;
    margin-right: 0;
    margin-bottom: 12px;
  }
  .search-card :deep(.el-date-editor) {
    width: 100% !important;
  }
  .data-section {
    padding: 12px;
    background: transparent;
    box-shadow: none;
  }
}
</style>
