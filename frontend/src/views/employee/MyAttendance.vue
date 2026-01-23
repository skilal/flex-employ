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
      <el-col :span="6">
        <el-card>
          <el-statistic title="本月迟到次数" :value="statistics.lateTimes">
            <template #suffix>次</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
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

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="attendanceId" label="考勤ID" width="100" />
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
        <el-table-column label="工作时长" width="120">
          <template #default="{ row }">
            {{ calculateWorkHours(row) }}
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="tableData.length === 0 && !loading" description="暂无考勤记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyAttendances } from '../../api/attendance'

const searchForm = reactive({
  attendanceDate: []
})

const tableData = ref([])
const loading = ref(false)

const statistics = reactive({
  normalDays: 0,
  absentDays: 0,
  lateTimes: 0,
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
    tableData.value = res.data.records || res.data || []
    
    // 计算统计数据
    calculateStatistics()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const calculateStatistics = () => {
  statistics.normalDays = tableData.value.filter(item => item.attendanceStatus === '正常').length
  statistics.absentDays = tableData.value.filter(item => item.attendanceStatus === '缺勤').length
  statistics.lateTimes = tableData.value.filter(item => item.attendanceStatus === '迟到').length
  statistics.leaveDays = tableData.value.filter(item => item.attendanceStatus === '请假').length
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
</style>
