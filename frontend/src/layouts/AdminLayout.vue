<template>
  <el-container class="admin-layout">
    <el-aside width="200px">
      <div class="sidebar-header">
        <h3>灵活用工平台</h3>
        <p>管理员</p>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#fff"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin/positions">
          <el-icon><Document /></el-icon>
          <span>岗位管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/applications">
          <el-icon><List /></el-icon>
          <span>岗位申请管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/workers">
          <el-icon><User /></el-icon>
          <span>在岗员工管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/leaves">
          <el-icon><Calendar /></el-icon>
          <span>请假管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/attendance">
          <el-icon><Clock /></el-icon>
          <span>考勤记录管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/salary">
          <el-icon><Coin /></el-icon>
          <span>薪资管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/companies">
          <el-icon><OfficeBuilding /></el-icon>
          <span>合作公司管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>{{ pageTitle }}</h2>
          <div class="header-right">
            <span>欢迎，{{ userStore.userInfo?.account || '管理员' }}</span>
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
import { Document, List, User, Calendar, Clock, Coin, OfficeBuilding } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 页面标题
const pageTitle = computed(() => {
  const titleMap = {
    '/admin/positions': '岗位管理',
    '/admin/applications': '岗位申请管理',
    '/admin/workers': '在岗员工管理',
    '/admin/workers': '在岗员工管理',
    '/admin/attendances': '考勤记录管理',
    '/admin/salaries': '工资管理',
    '/admin/companies': '合作公司管理',
    '/admin/users': '用户管理'
  }
  return titleMap[route.path] || '管理后台'
})

// 退出登录
const handleLogout = () => {
  userStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
  overflow-x: hidden;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  color: #fff;
  border-bottom: 1px solid #4a5968;
}

.sidebar-header h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.sidebar-header p {
  margin: 0;
  font-size: 14px;
  color: #909399;
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
