<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>人力服务公司薪资结算平台<br>账号登录</h2>
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
        
        // 保存 token
        userStore.setToken(res.data.token)
        
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
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 450px;
}

.login-card :deep(.el-card__header) {
  text-align: center;
  background-color: #f5f7fa;
}

.login-card h2 {
  margin: 0;
  color: #303133;
}
</style>
