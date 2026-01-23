<template>
  <div class="position-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="岗位名称">
          <el-input v-model="searchForm.positionName" placeholder="请输入岗位名称" clearable />
        </el-form-item>
        <el-form-item label="工作地点">
          <el-input v-model="searchForm.workLocation" placeholder="请输入工作地点" clearable />
        </el-form-item>
        <el-form-item label="岗位状态">
          <el-select v-model="searchForm.positionStatus" placeholder="请选择" clearable>
            <el-option label="未发布" :value="0" />
            <el-option label="招聘中" :value="1" />
            <el-option label="已关闭" :value="2" />
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
        新增岗位
      </el-button>
    </div>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="positionId" label="岗位ID" width="80" />
        <el-table-column prop="positionName" label="岗位名称" width="150" />
        <el-table-column prop="workLocation" label="工作地点" width="200" />
        <el-table-column prop="employmentType" label="用工类型" width="100" />
        <el-table-column prop="basicSalary" label="基本工资" width="100">
          <template #default="{ row }">¥{{ row.basicSalary }}</template>
        </el-table-column>
        <el-table-column prop="payCycle" label="薪资周期" width="100" />
        <el-table-column prop="dailyHours" label="每日工时" width="100" />
        <el-table-column prop="weeklyFreq" label="每周频次" width="100" />
        <el-table-column prop="positionStatus" label="岗位状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.positionStatus === 0" type="info">未发布</el-tag>
            <el-tag v-else-if="row.positionStatus === 1" type="success">招聘中</el-tag>
            <el-tag v-else type="danger">已关闭</el-tag>
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
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位名称" prop="positionName">
              <el-input v-model="form.positionName" placeholder="请输入岗位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作地点" prop="workLocation">
              <el-input v-model="form.workLocation" placeholder="请输入工作地点" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="地区代码" prop="regionCode">
              <el-input v-model="form.regionCode" placeholder="请输入地区代码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用工类型" prop="employmentType">
              <el-select v-model="form.employmentType" placeholder="请选择用工类型" style="width: 100%">
                <el-option label="兼职" value="兼职" />
                <el-option label="全职" value="全职" />
                <el-option label="临时工" value="临时工" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="职责描述" prop="dutyDesc">
          <el-input v-model="form.dutyDesc" type="textarea" :rows="3" placeholder="请输入职责描述" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工作开始时间" prop="workStartTime">
              <el-date-picker
                v-model="form.workStartTime"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作结束时间" prop="workEndTime">
              <el-date-picker
                v-model="form.workEndTime"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="基本工资" prop="basicSalary">
              <el-input-number v-model="form.basicSalary" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资周期" prop="payCycle">
              <el-select v-model="form.payCycle" placeholder="请选择薪资周期" style="width: 100%">
                <el-option label="日结" value="日结" />
                <el-option label="周结" value="周结" />
                <el-option label="月结" value="月结" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="每日工时" prop="dailyHours">
              <el-input-number v-model="form.dailyHours" :min="0" :max="24" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每周频次" prop="weeklyFreq">
              <el-input-number v-model="form.weeklyFreq" :min="0" :max="7" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="劳务公司ID" prop="laborCompanyId">
              <el-input-number v-model="form.laborCompanyId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位状态" prop="positionStatus">
              <el-select v-model="form.positionStatus" placeholder="请选择状态" style="width: 100%">
                <el-option label="未发布" :value="0" />
                <el-option label="招聘中" :value="1" />
                <el-option label="已关闭" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="薪资说明">
          <el-input v-model="form.salaryDesc" type="textarea" :rows="2" placeholder="请输入薪资说明" />
        </el-form-item>

        <el-form-item label="特殊说明">
          <el-input v-model="form.specialNote" type="textarea" :rows="2" placeholder="请输入特殊说明" />
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
import { getPositions, createPosition, updatePosition, deletePosition } from '../../api/position'

// 搜索表单
const searchForm = reactive({
  positionName: '',
  workLocation: '',
  positionStatus: null
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const submitLoading = ref(false)

const form = reactive({
  positionId: null,
  positionName: '',
  workLocation: '',
  regionCode: '',
  dutyDesc: '',
  workStartTime: '',
  workEndTime: '',
  employmentType: '',
  laborCompanyId: null,
  basicSalary: 0,
  payCycle: '',
  salaryDesc: '',
  dailyHours: 0,
  weeklyFreq: 0,
  positionStatus: 0,
  specialNote: ''
})

const rules = {
  positionName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  workLocation: [{ required: true, message: '请输入工作地点', trigger: 'blur' }],
  regionCode: [{ required: true, message: '请输入地区代码', trigger: 'blur' }],
  employmentType: [{ required: true, message: '请选择用工类型', trigger: 'change' }],
  laborCompanyId: [{ required: true, message: '请输入劳务公司ID', trigger: 'blur' }],
  basicSalary: [{ required: true, message: '请输入基本工资', trigger: 'blur' }],
  payCycle: [{ required: true, message: '请选择薪资周期', trigger: 'change' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    const res = await getPositions(params)
    tableData.value = res.data.records || res.data
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.positionName = ''
  searchForm.workLocation = ''
  searchForm.positionStatus = null
  handleSearch()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增岗位'
  Object.assign(form, {
    positionId: null,
    positionName: '',
    workLocation: '',
    regionCode: '',
    dutyDesc: '',
    workStartTime: '',
    workEndTime: '',
    employmentType: '',
    laborCompanyId: null,
    basicSalary: 0,
    payCycle: '',
    salaryDesc: '',
    dailyHours: 0,
    weeklyFreq: 0,
    positionStatus: 0,
    specialNote: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑岗位'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该岗位吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePosition(row.positionId)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 提交
const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (form.positionId) {
          await updatePosition(form.positionId, form)
          ElMessage.success('更新成功')
        } else {
          await createPosition(form)
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

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 分页
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
.position-management {
  width: 100%;
}

.search-card {
  margin-bottom: 20px;
}

.action-buttons {
  margin-bottom: 20px;
}
</style>
