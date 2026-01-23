<template>
  <div class="my-salary">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="薪资周期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="monthrange"
            range-separator="至"
            start-placeholder="开始月份"
            end-placeholder="结束月份"
            value-format="YYYY-MM"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 薪资列表 -->
    <el-card>
      <template #header>
        <h3>薪资记录</h3>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="payRecordId" label="薪资ID" width="100" />
        <el-table-column label="薪资周期" width="200">
          <template #default="{ row }">
            {{ row.cycleStart }} 至 {{ row.cycleEnd }}
          </template>
        </el-table-column>
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
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ row.netPay }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.paymentStatus === 'PENDING'" type="warning">待支付</el-tag>
            <el-tag v-else-if="row.paymentStatus === 'PAID'" type="success">已支付</el-tag>
            <el-tag v-else type="danger">支付失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentDate" label="支付日期" width="120" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="tableData.length === 0 && !loading" description="暂无薪资记录" />
    </el-card>

    <!-- 薪资详情对话框 -->
    <el-dialog v-model="detailVisible" title="薪资详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="薪资ID">{{ currentRow.payRecordId }}</el-descriptions-item>
        <el-descriptions-item label="支付日期">{{ currentRow.paymentDate }}</el-descriptions-item>
        <el-descriptions-item label="周期开始">{{ currentRow.cycleStart }}</el-descriptions-item>
        <el-descriptions-item label="周期结束">{{ currentRow.cycleEnd }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">收入明细</el-divider>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="基本工资">¥{{ currentRow.basePay }}</el-descriptions-item>
        <el-descriptions-item label="绩效奖金">¥{{ currentRow.performanceBonus }}</el-descriptions-item>
        <el-descriptions-item label="加班费">¥{{ currentRow.overtimePay }}</el-descriptions-item>
        <el-descriptions-item label="补贴">¥{{ currentRow.allowance }}</el-descriptions-item>
        <el-descriptions-item label="实际工时">{{ currentRow.actualWorkTime }}小时</el-descriptions-item>
        <el-descriptions-item label="应发工资">
          <span style="color: #67c23a; font-weight: bold;">¥{{ currentRow.grossPay }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">扣除明细</el-divider>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="社保合计">¥{{ currentRow.insuranceTotal }}</el-descriptions-item>
        <el-descriptions-item label="公积金">¥{{ currentRow.pfContribution }}</el-descriptions-item>
        <el-descriptions-item label="税款">¥{{ currentRow.taxAmount }}</el-descriptions-item>
        <el-descriptions-item label="迟到扣款">¥{{ currentRow.lateDeduction }}</el-descriptions-item>
        <el-descriptions-item label="自定义增减">¥{{ currentRow.customAddDeduct }}</el-descriptions-item>
        <el-descriptions-item label="扣除合计">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ currentRow.totalDeduction }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">实发工资</el-divider>
      <div style="text-align: center; padding: 20px 0;">
        <h2 style="color: #409eff; margin: 0;">¥{{ currentRow.netPay }}</h2>
        <p style="color: #909399; margin-top: 10px;">
          支付状态：
          <el-tag v-if="currentRow.paymentStatus === 'PENDING'" type="warning">待支付</el-tag>
          <el-tag v-else-if="currentRow.paymentStatus === 'PAID'" type="success">已支付</el-tag>
          <el-tag v-else type="danger">支付失败</el-tag>
        </p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMySalaries } from '../../api/salary'

const searchForm = reactive({
  dateRange: []
})

const tableData = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const currentRow = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startMonth = searchForm.dateRange[0]
      params.endMonth = searchForm.dateRange[1]
    }
    
    const res = await getMySalaries(params)
    tableData.value = res.data.records || res.data || []
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  searchForm.dateRange = []
  loadData()
}

const handleViewDetail = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-salary {
  width: 100%;
}

.search-card {
  margin-bottom: 20px;
}
</style>
