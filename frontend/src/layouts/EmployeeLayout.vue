<template>
  <el-container class="employee-layout">
    <el-aside width="200px">
      <div class="sidebar-header">
        <h3><h3>人力服务公司<br>薪资结算系统</h3></h3>
        <p>员工</p>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#1890ff"
        text-color="#fff"
        active-text-color="#ffd700"
      >
        <el-menu-item index="/employee/positions">
          <el-icon><Search /></el-icon>
          <span>岗位申请</span>
        </el-menu-item>
        <el-menu-item index="/employee/my-position">
          <el-icon><Briefcase /></el-icon>
          <span>个人岗位记录</span>
        </el-menu-item>
        <el-menu-item index="/employee/my-leave">
          <el-icon><Calendar /></el-icon>
          <span>个人请假申请</span>
        </el-menu-item>
        <el-menu-item index="/employee/my-attendance">
          <el-icon><Clock /></el-icon>
          <span>个人考勤记录</span>
        </el-menu-item>
        <el-menu-item index="/employee/my-salary">
          <el-icon><Coin /></el-icon>
          <span>个人薪资</span>
        </el-menu-item>
        <el-menu-item index="/employee/my-profile">
          <el-icon><User /></el-icon>
          <span>个人信息管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>{{ pageTitle }}</h2>
          <div class="header-right">
            <span>欢迎，{{ userStore.userInfo?.account || '员工' }}</span>
            <el-button @click="handleLogout" text>退出登录</el-button>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 页面标题
const pageTitle = computed(() => {
  const titleMap = {
    '/employee/positions': '岗位申请',
    '/employee/my-position': '个人岗位记录',
    '/employee/my-leave': '个人请假申请',
    '/employee/my-attendance': '个人考勤记录',
    '/employee/my-salary': '个人薪资',
    '/employee/my-profile': '个人信息管理'
  }
  return titleMap[route.path] || '员工中心'
})

// 退出登录
const handleLogout = () => {
  userStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/login')
}
</script>

<style scoped>
.employee-layout {
  height: 100vh;
}

.el-aside {
  background-color: #1890ff;
  overflow-x: hidden;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  color: #fff;
  border-bottom: 1px solid #40a9ff;
}

.sidebar-header h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.sidebar-header p {
  margin: 0;
  font-size: 14px;
  color: #e6f7ff;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
