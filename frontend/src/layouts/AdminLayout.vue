<template>
  <el-container class="admin-layout">
    <!-- 桌面端侧边栏 -->
    <el-aside width="240px" class="hidden-xs">
      <div class="sidebar-header">
        <el-icon :size="32" color="#fff"><DataAnalysis /></el-icon>
        <h3>人力服务公司</h3>
        <h3>薪资结算系统</h3>
        <p>系统管理员</p>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
        background-color="#304156"
        text-color="rgba(255,255,255,0.7)"
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
          <span>薪资结算管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/salary-configs">
          <el-icon><Setting /></el-icon>
          <span>薪资配置管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/companies">
          <el-icon><OfficeBuilding /></el-icon>
          <span>合作公司管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 移动端抽屉菜单 -->
    <el-drawer
      v-model="drawer"
      direction="ltr"
      size="280px"
      :with-header="false"
      class="admin-drawer"
    >
      <div class="sidebar-header mobile-header">
        <el-icon :size="40" color="#fff"><DataAnalysis /></el-icon>
        <h3>管理后台</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        @select="drawer = false"
        background-color="#304156"
        text-color="rgba(255,255,255,0.7)"
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
          <span>薪资结算管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/salary-configs">
          <el-icon><Setting /></el-icon>
          <span>薪资配置管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/companies">
          <el-icon><OfficeBuilding /></el-icon>
          <span>合作公司管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
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
  Document, List, User, Calendar, Clock, Coin, 
  OfficeBuilding, Setting, UserFilled, Menu, DataAnalysis 
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const drawer = ref(false)

const userDisplayName = computed(() => {
  return userStore.userInfo?.name || userStore.userInfo?.account || '管理员'
})

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 页面标题
const pageTitle = computed(() => {
  const titleMap = {
    '/admin/positions': '岗位管理',
    '/admin/applications': '岗位申请管理',
    '/admin/workers': '在岗员工管理',
    '/admin/leaves': '请假管理',
    '/admin/attendance': '考勤记录管理',
    '/admin/salary': '薪资结算管理',
    '/admin/salary-configs': '薪资配置管理',
    '/admin/companies': '公司管理',
    '/admin/users': '用户管理'
  }
  return titleMap[route.path] || '管理后台'
})

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

.el-aside {
  background-color: #304156;
}

.sidebar-menu {
  border-right: none;
}

.sidebar-header {
  padding: 32px 20px;
  text-align: center;
  color: #fff;
  background: rgba(0,0,0,0.1);
}

.sidebar-header h3 {
  margin: 12px 0 4px 0;
  font-size: 20px;
  font-weight: 700;
}

.sidebar-header p {
  margin: 0;
  font-size: 13px;
  opacity: 0.6;
}

.mobile-header {
  padding: 40px 20px;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
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
  color: #303133;
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
  background-color: #409EFF;
  cursor: pointer;
}

.el-main {
  background-color: #f0f2f5;
  padding: 0;
  overflow-y: auto;
}

.main-wrapper {
  padding: 24px;
}

@media (max-width: 768px) {
  .main-wrapper {
    padding: 16px;
  }
}

.menu-btn {
  border: none;
  background: #f4f4f5;
  color: #606266;
}

.admin-drawer :deep(.el-drawer__body) {
  padding: 0;
  background-color: #304156;
}
</style>
