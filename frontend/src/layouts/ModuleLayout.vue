<template>
  <el-container style="height: 100%">
    <el-aside width="200px" style="background: #fff; border-right: 1px solid #e6e6e6; display: flex; flex-direction: column; overflow: hidden">
      <div class="sidebar-title">{{ currentModule.title }}</div>
      <el-menu :default-active="$route.path" text-color="#333" active-text-color="#409EFF" router style="flex: 1; overflow-y: auto; border-right: none">
        <el-menu-item v-for="child in currentModule.children" :key="child.path" :index="child.path">
          <el-icon><component :is="child.icon" /></el-icon>
          <span>{{ child.name }}</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-button type="primary" text @click="$router.push('/home')">
          <el-icon><HomeFilled /></el-icon>
          返回主页
        </el-button>
      </div>
    </el-aside>
    <el-main style="background: #f0f2f5; padding: 20px">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const moduleMap = {
  '/system': {
    title: '系统管理',
    children: [
      { name: '用户管理', path: '/system/user', icon: 'User' },
      { name: '角色管理', path: '/system/role', icon: 'UserFilled' },
      { name: '权限管理', path: '/system/permission', icon: 'Lock' },
      { name: '日志管理', path: '/system/log', icon: 'Document' }
    ]
  },
  '/resource': {
    title: '基础资源',
    children: [
      { name: '耗材管理', path: '/resource/consumable', icon: 'Box' },
      { name: '仪器管理', path: '/resource/instrument', icon: 'Monitor' },
      { name: '实验室管理', path: '/resource/laboratory', icon: 'OfficeBuilding' }
    ]
  },
  '/reservation': {
    title: '预约管理',
    children: [
      { name: '预约列表', path: '/reservation/list', icon: 'List' },
      { name: '预约规则', path: '/reservation/rules', icon: 'SetUp' }
    ]
  },
  '/experiment': {
    title: '实验管理',
    children: [
      { name: '实验记录', path: '/experiment/log', icon: 'EditPen' },
      { name: '耗材领用', path: '/experiment/material', icon: 'Sell' },
      { name: '库存盘点', path: '/experiment/inventory', icon: 'Tickets' }
    ]
  },
  '/equipment': {
    title: '设备运维',
    children: [
      { name: '故障报修', path: '/equipment/repair', icon: 'WarnTriangleFilled' },
      { name: '保养管理', path: '/equipment/maintenance', icon: 'Tools' },
      { name: '安全预警', path: '/equipment/alert', icon: 'Bell' },
      { name: '危化品管控', path: '/equipment/hazardous', icon: 'Warning' },
      { name: '耗材归还', path: '/equipment/return', icon: 'RefreshRight' }
    ]
  },
  '/statistics': {
    title: '数据统计',
    children: [
      { name: '数据概览', path: '/statistics/overview', icon: 'DataLine' },
      { name: '趋势分析', path: '/statistics/trend', icon: 'TrendCharts' }
    ]
  },
  '/announcement': {
    title: '公告管理',
    children: [
      { name: '公告管理', path: '/announcement/manage', icon: 'Bell' },
      { name: '公告分类', path: '/announcement/category', icon: 'FolderOpened' }
    ]
  }
}

const currentModule = computed(() => {
  const basePath = '/' + route.path.split('/')[1]
  return moduleMap[basePath] || { title: '', children: [] }
})
</script>

<style scoped>
.sidebar-title {
  padding: 20px 16px 12px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}
.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}
</style>
