// 模块配置：定义六大功能模块及其子功能
export const modules = [
  {
    key: 'system',
    name: '系统管理',
    icon: 'Setting',
    color: '#409EFF',
    desc: '用户、角色、权限与日志管理',
    path: '/system',
    children: [
      { name: '用户管理', path: '/system/user' },
      { name: '角色管理', path: '/system/role' },
      { name: '权限管理', path: '/system/permission' },
      { name: '日志管理', path: '/system/log' },
    ]
  },
  {
    key: 'resource',
    name: '基础资源',
    icon: 'Box',
    color: '#67C23A',
    desc: '实验室、仪器与耗材',
    path: '/resource',
    children: [
      { name: '耗材管理', path: '/resource/consumable' },
      { name: '仪器管理', path: '/resource/instrument' },
      { name: '实验室管理', path: '/resource/laboratory' },
    ]
  },
  {
    key: 'reservation',
    name: '预约管理',
    icon: 'Calendar',
    color: '#E6A23C',
    desc: '预约列表与规则配置',
    path: '/reservation',
    children: [
      { name: '预约列表', path: '/reservation/list' },
      { name: '预约规则', path: '/reservation/rules' },
    ]
  },
  {
    key: 'experiment',
    name: '实验管理',
    icon: 'Notebook',
    color: '#F56C6C',
    desc: '实验记录、耗材领用与库存盘点',
    path: '/experiment',
    children: [
      { name: '实验记录', path: '/experiment/log' },
      { name: '耗材领用', path: '/experiment/material' },
      { name: '库存盘点', path: '/experiment/inventory' },
    ]
  },
  {
    key: 'equipment',
    name: '设备运维',
    icon: 'Tools',
    color: '#909399',
    desc: '报修、保养、预警与危化品管控',
    path: '/equipment',
    children: [
      { name: '故障报修', path: '/equipment/repair' },
      { name: '保养管理', path: '/equipment/maintenance' },
      { name: '安全预警', path: '/equipment/alert' },
      { name: '危化品管控', path: '/equipment/hazardous' },
      { name: '耗材归还', path: '/equipment/return' },
    ]
  },
  {
    key: 'statistics',
    name: '数据统计',
    icon: 'DataLine',
    color: '#8B5CF6',
    desc: '数据统计与可视化分析',
    path: '/statistics',
    children: [
      { name: '数据概览', path: '/statistics/overview' },
    ]
  },
]

export function getModuleByPath(path) {
  return modules.find(m => path.startsWith(m.path))
}
