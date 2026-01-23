<template>
  <div class="my-position">
    <el-card>
      <template #header>
        <h3>我的岗位记录</h3>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="onDutyWorkerId" label="记录ID" width="100" />
        <el-table-column prop="positionId" label="岗位ID" width="100" />
        <el-table-column prop="positionName" label="岗位名称" width="150" />
        <el-table-column prop="checkInTime" label="上班时间" width="120" />
        <el-table-column prop="checkOutTime" label="下班时间" width="120" />
        <el-table-column prop="hireDate" label="入职日期" width="120" />
        <el-table-column prop="leaveDate" label="离职日期" width="120" />
        <el-table-column prop="workerStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.workerStatus === '在岗'" type="success">在岗</el-tag>
            <el-tag v-else type="info">离职</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewSchedule(row)">查看排班</el-button>
          </template>
        </el-table-column>
      </el-table>

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
      </el-descriptions>

      <el-divider content-position="left">工作时间说明</el-divider>
      <p style="color: #606266;">
        您的排班时间为每天 {{ currentRow.checkInTime }} 至 {{ currentRow.checkOutTime }}，
        请按时打卡上下班。
      </p>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyWorkerRecord } from '../../api/worker'

const tableData = ref([])
const loading = ref(false)
const scheduleVisible = ref(false)
const currentRow = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyWorkerRecord()
    tableData.value = res.data.records || res.data || []
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleViewSchedule = (row) => {
  currentRow.value = row
  scheduleVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-position {
  width: 100%;
}
</style>
