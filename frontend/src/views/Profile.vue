<template>
  <div style="padding: 20px">
    <el-card>
      <template #header>
        <div style="display: flex; align-items: center; gap: 12px">
          <el-avatar :size="56" style="background: #409EFF; font-size: 24px">{{ (form.realName || form.username || 'U')[0] }}</el-avatar>
          <div>
            <div style="font-size: 18px; font-weight: bold">{{ form.realName || form.username }}</div>
            <el-tag size="small" :type="userTypeTag">{{ userTypeLabel }}</el-tag>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <el-descriptions :column="2" border style="margin-bottom: 20px">
            <el-descriptions-item label="用户名">{{ form.username }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ form.realName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学号/工号">{{ form.studentNo || form.staffNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用户类型">{{ userTypeLabel }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ form.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ form.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="院系/部门">{{ form.department || '-' }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ form.major || '-' }}</el-descriptions-item>
            <el-descriptions-item label="账号状态">
              <el-tag :type="form.status === 1 ? 'success' : 'danger'">{{ form.status === 1 ? '启用' : '禁用' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="最近登录">{{ form.lastLoginTime || '-' }}</el-descriptions-item>
          </el-descriptions>

          <el-divider>修改个人信息</el-divider>
          <el-form :model="editForm" label-width="100px" style="max-width: 500px">
            <el-form-item label="姓名">
              <el-input v-model="editForm.realName" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="editForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="editForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="院系/部门">
              <el-input v-model="editForm.department" placeholder="请输入院系/部门" />
            </el-form-item>
            <el-form-item label="专业">
              <el-input v-model="editForm.major" placeholder="请输入专业" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form :model="pwdForm" :rules="pwdRules" ref="pwdRef" label-width="100px" style="max-width: 500px; margin-top: 10px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword" :loading="changingPwd">确认修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 登录日志 -->
        <el-tab-pane label="登录日志" name="logs">
          <el-table :data="loginLogs" border stripe style="margin-top: 10px" v-loading="logsLoading">
            <el-table-column prop="loginTime" label="登录时间" width="180" />
            <el-table-column prop="ip" label="IP地址" width="150" />
            <el-table-column prop="location" label="登录地点" />
            <el-table-column prop="browser" label="浏览器" />
            <el-table-column prop="os" label="操作系统" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="msg" label="消息" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { usePermissionStore } from '../stores/permission'
const permissionStore = usePermissionStore()

const activeTab = ref('info')
const saving = ref(false)
const changingPwd = ref(false)
const logsLoading = ref(false)
const form = ref({})
const editForm = ref({})
const loginLogs = ref([])
const pwdRef = ref(null)

const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const userTypeMap = { ADMIN: '系统管理员', TEACHER: '教师', STUDENT: '学生', MAINTAINER: '设备维护员' }
const userTypeLabel = computed(() => userTypeMap[form.value.userType] || form.value.userType || '-')
const userTypeTag = computed(() => {
  const m = { ADMIN: 'danger', TEACHER: 'warning', STUDENT: '', MAINTAINER: 'info' }
  return m[form.value.userType] || ''
})

async function loadProfile() {
  try {
    const res = await request.get('/users/me')
    form.value = res.data || {}
    editForm.value = {
      realName: form.value.realName || '',
      email: form.value.email || '',
      phone: form.value.phone || '',
      department: form.value.department || '',
      major: form.value.major || ''
    }
  } catch (e) {
    console.error('加载个人信息失败', e)
  }
}

async function saveProfile() {
  saving.value = true
  try {
    await request.put('/users/me', editForm.value)
    ElMessage.success('个人信息修改成功')
    loadProfile()
  } catch (e) {
    ElMessage.error('修改失败')
  } finally {
    saving.value = false
  }
}

async function changePassword() {
  try {
    await pwdRef.value.validate()
  } catch { return }
  changingPwd.value = true
  try {
    await request.put('/users/me/password', { oldPassword: pwdForm.value.oldPassword, newPassword: pwdForm.value.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e) {
    ElMessage.error('密码修改失败')
  } finally {
    changingPwd.value = false
  }
}

async function loadLogs() {
  // 登录日志需要 system:log 权限（仅管理员），无权限角色直接跳过，避免进入个人中心的 403 提示
  if (!permissionStore.hasPermission('system:log')) {
    loginLogs.value = []
    return
  }
  logsLoading.value = true
  try {
    const res = await request.get('/logs/login', { params: { username: form.value.username, page: 0, size: 20 } })
    loginLogs.value = res.data || []
  } catch (e) {
    console.error('加载登录日志失败', e)
  } finally {
    logsLoading.value = false
  }
}

onMounted(() => {
  loadProfile()
  loadLogs()
})
</script>
