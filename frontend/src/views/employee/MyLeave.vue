<template>
  <div class="my-leave">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px">
        <el-row :gutter="10">
          <el-col :xs="12" :sm="10">
            <el-form-item label="请假类型">
              <el-select v-model="searchForm.leaveType" placeholder="全部类型" clearable @change="handleSearch" style="width: 100%">
                <el-option label="事假" value="事假" />
                <el-option label="病假" value="病假" />
                <el-option label="调休" value="调休" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="10">
            <el-form-item label="审批状态">
              <el-select v-model="searchForm.status" placeholder="全部状态" clearable @change="handleSearch" style="width: 100%">
                <el-option label="申请中" value="申请中" />
                <el-option label="已通过" value="已通过" />
                <el-option label="已拒绝" value="已拒绝" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 请假列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>我的请假申请记录</h3>
        </div>
      </template>
      <!-- 桌面端表格 -->
      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%" class="hidden-xs">
        <el-table-column prop="positionName" label="岗位名称" show-overflow-tooltip />
        <el-table-column prop="leaveType" label="类型" width="90" />
        <el-table-column prop="startDate" label="开始日期" width="110" />
        <el-table-column prop="endDate" label="结束日期" width="110" />
        <el-table-column prop="days" label="天数" width="70" />
        <el-table-column prop="status" label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === '申请中'" type="warning">申请中</el-tag>
            <el-tag v-else-if="row.status === '已通过' || row.status === '同意'" type="success">{{ row.status }}</el-tag>
            <el-tag v-else type="danger">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="160" />
      </el-table>

      <!-- 移动端卡片列表 -->
      <div class="mobile-list visible-xs">
        <div v-for="row in tableData" :key="row.leaveRequestId" class="leave-card">
          <div class="card-header">
            <span class="l-type">{{ row.leaveType }}</span>
            <el-tag :type="row.status === '申请中' ? 'warning' : (row.status === '已通过' || row.status === '同意') ? 'success' : 'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </div>
          <div class="card-pos">{{ row.positionName }}</div>
          <div class="card-time">
            <div class="time-box">
              <div class="t-line">开始：{{ row.startDate }}</div>
              <div class="t-line">结束：{{ row.endDate }}</div>
            </div>
            <div class="day-count">{{ row.days }}天</div>
          </div>
          <div class="card-reason" v-if="row.reason">
            原因：{{ row.reason }}
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

      <el-empty v-if="tableData.length === 0 && !loading" description="暂无请假记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyLeaves } from '../../api/leave'

const tableData = ref([])
const loading = ref(false)

const searchForm = reactive({
  leaveType: null,
  status: null
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const allData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      leaveType: searchForm.leaveType || undefined,
      status: searchForm.status || undefined
    }
    const res = await getMyLeaves(params)
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
  searchForm.leaveType = null
  searchForm.status = null
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

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-leave {
  max-width: 1200px;
  margin: 0 auto;
}
.search-card {
  margin-bottom: 20px;
}

/* 移动端卡片样式 */
.leave-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.l-type {
  font-weight: bold;
  color: #303133;
}
.card-pos {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}
.card-time {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  background: #f8f9fa;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 10px;
}
.time-box .t-line {
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
.day-count {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}
.card-reason {
  font-size: 12px;
  color: #909399;
  border-top: 1px dashed #f0f0f0;
  padding-top: 8px;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .search-card :deep(.el-form-item) {
    display: flex;
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>
