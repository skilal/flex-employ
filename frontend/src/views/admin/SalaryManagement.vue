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
    </div>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="payRecordId" label="薪资ID" width="100" />
        <el-table-column prop="onDutyWorkerId" label="在岗员工ID" width="120" />
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
        <el-table-column prop="paymentStatus" label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.paymentStatus === 'PENDING'" type="warning">待支付</el-tag>
            <el-tag v-else-if="row.paymentStatus === 'PAID'" type="success">已支付</el-tag>
            <el-tag v-else type="danger">支付失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentDate" label="支付日期" width="120" />
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
            <el-form-item label="在岗员工ID" prop="onDutyWorkerId">
              <el-input-number v-model="form.onDutyWorkerId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付日期" prop="paymentDate">
              <el-date-picker
                v-model="form.paymentDate"
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
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">收入项</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="基本工资" prop="basePay">
              <el-input-number v-model="form.basePay" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="绩效奖金">
              <el-input-number v-model="form.performanceBonus" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="加班费">
              <el-input-number v-model="form.overtimePay" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="补贴">
              <el-input-number v-model="form.allowance" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">扣除项</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="社保合计" prop="insuranceTotal">
              <el-input-number v-model="form.insuranceTotal" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公积金" prop="pfContribution">
              <el-input-number v-model="form.pfContribution" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="税款" prop="taxAmount">
              <el-input-number v-model="form.taxAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="迟到扣款">
              <el-input-number v-model="form.lateDeduction" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="自定义增减">
              <el-input-number v-model="form.customAddDeduct" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际工时" prop="actualWorkTime">
              <el-input-number v-model="form.actualWorkTime" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">汇总</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="应发工资" prop="grossPay">
              <el-input-number v-model="form.grossPay" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="扣除合计" prop="totalDeduction">
              <el-input-number v-model="form.totalDeduction" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实发工资" prop="netPay">
              <el-input-number v-model="form.netPay" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="支付状态" prop="paymentStatus">
          <el-select v-model="form.paymentStatus" placeholder="请选择" style="width: 100%">
            <el-option label="待支付" value="PENDING" />
            <el-option label="已支付" value="PAID" />
            <el-option label="支付失败" value="FAILED" />
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
import { getSalaries, createSalary, updateSalary, deleteSalary } from '../../api/salary'

const searchForm = reactive({
  paymentStatus: null
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
  payRecordId: null,
  onDutyWorkerId: null,
  cycleStart: '',
  cycleEnd: '',
  paymentDate: '',
  basePay: 0,
  performanceBonus: 0,
  overtimePay: 0,
  allowance: 0,
  actualWorkTime: 0,
  insuranceTotal: 0,
  pfContribution: 0,
  taxAmount: 0,
  lateDeduction: 0,
  customAddDeduct: 0,
  grossPay: 0,
  totalDeduction: 0,
  netPay: 0,
  paymentStatus: 'PENDING'
})

const rules = {
  onDutyWorkerId: [{ required: true, message: '请输入在岗员工ID', trigger: 'blur' }],
  cycleStart: [{ required: true, message: '请选择周期开始日期', trigger: 'change' }],
  cycleEnd: [{ required: true, message: '请选择周期结束日期', trigger: 'change' }],
  paymentDate: [{ required: true, message: '请选择支付日期', trigger: 'change' }],
  basePay: [{ required: true, message: '请输入基本工资', trigger: 'blur' }],
  actualWorkTime: [{ required: true, message: '请输入实际工时', trigger: 'blur' }],
  insuranceTotal: [{ required: true, message: '请输入社保合计', trigger: 'blur' }],
  pfContribution: [{ required: true, message: '请输入公积金', trigger: 'blur' }],
  taxAmount: [{ required: true, message: '请输入税款', trigger: 'blur' }],
  grossPay: [{ required: true, message: '请输入应发工资', trigger: 'blur' }],
  totalDeduction: [{ required: true, message: '请输入扣除合计', trigger: 'blur' }],
  netPay: [{ required: true, message: '请输入实发工资', trigger: 'blur' }],
  paymentStatus: [{ required: true, message: '请选择支付状态', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      paymentStatus: searchForm.paymentStatus || undefined
    }
    
    const res = await getSalaries(params)
    
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

const handleAdd = () => {
  dialogTitle.value = '新增薪资记录'
  Object.assign(form, {
    payRecordId: null,
    onDutyWorkerId: null,
    cycleStart: '',
    cycleEnd: '',
    paymentDate: '',
    basePay: 0,
    performanceBonus: 0,
    overtimePay: 0,
    allowance: 0,
    actualWorkTime: 0,
    insuranceTotal: 0,
    pfContribution: 0,
    taxAmount: 0,
    lateDeduction: 0,
    customAddDeduct: 0,
    grossPay: 0,
    totalDeduction: 0,
    netPay: 0,
    paymentStatus: 'PENDING'
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
          await updateSalary(form.payRecordId, form)
          ElMessage.success('更新成功')
        } else {
          await createSalary(form)
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
