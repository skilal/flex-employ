<template>
  <div class="my-salary">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="岗位名称">
          <el-input v-model="searchForm.positionName" placeholder="筛选岗位" clearable @change="handleSearch" />
        </el-form-item>
        <el-form-item label="计薪月份">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="monthrange"
            range-separator="至"
            start-placeholder="开始月份"
            end-placeholder="结束月份"
            value-format="YYYY-MM"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.paymentStatus" placeholder="全部状态" clearable @change="handleSearch">
            <el-option label="待支付" value="PENDING" />
            <el-option label="已支付" value="PAID" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 薪资列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>我的薪资单</h3>
          <span class="tip">（显示所有已结算的薪资记录）</span>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="positionName" label="所属岗位" show-overflow-tooltip />
        <el-table-column prop="cycleStart" label="周期开始" width="110" />
        <el-table-column prop="cycleEnd" label="周期结束" width="110" />
        <el-table-column label="应发金额" width="100">
          <template #default="{ row }">¥{{ row.grossPay }}</template>
        </el-table-column>
        <el-table-column label="扣除合计" width="100">
          <template #default="{ row }">¥{{ row.totalDeduction }}</template>
        </el-table-column>
        <el-table-column label="实发金额" width="100">
          <template #default="{ row }">
            <span style="color: #409eff; font-weight: bold">¥{{ row.netPay }}</span>
          </template>
        </el-table-column>
        <el-table-column label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.actualPaymentDate ? 'success' : 'warning'">
              {{ row.actualPaymentDate ? '已发放' : '待发放' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deadlineDate" label="预计最晚发放" width="120" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleViewDetail(row)">查看电子工资条</el-button>
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

    <!-- 电子工资条对话框 -->
    <el-dialog v-model="detailVisible" title="电子工资条" width="90%">
      <div v-if="currentRow" class="salary-bill">
        <div class="bill-header">
          <h2>{{ currentRow.cycleEnd.substring(0, 7) }} 薪资明细单</h2>
          <p>核算周期：{{ currentRow.cycleStart }} ~ {{ currentRow.cycleEnd }}</p>
        </div>

        <el-divider>收入项</el-divider>
        <div class="money-grid">
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
          <div class="item"><span>旷工扣款</span><strong>-¥{{ currentRow.absentDeduction }}</strong></div>
          <div class="item"><span>缺勤扣款</span><strong>-¥{{ currentRow.absenceDeduction || 0 }}</strong></div>
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
            结算状态：{{ currentRow.actualPaymentDate ? '已于 ' + currentRow.actualPaymentDate + ' 发放' : '待发放' }} | 预计最晚发放：{{ currentRow.deadlineDate }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyPaySlips } from '../../api/salary'

const tableData = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const currentRow = ref(null)

const searchForm = reactive({
  positionName: '',
  paymentStatus: null,
  dateRange: []
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const allData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      positionName: searchForm.positionName || undefined,
      paymentStatus: searchForm.paymentStatus || undefined
    }
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0] + "-01"
      const [year, month] = searchForm.dateRange[1].split('-').map(Number)
      const lastDay = new Date(year, month, 0).getDate()
      params.endDate = `${searchForm.dateRange[1]}-${lastDay}`
    }
    
    const res = await getMyPaySlips(params)
    const rawData = res.data || []
    allData.value = rawData
    total.value = rawData.length
    
    // 客户端分页切片
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    tableData.value = rawData.slice(start, end)
  } catch (error) {
    console.error('加载记录失败:', error)
    ElMessage.error('加载薪资单失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  searchForm.positionName = ''
  searchForm.paymentStatus = null
  searchForm.dateRange = []
  handleSearch()
}

const handleViewDetail = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

onMounted(() => { loadData() })
</script>

<style scoped>
.my-salary { width: 100%; box-sizing: border-box; }
.search-card { margin-bottom: 20px; }
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
