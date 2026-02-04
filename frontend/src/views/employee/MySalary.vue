<template>
  <div class="my-salary">
    <!-- 薪资列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>我的薪资单</h3>
          <span class="tip">（显示所有已结算的薪资记录）</span>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column label="计薪周期" width="200">
          <template #default="{ row }">
            {{ row.cycleStart }} 至 {{ row.cycleEnd }}
          </template>
        </el-table-column>
        <el-table-column label="实发金额" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥{{ row.netPay }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.paymentStatus)">{{ row.paymentStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deadlineDate" label="预计最晚发放" width="120" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleViewDetail(row)">查看电子工资条</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="tableData.length === 0 && !loading" description="暂无结算记录" />
    </el-card>

    <!-- 电子工资条对话框 -->
    <el-dialog v-model="detailVisible" title="电子工资条" width="90%">
      <div v-if="currentRow" class="salary-bill">
        <div class="bill-header">
          <h2>{{ currentRow.cycleEnd.substring(0, 7) }} 薪资明细单</h2>
          <p>核算周期：{{ currentRow.cycleStart }} ~ {{ currentRow.cycleEnd }}</p>
        </div>

        <el-divider>收入项</el-divider>
        <div class="money-grid">
          <div class="item"><span>周期标准工资</span><strong>¥{{ currentRow.periodBasePay }}</strong></div>
          <div class="item"><span>底薪 (出勤实发)</span><strong>¥{{ currentRow.basePay }}</strong></div>
          <div class="item"><span>绩效/奖金</span><strong>¥{{ currentRow.bonusPay }}</strong></div>
          <div class="item"><span>加班费</span><strong>¥{{ currentRow.overtimePay }}</strong></div>
          <div class="item"><span>各类补贴</span><strong>¥{{ currentRow.allowance }}</strong></div>
        </div>

        <el-divider>专项扣除 (五险一金/税)</el-divider>
        <div class="money-grid deduction">
          <div class="item"><span>养老保险</span><strong>-¥{{ currentRow.pensionDeduction }}</strong></div>
          <div class="item"><span>医疗保险</span><strong>-¥{{ currentRow.medicalDeduction }}</strong></div>
          <div class="item"><span>失业保险</span><strong>-¥{{ currentRow.unemploymentDeduction }}</strong></div>
          <div class="item"><span>工伤保险</span><strong>-¥{{ currentRow.injuryDeduction }}</strong></div>
          <div class="item"><span>公积金</span><strong>-¥{{ currentRow.pfDeduction }}</strong></div>
          <div class="item"><span>个人所得税</span><strong>-¥{{ currentRow.taxAmount }}</strong></div>
        </div>

        <el-divider>考勤及其他扣款</el-divider>
        <div class="money-grid deduction">
          <div class="item"><span>迟到扣款</span><strong>-¥{{ currentRow.lateDeduction }}</strong></div>
          <div class="item"><span>早退扣款</span><strong>-¥{{ currentRow.earlyLeaveDeduction }}</strong></div>
          <div class="item"><span>缺勤扣款</span><strong>-¥{{ currentRow.absentDeduction }}</strong></div>
          <div class="item"><span>请假扣款</span><strong>-¥{{ currentRow.leaveDeduction }}</strong></div>
        </div>

        <div class="bill-footer">
          <div class="total-line">
            应发合计：<span>¥{{ currentRow.grossPay }}</span>
          </div>
          <div class="total-line">
            扣款合计：<span>-¥{{ currentRow.totalDeduction }}</span>
          </div>
          <div class="net-pay">
            实发工薪：<span>¥{{ currentRow.netPay }}</span>
          </div>
          <div class="status-tip">
            结算状态：{{ currentRow.paymentStatus }} | 预计最晚发放：{{ currentRow.deadlineDate }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyPaySlips } from '../../api/salary'

const tableData = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const currentRow = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyPaySlips()
    tableData.value = res.data || []
  } catch (error) {
    ElMessage.error('加载薪资单失败')
  } finally {
    loading.value = false
  }
}

const handleViewDetail = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

const getStatusType = (status) => {
  if (status === '已发放') return 'success'
  if (status === '待结算') return 'warning'
  return 'danger'
}

onMounted(() => { loadData() })
</script>

<style scoped>
.bill-header { text-align: center; margin-bottom: 20px; }
.bill-header h2 { margin: 0; color: #333; }
.bill-header p { color: #999; font-size: 13px; }

.money-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 10px;
}
.money-grid .item {
  display: flex;
  justify-content: space-between;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 4px;
}
.money-grid.deduction .item { background: #fff5f5; }
.money-grid .item span { font-size: 13px; color: #666; }
.money-grid .item strong { color: #333; }
.deduction .item strong { color: #f56c6c; }

.bill-footer {
  margin-top: 20px;
  padding: 20px;
  background: #f0f7ff;
  border-radius: 8px;
}
.total-line { display: flex; justify-content: flex-end; font-size: 14px; margin-bottom: 5px; color: #666; }
.net-pay { display: flex; justify-content: flex-end; font-size: 22px; font-weight: bold; color: #409eff; margin-top: 10px; }
.status-tip { text-align: right; font-size: 12px; color: #999; margin-top: 10px; }
</style>
