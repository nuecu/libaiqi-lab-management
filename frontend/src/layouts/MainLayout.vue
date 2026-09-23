<template>
  <el-container style="height: 100vh; flex-direction: column">
    <!-- 顶部导航栏 -->
    <el-header class="top-header">
      <div class="header-left">
        <span class="system-title">实验室管理系统</span>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="32" style="background: #409EFF">{{ (userStore.userInfo.realName || 'U')[0] }}</el-avatar>
            <span>{{ userStore.userInfo.realName || userStore.userInfo.username }}</span>
            <el-tag size="small" type="info" style="margin-left: 6px">{{ roleLabel }}</el-tag>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <!-- 内容区：侧边栏 + 主内容 -->
    <el-container style="flex: 1; overflow: hidden">
      <el-aside width="220px" class="sidebar">
        <el-menu
          :default-active="$route.path"
          :default-openeds="defaultOpeneds"
          text-color="#333"
          active-text-color="#409EFF"
          router
          class="sidebar-menu"
        >
          <!-- 首页 -->
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <!-- 根据权限动态渲染菜单 -->
          <template v-for="menu in permissionStore.accessibleMenus" :key="menu.code">
            <el-sub-menu :index="menu.path">
              <template #title>
                <el-icon><component :is="menu.icon" /></el-icon>
                <span>{{ menu.title }}</span>
              </template>
              <el-menu-item
                v-for="child in menu.children"
                :key="child.code"
                :index="child.path"
              >
                {{ child.title }}
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-aside>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { usePermissionStore } from '../stores/permission'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const roleMap = {
  ADMIN: '管理员',
  TEACHER: '教师',
  STUDENT: '学生',
  MAINTAINER: '维护员'
}
const roleLabel = computed(() => roleMap[userStore.userInfo.role] || userStore.userInfo.role || '用户')

// 默认展开当前模块
const defaultOpeneds = computed(() => {
  const basePath = '/' + route.path.split('/')[1]
  return [basePath]
})

function handleCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.top-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 24px;
  height: 60px;
  z-index: 10;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.system-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  cursor: pointer;
  user-select: none;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #333;
}
.sidebar {
  background: #fff;
  border-right: 1px solid #e6e6e6;
  overflow-y: auto;
  overflow-x: hidden;
}
.sidebar-menu {
  border-right: none;
  height: 100%;
}
.main-content {
  background: #f0f2f5;
  padding: 20px;
  flex: 1;
  overflow: auto;
}
</style>
