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
        <el-table-column prop="companyName" label="所属劳务公司" min-width="120" show-overflow-tooltip />
        <el-table-column label="薪资发放主体" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="row.salaryPayerId" type="warning" effect="plain">{{ row.salaryPayerName }}</el-tag>
            <el-tag v-else type="info" effect="plain">人力服务公司</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workLocation" label="工作地点" min-width="150" show-overflow-tooltip />
        <el-table-column prop="employmentType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.employmentType === '全日制用工' ? '' : 'success'">
              {{ row.employmentType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="salaryConfigName" label="薪资模版" width="150" />
        <el-table-column label="招聘进度" width="120" align="center">
          <template #default="{ row }">
            <el-tooltip :content="'总数: ' + row.totalPositions + ' / 剩余: ' + row.remainingPositions" placement="top">
              <el-progress 
                :percentage="row.totalPositions > 0 ? Math.round(((row.totalPositions - row.remainingPositions) / row.totalPositions) * 100) : 0" 
                :format="() => row.remainingPositions + '/' + row.totalPositions"
                :status="(row.remainingPositions === 0) ? 'exception' : ''"
              />
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="岗位周期" width="180">
          <template #default="{ row }">
            <div style="font-size: 12px;">{{ row.workStartTime || '-' }} 至</div>
            <div style="font-size: 12px;">{{ row.workEndTime || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="工作时段" width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.checkInTime ? row.checkInTime.substring(0,5) : '-' }} - {{ row.checkOutTime ? row.checkOutTime.substring(0,5) : '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="工时频率" width="120">
          <template #default="{ row }">
            <div style="font-size: 12px;">每周 {{ row.weeklyFreq || 0 }} 天</div>
            <div style="font-size: 12px;">每天 {{ row.dailyHours || 0 }} 时</div>
          </template>
        </el-table-column>
        <el-table-column label="招聘状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.positionStatus === 1 ? 'success' : 'info'">
              {{ row.positionStatus === 1 ? '招聘中' : '已停止' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="handleShowQR(row)">
              <el-icon><Iphone /></el-icon> 考勤码
            </el-button>
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

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="薪资发放主体" prop="salaryPayerId">
              <el-select v-model="form.salaryPayerId" placeholder="人力服务公司 (缺省)" clearable style="width: 100%">
                <el-option v-for="c in companies" :key="c.companyId" :label="c.companyName" :value="c.companyId" />
              </el-select>
              <div style="font-size: 11px; color: #909399; margin-top: 4px;">留空表示由人力服务公司统一结算</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>周期与考勤配置</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计薪周期开始" prop="workStartTime">
              <el-date-picker v-model="form.workStartTime" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计薪周期结束" prop="workEndTime">
              <el-date-picker v-model="form.workEndTime" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上班打卡时间" prop="checkInTime">
              <el-time-picker v-model="form.checkInTime" value-format="HH:mm:ss" placeholder="09:00:00" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下班打卡时间" prop="checkOutTime">
              <el-time-picker v-model="form.checkOutTime" value-format="HH:mm:ss" placeholder="18:00:00" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="每周工作日" prop="workingDaysArray">
          <el-checkbox-group v-model="form.workingDaysArray">
            <el-checkbox label="1">周一</el-checkbox>
            <el-checkbox label="2">周二</el-checkbox>
            <el-checkbox label="3">周三</el-checkbox>
            <el-checkbox label="4">周四</el-checkbox>
            <el-checkbox label="5">周五</el-checkbox>
            <el-checkbox label="6">周六</el-checkbox>
            <el-checkbox label="7">周日</el-checkbox>
          </el-checkbox-group>
          <div style="font-size: 12px; color: #909399;">缺省即代表休息（不计缺勤）</div>
        </el-form-item>

        <el-divider>人数与工时设置</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="招聘总人数" prop="totalPositions">
              <el-input-number v-model="form.totalPositions" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="剩余名额" prop="remainingPositions">
              <el-input-number v-model="form.remainingPositions" :min="0" :max="form.totalPositions" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="每日工时" prop="dailyHours">
              <el-input-number v-model="form.dailyHours" :precision="1" :step="0.5" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="周工作天数" prop="weeklyFreq">
              <el-input-number v-model="form.weeklyFreq" :min="0" :max="7" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>管理与备注信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="行政区域代码" prop="regionCode">
              <el-input v-model="form.regionCode" placeholder="如 310100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="responsibleId">
              <el-select v-model="form.responsibleId" placeholder="请选择负责人" filterable style="width: 100%">
                <el-option 
                  v-for="u in responsibleUsers" 
                  :key="u.userId" 
                  :label="u.name ? `${u.name} (${u.account})` : u.account" 
                  :value="u.userId" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="薪资摘要描述" prop="salaryDesc">
          <el-input v-model="form.salaryDesc" placeholder="如：200元/天，包午餐" />
        </el-form-item>

        <el-form-item label="岗位职责描述" prop="dutyDesc">
          <el-input type="textarea" v-model="form.dutyDesc" :rows="3" placeholder="详细的职责与要求内容..." />
        </el-form-item>

        <el-form-item label="特别说明" prop="specialNote">
          <el-input v-model="form.specialNote" placeholder="对内可见的备注信息" />
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
        <el-button type="primary" @click="handleSubmit">确认保存</el-button>
      </template>
    </el-dialog>

    <!-- 考勤二维码弹窗 -->
    <el-dialog v-model="qrDialogVisible" :title="qrPositionName + ' - 考勤打卡二维码'" width="400px" center>
      <div style="text-align: center; padding: 20px 0;">
        <div class="qr-container" style="background: white; padding: 15px; display: inline-block; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
          <qrcode-vue :value="qrCodeValue" :size="240" level="H" />
        </div>
        <p style="margin-top: 20px; color: #606266; font-size: 14px;">
          请扫码进行该岗位的签到/签退操作
        </p>
        <p style="color: #909399; font-size: 12px; margin-top: 8px;">
          链接: {{ qrCodeValue }}
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="qrDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { Plus, Iphone } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import QrcodeVue from 'qrcode.vue'
import { getPositions, createPosition, updatePosition, deletePosition } from '../../api/position'
import { getCompanies } from '../../api/company'
import { getSalaryConfigs } from '../../api/salary'
import { getUsers } from '../../api/user'

const positions = ref([])
const companies = ref([])
const salaryConfigs = ref([])
const responsibleUsers = ref([])
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
  totalPositions: 1,
  remainingPositions: 1,
  workingDaysArray: ['1', '2', '3', '4', '5'],
  workStartTime: '',
  workEndTime: '',
  salaryPayerId: null,
  specialNote: ''
})

// 二维码相关
const qrDialogVisible = ref(false)
const qrCodeValue = ref('')
const qrPositionName = ref('')

const handleShowQR = (row) => {
  qrPositionName.value = row.positionName
  // 生成打卡链接，如: http://domain/punch/123
  const origin = window.location.origin
  qrCodeValue.value = `${origin}/punch/${row.positionId}`
  qrDialogVisible.value = true
}

// 自动计算逻辑
watch(() => [form.checkInTime, form.checkOutTime], ([inTime, outTime]) => {
  if (inTime && outTime) {
    const start = new Date(`2000-01-01 ${inTime}`)
    const end = new Date(`2000-01-01 ${outTime}`)
    if (end > start) {
      const hours = (end - start) / (1000 * 60 * 60)
      form.dailyHours = parseFloat(hours.toFixed(1))
    }
  }
})

watch(() => form.workingDaysArray, (newDays) => {
  form.weeklyFreq = newDays.length
}, { deep: true })

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
  const [compRes, skillRes, userRes] = await Promise.all([
    getCompanies(), 
    getSalaryConfigs(),
    getUsers({ role: 'ADMIN' })
  ])
  companies.value = compRes.data
  salaryConfigs.value = skillRes.data
  responsibleUsers.value = userRes.data
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
    positionStatus: 1,
    totalPositions: 1,
    remainingPositions: 1,
    workingDaysArray: ['1', '2', '3', '4', '5'],
    checkInTime: '09:00:00',
    checkOutTime: '18:00:00',
    workStartTime: '',
    workEndTime: '',
    dailyHours: 8,
    weeklyFreq: 5,
    responsibleId: 1,
    salaryDesc: '',
    specialNote: '',
    regionCode: '310100',
    salaryPayerId: null
  })
  dialogFormVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, row)
  // 处理工作日多选框回显
  if (row.workingDays) {
    form.workingDaysArray = row.workingDays.split(',')
  } else {
    form.workingDaysArray = []
  }
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
      // 提交前将工作日数组转换为逗号分隔字符串
      form.workingDays = form.workingDaysArray.join(',')
      
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
