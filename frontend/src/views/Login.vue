<template>
  <div class="auth-layout">
    <!-- 独立高亮的系统大标题 -->
    <div class="system-header">
      <div class="logo-placeholder">
        <el-icon :size="48" color="#fff"><DataAnalysis /></el-icon>
      </div>
      <h1 class="system-title">人力服务公司薪资结算系统</h1>
      <p class="system-subtitle">Flex Employ Platform · 高效协同 智能结算</p>
    </div>

    <el-card class="auth-card">
      <template #header>
        <div class="card-header">
          <h2>欢迎登录</h2>
          <p class="welcome-text">请输入您的账号与密码</p>
        </div>
      </template>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="80px"
      >
        <el-form-item label="账号" prop="account">
          <el-input
            v-model="loginForm.account"
            placeholder="请输入账号"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
        
        <el-form-item>
          <el-link type="primary" @click="goToRegister">还没有账号？立即注册</el-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataAnalysis } from '@element-plus/icons-vue'
import { login, getCurrentUser } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  account: '',
  password: ''
})

const loginRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

// 登录
const handleLogin = async () => {
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        console.log('发起登录请求:', loginForm)
        const res = await login(loginForm)
        console.log('登录响应:', res)
        
        // 保存 token（后端返回字段名为 accessToken）
        userStore.setToken(res.data.accessToken)
        // 保存 refreshToken
        userStore.setRefreshToken(res.data.refreshToken)
        
        // 获取用户信息
        const userRes = await getCurrentUser()
        console.log('用户信息:', userRes)
        
        // 注意：后端返回的数据结构是 { code, message, data }
        // data 里面才是真正的用户信息
        const userData = userRes.data
        console.log('用户数据:', userData)
        console.log('用户角色:', userData.role)
        
        userStore.setUserInfo(userData)
        
        console.log('Store 中的角色:', userStore.role)
        console.log('是否管理员:', userStore.isAdmin())
        
        ElMessage.success('登录成功')
        
        // 根据角色跳转到不同页面
        if (userStore.isAdmin()) {
          console.log('跳转到管理员页面')
          await router.push('/admin')
        } else {
          console.log('跳转到员工页面')
          await router.push('/employee')
        }
      } catch (error) {
        console.error('登录失败完整错误:', error)
        console.error('错误响应:', error.response)
        console.error('错误数据:', error.response?.data)
        
        let errorMsg = '登录失败'
        if (error.response) {
          // 服务器返回了错误响应
          errorMsg = error.response.data?.message || error.response.statusText || errorMsg
        } else if (error.request) {
          // 请求已发出但没有收到响应
          errorMsg = '网络错误，请检查后端服务是否启动'
        } else {
          // 发送请求时出错
          errorMsg = error.message || errorMsg
        }
        
        ElMessage.error(errorMsg)
      } finally {
        loading.value = false
      }
    }
  })
}

// 跳转注册
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  /* 现代动态炫光毛玻璃背景 */
  background: 
    radial-gradient(circle at 15% 50%, rgba(118, 75, 162, 0.4), transparent 50%),
    radial-gradient(circle at 85% 30%, rgba(102, 126, 234, 0.4), transparent 50%),
    #0f172a; /* 深黛蓝基底 */
  padding: 40px 20px;
  box-sizing: border-box;
}

.system-header {
  text-align: center;
  margin-bottom: 40px;
  animation: fadeInDown 0.8s ease-out;
}

.logo-placeholder {
  margin-bottom: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 8px 32px rgba(118, 75, 162, 0.3);
}

.system-title {
  font-size: 36px;
  font-weight: 800;
  color: #ffffff;
  margin: 0 0 12px 0;
  letter-spacing: 2px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.system-subtitle {
  font-size: 16px;
  color: #94a3b8;
  margin: 0;
  letter-spacing: 1px;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  animation: fadeInUp 0.8s ease-out;
}

.auth-card :deep(.el-card__header) {
  padding: 30px 30px 10px;
  border-bottom: none;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #1e293b;
  font-weight: 600;
}

.welcome-text {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.auth-card :deep(.el-card__body) {
  padding: 20px 30px 30px;
}

.el-button {
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
  margin-top: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(118, 75, 162, 0.3);
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
