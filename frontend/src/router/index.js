import { createRouter, createWebHistory } from 'vue-router'
import { usePermissionStore } from '../stores/permission'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { title: '登录' } },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', component: () => import('../views/Home.vue'), meta: { title: '首页' } },
      { path: 'profile', component: () => import('../views/Profile.vue'), meta: { title: '个人信息' } },
      // 系统管理
      { path: 'system/user', component: () => import('../views/system/UserManage.vue'), meta: { title: '用户管理', permission: 'system:user' } },
      { path: 'system/role', component: () => import('../views/system/RoleManage.vue'), meta: { title: '角色管理', permission: 'system:role' } },
      { path: 'system/permission', component: () => import('../views/system/PermissionManage.vue'), meta: { title: '权限管理', permission: 'system:permission' } },
      { path: 'system/log', component: () => import('../views/system/LogManage.vue'), meta: { title: '日志管理', permission: 'system:log' } },
      // 基础资源
      { path: 'resource/consumable', component: () => import('../views/resource/ConsumableManage.vue'), meta: { title: '耗材管理', permission: 'resource:consumable' } },
      { path: 'resource/instrument', component: () => import('../views/resource/InstrumentManage.vue'), meta: { title: '仪器管理', permission: 'resource:instrument' } },
      { path: 'resource/laboratory', component: () => import('../views/resource/LaboratoryManage.vue'), meta: { title: '实验室管理', permission: 'resource:laboratory' } },
      // 预约管理
      { path: 'reservation/list', component: () => import('../views/reservation/ReservationList.vue'), meta: { title: '预约列表', permission: 'reservation:list' } },
      { path: 'reservation/rules', component: () => import('../views/reservation/ReservationRules.vue'), meta: { title: '预约规则', permission: 'reservation:rules' } },
      // 实验管理
      { path: 'experiment/log', component: () => import('../views/experiment/ExperimentLog.vue'), meta: { title: '实验记录', permission: 'experiment:log' } },
      { path: 'experiment/material', component: () => import('../views/experiment/MaterialManage.vue'), meta: { title: '耗材领用', permission: 'experiment:material' } },
      { path: 'experiment/inventory', component: () => import('../views/experiment/InventoryCheck.vue'), meta: { title: '库存盘点', permission: 'experiment:inventory' } },
      // 设备运维
      { path: 'equipment/repair', component: () => import('../views/equipment/RepairManage.vue'), meta: { title: '故障报修', permission: 'equipment:repair' } },
      { path: 'equipment/maintenance', component: () => import('../views/equipment/MaintenanceManage.vue'), meta: { title: '保养管理', permission: 'equipment:maintenance' } },
      { path: 'equipment/alert', component: () => import('../views/equipment/AlertManage.vue'), meta: { title: '安全预警', permission: 'equipment:alert' } },
      { path: 'equipment/hazardous', component: () => import('../views/equipment/HazardousManage.vue'), meta: { title: '危化品管控', permission: 'equipment:hazardous' } },
      { path: 'equipment/return', component: () => import('../views/equipment/ReturnManage.vue'), meta: { title: '耗材归还', permission: 'equipment:return' } },
      // 数据统计
      { path: 'statistics/overview', component: () => import('../views/statistics/StatisticsOverview.vue'), meta: { title: '数据概览', permission: 'statistics:overview' } },
      { path: 'statistics/trend', component: () => import('../views/statistics/StatisticsTrend.vue'), meta: { title: '趋势分析', permission: 'statistics:trend' } },
      { path: 'statistics/my', component: () => import('../views/statistics/MyStatistics.vue'), meta: { title: '我的数据统计', permission: 'statistics:my' } },
      // 公告管理
      { path: 'announcement/manage', component: () => import('../views/AnnouncementManage.vue'), meta: { title: '公告管理', permission: 'announcement:manage' } },
      { path: 'announcement/category', component: () => import('../views/announcement/AnnouncementCategory.vue'), meta: { title: '公告分类', permission: 'announcement:category' } },
      // AI助手
      { path: 'ai/assistant', component: () => import('../views/ai/Assistant.vue'), meta: { title: '智能问答', permission: 'ai:assistant' } },
      { path: 'ai/knowledge', component: () => import('../views/ai/KnowledgeManage.vue'), meta: { title: '知识库管理', permission: 'ai:knowledge' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：登录校验 + 权限校验
// 进入应用后用服务端权限刷新本地缓存，避免种子/角色变更后 localStorage 里的旧权限码导致误拦截或误请求
let permissionsSynced = false
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  // 未登录跳转到登录页
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }
  // 已登录访问登录页跳转到首页
  if (to.path === '/login' && token) {
    permissionsSynced = false
    next('/')
    return
  }
  // 每次会话首次进入受保护页面时，向服务端拉取最新权限（自愈旧缓存）
  if (token && !permissionsSynced) {
    try {
      await usePermissionStore().loadPermissions()
    } catch (e) { /* 拉取失败则退回本地缓存，不阻断导航 */ }
    permissionsSynced = true
  }
  // 权限校验（首页和个人信息不需要权限）
  if (to.meta.permission) {
    const permissions = usePermissionStore().permissions || []
    if (!permissions.includes(to.meta.permission)) {
      // 无权限时跳转到首页
      next('/home')
      return
    }
  }
  next()
})

export default router
