<template>
  <div class="attendance-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="考勤日期">
          <el-date-picker
            v-model="searchForm.attendanceDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="考勤状态">
          <el-select v-model="searchForm.attendanceStatus" placeholder="请选择" clearable>
            <el-option label="正常" value="正常" />
            <el-option label="迟到" value="迟到" />
            <el-option label="早退" value="早退" />
            <el-option label="缺勤" value="缺勤" />
            <el-option label="请假" value="请假" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
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
        <el-table-column prop="attendanceId" label="考勤ID" width="100" />
        <el-table-column prop="onDutyWorkerId" label="在岗员工ID" width="120" />
        <el-table-column prop="positionId" label="岗位ID" width="100" />
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
        <el-form-item label="在岗员工ID" prop="onDutyWorkerId">
          <el-input-number v-model="form.onDutyWorkerId" :min="1" style="width: 100%" />
        </el-form-item>

        <el-form-item label="岗位ID" prop="positionId">
          <el-input-number v-model="form.positionId" :min="1" style="width: 100%" />
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

        <el-form-item label="考勤状态" prop="attendanceStatus">
          <el-select v-model="form.attendanceStatus" placeholder="请选择" style="width: 100%">
            <el-option label="正常" value="正常" />
            <el-option label="迟到" value="迟到" />
            <el-option label="早退" value="早退" />
            <el-option label="缺勤" value="缺勤" />
            <el-option label="请假" value="请假" />
          </el-select>
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

const searchForm = reactive({
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
  attendanceDate: '',
  actualCheckIn: '',
  actualCheckOut: '',
  attendanceStatus: '缺勤'
})

const rules = {
  onDutyWorkerId: [{ required: true, message: '请输入在岗员工ID', trigger: 'blur' }],
  positionId: [{ required: true, message: '请输入岗位ID', trigger: 'blur' }],
  attendanceDate: [{ required: true, message: '请选择考勤日期', trigger: 'change' }],
  attendanceStatus: [{ required: true, message: '请选择考勤状态', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    const res = await getAttendances(params)
    tableData.value = res.data.records || res.data
    total.value = res.data.total || 0
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
  searchForm.attendanceDate = null
  searchForm.attendanceStatus = null
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增考勤记录'
  Object.assign(form, {
    attendanceId: null,
    onDutyWorkerId: null,
    positionId: null,
    attendanceDate: '',
    actualCheckIn: '',
    actualCheckOut: '',
    attendanceStatus: '缺勤'
  })
  dialogVisible.value = true
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
