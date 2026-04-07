<template>
  <el-container class="employee-layout">
    <!-- 桌面端侧边栏 -->
    <el-aside width="240px" class="hidden-xs">
      <div class="sidebar-header">
        <el-icon :size="32" color="#fff"><DataAnalysis /></el-icon>
        <h3>人力服务公司</h3>
        <h3>薪资结算系统</h3>
        <p>员工版</p>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
        background-color="#1890ff"
        text-color="rgba(255,255,255,0.8)"
        active-text-color="#fff"
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

    <!-- 移动端抽屉菜单 -->
    <el-drawer
      v-model="drawer"
      direction="ltr"
      size="280px"
      :with-header="false"
      class="mobile-drawer"
    >
      <div class="sidebar-header mobile-header">
        <el-icon :size="40" color="#fff"><DataAnalysis /></el-icon>
        <h3>灵活用工平台</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        @select="drawer = false"
        background-color="#1890ff"
        text-color="rgba(255,255,255,0.8)"
        active-text-color="#fff"
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
    </el-drawer>
    
    <el-container class="main-container">
      <el-header height="64px">
        <div class="header-content">
          <div class="header-left">
            <el-button
              class="visible-xs menu-btn"
              @click="drawer = true"
              circle
            >
              <el-icon><Menu /></el-icon>
            </el-button>
            <h2 class="page-title">{{ pageTitle }}</h2>
          </div>
          <div class="header-right">
            <span class="user-greeting hidden-xs">欢迎，{{ userDisplayName }}</span>
            <el-dropdown trigger="click">
              <span class="el-dropdown-link">
                <el-avatar :size="32" class="user-avatar">{{ userDisplayName.charAt(0) }}</el-avatar>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item class="visible-xs" disabled>{{ userDisplayName }}</el-dropdown-item>
                  <el-dropdown-item @click="router.push('/employee/my-profile')">个人信息</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <div class="main-wrapper">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { 
  Search, Briefcase, Calendar, Clock, Coin, User, Menu, DataAnalysis 
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const drawer = ref(false)

const userDisplayName = computed(() => {
  return userStore.userInfo?.name || userStore.userInfo?.account || '员工'
})

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
    '/employee/my-profile': '个人信息'
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
  overflow: hidden;
}

.el-aside {
  background-color: #1890ff;
  display: flex;
  flex-direction: column;
}

.sidebar-menu {
  border-right: none;
  flex: 1;
}

.sidebar-header {
  padding: 32px 20px;
  text-align: center;
  color: #fff;
  background: linear-gradient(180deg, rgba(255,255,255,0.1) 0%, transparent 100%);
}

.sidebar-header h3 {
  margin: 12px 0 4px 0;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
}

.sidebar-header p {
  margin: 0;
  font-size: 13px;
  opacity: 0.7;
}

.mobile-header {
  padding: 40px 20px;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
  z-index: 10;
  padding: 0 24px;
}

.header-content {
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2f3d;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-greeting {
  font-size: 14px;
  color: #606266;
}

.user-avatar {
  background-color: #1890ff;
  cursor: pointer;
}

.el-main {
  background-color: #f7f8fa;
  padding: 0;
  overflow-y: auto;
}

.main-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

@media (max-width: 768px) {
  .main-wrapper {
    padding: 16px;
  }
  
  .el-header {
    padding: 0 16px;
  }
  
  .page-title {
    font-size: 16px;
  }
}

.menu-btn {
  border: none;
  background: #f0f2f5;
  color: #606266;
}

.mobile-drawer :deep(.el-drawer__body) {
  padding: 0;
  background-color: #1890ff;
}
</style>
