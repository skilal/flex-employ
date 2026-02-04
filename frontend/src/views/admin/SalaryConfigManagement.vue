<template>
  <div class="salary-config-management">
    <el-card>
      <template #header>
        <div class="header-content">
          <span>薪资配置模版管理</span>
          <el-button type="primary" @click="handleAdd">新增模版</el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="configId" label="ID" width="80" />
        <el-table-column prop="configName" label="模版名称" min-width="150" />
        <el-table-column prop="payCycle" label="结算周期" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.payCycle }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="billingMethod" label="计费方式" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.billingMethod === 1 ? 'warning' : 'success'">
              {{ row.billingMethod === 1 ? '按小时' : '按天' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="baseRate" label="基础单价" width="120">
          <template #default="{ row }">
            ¥{{ row.baseRate }} / {{ row.billingMethod === 1 ? '时' : '天' }}
          </template>
        </el-table-column>
        <el-table-column prop="overtimeRate" label="加班费" width="100">
          <template #default="{ row }">
            {{ row.overtimeRate ? `¥${row.overtimeRate}/h` : '无' }}
          </template>
        </el-table-column>
        <el-table-column label="异常扣款预览" min-width="180">
          <template #default="{ row }">
            <div style="font-size: 12px">
              迟到: ¥{{ row.latePenalty }} | 早退: ¥{{ row.earlyPenalty }} | 旷工: ¥{{ row.absentPenalty }}
            </div>
            <div style="font-size: 12px; color: #909399">
              病假: {{ row.sickLeaveRate*100 }}% | 事假: {{ row.personalLeaveRate*100 }}%
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="模版名称" prop="configName">
          <el-input v-model="form.configName" placeholder="如：全日制标准模版-2026" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="结算周期" prop="payCycle">
              <el-select v-model="form.payCycle" placeholder="请选择" style="width: 100%">
                <el-option label="日结" value="日结" />
                <el-option label="周结" value="周结" />
                <el-option label="15日结" value="15日结" />
                <el-option label="月结" value="月结" />
                <el-option label="一次性结算" value="一次性结算" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计费方式" prop="billingMethod">
              <el-radio-group v-model="form.billingMethod">
                <el-radio :label="1">按小时</el-radio>
                <el-radio :label="2">按天</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="基础单价" prop="baseRate">
              <el-input-number v-model="form.baseRate" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="加班费标准" prop="overtimeRate">
              <el-input-number v-model="form.overtimeRate" :min="0" :precision="2" style="width: 100%" placeholder="元/小时" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">考勤扣款配置</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="迟到扣款" prop="latePenalty">
              <el-input-number v-model="form.latePenalty" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="早退扣款" prop="earlyPenalty">
              <el-input-number v-model="form.earlyPenalty" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="旷工扣款" prop="absentPenalty">
              <el-input-number v-model="form.absentPenalty" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="病假扣款比例" prop="sickLeaveRate">
              <el-input-number v-model="form.sickLeaveRate" :min="0" :max="1" :step="0.1" style="width: 100%" />
              <div class="tip">比例（0.4 代表扣除当日工资的 40%）</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="事假扣款比例" prop="personalLeaveRate">
              <el-input-number v-model="form.personalLeaveRate" :min="0" :max="1" :step="0.1" style="width: 100%" />
              <div class="tip">比例（1.0 代表扣除当日全薪）</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSalaryConfigs, createSalaryConfig, updateSalaryConfig, deleteSalaryConfig } from '../../api/salary'

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  configId: null,
  configName: '',
  payCycle: '月结',
  billingMethod: 2,
  baseRate: 0,
  overtimeRate: null,
  latePenalty: 0,
  earlyPenalty: 0,
  absentPenalty: 0,
  sickLeaveRate: 0.4,
  personalLeaveRate: 1.0
})

const rules = {
  configName: [{ required: true, message: '请输入模版名称', trigger: 'blur' }],
  payCycle: [{ required: true, message: '请选择结算周期', trigger: 'change' }],
  billingMethod: [{ required: true, message: '请选择计费方式', trigger: 'change' }],
  baseRate: [{ required: true, message: '请输入基础单价', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSalaryConfigs()
    tableData.value = res.data || []
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增薪资模版'
  Object.assign(form, {
    configId: null,
    configName: '',
    payCycle: '月结',
    billingMethod: 2,
    baseRate: 0,
    overtimeRate: null,
    latePenalty: 0,
    earlyPenalty: 0,
    absentPenalty: 0,
    sickLeaveRate: 0.4,
    personalLeaveRate: 1.0
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑薪资模版'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该薪资配置模版吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSalaryConfig(row.configId)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (form.configId) {
          await updateSalaryConfig(form.configId, form)
          ElMessage.success('更新成功')
        } else {
          await createSalaryConfig(form)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error('提交失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
