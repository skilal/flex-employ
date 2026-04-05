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
          <h2>账号注册</h2>
          <p class="welcome-text">加入系统，探索更多</p>
        </div>
      </template>
      
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="100px"
      >
        <el-form-item label="账号" prop="account">
          <el-input
            v-model="registerForm.account"
            placeholder="请输入账号"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="registerForm.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="出生日期" prop="birthDate">
          <el-date-picker
            v-model="registerForm.birthDate"
            type="date"
            placeholder="请选择出生日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="银行卡号" prop="bankCard">
          <el-input
            v-model="registerForm.bankCard"
            placeholder="请输入银行卡号（可选）"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="registerForm.role">
            <el-radio label="员工">员工</el-radio>
            <el-radio label="管理员">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
        
        <el-form-item>
          <el-link type="primary" @click="goToLogin">已有账号？立即登录</el-link>
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
import { register } from '../api/auth'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  account: '',
  password: '',
  confirmPassword: '',
  phone: '',
  gender: '男',
  birthDate: '',
  bankCard: '',
  role: '员工'
})

// 验证手机号
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[1-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在3-20位之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 注册
const handleRegister = async () => {
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 移除确认密码字段，并转换性别和角色为英文代码
        const { confirmPassword, gender, role, ...otherData } = registerForm
        
        // 转换性别：男 -> M, 女 -> F
        const genderCode = gender === '男' ? 'M' : 'F'
        
        // 转换角色：员工 -> EMPLOYEE, 管理员 -> ADMIN
        const roleCode = role === '管理员' ? 'ADMIN' : 'EMPLOYEE'
        
        const data = {
          ...otherData,
          gender: genderCode,
          role: roleCode
        }
        
        console.log('提交注册数据:', data)
        await register(data)
        
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        console.error('注册失败:', error)
        ElMessage.error(error.response?.data?.message || '注册失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 跳转登录
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: 
    radial-gradient(circle at 15% 50%, rgba(118, 75, 162, 0.4), transparent 50%),
    radial-gradient(circle at 85% 30%, rgba(102, 126, 234, 0.4), transparent 50%),
    #0f172a;
  padding: 40px 20px;
  box-sizing: border-box;
}

.system-header {
  text-align: center;
  margin-bottom: 30px;
  animation: fadeInDown 0.8s ease-out;
}

.logo-placeholder {
  margin-bottom: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 8px 32px rgba(118, 75, 162, 0.3);
}

.system-title {
  font-size: 32px;
  font-weight: 800;
  color: #ffffff;
  margin: 0 0 8px 0;
  letter-spacing: 2px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.system-subtitle {
  font-size: 15px;
  color: #94a3b8;
  margin: 0;
  letter-spacing: 1px;
}

.auth-card {
  width: 100%;
  max-width: 480px; /* 注册表单比较多，卡片稍微放宽 */
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  animation: fadeInUp 0.8s ease-out;
}

.auth-card :deep(.el-card__header) {
  padding: 24px 30px 10px;
  border-bottom: none;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 6px 0;
  font-size: 22px;
  color: #1e293b;
  font-weight: 600;
}

.welcome-text {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.auth-card :deep(.el-card__body) {
  padding: 10px 30px 30px;
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
