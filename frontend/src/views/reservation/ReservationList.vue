<template>
  <div class="reservation-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约管理</span>
          <el-button type="primary" @click="handleAdd">新增预约</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="预约人">
          <el-input v-model="searchForm.userName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待审批" value="PENDING" />
            <el-option label="已批准" value="APPROVED" />
            <el-option label="使用中" value="IN_USE" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期">
          <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="page = 1; loadData()">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border>
        <el-table-column prop="userName" label="预约人" />
        <el-table-column prop="resourceName" label="资源名称" />
        <el-table-column prop="resourceType" label="资源类型">
          <template #default="{ row }">{{ resourceTypeText(row.resourceType) }}</template>
        </el-table-column>
        <el-table-column prop="reservationDate" label="预约日期" />
        <el-table-column prop="startTime" label="开始时间" width="120" />
        <el-table-column prop="endTime" label="结束时间" width="120" />
        <el-table-column prop="experimentTopic" label="实验主题" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button v-permission="'reservation:approve'" v-if="row.status === 'PENDING'" size="small" type="success" @click="handleApprove(row)">批准</el-button>
            <el-button v-permission="'reservation:approve'" v-if="row.status === 'PENDING'" size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
            <el-button size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 'APPROVED'" size="small" type="primary" @click="handleCheckIn(row)">签到</el-button>
            <el-button v-if="row.status === 'IN_USE'" size="small" type="success" @click="handleCheckOut(row)">签退</el-button>
            <el-button v-if="row.status === 'APPROVED'" size="small" type="warning" @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" style="margin-top: 16px" />
    </el-card>
    <el-dialog v-model="dialogVisible" title="新增预约" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="资源类型">
          <el-select v-model="form.resourceType" placeholder="请选择" @change="onResourceTypeChange">
            <el-option label="实验室" value="LABORATORY" />
            <el-option label="仪器" value="INSTRUMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源" v-if="form.resourceType === 'LABORATORY'">
          <el-select v-model="form.resourceId" placeholder="请选择实验室" filterable>
            <el-option v-for="item in laboratories" :key="item.id" :label="item.labName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源" v-if="form.resourceType === 'INSTRUMENT'">
          <el-select v-model="form.resourceId" placeholder="请选择仪器" filterable>
            <el-option v-for="item in instruments" :key="item.id" :label="item.instrumentName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期"><el-date-picker v-model="form.reservationDate" type="date" placeholder="选择预约日期" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="开始时间"><el-time-picker v-model="form.startTime" placeholder="选择开始时间" value-format="HH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-time-picker v-model="form.endTime" placeholder="选择结束时间" value-format="HH:mm:ss" /></el-form-item>
        <el-form-item label="实验主题"><el-input v-model="form.experimentTopic" type="textarea" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">提交</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="detailVisible" title="预约详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="预约人">{{ detailData.userName }}</el-descriptions-item>
        <el-descriptions-item label="资源名称">{{ detailData.resourceName }}</el-descriptions-item>
        <el-descriptions-item label="资源类型">{{ resourceTypeText(detailData.resourceType) }}</el-descriptions-item>
        <el-descriptions-item label="预约日期">{{ detailData.reservationDate }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detailData.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detailData.endTime }}</el-descriptions-item>
        <el-descriptions-item label="实验主题">{{ detailData.experimentTopic }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusType(detailData.status)">{{ statusText(detailData.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="备注">{{ detailData.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const isStudent = computed(() => userStore.userInfo.role === 'STUDENT')

const searchForm = ref({ userName: '', status: '', dateRange: [] })
const tableData = ref([])
const instruments = ref([])
const laboratories = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const form = ref({})
const detailData = ref({})

const statusText = (s) => ({ PENDING: '待审批', APPROVED: '已批准', IN_USE: '使用中', REJECTED: '已拒绝', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s)
const statusType = (s) => ({ PENDING: 'warning', APPROVED: 'success', IN_USE: 'primary', REJECTED: 'danger', COMPLETED: 'info', CANCELLED: 'info' }[s] || '')
const resourceTypeText = (t) => ({ LABORATORY: '实验室', INSTRUMENT: '仪器' }[t] || t || '—')

const loadData = async () => {
  const params = { ...searchForm.value, page: page.value - 1, size: size.value }
  if (params.dateRange && params.dateRange.length === 2) { params.startDate = params.dateRange[0]; params.endDate = params.dateRange[1] }
  delete params.dateRange
  const res = await request.get('/reservations', { params })
  if (res.code === 200) { tableData.value = res.data; total.value = res.total || 0 }
}
const loadOptions = async () => {
  const [iRes, lRes] = await Promise.all([request.get('/instruments'), request.get('/laboratories')])
  if (iRes.code === 200) instruments.value = iRes.data
  if (lRes.code === 200) laboratories.value = lRes.data
}
const resetSearch = () => { searchForm.value = { userName: '', status: '', dateRange: [] }; page.value = 1; loadData() }
const handleAdd = () => { form.value = { resourceType: 'LABORATORY' }; dialogVisible.value = true }
const onResourceTypeChange = () => { form.value.resourceId = null }
const handleSave = async () => {
  const res = await request.post('/reservations', form.value)
  if (res.code === 200) { ElMessage.success('预约提交成功'); dialogVisible.value = false; loadData() }
}
const handleApprove = (row) => {
  ElMessageBox.confirm('确认批准该预约?', '提示', { type: 'info' }).then(async () => {
    await request.post(`/reservations/${row.id}/approve`, { approverId: userStore.userInfo.userId })
    ElMessage.success('已批准'); loadData()
  })
}
const handleReject = (row) => {
  ElMessageBox.prompt('请输入拒绝原因', '拒绝预约', { type: 'warning' }).then(async ({ value }) => {
    await request.post(`/reservations/${row.id}/reject`, { approverId: userStore.userInfo.userId, reason: value })
    ElMessage.success('已拒绝'); loadData()
  })
}
const handleCancel = (row) => {
  ElMessageBox.confirm('确认取消该预约?', '提示', { type: 'warning' }).then(async () => {
    await request.post(`/reservations/${row.id}/cancel`)
    ElMessage.success('已取消'); loadData()
  })
}
const handleCheckIn = (row) => {
  ElMessageBox.confirm('确认签到并开始使用该资源?', '签到', { type: 'info' }).then(async () => {
    await request.post(`/reservations/${row.id}/checkin`)
    ElMessage.success('签到成功，预约已进入“使用中”'); loadData()
  })
}
const handleCheckOut = (row) => {
  ElMessageBox.confirm('确认签退并结束本次使用?', '签退', { type: 'warning' }).then(async () => {
    await request.post(`/reservations/${row.id}/checkout`)
    ElMessage.success('签退成功，预约已完成'); loadData()
  })
}
const handleView = (row) => { detailData.value = row; detailVisible.value = true }

onMounted(() => { if (route.query.status) searchForm.value.status = route.query.status; loadData(); loadOptions() })
</script>
<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
