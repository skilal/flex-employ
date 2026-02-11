<template>
  <div class="punch-clock-container">
    <div class="glass-card">
      <div class="header">
        <h2>考勤打卡</h2>
        <div class="position-info" v-if="position">
          <div class="name">{{ position.positionName }}</div>
          <div class="location">
            <el-icon><Location /></el-icon>
            {{ position.workLocation }}
          </div>
        </div>
      </div>

      <div class="timer-section">
        <div class="current-date">{{ currentDate }}</div>
        <div class="current-time">{{ currentTime }}</div>
      </div>

      <div class="info-grid" v-if="position">
        <div class="info-item">
          <span class="label">应签到</span>
          <span class="value">{{ position.workStartTime || '未设置' }}</span>
        </div>
        <div class="info-item">
          <span class="label">应签退</span>
          <span class="value">{{ position.workEndTime || '未设置' }}</span>
        </div>
      </div>

      <div class="actions">
        <el-button 
          type="primary" 
          class="punch-btn check-in" 
          :loading="loading"
          @click="handlePunch('check-in')"
        >
          <div class="btn-content">
            <el-icon size="24"><Select /></el-icon>
            <span>点击签到</span>
          </div>
        </el-button>

        <el-button 
          type="success" 
          class="punch-btn check-out" 
          :loading="loading"
          @click="handlePunch('check-out')"
        >
          <div class="btn-content">
            <el-icon size="24"><CircleCheck /></el-icon>
            <span>点击签退</span>
          </div>
        </el-button>
      </div>

      <div class="footer-tip">
        💡 请确保您已到达工作现场
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Select, CircleCheck } from '@element-plus/icons-vue'
import { getPositionById } from '../../api/position'
import { qrPunch } from '../../api/attendance'

const route = useRoute()
const positionId = route.params.positionId
const position = ref(null)
const loading = ref(false)

// 实时时间相关
const currentTime = ref('')
const currentDate = ref('')
let timer = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
  currentDate.value = now.toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
}

const fetchPositionInfo = async () => {
  try {
    const res = await getPositionById(positionId)
    position.value = res.data
  } catch (error) {
    console.error('获取岗位信息失败:', error)
    ElMessage.error('无法获取岗位信息，请检查二维码是否有效')
  }
}

const handlePunch = async (type) => {
  const token = route.query.token
  if (!token) {
    ElMessage.error('无效的访问，请扫描二维码进入')
    return
  }

  loading.value = true
  try {
    const res = await qrPunch({
      positionId: positionId,
      punchType: type,
      qrToken: token // 传给后端进行安全性校验
    })
    ElMessage({
      message: res.data || (type === 'check-in' ? '签到成功' : '签退成功'),
      type: 'success',
      duration: 5000,
      showClose: true
    })
  } catch (error) {
    console.error('打卡操作失败:', error)
    ElMessage.error(error.response?.data?.message || '打卡失败，请检查二维码是否已过期')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchPositionInfo()
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.punch-clock-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  width: 100%;
  max-width: 400px;
  padding: 32px 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.header h2 {
  margin: 0;
  color: #2d3748;
  font-size: 24px;
}

.position-info {
  margin-top: 12px;
}

.position-info .name {
  font-size: 18px;
  font-weight: 600;
  color: #4a5568;
}

.position-info .location {
  font-size: 14px;
  color: #718096;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 4px;
}

.timer-section {
  margin: 40px 0;
}

.current-date {
  font-size: 14px;
  color: #718096;
  margin-bottom: 8px;
}

.current-time {
  font-size: 48px;
  font-weight: 700;
  color: #2d3748;
  font-family: 'Courier New', Courier, monospace;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 40px;
}

.info-item {
  background: #f7fafc;
  padding: 12px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
}

.info-item .label {
  font-size: 12px;
  color: #a0aec0;
  margin-bottom: 4px;
}

.info-item .value {
  font-size: 16px;
  font-weight: 600;
  color: #4a5568;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.punch-btn {
  height: 64px;
  border-radius: 16px;
  font-size: 18px;
  font-weight: 600;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.footer-tip {
  margin-top: 32px;
  font-size: 13px;
  color: #a0aec0;
}

/* 适配移动端点击效果 */
.punch-btn:active {
  transform: scale(0.98);
}
</style>
