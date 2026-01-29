<template>
  <div class="position-apply">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="岗位名称">
          <el-input 
            v-model="searchForm.positionName" 
            placeholder="请输入岗位名称" 
            clearable
            @clear="handleSearch"
            style="width: 200px"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="工作地点">
          <el-input 
            v-model="searchForm.workLocation" 
            placeholder="请输入工作地点" 
            clearable
            @clear="handleSearch"
            style="width: 200px"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="用工类型">
          <el-select 
            v-model="searchForm.employmentType" 
            placeholder="用工类型" 
            clearable
            @change="handleSearch"
            @clear="handleSearch"
            style="width: 150px"
          >
            <el-option label="兼职" value="兼职" />
            <el-option label="全职" value="全职" />
            <el-option label="临时工" value="临时工" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="招聘中岗位" name="recruiting">
        <el-card>
          <el-table :data="tableData" border stripe v-loading="loading">
            <el-table-column prop="positionId" label="岗位ID" width="80" />
            <el-table-column prop="positionName" label="岗位名称" width="150" />
            <el-table-column prop="workLocation" label="工作地点" width="200" />
            <el-table-column prop="employmentType" label="用工类型" width="100" />
            <el-table-column prop="basicSalary" label="基本工资" width="100">
              <template #default="{ row }">¥{{ row.basicSalary }}</template>
            </el-table-column>
            <el-table-column prop="payCycle" label="薪资周期" width="100" />
            <el-table-column prop="dailyHours" label="每日工时" width="100" />
            <el-table-column prop="remainingPositions" label="剩余人数" width="100">
              <template #default="{ row }">
                <span :style="{ color: row.remainingPositions <= 0 ? 'red' : row.remainingPositions < 3 ? 'orange' : '' }">
                  {{ row.remainingPositions || 0 }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="dutyDesc" label="职责描述" show-overflow-tooltip />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button 
                  size="small" 
                  type="primary" 
                  @click="handleApply(row)"
                  :disabled="row.remainingPositions <= 0"
                >
                  {{ row.remainingPositions <= 0 ? '已招满' : '申请' }}
                </el-button>
                <el-button size="small" @click="handleViewDetail(row)">详情</el-button>
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
      </el-tab-pane>

      <el-tab-pane label="我的申请" name="myApplications">
        <el-card>
          <el-table :data="myApplications" border stripe v-loading="applicationLoading">
            <el-table-column prop="applicationId" label="申请ID" width="80" />
            <el-table-column prop="positionName" label="岗位名称" width="150" />
            <el-table-column prop="resumePdfPath" label="简历路径" width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="申请状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === '已申请'" type="warning">已申请</el-tag>
                <el-tag v-else-if="row.status === '已通过'" type="success">已通过</el-tag>
                <el-tag v-else type="danger">已拒绝</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applyTime" label="申请时间" width="180" />
            <el-table-column prop="approveTime" label="审批时间" width="180" />
            <el-table-column prop="applicationNote" label="申请说明" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 岗位详情对话框 -->
    <el-dialog v-model="detailVisible" title="岗位详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位名称">{{ currentPosition.positionName }}</el-descriptions-item>
        <el-descriptions-item label="工作地点">{{ currentPosition.workLocation }}</el-descriptions-item>
        <el-descriptions-item label="地区代码">{{ currentPosition.regionCode }}</el-descriptions-item>
        <el-descriptions-item label="用工类型">{{ currentPosition.employmentType }}</el-descriptions-item>
        <el-descriptions-item label="基本工资">¥{{ currentPosition.basicSalary }}</el-descriptions-item>
        <el-descriptions-item label="薪资周期">{{ currentPosition.payCycle }}</el-descriptions-item>
        <el-descriptions-item label="每日工时">{{ currentPosition.dailyHours }}小时</el-descriptions-item>
        <el-descriptions-item label="每周频次">{{ currentPosition.weeklyFreq }}天</el-descriptions-item>
        <el-descriptions-item label="工作开始时间">{{ currentPosition.workStartTime }}</el-descriptions-item>
        <el-descriptions-item label="工作结束时间">{{ currentPosition.workEndTime }}</el-descriptions-item>
        <el-descriptions-item label="职责描述" :span="2">{{ currentPosition.dutyDesc }}</el-descriptions-item>
        <el-descriptions-item label="薪资说明" :span="2">{{ currentPosition.salaryDesc }}</el-descriptions-item>
        <el-descriptions-item label="特殊说明" :span="2">{{ currentPosition.specialNote }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 申请对话框 -->
    <el-dialog v-model="applyVisible" title="申请岗位" width="500px">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px">
        <el-form-item label="岗位名称">
          <el-input v-model="currentPosition.positionName" disabled />
        </el-form-item>
        <el-form-item label="简历路径" prop="resumePdfPath">
          <el-input
            v-model="applyForm.resumePdfPath"
            placeholder="请输入简历PDF路径或上传简历"
          />
          <span style="font-size: 12px; color: #999;">提示:请输入简历文件路径</span>
        </el-form-item>
        <el-form-item label="申请说明" prop="applicationNote">
          <el-input
            v-model="applyForm.applicationNote"
            type="textarea"
            :rows="4"
            placeholder="请简要说明您为什么适合这个岗位，以及您的优势..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitApply" :loading="submitLoading">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getRecruitingPositions } from '../../api/position'
import { getMyApplications, createApplication, checkWorkerStatus, checkTimeConflict } from '../../api/application'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const searchForm = reactive({
  positionName: '',
  workLocation: '',
  employmentType: null
})

const activeTab = ref('recruiting')
const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const myApplications = ref([])
const applicationLoading = ref(false)

const detailVisible = ref(false)
const applyVisible = ref(false)
const currentPosition = ref({})
const applyFormRef = ref(null)
const submitLoading = ref(false)

const applyForm = reactive({
  resumePdfPath: '',
  applicationNote: ''
})

const applyRules = {
  resumePdfPath: [{ required: true, message: '请输入简历路径', trigger: 'blur' }]
}

// 加载招聘中岗位
const loadPositions = async () => {
  loading.value = true
  try {
    // 招聘中岗位不需要查询参数，后端已经筛选了status=1
    const res = await getRecruitingPositions()
    
    // 客户端分页和筛选
    let allData = res.data || []
    
    // 前端筛选
    if (searchForm.positionName) {
      allData = allData.filter(item => 
        item.positionName && item.positionName.includes(searchForm.positionName)
      )
    }
    if (searchForm.workLocation) {
      allData = allData.filter(item => 
        item.workLocation && item.workLocation.includes(searchForm.workLocation)
      )
    }
    if (searchForm.employmentType) {
      allData = allData.filter(item => item.employmentType === searchForm.employmentType)
    }
    
    total.value = allData.length
    
    // 客户端分页
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

// 加载我的申请
const loadMyApplications = async () => {
  applicationLoading.value = true
  try {
    const res = await getMyApplications()
    myApplications.value = res.data.records || res.data
  } catch (error) {
    console.error('加载申请记录失败:', error)
    ElMessage.error('加载申请记录失败')
  } finally {
    applicationLoading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadPositions()
}

const handleReset = () => {
  searchForm.positionName = ''
  searchForm.workLocation = ''
  searchForm.employmentType = null
  handleSearch()
}

const handleTabChange = (tab) => {
  if (tab === 'myApplications') {
    loadMyApplications()
  }
}

const handleViewDetail = (row) => {
  currentPosition.value = row
  detailVisible.value = true
}

const handleApply = async (row) => {
  // 先检查是否已在岗
  try {
    const checkRes = await checkWorkerStatus(row.positionId)
    if (checkRes.data.isOnDuty) {
      ElMessage.warning(checkRes.data.message)
      return
    }
    
    // 检查时间冲突
    const conflictRes = await checkTimeConflict(row.positionId)
    if (conflictRes.data.hasConflict) {
      ElMessage.error(conflictRes.data.message)
      return
    }
  } catch (error) {
    console.error('检查申请状态失败:', error)
  }
  
  currentPosition.value = row
  applyForm.resumePdfPath = ''
  applyForm.applicationNote = ''
  applyVisible.value = true
}

const handleSubmitApply = async () => {
  await applyFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await createApplication({
          positionId: currentPosition.value.positionId,
          resumePdfPath: applyForm.resumePdfPath,
          applicationNote: applyForm.applicationNote
        })
        ElMessage.success('申请提交成功')
        applyVisible.value = false
        // 刷新我的申请列表
        if (activeTab.value === 'myApplications') {
          loadMyApplications()
        }
      } catch (error) {
        console.error('提交申请失败:', error)
        ElMessage.error(error.response?.data?.message || '提交申请失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadPositions()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadPositions()
}

onMounted(() => {
  loadPositions()
})
</script>

<style scoped>
.position-apply {
  width: 100%;
}

.search-card {
  margin-bottom: 20px;
}
</style>
