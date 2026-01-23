<template>
  <div class="my-profile">
    <el-card>
      <template #header>
        <h3>个人信息</h3>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-form
            ref="basicFormRef"
            :model="basicForm"
            :rules="basicRules"
            label-width="120px"
            style="max-width: 600px;"
          >
            <el-form-item label="账号">
              <el-input v-model="basicForm.account" disabled />
            </el-form-item>

            <el-form-item label="角色">
              <el-input v-model="basicForm.role" disabled />
            </el-form-item>

            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="basicForm.gender">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker
                v-model="basicForm.birthDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="basicForm.phone" />
            </el-form-item>

            <el-form-item label="银行卡号" prop="bankCard">
              <el-input v-model="basicForm.bankCard" />
            </el-form-item>

            <el-form-item label="账号状态">
              <el-tag v-if="basicForm.accountStatus === 1" type="success">正常</el-tag>
              <el-tag v-else type="danger">禁用</el-tag>
            </el-form-item>

            <el-form-item label="注册时间">
              <el-input v-model="basicForm.createdAt" disabled />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleUpdateBasicInfo" :loading="basicLoading">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
            style="max-width: 600px;"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（至少6位）"
                show-password
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleUpdatePassword" :loading="passwordLoading">
                修改密码
              </el-button>
              <el-button @click="handleResetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCurrentUser } from '../../api/auth'
import { updateUserInfo, updatePassword } from '../../api/user'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const activeTab = ref('basic')

const basicFormRef = ref(null)
const basicLoading = ref(false)

const basicForm = reactive({
  userId: null,
  account: '',
  role: '',
  gender: '',
  birthDate: '',
  phone: '',
  bankCard: '',
  accountStatus: 1,
  createdAt: ''
})

const basicRules = {
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const passwordFormRef = ref(null)
const passwordLoading = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getCurrentUser()
    Object.assign(basicForm, res.data)
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  }
}

// 更新基本信息
const handleUpdateBasicInfo = async () => {
  await basicFormRef.value.validate(async (valid) => {
    if (valid) {
      basicLoading.value = true
      try {
        await updateUserInfo(basicForm.userId, {
          gender: basicForm.gender,
          birthDate: basicForm.birthDate,
          phone: basicForm.phone,
          bankCard: basicForm.bankCard
        })
        ElMessage.success('信息更新成功')
        loadUserInfo()
      } catch (error) {
        console.error('更新失败:', error)
        ElMessage.error(error.response?.data?.message || '更新失败')
      } finally {
        basicLoading.value = false
      }
    }
  })
}

// 修改密码
const handleUpdatePassword = async () => {
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await updatePassword(basicForm.userId, {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        // 清空表单
        handleResetPasswordForm()
        // 退出登录
        setTimeout(() => {
          userStore.logout()
          window.location.href = '/login'
        }, 1500)
      } catch (error) {
        console.error('修改密码失败:', error)
        ElMessage.error(error.response?.data?.message || '修改密码失败')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

// 重置密码表单
const handleResetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.my-profile {
  width: 100%;
}
</style>
