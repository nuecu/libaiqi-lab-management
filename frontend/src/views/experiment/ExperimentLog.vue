<template>
  <div class="experiment-log">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>实验记录管理</span>
          <span class="header-tip">实验记录由预约审批自动生成，与预约列表一一对应</span>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="实验名称"><el-input v-model="searchForm.experimentTopic" placeholder="请输入" clearable /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="page = 1; loadData()">搜索</el-button>
          <el-button @click="searchForm = {}; loadData()">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border>
        <el-table-column prop="experimentTopic" label="实验名称" />
        <el-table-column prop="userName" label="实验人" />
        <el-table-column prop="laboratoryName" label="实验室" />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button v-if="row.status === 'NOT_STARTED'" size="small" type="success" @click="handleCheckIn(row)">签到</el-button>
            <el-button v-if="row.status === 'IN_PROGRESS' && canCheckOut(row)" size="small" type="warning" @click="handleCheckOut(row)">签退</el-button>
            <el-button v-if="!isStudent && (row.status === 'NOT_STARTED' || row.status === 'IN_PROGRESS')" size="small" type="info" plain @click="handleCancel(row)">取消</el-button>
            <el-button v-if="!isStudent && !isTerminal(row)" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="!isStudent && !isTerminal(row)" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            <span v-if="availableActions(row) === 0" class="readonly-tip">{{ isTerminal(row) ? '已完成/已取消，不可操作' : '—' }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" style="margin-top: 16px" />
    </el-card>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="实验名称"><el-input v-model="form.experimentTopic" /></el-form-item>
        <el-form-item label="实验室">
          <el-select v-model="form.laboratoryId" placeholder="请选择">
            <el-option v-for="item in laboratories" :key="item.id" :label="item.labName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isStudent = computed(() => userStore.userInfo.role === 'STUDENT')
const isAdmin = computed(() => userStore.userInfo.role === 'ADMIN')
const currentUserId = computed(() => Number(userStore.userInfo.userId))
// 只有实验发起者本人可签退；管理员可代为签退
const canCheckOut = (row) => isAdmin.value || Number(row.userId) === currentUserId.value
// 终态（已完成/已取消）记录只读，不可签到/签退/取消/编辑/删除
const isTerminal = (row) => row.status === 'COMPLETED' || row.status === 'CANCELLED'
const statusText = (s) => ({ NOT_STARTED: '未开始', IN_PROGRESS: '进行中', COMPLETED: '已完成', CANCELLED: '已取消', PAUSED: '已暂停' }[s] || s)
const statusTagType = (s) => ({ COMPLETED: 'success', IN_PROGRESS: 'warning', CANCELLED: 'danger', NOT_STARTED: 'info', PAUSED: 'info' }[s] || 'info')
const availableActions = (row) => {
  let n = 0
  if (row.status === 'NOT_STARTED') n++
  if (row.status === 'IN_PROGRESS' && canCheckOut(row)) n++
  if (!isStudent.value && (row.status === 'NOT_STARTED' || row.status === 'IN_PROGRESS')) n++
  if (!isStudent.value && !isTerminal(row)) n += 2 // 编辑 + 删除
  return n
}

const searchForm = ref({})
const tableData = ref([])
const laboratories = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({})

const loadData = async () => {
  const res = await request.get('/experiments/logs', { params: { ...searchForm.value, page: page.value - 1, size: size.value } })
  if (res.code === 200) { tableData.value = res.data; total.value = res.total || 0 }
}
const loadLaboratories = async () => {
  const res = await request.get('/laboratories')
  if (res.code === 200) laboratories.value = res.data
}
const handleEdit = (row) => { dialogTitle.value = '编辑实验记录'; form.value = { ...row }; dialogVisible.value = true }
const handleSave = async () => {
  if (!form.value.id) return
  const res = await request.put(`/experiments/logs/${form.value.id}`, form.value)
  if (res.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; loadData() }
}
const handleCheckIn = async (row) => {
  const res = await request.post(`/experiments/logs/${row.id}/check-in`)
  if (res.code === 200) { ElMessage.success('签到成功'); loadData() }
}
const handleCheckOut = async (row) => {
  const res = await request.post(`/experiments/logs/${row.id}/check-out`)
  if (res.code === 200) { ElMessage.success('签退成功'); loadData() }
}
const handleCancel = (row) => {
  ElMessageBox.confirm('确认取消该实验记录？取消后关联的预约也将标记为已取消。', '提示', { type: 'warning' }).then(async () => {
    const res = await request.post(`/experiments/logs/${row.id}/cancel`)
    if (res.code === 200) { ElMessage.success('已取消'); loadData() }
  })
}
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' }).then(async () => {
    const res = await request.delete(`/experiments/logs/${row.id}`)
    if (res.code === 200) { ElMessage.success('删除成功'); loadData() }
  })
}
onMounted(() => { loadData(); loadLaboratories() })
</script>
<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-tip { font-size: 12px; color: var(--el-text-color-secondary); }
.readonly-tip { font-size: 12px; color: var(--el-text-color-secondary); }
.search-form { margin-bottom: 16px; }
</style>
