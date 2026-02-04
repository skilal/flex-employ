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
            <div class="card-header">
              <span class="p-name">{{ p.positionName }}</span>
              <el-tag size="small" :type="p.employmentType === '全日制用工' ? '' : 'success'">
                {{ p.employmentType }}
              </el-tag>
            </div>
            <div class="p-info">
              <p><el-icon><Location /></el-icon> {{ p.workLocation }}</p>
              <p><el-icon><Menu /></el-icon> {{ p.companyName }}</p>
              <p class="salary-desc">{{ p.salaryDesc || '面议' }}</p>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="职位详情" width="600px">
      <div v-if="selectedPosition" class="detail-container">
        <h3>{{ selectedPosition.positionName }}</h3>
        <el-divider />
        <div class="detail-item">
          <strong>招聘单位：</strong> {{ selectedPosition.companyName }}
        </div>
        <div class="detail-item">
          <strong>工作地点：</strong> {{ selectedPosition.workLocation }}
        </div>
        <div class="detail-item">
          <strong>用工类型：</strong> {{ selectedPosition.employmentType }}
        </div>
        <div class="detail-item">
          <strong>工作时间：</strong> {{ selectedPosition.checkInTime }} ~ {{ selectedPosition.checkOutTime }}
        </div>
        <div class="detail-item">
          <strong>岗位职责：</strong>
          <p class="duty-text">{{ selectedPosition.dutyDesc }}</p>
        </div>
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
.repo-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.p-name { font-size: 18px; font-weight: bold; color: #303133; }
.p-info p { margin: 8px 0; color: #606266; font-size: 14px; display: flex; align-items: center; gap: 5px; }
.salary-desc { color: #f56c6c; font-weight: bold; font-size: 16px; margin-top: 10px !important; }
.card-footer { margin-top: 20px; display: flex; justify-content: space-between; }
.detail-item { margin-bottom: 15px; line-height: 1.6; }
.duty-text { white-space: pre-wrap; color: #666; margin-top: 5px; }
</style>
