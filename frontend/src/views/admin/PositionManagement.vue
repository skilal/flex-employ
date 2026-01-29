<template>
  <div class="position-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="岗位名称">
          <el-input 
            v-model="searchForm.positionName" 
            placeholder="请输入岗位名称" 
            clearable 
            @clear="handleSearch"
            style="width: 200px"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="工作地点">
          <el-input 
            v-model="searchForm.workLocation" 
            placeholder="请输入工作地点" 
            clearable 
            @clear="handleSearch"
            style="width: 200px"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="用工类型">
          <el-select 
            v-model="searchForm.employmentType" 
            placeholder="用工类型" 
            clearable
            @change="handleSearch"
            @clear="handleSearch"
            style="width: 150px"
          >
            <el-option label="全日制用工" value="全日制用工" />
            <el-option label="非全日制用工" value="非全日制用工" />
            <el-option label="项目制用工" value="项目制用工" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位状态">
          <el-select 
            v-model="searchForm.positionStatus" 
            placeholder="岗位状态" 
            clearable
            @change="handleSearch"
            @clear="handleSearch"
            style="width: 150px"
          >
            <el-option label="未发布" :value="0" />
            <el-option label="招聘中" :value="1" />
            <el-option label="已关闭" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
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
        <el-table-column prop="employmentType" label="用工类型" width="120" />
        <el-table-column prop="basicSalary" label="基本工资" width="100">
          <template #default="{ row }">¥{{ row.basicSalary }}</template>
        </el-table-column>
        <el-table-column prop="payCycle" label="薪资周期" width="100" />
        <el-table-column prop="dailyHours" label="每日工时" width="100" />
        <el-table-column prop="weeklyFreq" label="每周频次" width="100" />
        <el-table-column label="劳务公司" width="200">
          <template #default="{ row }">
            <div>
              <div>ID: {{ row.laborCompanyId || '-' }}</div>
              <div style="color: #909399; font-size: 12px;">
                {{ row.companyName || '-' }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalPositions" label="招聘人数" width="100" />
        <el-table-column prop="remainingPositions" label="剩余人数" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.remainingPositions <= 0 ? 'red' : '' }">
              {{ row.remainingPositions || 0 }}
            </span>
          </template>
        </el-table-column>
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
              <el-select 
                v-model="form.employmentType" 
                placeholder="请选择用工类型" 
                style="width: 100%"
                @change="handleEmploymentTypeChange"
              >
                <el-option label="全日制用工" value="全日制用工" />
                <el-option label="非全日制用工" value="非全日制用工" />
                <el-option label="项目制用工" value="项目制用工" />
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
                <el-option label="一次性结算" value="一次性结算" />
                <el-option label="日结" value="日结" />
                <el-option label="周结" value="周结" />
                <el-option label="15日结" value="15日结" />
                <el-option label="月结" value="月结" />
              </el-select>
              <span v-if="payCycleTip" style="font-size: 12px; color: #909399; margin-top: 4px; display: block;">
                {{ payCycleTip }}
              </span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="每日工时" prop="dailyHours">
              <el-input-number 
                v-model="form.dailyHours" 
                :min="0" 
                :max="24" 
                :precision="1" 
                style="width: 100%" 
              />
              <span v-if="form.employmentType === '非全日制用工'" style="font-size: 12px; color: #E6A23C; margin-top: 4px; display: block;">
                ⚠️ 非全日制用工每日工时不超过4小时
              </span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每周频次" prop="weeklyFreq">
              <el-input-number 
                v-model="form.weeklyFreq" 
                :min="0" 
                :max="7" 
                style="width: 100%" 
              />
              <span v-if="form.employmentType === '非全日制用工' && form.dailyHours && form.weeklyFreq" 
                    style="font-size: 12px; margin-top: 4px; display: block;"
                    :style="{ color: (form.dailyHours * form.weeklyFreq > 24) ? '#F56C6C' : '#67C23A' }">
                {{ form.dailyHours * form.weeklyFreq > 24 ? '❌' : '✓' }} 
                周总工时: {{ (form.dailyHours * form.weeklyFreq).toFixed(1) }}小时 
                {{ form.dailyHours * form.weeklyFreq > 24 ? '(不超过24小时)' : '' }}
              </span>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="招聘人数" prop="totalPositions">
              <el-input-number 
                v-model="form.totalPositions" 
                :min="1" 
                :max="999" 
                style="width: 100%" 
                placeholder="请输入招聘总人数"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="剩余人数" prop="remainingPositions">
              <el-input-number 
                v-model="form.remainingPositions" 
                :min="0" 
                :max="999" 
                style="width: 100%" 
                placeholder="剩余可招聘人数"
              />
              <span style="font-size: 12px; color: #E6A23C; margin-top: 4px; display: block;">
                ⚠️ 修改此值会影响岗位招聘状态
              </span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="劳务公司ID" prop="laborCompanyId">
              <el-input-number 
                v-model="form.laborCompanyId" 
                :min="1" 
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
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
import { Search } from '@element-plus/icons-vue'
import { getPositions, createPosition, updatePosition, deletePosition } from '../../api/position'

// 搜索表单
const searchForm = reactive({
  positionName: '',
  workLocation: '',
  employmentType: null,
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

// 薪资周期提示
const payCycleTip = ref('')

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
  responsibleId: null,
  specialNote: '',
  totalPositions: 1,        // 招聘人数默认1
  remainingPositions: 1     // 剩余人数默认1
})

const rules = {
  positionName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  workLocation: [{ required: true, message: '请输入工作地点', trigger: 'blur' }],
  regionCode: [{ required: true, message: '请输入地区代码', trigger: 'blur' }],
  employmentType: [{ required: true, message: '请选择用工类型', trigger: 'change' }],
  laborCompanyId: [{ required: true, message: '请输入劳务公司ID', trigger: 'blur' }],
  basicSalary: [{ required: true, message: '请输入基本工资', trigger: 'blur' }],
  payCycle: [{ required: true, message: '请选择薪资周期', trigger: 'change' }],
  dailyHours: [
    { 
      validator: (rule, value, callback) => {
        if (form.employmentType === '非全日制用工' && value > 4) {
          callback(new Error('非全日制用工每日工时不能超过4小时'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  weeklyFreq: [
    { 
      validator: (rule, value, callback) => {
        if (form.employmentType === '非全日制用工') {
          const weeklyHours = form.dailyHours * value
          if (weeklyHours > 24) {
            callback(new Error(`每周总工时${weeklyHours.toFixed(1)}小时，不能超过24小时`))
          } else {
            callback()
          }
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 用工类型变化处理
const handleEmploymentTypeChange = (type) => {
  // 根据用工类型提供薪资周期建议
  switch(type) {
    case '全日制用工':
      payCycleTip.value = '💡 推荐：月结'
      form.payCycle = '月结'
      form.dailyHours = 8
      form.weeklyFreq = 5
      break
    case '非全日制用工':
      payCycleTip.value = '💡 推荐：15日结（每日≤4h，每周≤24h）'
      form.payCycle = '15日结'
      form.dailyHours = 4
      form.weeklyFreq = 5
      break
    case '项目制用工':
      payCycleTip.value = '💡 可选：一次性结算、日结、周结、月结'
      form.payCycle = ''
      break
    default:
      payCycleTip.value = ''
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 只传筛选参数，不传分页参数
    const params = {
      positionName: searchForm.positionName || undefined,
      workLocation: searchForm.workLocation || undefined,
      employmentType: searchForm.employmentType || undefined,
      positionStatus: searchForm.positionStatus !== null ? searchForm.positionStatus : undefined
    }
    
    const res = await getPositions(params)
    
    // 后端返回的是完整数据列表，需要在前端进行分页
   const allData = res.data || []
    total.value = allData.length
    
    // 客户端分页：计算当前页应该显示的数据
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    tableData.value = allData.slice(start, end)
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
  searchForm.employmentType = null
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
    specialNote: '',
    totalPositions: 1,
    remainingPositions: 1
  })
  payCycleTip.value = ''
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
