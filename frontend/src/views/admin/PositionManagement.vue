<template>
  <div class="position-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="岗位名称">
          <el-input v-model="searchForm.positionName" placeholder="模糊搜索" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="用工类型">
          <el-select v-model="searchForm.employmentType" placeholder="全部" clearable style="width: 150px">
            <el-option label="全日制用工" value="全日制用工" />
            <el-option label="非全日制用工" value="非全日制用工" />
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
        <el-icon><Plus /></el-icon> 发布新岗位
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-card>
      <el-table :data="positions" border stripe v-loading="loading">
        <el-table-column prop="positionId" label="ID" width="60" />
        <el-table-column prop="positionName" label="岗位名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="companyName" label="所属劳务公司" min-width="150" show-overflow-tooltip />
        <el-table-column prop="workLocation" label="工作地点" min-width="150" show-overflow-tooltip />
        <el-table-column prop="employmentType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.employmentType === '全日制用工' ? '' : 'success'">
              {{ row.employmentType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="salaryConfigName" label="薪资模版" width="150">
          <template #default="{ row }">
             <el-tag type="info">{{ row.salaryConfigName || '未关联' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="招聘状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.positionStatus === 1 ? 'success' : 'info'">
              {{ row.positionStatus === 1 ? '招聘中' : '已停止' }}
            </el-tag>
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
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 发布/编辑对话框 -->
    <el-dialog v-model="dialogFormVisible" :title="form.positionId ? '编辑岗位' : '发布岗位'" width="800px">
      <el-form ref="positionFormRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位名称" prop="positionName">
              <el-input v-model="form.positionName" placeholder="请输入岗位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用工类型" prop="employmentType">
              <el-select v-model="form.employmentType" placeholder="请选择类型" style="width: 100%">
                <el-option label="全日制用工" value="全日制用工" />
                <el-option label="非全日制用工" value="非全日制用工" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属公司" prop="laborCompanyId">
              <el-select v-model="form.laborCompanyId" placeholder="选择劳务公司" style="width: 100%">
                <el-option v-for="c in companies" :key="c.companyId" :label="c.companyName" :value="c.companyId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资模版" prop="salaryConfigId">
              <el-select v-model="form.salaryConfigId" placeholder="关联薪资配置模版" style="width: 100%">
                <el-option v-for="item in salaryConfigs" :key="item.configId" :label="item.configName" :value="item.configId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="工作地点" prop="workLocation">
          <el-input v-model="form.workLocation" placeholder="详细工作地址" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="招聘人数" prop="totalPositions">
              <el-input-number v-model="form.totalPositions" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人ID" prop="responsibleId">
              <el-input-number v-model="form.responsibleId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="打卡上班" prop="checkInTime">
              <el-time-picker v-model="form.checkInTime" value-format="HH:mm:ss" placeholder="早打卡" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="打卡下班" prop="checkOutTime">
              <el-time-picker v-model="form.checkOutTime" value-format="HH:mm:ss" placeholder="晚打卡" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述/要求" prop="dutyDesc">
          <el-input type="textarea" v-model="form.dutyDesc" :rows="4" placeholder="详细岗位职责描述..." />
        </el-form-item>

        <el-form-item label="岗位状态">
          <el-radio-group v-model="form.positionStatus">
            <el-radio :label="1">招聘中</el-radio>
            <el-radio :label="0">停止招聘</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPositions, createPosition, updatePosition, deletePosition } from '../../api/position'
import { getCompanies } from '../../api/company'
import { getSalaryConfigs } from '../../api/salary'

const positions = ref([])
const companies = ref([])
const salaryConfigs = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  positionName: '',
  employmentType: ''
})

const dialogFormVisible = ref(false)
const positionFormRef = ref(null)

let form = reactive({
  positionId: null,
  positionName: '',
  workLocation: '',
  regionCode: '310100',
  dutyDesc: '',
  employmentType: '全日制用工',
  laborCompanyId: null,
  salaryConfigId: null,
  salaryDesc: '',
  dailyHours: 8,
  weeklyFreq: 5,
  workingDays: '1,2,3,4,5',
  checkInTime: '09:00:00',
  checkOutTime: '18:00:00',
  positionStatus: 1,
  responsibleId: 1,
  totalPositions: 10,
  remainingPositions: 10
})

const rules = {
  positionName: [{ required: true, message: '必填项', trigger: 'blur' }],
  laborCompanyId: [{ required: true, message: '必选项', trigger: 'change' }],
  salaryConfigId: [{ required: true, message: '请关联薪资模版', trigger: 'change' }],
  employmentType: [{ required: true, message: '必选项', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPositions({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    })
    positions.value = res.data.list || res.data
    total.value = res.data.total || positions.value.length
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const loadConfigs = async () => {
  const [compRes, skillRes] = await Promise.all([getCompanies(), getSalaryConfigs()])
  companies.value = compRes.data
  salaryConfigs.value = skillRes.data
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.positionName = ''
  searchForm.employmentType = ''
  handleSearch()
}

const handleAdd = () => {
  Object.assign(form, {
    positionId: null,
    positionName: '',
    salaryConfigId: null,
    laborCompanyId: null,
    positionStatus: 1
  })
  dialogFormVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, row)
  dialogFormVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除吗？', '警告', { type: 'error' }).then(async () => {
    await deletePosition(row.positionId)
    ElMessage.success('已删除')
    loadData()
  })
}

const handleSubmit = async () => {
  await positionFormRef.value.validate(async (valid) => {
    if (valid) {
      if (form.positionId) await updatePosition(form.positionId, form)
      else await createPosition(form)
      ElMessage.success('操作成功')
      dialogFormVisible.value = false
      loadData()
    }
  })
}

const handleSizeChange = val => { pageSize.value = val; loadData() }
const handleCurrentChange = val => { currentPage.value = val; loadData() }

onMounted(() => {
  loadData()
  loadConfigs()
})
</script>
