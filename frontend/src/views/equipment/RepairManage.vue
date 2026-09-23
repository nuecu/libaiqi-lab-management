<template>
  <div class="repair-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>故障报修管理</span>
          <el-button type="primary" @click="handleAdd">提交报修</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待处理" value="PENDING" />
            <el-option label="已分配" value="ASSIGNED" />
            <el-option label="维修中" value="REPAIRING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="page = 1; loadData()">搜索</el-button>
          <el-button @click="searchForm = {}; loadData()">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border>
        <el-table-column prop="orderNo" label="工单号" />
        <el-table-column prop="instrumentName" label="设备名称" />
        <el-table-column prop="reporterName" label="报修人" />
        <el-table-column prop="faultDesc" label="故障描述" show-overflow-tooltip />
        <el-table-column prop="reportTime" label="报修时间" width="160" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="{ PENDING: 'danger', ASSIGNED: 'warning', REPAIRING: 'warning', COMPLETED: 'success', CLOSED: 'info' }[row.status]">
              {{ { PENDING: '待处理', ASSIGNED: '已分配', REPAIRING: '维修中', COMPLETED: '已完成', CLOSED: '已关闭' }[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING'" size="small" type="primary" @click="handleProcess(row)">处理</el-button>
            <el-button v-if="row.status === 'REPAIRING'" size="small" type="success" @click="handleComplete(row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" style="margin-top: 16px" />
    </el-card>
    <el-dialog v-model="dialogVisible" title="提交报修" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="设备">
          <el-select v-model="form.instrumentId" placeholder="请选择" filterable>
            <el-option v-for="item in instruments" :key="item.id" :label="item.instrumentName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述"><el-input v-model="form.faultDesc" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">提交</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="detailVisible" title="报修详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="工单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="设备">{{ detailData.instrumentName }}</el-descriptions-item>
        <el-descriptions-item label="报修人">{{ detailData.reporterName }}</el-descriptions-item>
        <el-descriptions-item label="故障描述">{{ detailData.faultDesc }}</el-descriptions-item>
        <el-descriptions-item label="报修时间">{{ detailData.reportTime }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ detailData.handlerName }}</el-descriptions-item>
        <el-descriptions-item label="维修描述">{{ detailData.repairDesc }}</el-descriptions-item>
        <el-descriptions-item label="维修结果">{{ detailData.repairResult }}</el-descriptions-item>
        <el-descriptions-item label="维修费用">{{ detailData.repairCost }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ { PENDING: '待处理', ASSIGNED: '已分配', REPAIRING: '维修中', COMPLETED: '已完成', CLOSED: '已关闭' }[detailData.status] || detailData.status }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const searchForm = ref({})
const tableData = ref([])
const instruments = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const form = ref({})
const detailData = ref({})

const loadData = async () => {
  const res = await request.get('/equipment/repairs', { params: { ...searchForm.value, page: page.value - 1, size: size.value } })
  if (res.code === 200) { tableData.value = res.data; total.value = res.total || 0 }
}
const loadInstruments = async () => {
  const res = await request.get('/instruments')
  if (res.code === 200) instruments.value = res.data
}
const handleAdd = () => { form.value = {}; dialogVisible.value = true }
const handleSave = async () => {
  const res = await request.post('/equipment/repairs', form.value)
  if (res.code === 200) { ElMessage.success('报修提交成功'); dialogVisible.value = false; loadData() }
}
const handleProcess = (row) => {
  ElMessageBox.prompt('请输入处理说明', '处理报修', { type: 'info' }).then(async ({ value }) => {
    await request.put(`/equipment/repairs/${row.id}/process`, { handleResult: value })
    ElMessage.success('已开始处理'); loadData()
  })
}
const handleComplete = async (row) => {
  await request.put(`/equipment/repairs/${row.id}/complete`, {})
  ElMessage.success('已完成'); loadData()
}
const handleView = (row) => { detailData.value = row; detailVisible.value = true }
onMounted(() => { loadData(); loadInstruments() })
</script>
<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
