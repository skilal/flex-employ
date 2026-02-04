import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

// 路由配置
const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue'),
        meta: { requiresAuth: false }
    },
    // 管理员路由
    {
        path: '/admin',
        component: () => import('../layouts/AdminLayout.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' },
        children: [
            {
                path: '',
                redirect: '/admin/positions'
            },
            {
                path: 'positions',
                name: 'AdminPositions',
                component: () => import('../views/admin/PositionManagement.vue')
            },
            {
                path: 'applications',
                name: 'AdminApplications',
                component: () => import('../views/admin/ApplicationManagement.vue')
            },
            {
                path: 'workers',
                name: 'AdminWorkers',
                component: () => import('../views/admin/WorkerManagement.vue')
            },
            {
                path: 'leaves',
                name: 'AdminLeaves',
                component: () => import('../views/admin/LeaveManagement.vue')
            },
            {
                path: 'attendance',
                name: 'AdminAttendance',
                component: () => import('../views/admin/AttendanceManagement.vue')
            },
            {
                path: 'salary',
                name: 'AdminSalary',
                component: () => import('../views/admin/SalaryManagement.vue')
            },
            {
                path: 'salary-configs',
                name: 'AdminSalaryConfigs',
                component: () => import('../views/admin/SalaryConfigManagement.vue')
            },
            {
                path: 'companies',
                name: 'AdminCompanies',
                component: () => import('../views/admin/CompanyManagement.vue')
            },
            {
                path: 'users',
                name: 'AdminUsers',
                component: () => import('../views/admin/UserManagement.vue')
            }
        ]
    },
    // 员工路由
    {
        path: '/employee',
        component: () => import('../layouts/EmployeeLayout.vue'),
        meta: { requiresAuth: true, role: 'EMPLOYEE' },
        children: [
            {
                path: '',
                redirect: '/employee/positions'
            },
            {
                path: 'positions',
                name: 'EmployeePositions',
                component: () => import('../views/employee/PositionApply.vue')
            },
            {
                path: 'my-position',
                name: 'MyPosition',
                component: () => import('../views/employee/MyPosition.vue')
            },
            {
                path: 'my-leave',
                name: 'MyLeave',
                component: () => import('../views/employee/MyLeave.vue')
            },
            {
                path: 'my-attendance',
                name: 'MyAttendance',
                component: () => import('../views/employee/MyAttendance.vue')
            },
            {
                path: 'my-salary',
                name: 'MySalary',
                component: () => import('../views/employee/MySalary.vue')
            },
            {
                path: 'my-profile',
                name: 'MyProfile',
                component: () => import('../views/employee/MyProfile.vue')
            },
            {
                path: '/punch/:positionId',
                name: 'PunchClock',
                component: () => import('../views/employee/PunchClock.vue')
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()

    console.log('路由守卫 - 前往:', to.path)
    console.log('路由守卫 - 用户角色:', userStore.role)
    console.log('路由守卫 - 是否登录:', userStore.isLoggedIn())

    // 检查是否需要登录
    if (to.meta.requiresAuth) {
        if (!userStore.isLoggedIn()) {
            // 未登录，跳转到登录页
            console.log('未登录，跳转到登录页')
            next('/login')
            return
        }

        // 检查角色权限
        if (to.meta.role && userStore.role !== to.meta.role) {
            console.log('角色不匹配，需要:', to.meta.role, ', 实际:', userStore.role)
            // 角色不匹配，跳转到对应角色的首页
            if (userStore.isAdmin()) {
                next('/admin')
            } else {
                next('/employee')
            }
            return
        }
    }

    // 如果已登录，访问登录页，则跳转到对应角色首页
    if (to.path === '/login' && userStore.isLoggedIn()) {
        console.log('已登录，从登录页跳转')
        if (userStore.isAdmin()) {
            next('/admin')
        } else {
            next('/employee')
        }
        return
    }

    console.log('路由守卫 - 放行')
    next()
})

export default router
