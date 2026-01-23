# 灵活用工平台前端项目

## 项目概述

本项目是为人力服务公司设计的灵活用工平台前端系统，使用 Vue 3 + Element Plus 构建，支持管理员和员工两种角色。

## 技术栈

- **前端框架**: Vue 3 (Composition API)
- **构建工具**: Vite 7
- **UI 组件库**: Element Plus
- **路由管理**: Vue Router 4
- **状态管理**: Pinia
- **HTTP 客户端**: Axios
- **语言**: JavaScript / 简体中文界面

## 项目结构

```
frontend/
├── public/              # 静态资源
├── src/
│   ├── api/            # API 接口封装
│   │   ├── auth.js           # 认证接口
│   │   ├── position.js       # 岗位管理接口
│   │   ├── application.js    # 申请管理接口  
│   │   ├── worker.js         # 在岗员工管理接口
│   │   ├── leave.js          # 请假管理接口
│   │   ├── attendance.js     # 考勤管理接口
│   │   ├── salary.js         # 薪资管理接口
│   │   └── user.js           # 用户管理接口
│   ├── assets/         # 资源文件
│   │   └── main.css          # 全局样式
│   ├── components/     # 公共组件（预留）
│   ├── layouts/        # 布局组件
│   │   ├── AdminLayout.vue   # 管理员布局
│   │   └── EmployeeLayout.vue # 员工布局
│   ├── router/         # 路由配置
│   │   └── index.js          # 路由规则和守卫
│   ├── stores/         # Pinia 状态管理
│   │   └── user.js           # 用户状态
│   ├── utils/          # 工具函数
│   │   └── request.js        # Axios 封装
│   ├── views/          # 页面组件
│   │   ├── admin/            # 管理员页面
│   │   │   ├── PositionManagement.vue      # 岗位管理
│   │   │   ├── ApplicationManagement.vue   # 岗位申请管理
│   │   │   ├── WorkerManagement.vue        # 在岗员工管理
│   │   │   ├── LeaveManagement.vue         # 请假管理
│   │   │   ├── AttendanceManagement.vue    # 考勤记录管理
│   │   │   └── SalaryManagement.vue        # 薪资管理
│   │   ├── employee/         # 员工页面
│   │   │   ├── PositionApply.vue           # 岗位申请
│   │   │   ├── MyPosition.vue              # 个人岗位记录
│   │   │   ├── MyLeave.vue                 # 个人请假申请
│   │   │   ├── MyAttendance.vue           # 个人考勤记录
│   │   │   ├── MySalary.vue                # 个人薪资
│   │   │   └── MyProfile.vue               # 个人信息管理
│   │   ├── Login.vue         # 登录页面
│   │   └── Register.vue      # 注册页面
│   ├── App.vue         # 根组件
│   └── main.js         # 入口文件
├── index.html
├── package.json
└── vite.config.js      # Vite 配置
```

## 功能模块

### 公共功能
- ✅ 用户登录（支持管理员和员工）
- ✅ 用户注册（可选择角色）
- ✅ 基于角色的路由守卫
- ✅ Token 认证机制

### 管理员功能
1. **岗位管理** - 完整的 CRUD 操作
   - 新增岗位（岗位名称、地点、薪资、工时等）
   - 编辑岗位信息
   - 删除岗位
   - 搜索和筛选

2. **岗位申请管理**
   - 查看所有申请记录
   - 审批申请（通过/拒绝）
   - 删除申请记录

3. **在岗员工管理** - 完整的 CRUD 操作
   - 添加在岗员工
   - 编辑员工信息（打卡时间、入/离职日期）
   - 删除员工记录

4. **请假管理**
   - 查看所有请假申请
   - 审批请假（通过/拒绝）
   - 删除请假记录

5. **考勤记录管理** - 完整的 CRUD 操作
   - 新增考勤记录
   - 编辑考勤（签到/签退时间、状态）
   - 删除考勤记录
   - 按日期筛选

6. **薪资管理** - 完整的 CRUD 操作
   - 新增薪资记录（包含详细收入和扣除项）
   - 编辑薪资信息
   - 删除薪资记录
   - 查看薪资明细

### 员工功能
1. **岗位申请**
   - 浏览招聘中岗位
   - 查看岗位详情
   - 提交岗位申请
   - 查看我的申请记录

2. **个人岗位记录**
   - 查看在岗记录
   - 查看排班信息

3. **个人请假申请**
   - 提交请假申请
   - 查看请假记录和审批状态

4. **个人考勤记录**
   - 查看考勤记录
   - 考勤统计（出勤、缺勤、迟到、请假天数）

5. **个人薪资**
   - 查看薪资记录
   - 查看详细薪资明细（收入/扣除项）

6. **个人信息管理**
   - 修改基本信息（性别、电话、银行卡等）
   - 修改密码

## 快速开始

### 安装依赖

```bash
cd frontend
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:5173/

### 构建生产版本

```bash
npm run build
```

## API 配置

前端通过 Vite 代理与后端通信，配置在 `vite.config.js`：

```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',  // 后端服务地址
    changeOrigin: true
  }
}
```

## 后端接口说明

前端需要后端提供以下 RESTful API 接口（详见实施计划）：

- `/api/auth/*` - 认证相关
- `/api/positions/*` - 岗位管理
- `/api/applications/*` - 申请管理
- `/api/workers/*` - 在岗员工管理
- `/api/leaves/*` - 请假管理
- `/api/attendances/*` - 考勤管理
- `/api/salaries/*` - 薪资管理
- `/api/users/*` - 用户管理

**重要提示**：
- 所有接口需要支持 CORS
- 需要实现 JWT Token 认证
- 接口响应格式应为 JSON
- 建议分页接口返回格式：`{ data: { records: [], total: 0 } }`

## 注意事项

### 开发环境
- Node.js 版本：建议 16+
- 前端服务器端口：5173
- 后端服务器端口：8080（可在 vite.config.js 修改）

### 后续开发
1. 需要开发配套的后端 Spring Boot API 接口
2. 需要配置 CORS 跨域
3. 需要实现 JWT Token 认证
4. 建议使用 MyBatis 或 JPA 实现数据访问层

### 已知限制
- 当前版本未实现文件上传功能（简历上传使用路径字符串）
- 未实现数据导出功能
- 未实现实时通知功能

## 开发者

- 完成日期：2026-01-23
- 技术栈：Vue 3 + Vite + Element Plus
- UI 语言：简体中文

---

**下一步工作**：开发后端 Spring Boot API 接口并进行前后端联调
