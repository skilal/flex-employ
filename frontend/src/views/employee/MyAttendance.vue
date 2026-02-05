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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 考勤统计 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card>
          <el-statistic title="本月出勤天数" :value="statistics.normalDays">
            <template #suffix>天</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <el-statistic title="本月缺勤天数" :value="statistics.absentDays">
            <template #suffix>天</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card>
          <el-statistic title="本月迟到次数" :value="statistics.lateTimes">
            <template #suffix>次</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card>
          <el-statistic title="本月早退次数" :value="statistics.earlyTimes">
            <template #suffix>次</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card>
          <el-statistic title="本月请假天数" :value="statistics.leaveDays">
            <template #suffix>天</template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 考勤记录 -->
    <el-card>
      <template #header>
        <h3>考勤记录</h3>
      </template>

      <el-table :data="pagedData" border stripe v-loading="loading">
        <el-table-column prop="attendanceId" label="ID" width="80" />
        <el-table-column prop="attendanceDate" label="考勤日期" width="110" />
        <el-table-column label="所属岗位" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="pro-pos-name">{{ row.positionName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="排班规则" min-width="170">
          <template #default="{ row }">
            <div class="pro-rule-text">
              <el-icon><Calendar /></el-icon> {{ formatWorkingDays(row.workingDays) }}
            </div>
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
        <el-table-column label="计算工时" width="100">
          <template #default="{ row }">
            {{ calculateWorkHours(row) }}
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="handlePageChange"
        />
      </div>

      <el-empty v-if="tableData.length === 0 && !loading" description="暂无考勤记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Calendar, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMyAttendances } from '../../api/attendance'

const searchForm = reactive({
  attendanceDate: []
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
  leaveDays: 0
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
    const allData = res.data.records || res.data || []
    tableData.value = allData
    total.value = allData.length
    
    updatePagedData()
    // 计算统计数据
    calculateStatistics()
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

const calculateStatistics = () => {
  statistics.normalDays = tableData.value.filter(item => item.attendanceStatus === '正常').length
  statistics.absentDays = tableData.value.filter(item => item.attendanceStatus === '缺勤').length
  statistics.lateTimes = tableData.value.filter(item => 
    item.attendanceStatus === '迟到' || item.attendanceStatus === '迟到且早退'
  ).length
  statistics.earlyTimes = tableData.value.filter(item => 
    item.attendanceStatus === '早退' || item.attendanceStatus === '迟到且早退'
  ).length
  statistics.leaveDays = tableData.value.filter(item => item.attendanceStatus === '请假').length
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
    '请假': 'info'
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
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-attendance {
  width: 100%;
}

.search-card {
  margin-bottom: 20px;
}
.pro-pos-name {
  font-weight: 600;
  color: #34495e;
}
.pro-rule-text {
  color: #e67e22;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.pro-time-text {
  color: #7f8c8d;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
