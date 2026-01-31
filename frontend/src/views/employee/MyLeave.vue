<template>
  <div class="my-leave">
    <!-- 请假列表 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="leaveRequestId" label="请假ID" width="80" />
        <el-table-column label="岗位信息" width="180">
          <template #default="{ row }">
            <div>{{ row.positionName || '-' }}</div>
            <div style="color: #909399; font-size: 12px;">ID: {{ row.positionId }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="leaveType" label="请假类型" width="100" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="days" label="请假天数" width="100" />
        <el-table-column prop="reason" label="请假原因" show-overflow-tooltip />
        <el-table-column prop="status" label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === '申请中'" type="warning">申请中</el-tag>
            <el-tag v-else-if="row.status === '已通过' || row.status === '同意'" type="success">{{ row.status }}</el-tag>
            <el-tag v-else type="danger">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="170" />
        <el-table-column prop="approveTime" label="审批时间" width="170" />
      </el-table>

      <el-empty v-if="tableData.length === 0 && !loading" description="暂无请假记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyLeaves } from '../../api/leave'

const tableData = ref([])
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyLeaves()
    tableData.value = res.data.records || res.data || []
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-leave {
  width: 100%;
}

.action-buttons {
  margin-bottom: 20px;
}
</style>
