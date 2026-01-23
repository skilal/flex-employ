<template>
  <div class="my-leave">
    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        申请请假
      </el-button>
    </div>

    <!-- 请假列表 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="leaveRequestId" label="请假ID" width="100" />
        <el-table-column prop="leaveType" label="请假类型" width="100" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="days" label="请假天数" width="100" />
        <el-table-column prop="reason" label="请假原因" show-overflow-tooltip />
        <el-table-column prop="status" label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === '申请中'" type="warning">申请中</el-tag>
            <el-tag v-else-if="row.status === '已通过'" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column prop="approveTime" label="审批时间" width="180" />
      </el-table>

      <el-empty v-if="tableData.length === 0 && !loading" description="暂无请假记录" />
    </el-card>

    <!-- 申请请假对话框 -->
    <el-dialog v-model="dialogVisible" title="申请请假" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="岗位ID" prop="positionId">
          <el-input-number v-model="form.positionId" :min="1" style="width: 100%" />
        </el-form-item>

        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="form.leaveType" placeholder="请选择请假类型" style="width: 100%">
            <el-option label="事假" value="事假" />
            <el-option label="病假" value="病假" />
            <el-option label="年假" value="年假" />
            <el-option label="调休" value="调休" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            @change="calculateDays"
          />
        </el-form-item>

        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            @change="calculateDays"
          />
        </el-form-item>

        <el-form-item label="请假天数" prop="days">
          <el-input-number v-model="form.days" :min="0.5" :precision="1" style="width: 100%" disabled />
        </el-form-item>

        <el-form-item label="请假原因" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入请假原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyLeaves, createLeave } from '../../api/leave'

const tableData = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)

const form = reactive({
  positionId: null,
  leaveType: '',
  startDate: '',
  endDate: '',
  days: 0,
  reason: ''
})

const rules = {
  positionId: [{ required: true, message: '请输入岗位ID', trigger: 'blur' }],
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  days: [{ required: true, message: '请假天数不能为空', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

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

const handleAdd = () => {
  Object.assign(form, {
    positionId: null,
    leaveType: '',
    startDate: '',
    endDate: '',
    days: 0,
    reason: ''
  })
  dialogVisible.value = true
}

// 计算请假天数
const calculateDays = () => {
  if (form.startDate && form.endDate) {
    const start = new Date(form.startDate)
    const end = new Date(form.endDate)
    const diffTime = Math.abs(end - start)
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1
    form.days = diffDays
  }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await createLeave(form)
        ElMessage.success('请假申请提交成功')
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
