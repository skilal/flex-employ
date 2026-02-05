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
        <el-table-column prop="hasOvertimePay" label="计加班费" width="100">
          <template #default="{ row }">
            <el-tag :type="row.hasOvertimePay === 1 ? 'success' : 'info'" size="small">
              {{ row.hasOvertimePay === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="扣款/费率预览" min-width="250">
          <template #default="{ row }">
            <div style="font-size: 12px">
              <b>扣款:</b> 迟到 ¥{{ row.latePenalty }} | 早退 ¥{{ row.earlyPenalty }} | 旷工 ¥{{ row.absentPenalty }} | 缺勤 ¥{{ row.absencePenalty || 0 }}
            </div>
            <div style="font-size: 12px; color: #67C23A">
              <b>绩效:</b> ¥{{ row.performanceBonus }} | 提成: {{ row.commission }}% | 奖金: ¥{{ row.bonus }}
            </div>
            <div style="font-size: 11px; color: #909399">
              <b>个人费率:</b> 养老 {{ (row.pensionRate*100).toFixed(2) }}% | 医疗 {{ (row.medicalRate*100).toFixed(2) }}% | 公积金 {{ (row.housingFundRate*100).toFixed(2) }}%
            </div>
            <div style="font-size: 11px; color: #409EFF">
              <b>企业费率:</b> 养老 {{ ((row.pensionRateEnt || 0)*100).toFixed(2) }}% | 医疗 {{ ((row.medicalRateEnt || 0)*100).toFixed(2) }}% | 公积金 {{ ((row.housingFundRateEnt || 0)*100).toFixed(2) }}%
            </div>
            <div style="font-size: 11px; color: #E6A23C">
              <b>加班:</b> {{ row.overtimeWeekdayMultiplier }}倍 / {{ row.overtimeWeekendMultiplier }}倍 / {{ row.overtimeHolidayMultiplier }}倍
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
            <el-form-item label="启用加班费" prop="hasOvertimePay">
              <el-switch v-model="form.hasOvertimePay" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">考勤扣款配置</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="迟到扣款" prop="latePenalty">
              <el-input-number v-model="form.latePenalty" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="早退扣款" prop="earlyPenalty">
              <el-input-number v-model="form.earlyPenalty" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="旷工扣款" prop="absentPenalty">
              <el-input-number v-model="form.absentPenalty" :min="0" controls-position="right" style="width: 100%" />
              <div class="tip">全天未签到定为旷工</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="缺勤扣款" prop="absencePenalty">
              <el-input-number v-model="form.absencePenalty" :min="0" controls-position="right" style="width: 100%" />
              <div class="tip">迟到/早退超阈值定为缺勤</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="病假扣款比例" prop="sickLeaveRate">
              <el-input-number v-model="form.sickLeaveRate" :min="0" :max="1" :step="0.05" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="事假扣款比例" prop="personalLeaveRate">
              <el-input-number v-model="form.personalLeaveRate" :min="0" :max="1" :step="0.05" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">绩效、提成与奖金</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="固定绩效" prop="performanceBonus">
              <el-input-number v-model="form.performanceBonus" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提成比例(%)" prop="commission">
              <el-input-number v-model="form.commission" :min="0" :max="100" :precision="2" :step="0.01" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="固定奖金" prop="bonus">
              <el-input-number v-model="form.bonus" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">五险一金费率(个人%)</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="养老保险" prop="pensionRate">
              <el-input-number v-model="form.pensionRate" :min="0" :max="100" :precision="2" :step="0.01" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="医疗保险" prop="medicalRate">
              <el-input-number v-model="form.medicalRate" :min="0" :max="100" :precision="2" :step="0.01" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="失业保险" prop="unemploymentRate">
              <el-input-number v-model="form.unemploymentRate" :min="0" :max="100" :precision="2" :step="0.01" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="工伤保险" prop="injuryRate">
              <el-input-number v-model="form.injuryRate" :min="0" :max="100" :precision="2" :step="0.01" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="公积金率" prop="housingFundRate">
              <el-input-number v-model="form.housingFundRate" :min="0" :max="100" :precision="2" :step="0.01" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">五险一金费率(企业%)</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="养老保险" prop="pensionRateEnt">
              <el-input-number v-model="form.pensionRateEnt" :min="0" :max="100" :precision="2" :step="0.01" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="医疗保险" prop="medicalRateEnt">
              <el-input-number v-model="form.medicalRateEnt" :min="0" :max="100" :precision="2" :step="0.01" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="失业保险" prop="unemploymentRateEnt">
              <el-input-number v-model="form.unemploymentRateEnt" :min="0" :max="100" :precision="2" :step="0.01" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="工伤保险" prop="injuryRateEnt">
              <el-input-number v-model="form.injuryRateEnt" :min="0" :max="100" :precision="2" :step="0.01" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="公积金率" prop="housingFundRateEnt">
              <el-input-number v-model="form.housingFundRateEnt" :min="0" :max="100" :precision="2" :step="0.01" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">加班多倍费率与规则</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="日常加班倍率" prop="overtimeWeekdayMultiplier">
              <el-input-number v-model="form.overtimeWeekdayMultiplier" :min="1" :precision="2" :step="0.1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="周末加班倍率" prop="overtimeWeekendMultiplier">
              <el-input-number v-model="form.overtimeWeekendMultiplier" :min="1" :precision="2" :step="0.1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="节假日倍率" prop="overtimeHolidayMultiplier">
              <el-input-number v-model="form.overtimeHolidayMultiplier" :min="1" :precision="2" :step="0.1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="加班计算模式" prop="overtimeCalcMode">
              <el-select v-model="form.overtimeCalcMode" placeholder="请选择" style="width: 100%">
                <el-option label="起算阈值模式" :value="1" />
                <el-option label="四舍五入到整" :value="2" />
                <el-option label="分钟折算(合规)" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.overtimeCalcMode === 1">
            <el-form-item label="起算阈值(分)" prop="overtimeThresholdMin">
              <el-input-number v-model="form.overtimeThresholdMin" :min="0" :step="5" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.overtimeCalcMode === 2">
            <el-form-item label="取整单位(h)" prop="overtimeRoundingUnit">
              <el-input-number v-model="form.overtimeRoundingUnit" :min="0.1" :max="1.0" :step="0.1" :precision="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="tip" style="margin-left: 120px; margin-bottom: 20px;">
          * {{ form.overtimeCalcMode === 1 ? '起算阈值：加班不满该分钟数不计薪' : (form.overtimeCalcMode === 2 ? '取整计算：按照设定的单位进行四舍五入取整' : '分钟折算：严格按照实际分钟数进行比例换算') }}
        </div>

        <el-divider content-position="left">缺勤判定阈值</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="迟到视为缺勤" prop="lateThresholdMin">
              <el-input-number v-model="form.lateThresholdMin" :min="0" controls-position="right" style="width: 100%" />
              <div class="tip">超过该分钟数未签到即视为缺勤 (0 表示禁用)</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="早退视为缺勤" prop="earlyLeaveThresholdMin">
              <el-input-number v-model="form.earlyLeaveThresholdMin" :min="0" controls-position="right" style="width: 100%" />
              <div class="tip">早退超过该分钟数即视为缺勤 (0 表示禁用)</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">计薪基数预留 (暂未启用逻辑)</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="社保基数上限" prop="socialSecurityBaseUpper">
              <el-input-number v-model="form.socialSecurityBaseUpper" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="社保基数下限" prop="socialSecurityBaseLower">
              <el-input-number v-model="form.socialSecurityBaseLower" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="tip" style="margin-left: 120px; margin-bottom: 20px;">* 以上费率请输入百分比数值，如 8 代表 8%</div>
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
  hasOvertimePay: 1,
  latePenalty: 0,
  earlyPenalty: 0,
  absentPenalty: 0,
  absencePenalty: 0,
  sickLeaveRate: 0.4,
  personalLeaveRate: 1.0,
  performanceBonus: 0,
  commission: 0,
  bonus: 0,
  pensionRate: 0,
  medicalRate: 0,
  unemploymentRate: 0,
  injuryRate: 0,
  housingFundRate: 0,
  pensionRateEnt: 0,
  medicalRateEnt: 0,
  unemploymentRateEnt: 0,
  injuryRateEnt: 0,
  housingFundRateEnt: 0,
  socialSecurityBaseUpper: 0,
  socialSecurityBaseLower: 0,
  overtimeWeekdayMultiplier: 1.5,
  overtimeWeekendMultiplier: 2.0,
  overtimeHolidayMultiplier: 3.0,
  overtimeCalcMode: 3,
  overtimeThresholdMin: 30,
  overtimeRoundingUnit: 1.0,
  lateThresholdMin: 0,
  earlyLeaveThresholdMin: 0
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
    hasOvertimePay: 1,
    latePenalty: 0,
    earlyPenalty: 0,
    absentPenalty: 0,
    absencePenalty: 0,
    sickLeaveRate: 0.4,
    personalLeaveRate: 1.0,
    performanceBonus: 0,
    commission: 0,
    bonus: 0,
    pensionRate: 8,
    medicalRate: 2,
    unemploymentRate: 0.5,
    injuryRate: 0,
    housingFundRate: 7,
    pensionRateEnt: 16,
    medicalRateEnt: 9,
    unemploymentRateEnt: 0.5,
    injuryRateEnt: 0.4,
    housingFundRateEnt: 7,
    socialSecurityBaseUpper: 0,
    socialSecurityBaseLower: 0,
    overtimeWeekdayMultiplier: 1.5,
    overtimeWeekendMultiplier: 2.0,
    overtimeHolidayMultiplier: 3.0,
    overtimeCalcMode: 3,
    overtimeThresholdMin: 30,
    overtimeRoundingUnit: 1.0,
    lateThresholdMin: 30,
    earlyLeaveThresholdMin: 30
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑薪资模版'
  // 将后端的 0.08 费率转为前端显示的 8
  const rowFixed = { ...row }
  rowFixed.pensionRate = (row.pensionRate || 0) * 100
  rowFixed.medicalRate = (row.medicalRate || 0) * 100
  rowFixed.unemploymentRate = (row.unemploymentRate || 0) * 100
  rowFixed.injuryRate = (row.injuryRate || 0) * 100
  rowFixed.housingFundRate = (row.housingFundRate || 0) * 100
  rowFixed.pensionRateEnt = (row.pensionRateEnt || 0) * 100
  rowFixed.medicalRateEnt = (row.medicalRateEnt || 0) * 100
  rowFixed.unemploymentRateEnt = (row.unemploymentRateEnt || 0) * 100
  rowFixed.injuryRateEnt = (row.injuryRateEnt || 0) * 100
  rowFixed.housingFundRateEnt = (row.housingFundRateEnt || 0) * 100
  rowFixed.commission = (row.commission || 0) * 100
  
  Object.assign(form, rowFixed)
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
        // 提交前将百分比费率转回小数
        const submitData = { ...form }
        submitData.pensionRate = form.pensionRate / 100
        submitData.medicalRate = form.medicalRate / 100
        submitData.unemploymentRate = form.unemploymentRate / 100
        submitData.injuryRate = form.injuryRate / 100
        submitData.housingFundRate = form.housingFundRate / 100
        submitData.pensionRateEnt = form.pensionRateEnt / 100
        submitData.medicalRateEnt = form.medicalRateEnt / 100
        submitData.unemploymentRateEnt = form.unemploymentRateEnt / 100
        submitData.injuryRateEnt = form.injuryRateEnt / 100
        submitData.housingFundRateEnt = form.housingFundRateEnt / 100
        submitData.commission = form.commission / 100

        console.log('提交薪资配置:', submitData)

        if (form.configId) {
          await updateSalaryConfig(form.configId, submitData)
          ElMessage.success('更新成功')
        } else {
          await createSalaryConfig(submitData)
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
