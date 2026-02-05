<template>
  <div class="salary-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="支付状态">
          <el-select 
            v-model="searchForm.paymentStatus" 
            placeholder="支付状态" 
            clearable
            @change="handleSearch"
            @clear="handleSearch"
            style="width: 150px"
          >
            <el-option label="待支付" value="PENDING" />
            <el-option label="已支付" value="PAID" />
            <el-option label="支付失败" value="FAILED" />
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
        新增薪资记录
      </el-button>
      <el-button type="success" :loading="generateLoading" @click="handleAutoGenerate">
        <el-icon><VideoPlay /></el-icon>
        执行自动核算
      </el-button>
    </div>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="payRecordId" label="薪资ID" width="100" />
        <el-table-column label="员工信息" width="180">
          <template #default="{ row }">
            <div style="font-weight: bold">{{ row.userName }}</div>
            <div style="font-size: 12px; color: #909399">{{ row.positionName }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="cycleStart" label="周期开始" width="120" />
        <el-table-column prop="cycleEnd" label="周期结束" width="120" />
        <el-table-column prop="basePay" label="基本工资" width="100">
          <template #default="{ row }">¥{{ row.basePay }}</template>
        </el-table-column>
        <el-table-column prop="grossPay" label="应发工资" width="100">
          <template #default="{ row }">¥{{ row.grossPay }}</template>
        </el-table-column>
        <el-table-column prop="totalDeduction" label="扣除合计" width="100">
          <template #default="{ row }">¥{{ row.totalDeduction }}</template>
        </el-table-column>
        <el-table-column prop="netPay" label="实发工资" width="100">
          <template #default="{ row }">¥{{ row.netPay }}</template>
        </el-table-column>
        <el-table-column prop="deadlineDate" label="最晚支付日期" width="120" />
        <el-table-column label="支付状态" width="180">
          <template #default="{ row }">
            <div v-if="row.actualPaymentDate">
              <el-tag type="success" size="small">已支付</el-tag>
              <div style="font-size: 12px; color: #67C23A; margin-top: 2px;">{{ row.actualPaymentDate }}</div>
            </div>
            <el-tag v-else type="warning" size="small">待支付</el-tag>
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
        <el-form-item label="在岗员工" prop="onDutyWorkerId">
          <el-select
            v-model="form.onDutyWorkerId"
            placeholder="请选择在岗员工"
            filterable
            style="width: 100%"
            :disabled="!!form.payRecordId"
            @change="handleWorkerChange"
          >
            <el-option
              v-for="item in workers"
              :key="item.onDutyWorkerId"
              :label="item.userName + ' - ' + item.positionName"
              :value="item.onDutyWorkerId"
            />
          </el-select>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="周期开始" prop="cycleStart">
              <el-date-picker
                v-model="form.cycleStart"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="周期结束" prop="cycleEnd">
              <el-date-picker
                v-model="form.cycleEnd"
                type="date"
                placeholder="选择结束日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                @change="updateDeadline"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最晚支付日期">
              <el-date-picker
                v-model="form.deadlineDate"
                type="date"
                placeholder="自动计算"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                disabled
              />
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">
                💡 根据周期结束日期自动计算
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际支付日期">
              <el-date-picker
                v-model="form.actualPaymentDate"
                type="date"
                placeholder="未支付"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">
                💡 为空表示未支付
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">收入项</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="基本工资" prop="basePay">
              <el-input-number v-model="form.basePay" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="绩效奖金">
              <el-input-number v-model="form.bonusPay" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="加班费">
              <el-input-number v-model="form.overtimePay" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="补贴">
              <el-input-number v-model="form.allowance" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">扣除项</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="养老保险">
              <el-input-number v-model="form.pensionDeduction" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="医疗保险">
              <el-input-number v-model="form.medicalDeduction" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="失业保险">
              <el-input-number v-model="form.unemploymentDeduction" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工伤保险">
              <el-input-number v-model="form.injuryDeduction" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="公积金">
              <el-input-number v-model="form.pfDeduction" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="个人所得税">
              <el-input-number v-model="form.taxAmount" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="迟到扣款">
              <el-input-number v-model="form.lateDeduction" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="早退扣款">
              <el-input-number v-model="form.earlyLeaveDeduction" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="旷工扣款">
              <el-input-number v-model="form.absentDeduction" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="请假扣款">
              <el-input-number v-model="form.leaveDeduction" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="缺勤扣款">
              <el-input-number v-model="form.absenceDeduction" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">汇总</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="应发工资" prop="grossPay">
              <el-input-number v-model="form.grossPay" :min="0" :precision="2" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="扣除合计" prop="totalDeduction">
              <el-input-number v-model="form.totalDeduction" :min="0" :precision="2" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实发工资" prop="netPay">
              <el-input-number v-model="form.netPay" :min="0" :precision="2" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
        </el-row>

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
import { getPaySlips, createPaySlip, updatePaySlip, deleteSalary, getPredictDeadline, getSuggestedCycle, generatePaySlips } from '../../api/salary'
import { getWorkers } from '../../api/worker'

const searchForm = reactive({
  // 现在通过 actualPaymentDate 判断支付状态，不需要独立的筛选字段
})

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const workers = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const submitLoading = ref(false)
const generateLoading = ref(false)

const form = reactive({
  payRecordId: null,
  onDutyWorkerId: null,
  cycleStart: '',
  cycleEnd: '',
  deadlineDate: '',
  actualPaymentDate: '',
  paymentMethod: '银行转账',
  basePay: 0,
  bonusPay: 0,
  overtimePay: 0,
  allowance: 0,
  pensionDeduction: 0,
  medicalDeduction: 0,
  unemploymentDeduction: 0,
  injuryDeduction: 0,
  pfDeduction: 0,
  taxAmount: 0,
  lateDeduction: 0,
  earlyLeaveDeduction: 0,
  absentDeduction: 0,
  absenceDeduction: 0,
  leaveDeduction: 0,
  grossPay: 0,
  totalDeduction: 0,
  netPay: 0,
  confirmStatus: 1
})

const rules = {
  onDutyWorkerId: [{ required: true, message: '请输入在岗员工ID', trigger: 'blur' }],
  cycleStart: [{ required: true, message: '请选择周期开始日期', trigger: 'change' }],
  cycleEnd: [{ required: true, message: '请选择周期结束日期', trigger: 'change' }],
  basePay: [{ required: true, message: '请输入基本工资', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      paymentStatus: searchForm.paymentStatus || undefined
    }
    
    const res = await getPaySlips(params)
    
    // 客户端分页
    const allData = res.data || []
    total.value = allData.length
    
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

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.paymentStatus = null
  handleSearch()
}

// 计算汇总金额
const calculateTotal = () => {
  // 计算应发工资 = 基本工资 + 绩效奖金 + 加班费 + 补贴
  const grossPay = (form.basePay || 0) + (form.bonusPay || 0) + 
                   (form.overtimePay || 0) + (form.allowance || 0)
  form.grossPay = parseFloat(grossPay.toFixed(2))
  
  // 计算扣除合计 = 养老 + 医疗 + 失业 + 工伤 + 公积金 + 税款 + 迟到 + 早退 + 旷工 + 请假
  const totalDeduction = (form.pensionDeduction || 0) + (form.medicalDeduction || 0) + 
                         (form.unemploymentDeduction || 0) + (form.injuryDeduction || 0) +
                         (form.pfDeduction || 0) + (form.taxAmount || 0) +
                         (form.lateDeduction || 0) + (form.earlyLeaveDeduction || 0) +
                         (form.absentDeduction || 0) + (form.absenceDeduction || 0) + (form.leaveDeduction || 0)
  form.totalDeduction = parseFloat(totalDeduction.toFixed(2))
  
  // 计算实发工资 = 应发工资 - 扣除合计
  let netPay = grossPay - totalDeduction
  if (netPay < 0) netPay = 0
  form.netPay = parseFloat(netPay.toFixed(2))
}

// 自动更新建议周期和最晚支付日期
const handleWorkerChange = async (workerId) => {
  if (!workerId) return
  
  try {
    const res = await getSuggestedCycle(workerId)
    if (res.data) {
      form.cycleStart = res.data.cycleStart
      form.cycleEnd = res.data.cycleEnd
      // 填充周期后，同步触发最晚发放日期的预测
      updateDeadline()
    }
  } catch (error) {
    console.error('获取建议周期失败:', error)
  }
}

// 自动更新最晚支付日期
const updateDeadline = async () => {
  if (form.onDutyWorkerId && form.cycleEnd) {
    try {
      const res = await getPredictDeadline(form.onDutyWorkerId, form.cycleEnd)
      form.deadlineDate = res.data
    } catch (error) {
      console.error('获取预测日期失败:', error)
    }
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增薪资记录'
  Object.assign(form, {
    payRecordId: null,
    onDutyWorkerId: null,
    cycleStart: '',
    cycleEnd: '',
    deadlineDate: '',
    actualPaymentDate: '',
    paymentMethod: '银行转账',
    basePay: 0,
    bonusPay: 0,
    overtimePay: 0,
    allowance: 0,
    pensionDeduction: 0,
    medicalDeduction: 0,
    unemploymentDeduction: 0,
    injuryDeduction: 0,
    pfDeduction: 0,
    taxAmount: 0,
    lateDeduction: 0,
    earlyLeaveDeduction: 0,
    absentDeduction: 0,
    absenceDeduction: 0,
    leaveDeduction: 0,
    grossPay: 0,
    totalDeduction: 0,
    netPay: 0,
    confirmStatus: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑薪资记录'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该薪资记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSalary(row.payRecordId)
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
        if (form.payRecordId) {
          await updatePaySlip(form.payRecordId, form)
          ElMessage.success('更新成功')
        } else {
          await createPaySlip(form)
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

const handleAutoGenerate = () => {
  ElMessageBox.confirm('确定要手动执行全员薪资自动核算任务吗？这将根据考勤和请假记录尝试为所有在岗员工生成最新的薪资条。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    generateLoading.value = true
    try {
      await generatePaySlips()
      ElMessage.success('薪资自动结算任务已启动并完成')
      loadData()
    } catch (error) {
      console.error('执行失败:', error)
      ElMessage.error(error.response?.data?.message || '执行失败')
    } finally {
      generateLoading.value = false
    }
  }).catch(() => {})
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

const loadWorkers = async () => {
  try {
    const res = await getWorkers()
    workers.value = res.data || []
  } catch (error) {
    console.error('加载员工列表失败:', error)
  }
}

onMounted(() => {
  loadData()
  loadWorkers()
})
</script>

<style scoped>
.salary-management {
  width: 100%;
}

.search-card {
  margin-bottom: 20px;
}

.action-buttons {
  margin-bottom: 20px;
}
</style>
