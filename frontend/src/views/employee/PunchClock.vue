<template>
  <div class="punch-clock-wrapper">
    <!-- 动态背景光点 -->
    <div class="bg-blur-1"></div>
    <div class="bg-blur-2"></div>
    
    <div class="glass-card main-card">
      <div class="card-header">
        <h2 class="title text-glow">现场核销打卡</h2>
        <div class="status-badge" v-if="position">
          <el-icon><Location /></el-icon>
          <span>{{ position.workLocation }}</span>
        </div>
      </div>

      <div class="position-info-box" v-if="position">
        <div class="p-name">{{ position.positionName }}</div>
        <div class="enterprise-name">{{ position.companyName || '关联机构' }}</div>
      </div>

      <div class="timer-display">
        <div class="date-text">{{ currentDate }}</div>
        <div class="time-text glow-text">{{ currentTime }}</div>
      </div>

      <div class="attendance-window" v-if="position">
        <div class="win-item border-right">
          <span class="lab">签到窗口</span>
          <span class="val">{{ position.workStartTime || '--:--' }}</span>
        </div>
        <div class="win-item">
          <span class="lab">签退窗口</span>
          <span class="val">{{ position.workEndTime || '--:--' }}</span>
        </div>
      </div>

      <div class="action-buttons">
        <el-button 
          type="primary" 
          class="punch-btn in-btn" 
          :loading="loading"
          @click="handlePunch('check-in')"
        >
          <div class="btn-inner">
            <el-icon :size="20"><Select /></el-icon>
            <span>准时签到</span>
          </div>
        </el-button>

        <el-button 
          type="success" 
          class="punch-btn out-btn" 
          :loading="loading"
          @click="handlePunch('check-out')"
        >
          <div class="btn-inner">
            <el-icon :size="20"><CircleCheck /></el-icon>
            <span>确认下班</span>
          </div>
        </el-button>
      </div>

      <div class="bottom-tips">
        <el-icon><InfoFilled /></el-icon>
        <span>打卡前请确认定位权限已开启</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Select, CircleCheck, InfoFilled } from '@element-plus/icons-vue'
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
.punch-clock-wrapper {
  min-height: 100vh;
  background: #0f172a;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
}

/* 动态背景点 */
.bg-blur-1 {
  position: absolute;
  top: -10%;
  left: -10%;
  width: 50%;
  height: 50%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.2) 0%, transparent 70%);
  filter: blur(60px);
  animation: pulse 8s infinite alternate;
}
.bg-blur-2 {
  position: absolute;
  bottom: -10%;
  right: -10%;
  width: 60%;
  height: 60%;
  background: radial-gradient(circle, rgba(168, 85, 247, 0.15) 0%, transparent 70%);
  filter: blur(60px);
  animation: pulse 12s infinite alternate-reverse;
}

.main-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 32px;
  padding: 40px 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  position: relative;
  z-index: 1;
}

.title {
  color: #fff;
  font-size: 26px;
  font-weight: 800;
  margin: 0 0 12px 0;
  letter-spacing: 1px;
}

.text-glow {
  text-shadow: 0 0 15px rgba(255, 255, 255, 0.3);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  color: #94a3b8;
  font-size: 13px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.position-info-box {
  margin: 30px 0;
}

.position-info-box .p-name {
  color: #f1f5f9;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 6px;
}

.enterprise-name {
  color: #64748b;
  font-size: 14px;
}

.timer-display {
  margin: 40px 0;
  padding: 24px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.03);
}

.date-text {
  color: #94a3b8;
  font-size: 14px;
  margin-bottom: 12px;
}

.time-text {
  color: #fff;
  font-size: 56px;
  font-weight: 800;
  font-family: 'JetBrains Mono', 'Courier New', monospace;
  letter-spacing: -2px;
}

.glow-text {
  text-shadow: 0 0 20px rgba(64, 158, 255, 0.4);
}

.attendance-window {
  display: flex;
  justify-content: space-around;
  margin-bottom: 40px;
  background: rgba(255, 255, 255, 0.04);
  padding: 16px;
  border-radius: 16px;
}

.win-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.border-right {
  border-right: 1px solid rgba(255, 255, 255, 0.05);
}

.win-item .lab {
  font-size: 11px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.win-item .val {
  font-size: 18px;
  color: #f1f5f9;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.punch-btn {
  height: 64px;
  border-radius: 18px;
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.in-btn {
  background: linear-gradient(135deg, #6366f1 0%, #4338ca 100%);
  box-shadow: 0 10px 20px -5px rgba(99, 102, 241, 0.4);
}

.out-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  box-shadow: 0 10px 20px -5px rgba(16, 185, 129, 0.4);
}

.btn-inner {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 17px;
  font-weight: 700;
}

.bottom-tips {
  margin-top: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #475569;
  font-size: 12px;
}

@keyframes pulse {
  from { transform: scale(1); opacity: 0.5; }
  to { transform: scale(1.1); opacity: 0.8; }
}

.punch-btn:active {
  transform: scale(0.96);
  opacity: 0.9;
}

@media (max-width: 480px) {
  .time-text {
    font-size: 48px;
  }
}
</style>
