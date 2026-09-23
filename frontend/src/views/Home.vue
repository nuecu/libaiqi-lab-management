<template>
  <div class="home-page">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>{{ greeting }}，{{ userStore.userInfo.realName || userStore.userInfo.username || '用户' }}！</h2>
          <p>{{ currentDate }} · 欢迎回到实验室管理系统</p>
        </div>
        <div class="welcome-stats-mini">
          <div class="mini-item" v-if="stats.activeAlerts > 0">
            <el-icon :size="22" color="#F56C6C"><WarningFilled /></el-icon>
            <span>{{ stats.activeAlerts }} 条活跃预警</span>
          </div>
          <div class="mini-item" v-if="stats.lowStockItems > 0">
            <el-icon :size="22" color="#E6A23C"><Warning /></el-icon>
            <span>{{ stats.lowStockItems }} 项耗材库存不足</span>
          </div>
          <div class="mini-item" v-if="stats.pendingReservations > 0">
            <el-icon :size="22" color="#409EFF"><Clock /></el-icon>
            <span>{{ stats.pendingReservations }} 条预约待审核</span>
          </div>
          <div class="mini-item" v-if="stats.activeAlerts === 0 && stats.lowStockItems === 0 && stats.pendingReservations === 0">
            <el-icon :size="22" color="#67C23A"><CircleCheckFilled /></el-icon>
            <span>一切正常，暂无待处理事项</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="item in statCards" :key="item.key">
        <div class="stat-card" :class="{ 'stat-card--link': canGo(item) }" :style="{ '--accent': item.color }" @click="goCard(item)">
          <div class="stat-icon" :style="{ background: item.color + '15' }">
            <el-icon :size="28" :color="item.color"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats[item.key] || 0 }}</div>
            <div class="stat-label">{{ item.title }}</div>
          </div>
          <div class="stat-go" v-if="canGo(item)">
            <el-icon :size="16"><ArrowRight /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区（仅拥有全局统计概览权限的角色可见；学生个人视图隐藏，避免出现两张空图） -->
    <el-row v-if="!isPersonalView" :gutter="16" style="margin-bottom: 16px">
      <el-col :span="10">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="card-title">仪器状态分布</span></template>
          <div ref="chartRef1" style="height: 240px"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="card-title">预约状态统计</span></template>
          <div ref="chartRef2" style="height: 240px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告 + 快捷操作 -->
    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="14">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="card-title">最新公告</span>
              <el-button text type="primary" size="small" @click="router.push('/announcement/manage')">查看全部</el-button>
            </div>
          </template>
          <div v-if="announcements.length === 0" style="text-align:center;color:#999;padding:30px 0">暂无公告</div>
          <div v-else class="announcement-list">
            <div v-for="a in announcements" :key="a.id" class="announcement-item">
              <el-tag :type="tagType(a.type)" size="small" effect="plain">{{ tagLabel(a.type) }}</el-tag>
              <span class="a-title">{{ a.title }}</span>
              <span class="a-time">{{ a.publishTime }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="card-title">快捷操作</span></template>
          <div class="quick-actions">
            <div v-for="action in quickActions" :key="action.label" class="action-item" @click="router.push(action.path)">
              <el-icon :size="24" :color="action.color"><component :is="action.icon" /></el-icon>
              <span>{{ action.label }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 功能模块入口 -->
    <div class="section-title">功能模块</div>
    <div class="module-grid">
      <el-card v-for="mod in modules" :key="mod.path" shadow="never" class="module-card" @click="router.push(mod.path)">
        <div class="module-icon" :style="{ background: mod.bgColor }">
          <el-icon :size="40" color="#fff"><component :is="mod.icon" /></el-icon>
        </div>
        <div class="module-info">
          <div class="module-title">{{ mod.title }}</div>
          <div class="module-desc">{{ mod.desc }}</div>
        </div>
        <div class="module-arrow">
          <el-icon :size="22" color="#c0c4cc"><ArrowRight /></el-icon>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { usePermissionStore } from '../stores/permission'
import * as echarts from 'echarts'
import request from '../utils/request'

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const stats = ref({})
const announcements = ref([])
const chartRef1 = ref()
const chartRef2 = ref()

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const currentDate = computed(() => {
  const d = new Date()
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${weekDays[d.getDay()]}`
})

// 学生个人视图：无全局概览权限、但拥有「我的数据统计」权限时，首页展示个人数据
const isPersonalView = computed(() =>
  !permissionStore.hasPermission('statistics:overview') && permissionStore.hasPermission('statistics:my')
)

const globalStatCards = [
  { title: '耗材总数', key: 'totalConsumables', icon: 'Box', color: '#409EFF', path: '/resource/consumable', perm: 'resource:consumable' },
  { title: '仪器总数', key: 'totalInstruments', icon: 'Monitor', color: '#67C23A', path: '/resource/instrument', perm: 'resource:instrument' },
  { title: '预约总数', key: 'totalReservations', icon: 'Calendar', color: '#E6A23C', path: '/reservation/list', perm: 'reservation:list' },
  { title: '待审预约', key: 'pendingReservations', icon: 'Clock', color: '#F56C6C', path: '/reservation/list', query: { status: 'PENDING' }, perm: 'reservation:list' },
  { title: '实验记录', key: 'totalExperiments', icon: 'Notebook', color: '#a18cd1', path: '/experiment/log', perm: 'experiment:log' },
  { title: '活跃预警', key: 'activeAlerts', icon: 'WarningFilled', color: '#F56C6C', path: '/equipment/alert', perm: 'equipment:alert' },
  { title: '系统用户', key: 'totalUsers', icon: 'User', color: '#4facfe', path: '/system/user', perm: 'system:user' },
  { title: '库存不足', key: 'lowStockItems', icon: 'WarnTriangleFilled', color: '#fa709a', path: '/resource/consumable', perm: 'resource:consumable' }
]

const personalStatCards = [
  { title: '我的预约总数', key: 'totalReservations', icon: 'Calendar', color: '#E6A23C', path: '/reservation/list', perm: 'reservation:list' },
  { title: '本月预约', key: 'monthReservations', icon: 'Clock', color: '#409EFF', path: '/reservation/list', perm: 'reservation:list' },
  { title: '我的实验记录', key: 'totalExperiments', icon: 'Notebook', color: '#a18cd1', path: '/experiment/log', perm: 'experiment:log' },
  { title: '我的领用申请', key: 'totalRequisitions', icon: 'Sell', color: '#67C23A', path: '/experiment/material', perm: 'experiment:material' }
]

const statCards = computed(() => (isPersonalView.value ? personalStatCards : globalStatCards))

// 仅当卡片配置了目标路由且当前用户拥有该页面权限时，才允许点击跳转
function canGo(item) {
  return !!(item && item.path && item.perm && permissionStore.hasPermission(item.perm))
}
function goCard(item) {
  if (!item || !item.path) return
  if (!permissionStore.hasPermission(item.perm)) {
    ElMessage.warning('您没有访问该页面的权限')
    return
  }
  router.push(item.query ? { path: item.path, query: item.query } : item.path)
}

const allModules = [
  { title: '系统管理', desc: '用户、角色、权限、日志管理', icon: 'Setting', path: '/system/user', bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', perm: 'system' },
  { title: '基础资源', desc: '实验室、仪器、耗材', icon: 'Box', path: '/resource/consumable', bgColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', perm: 'resource' },
  { title: '预约管理', desc: '预约列表与预约规则配置', icon: 'Calendar', path: '/reservation/list', bgColor: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', perm: 'reservation' },
  { title: '实验管理', desc: '实验记录、耗材领用、库存盘点', icon: 'Notebook', path: '/experiment/log', bgColor: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', perm: 'experiment' },
  { title: '设备运维', desc: '报修、保养、预警、危化品管控', icon: 'Tools', path: '/equipment/repair', bgColor: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', perm: 'equipment' },
  { title: '数据统计', desc: '数据概览与趋势分析', icon: 'DataLine', path: '/statistics/overview', bgColor: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)', perm: 'statistics' },
  { title: '公告管理', desc: '公告发布与分类管理', icon: 'Bell', path: '/announcement/manage', bgColor: 'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)', perm: 'announcement' }
]

const modules = computed(() => allModules
  .filter(m => permissionStore.hasPermission(m.perm))
  .map(m => {
    if (m.perm === 'statistics' && isPersonalView.value) {
      return { ...m, title: '我的数据', desc: '我的预约、实验与领用统计', path: '/statistics/my' }
    }
    return m
  })
)

const allQuickActions = [
  { label: '新建预约', icon: 'Plus', color: '#409EFF', path: '/reservation/list', perm: 'reservation:list' },
  { label: '耗材领用', icon: 'Sell', color: '#67C23A', path: '/experiment/material', perm: 'experiment:material' },
  { label: '故障报修', icon: 'WarnTriangleFilled', color: '#F56C6C', path: '/equipment/repair', perm: 'equipment:repair' },
  { label: '库存盘点', icon: 'Tickets', color: '#E6A23C', path: '/experiment/inventory', perm: 'experiment:inventory' },
  { label: '仪器管理', icon: 'Monitor', color: '#a18cd1', path: '/resource/instrument', perm: 'resource:instrument' },
  { label: '安全预警', icon: 'Bell', color: '#fa709a', path: '/equipment/alert', perm: 'equipment:alert' }
]

const quickActions = computed(() => allQuickActions.filter(a => permissionStore.hasPermission(a.perm)))

function tagType(type) {
  return { SYSTEM: 'danger', EXPERIMENT: 'success', SAFETY: 'warning', OTHER: 'info' }[type] || 'info'
}
function tagLabel(type) {
  return { SYSTEM: '系统', EXPERIMENT: '实验', SAFETY: '安全', OTHER: '其他' }[type] || type
}

function initCharts() {
  if (chartRef1.value) {
    const c1 = echarts.init(chartRef1.value)
    c1.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 12 } },
      series: [{
        type: 'pie', radius: ['42%', '70%'], center: ['50%', '45%'],
        label: { show: false },
        data: [
          { value: stats.value.idle || 0, name: '空闲', itemStyle: { color: '#67C23A' } },
          { value: stats.value.inUse || 0, name: '使用中', itemStyle: { color: '#409EFF' } },
          { value: stats.value.fault || 0, name: '故障', itemStyle: { color: '#F56C6C' } },
          { value: stats.value.maintenance || 0, name: '保养中', itemStyle: { color: '#E6A23C' } },
          { value: stats.value.scrapped || 0, name: '已报废', itemStyle: { color: '#909399' } }
        ]
      }]
    })
    window.addEventListener('resize', () => c1.resize())
  }
  if (chartRef2.value) {
    const c2 = echarts.init(chartRef2.value)
    c2.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: ['待审核', '已通过', '使用中', '已驳回', '已取消', '已完成'], axisLabel: { fontSize: 12 } },
      yAxis: { type: 'value', minInterval: 1, axisLabel: { fontSize: 12 } },
      series: [{
        type: 'bar', barWidth: 28, itemStyle: { borderRadius: [4, 4, 0, 0] },
        data: [
          { value: stats.value.pending || 0, itemStyle: { color: '#E6A23C' } },
          { value: stats.value.approved || 0, itemStyle: { color: '#67C23A' } },
          { value: stats.value.inUse || 0, itemStyle: { color: '#13C2C2' } },
          { value: stats.value.rejected || 0, itemStyle: { color: '#F56C6C' } },
          { value: stats.value.cancelled || 0, itemStyle: { color: '#909399' } },
          { value: stats.value.completed || 0, itemStyle: { color: '#409EFF' } }
        ]
      }]
    })
    window.addEventListener('resize', () => c2.resize())
  }
}

onMounted(async () => {
  // 公告与当前用户相关，所有角色可见；失败静默降级
  request.get('/announcements', { params: { page: 0, size: 5 } })
    .then(r => { announcements.value = r.data || [] })
    .catch(() => { /* 忽略 */ })

  // 统计概览需要 statistics:overview 权限，无权限角色（如设备维护员）跳过请求，避免落地页弹出 403 提示
  if (permissionStore.hasPermission('statistics:overview')) {
    try {
      const [dashRes, instRes, resvRes] = await Promise.all([
        request.get('/statistics/dashboard'),
        request.get('/statistics/instruments'),
        request.get('/statistics/reservations')
      ])
      stats.value = {
        ...(dashRes.data || {}),
        ...(instRes.data || {}),
        ...(resvRes.data || {})
      }
    } catch (e) { /* ignore */ }
  } else if (permissionStore.hasPermission('statistics:my')) {
    // 学生个人视图：拉取本人统计数据填充首页卡片与横幅提示
    try {
      const myRes = await request.get('/statistics/my-overview')
      const m = myRes.data || {}
      stats.value = { ...m, pendingReservations: m.pending || 0 }
    } catch (e) { /* ignore */ }
  }
  await nextTick()
  initCharts()
})
</script>

<style scoped>
.home-page { max-width: 1400px; margin: 0 auto; }

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px 32px;
  margin-bottom: 20px;
  color: #fff;
}
.welcome-content { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; }
.welcome-text h2 { font-size: 22px; font-weight: 600; margin: 0 0 6px 0; }
.welcome-text p { font-size: 14px; opacity: 0.85; margin: 0; }
.welcome-stats-mini { display: flex; gap: 20px; flex-wrap: wrap; }
.mini-item { display: flex; align-items: center; gap: 6px; font-size: 13px; background: rgba(255,255,255,0.18); padding: 6px 14px; border-radius: 20px; }

/* 统计卡片 */
.stat-row { margin-bottom: 16px; }
.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid #f0f0f0;
  transition: box-shadow 0.2s, transform 0.2s;
  cursor: default;
  margin-bottom: 0;
}
.stat-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); transform: translateY(-2px); }
.stat-card--link { cursor: pointer; }
.stat-card--link:hover { border-color: var(--accent); box-shadow: 0 6px 20px rgba(0,0,0,0.12); }
.stat-go { margin-left: auto; opacity: 0; transform: translateX(-4px); transition: opacity 0.2s, transform 0.2s; color: var(--accent); display: flex; align-items: center; }
.stat-card--link:hover .stat-go { opacity: 1; transform: translateX(0); }
.stat-icon { width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-value { font-size: 24px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 2px; }

/* 图表卡片 */
.chart-card { border-radius: 10px; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }

/* 公告列表 */
.announcement-list { display: flex; flex-direction: column; gap: 12px; }
.announcement-item { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
.announcement-item:last-child { border-bottom: none; }
.a-title { flex: 1; font-size: 14px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.a-time { font-size: 12px; color: #999; flex-shrink: 0; }

/* 快捷操作 */
.quick-actions { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.action-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 16px 8px; border-radius: 10px; cursor: pointer;
  transition: background 0.2s; font-size: 13px; color: #555;
}
.action-item:hover { background: #f5f7fa; }

/* 模块标题 */
.section-title { font-size: 18px; font-weight: 600; color: #303133; margin-bottom: 16px; }

/* 模块卡片 */
.module-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(380px, 1fr)); gap: 20px; }
.module-card { cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; border-radius: 12px; }
.module-card:hover { transform: translateY(-5px); box-shadow: 0 12px 32px rgba(0,0,0,0.12); }
.module-card :deep(.el-card__body) { display: flex; align-items: center; gap: 18px; padding: 24px 26px; }
.module-icon { width: 68px; height: 68px; border-radius: 16px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.module-info { flex: 1; min-width: 0; }
.module-title { font-size: 18px; font-weight: 600; color: #303133; margin-bottom: 5px; }
.module-desc { font-size: 14px; color: #909399; line-height: 1.5; }
.module-arrow { flex-shrink: 0; }
</style>
