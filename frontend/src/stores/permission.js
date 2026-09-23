import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request'

export const usePermissionStore = defineStore('permission', () => {
  // 权限编码列表
  const permissions = ref(JSON.parse(localStorage.getItem('permissions') || '[]'))
  // 用户角色列表
  const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))

  // 菜单配置：定义所有菜单项及其对应的权限编码
  const allMenus = ref([
    {
      code: 'system', title: '系统管理', icon: 'Setting', path: '/system',
      children: [
        { code: 'system:user', title: '用户管理', path: '/system/user' },
        { code: 'system:role', title: '角色管理', path: '/system/role' },
        { code: 'system:permission', title: '权限管理', path: '/system/permission' },
        { code: 'system:log', title: '日志管理', path: '/system/log' }
      ]
    },
    {
      code: 'resource', title: '基础资源', icon: 'Box', path: '/resource',
      children: [
        { code: 'resource:consumable', title: '耗材管理', path: '/resource/consumable' },
        { code: 'resource:instrument', title: '仪器管理', path: '/resource/instrument' },
        { code: 'resource:laboratory', title: '实验室管理', path: '/resource/laboratory' }
      ]
    },
    {
      code: 'reservation', title: '预约管理', icon: 'Calendar', path: '/reservation',
      children: [
        { code: 'reservation:list', title: '预约列表', path: '/reservation/list' },
        { code: 'reservation:rules', title: '预约规则', path: '/reservation/rules' }
      ]
    },
    {
      code: 'experiment', title: '实验管理', icon: 'Document', path: '/experiment',
      children: [
        { code: 'experiment:log', title: '实验记录', path: '/experiment/log' },
        { code: 'experiment:material', title: '耗材领用', path: '/experiment/material' },
        { code: 'experiment:inventory', title: '库存盘点', path: '/experiment/inventory' }
      ]
    },
    {
      code: 'equipment', title: '设备运维', icon: 'Tools', path: '/equipment',
      children: [
        { code: 'equipment:repair', title: '故障报修', path: '/equipment/repair' },
        { code: 'equipment:maintenance', title: '保养管理', path: '/equipment/maintenance' },
        { code: 'equipment:alert', title: '安全预警', path: '/equipment/alert' },
        { code: 'equipment:hazardous', title: '危化品管控', path: '/equipment/hazardous' },
        { code: 'equipment:return', title: '耗材归还', path: '/equipment/return' }
      ]
    },
    {
      code: 'statistics', title: '数据统计', icon: 'DataAnalysis', path: '/statistics',
      children: [
        { code: 'statistics:overview', title: '数据概览', path: '/statistics/overview' },
        { code: 'statistics:trend', title: '趋势分析', path: '/statistics/trend' },
        { code: 'statistics:my', title: '我的数据统计', path: '/statistics/my' }
      ]
    },
    {
      code: 'announcement', title: '公告管理', icon: 'Bell', path: '/announcement',
      children: [
        { code: 'announcement:manage', title: '公告管理', path: '/announcement/manage' },
        { code: 'announcement:category', title: '公告分类', path: '/announcement/category' }
      ]
    },
    {
      code: 'ai', title: 'AI助手', icon: 'MagicStick', path: '/ai',
      children: [
        { code: 'ai:assistant', title: '智能问答', path: '/ai/assistant' },
        { code: 'ai:knowledge', title: '知识库管理', path: '/ai/knowledge' }
      ]
    }
  ])

  // 根据权限过滤后的菜单
  const accessibleMenus = computed(() => {
    return allMenus.value
      .filter(menu => hasPermission(menu.code))
      .map(menu => ({
        ...menu,
        children: menu.children.filter(child => hasPermission(child.code))
      }))
      .filter(menu => menu.children.length > 0)
  })

  // 所有可访问的路径
  const accessiblePaths = computed(() => {
    const paths = ['/home', '/profile']
    for (const menu of accessibleMenus.value) {
      paths.push(menu.path)
      for (const child of menu.children) {
        paths.push(child.path)
      }
    }
    return paths
  })

  // 检查是否拥有某个权限
  function hasPermission(code) {
    return permissions.value.includes(code)
  }

  // 设置权限（登录时调用）
  function setPermissions(perms, userRoles) {
    permissions.value = perms || []
    roles.value = userRoles || []
    localStorage.setItem('permissions', JSON.stringify(perms || []))
    localStorage.setItem('roles', JSON.stringify(userRoles || []))
  }

  // 从后端重新加载权限
  async function loadPermissions() {
    try {
      const res = await request.get('/auth/permissions')
      permissions.value = res.data || []
      localStorage.setItem('permissions', JSON.stringify(res.data || []))
    } catch (e) {
      console.error('加载权限失败', e)
    }
  }

  // 清除权限
  function clearPermissions() {
    permissions.value = []
    roles.value = []
    localStorage.removeItem('permissions')
    localStorage.removeItem('roles')
  }

  return {
    permissions, roles, allMenus, accessibleMenus, accessiblePaths,
    hasPermission, setPermissions, loadPermissions, clearPermissions
  }
})
