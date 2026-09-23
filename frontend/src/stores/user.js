import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../utils/request'
import { usePermissionStore } from './permission'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    token.value = res.data.token
    userInfo.value = res.data
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    // 同步保存权限信息
    const permissionStore = usePermissionStore()
    permissionStore.setPermissions(res.data.permissions, res.data.roles)
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    const permissionStore = usePermissionStore()
    permissionStore.clearPermissions()
  }

  return { token, userInfo, login, logout }
})
