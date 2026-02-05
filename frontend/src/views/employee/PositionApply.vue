<template>
  <div class="position-apply">
    <!-- 头部横幅或筛选器 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="岗位名称/地点" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">搜索职位</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 职位列表 -->
    <div v-loading="loading" class="position-list">
      <el-row :gutter="20">
        <el-col v-for="p in recruitingPositions" :key="p.positionId" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="repo-card" shadow="hover">
            <div class="salary-float">
              <span class="s-val">¥{{ p.baseRate }}</span>
              <span class="s-unit">/{{ p.billingMethod === 1 ? '时' : '天' }}</span>
            </div>
            <div class="card-header">
              <div class="p-title-box">
                <span class="p-name">{{ p.positionName }}</span>
                <el-tag size="small" :type="p.employmentType === '全日制用工' ? '' : 'success'">
                  {{ p.employmentType }}
                </el-tag>
              </div>
            </div>
            <div class="p-info">
              <p><el-icon><Location /></el-icon> {{ p.workLocation }}</p>
              <p><el-icon><Menu /></el-icon> {{ p.companyName }}</p>
              <p class="salary-remark">{{ p.salaryDesc || '面议' }}</p>
            </div>
            <div class="card-footer">
              <el-button type="primary" plain @click="handleViewDetail(p)">查看详情</el-button>
              <el-button type="primary" @click="handleApply(p)">立即申请</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="recruitingPositions.length === 0" description="暂无符合条件的招聘职位" />
    </div>

    <el-dialog v-model="detailVisible" title="职位详情" width="650px">
      <div v-if="selectedPosition" class="detail-container">
        <div class="job-title-row">
          <span class="job-name">{{ selectedPosition.positionName }}</span>
          <el-tag effect="dark">{{ selectedPosition.employmentType }}</el-tag>
        </div>
        
        <el-descriptions :column="2" border class="m-t-20">
          <el-descriptions-item label="招聘单位" :span="2">{{ selectedPosition.companyName }}</el-descriptions-item>
          <el-descriptions-item label="工作地点" :span="2">{{ selectedPosition.workLocation }}</el-descriptions-item>
          
          <el-descriptions-item label="薪资标准">
            <span class="salary-highlight">¥{{ selectedPosition.baseRate }} / {{ selectedPosition.billingMethod === 1 ? '小时' : '天' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="结算周期">
            <el-tag size="small" type="warning">{{ selectedPosition.payCycle }}</el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="工作时间">
            {{ selectedPosition.checkInTime }} ~ {{ selectedPosition.checkOutTime }}
          </el-descriptions-item>
          <el-descriptions-item label="每周工作日">
            {{ formatWorkingDays(selectedPosition.workingDays) }}
          </el-descriptions-item>
          
          <el-descriptions-item label="招聘人数">
            <span :class="selectedPosition.remainingPositions > 0 ? 'text-success' : 'text-danger'">
              剩余 {{ selectedPosition.remainingPositions }} / 总 {{ selectedPosition.totalPositions }}
            </span>
          </el-descriptions-item>

          <el-descriptions-item label="加班政策" :span="2">
            <template v-if="selectedPosition.hasOvertimePay === 1">
              工作日 {{ selectedPosition.overtimeWeekdayMultiplier }}倍 / 
              周末 {{ selectedPosition.overtimeWeekendMultiplier }}倍 / 
              节假日 {{ selectedPosition.overtimeHolidayMultiplier }}倍
            </template>
            <el-tag v-else type="info" size="small">无加班费</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="section-title">岗位职责</div>
        <p class="duty-text">{{ selectedPosition.dutyDesc }}</p>

        <div v-if="selectedPosition.specialNote" class="section-title">特别说明</div>
        <p v-if="selectedPosition.specialNote" class="note-text">{{ selectedPosition.specialNote }}</p>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleApply(selectedPosition)">确认申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Location, Menu } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const recruitingPositions = ref([])
const loading = ref(false)
const searchForm = reactive({ keyword: '' })
const detailVisible = ref(false)
const selectedPosition = ref(null)

const formatWorkingDays = (daysStr) => {
  if (!daysStr) return '未设置'
  const dayMap = { '1': '周一', '2': '周二', '3': '周三', '4': '周四', '5': '周五', '6': '周六', '7': '周日' }
  const days = daysStr.split(',').sort().map(d => dayMap[d])
  if (daysStr === '1,2,3,4,5') return '周一至周五'
  if (daysStr === '1,2,3,4,5,6,7') return '全周'
  return days.join(', ')
}

const loadPositions = async () => {
  loading.value = true
  try {
    // 调用获取招聘中岗位的接口
    const res = await request({
      url: '/positions/recruiting',
      method: 'get'
    })
    recruitingPositions.value = res.data || []
  } catch (error) {
    ElMessage.error('加载职位失败')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  // 简化的客户端过滤演示，实际应调用接口
  loadPositions()
}

const handleViewDetail = (p) => {
  selectedPosition.value = p
  detailVisible.value = true
}

const handleApply = (p) => {
  ElMessageBox.confirm(`确定要申请 ${p.positionName} 岗位吗？`, '提示', {
    type: 'success'
  }).then(async () => {
    try {
      await request({
        url: '/applications',
        method: 'post',
        data: { positionId: p.positionId }
      })
      ElMessage.success('申请提交成功，请等待管理员审核')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '申请失败')
    }
  })
}

onMounted(() => {
  loadPositions()
})
</script>

<style scoped>
.position-apply { padding: 20px; }
.filter-card { margin-bottom: 25px; }
.repo-card { margin-bottom: 20px; position: relative; }
.card-header { margin-bottom: 15px; }
.p-info p { margin: 8px 0; color: #606266; font-size: 14px; display: flex; align-items: center; gap: 5px; }
.salary-float { position: absolute; top: 15px; right: 15px; text-align: right; }
.s-val { color: #f56c6c; font-weight: bold; font-size: 15px; }
.s-unit { color: #909399; font-size: 11px; }
.salary-remark { color: #909399; font-size: 12px; margin-top: 4px !important; }
.p-title-box { display: flex; flex-direction: column; gap: 5px; align-items: flex-start; padding-right: 60px; }
.card-footer { margin-top: 20px; display: flex; justify-content: space-between; }
.detail-container { padding: 0 10px; }
.job-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.job-name { font-size: 20px; font-weight: bold; color: #303133; }
.salary-highlight { color: #f56c6c; font-weight: bold; font-size: 16px; }
.section-title { margin-top: 20px; margin-bottom: 10px; font-weight: bold; color: #303133; border-left: 4px solid #409eff; padding-left: 10px; }
.duty-text { white-space: pre-wrap; color: #606266; line-height: 1.6; font-size: 14px; background: #f8f9fb; padding: 12px; border-radius: 4px; }
.note-text { color: #e6a23c; font-size: 13px; line-height: 1.5; padding: 0 12px; }
.m-t-20 { margin-top: 20px; }
.text-success { color: #67c23a; font-weight: bold; }
.text-danger { color: #f56c6c; font-weight: bold; }
</style>
