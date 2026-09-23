<template>
  <div class="maintenance-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>设备保养管理</span>
          <el-button type="primary" @click="handleAdd">新增保养计划</el-button>
        </div>
      </template>
      <el-tabs v-model="activeTab" @tab-change="loadData">
        <el-tab-pane label="保养计划" name="plans" />
        <el-tab-pane label="保养记录" name="records" />
      </el-tabs>
      <el-table v-if="activeTab === 'plans'" :data="plans" border>
        <el-table-column prop="planName" label="计划名称" />
        <el-table-column prop="instrumentName" label="设备名称" />
        <el-table-column prop="maintenanceType" label="周期类型">
          <template #default="{ row }">{{ { DAILY: '每日', WEEKLY: '每周', MONTHLY: '每月', QUARTERLY: '每季', YEARLY: '每年' }[row.maintenanceType] || row.maintenanceType }}</template>
        </el-table-column>
        <el-table-column prop="lastMaintenanceDate" label="上次保养" width="120" />
        <el-table-column prop="nextMaintenanceDate" label="下次保养" width="120" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }"><el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ { ACTIVE: '启用', OVERDUE: '逾期', COMPLETED: '完成' }[row.status] || row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEditPlan(row)">编辑</el-button>
            <el-button size="small" type="success" @click="handleExecute(row)">执行</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-table v-if="activeTab === 'records'" :data="records" border>
        <el-table-column prop="instrumentName" label="设备名称" />
        <el-table-column prop="planName" label="保养计划" />
        <el-table-column prop="executorName" label="执行人" />
        <el-table-column prop="maintenanceDate" label="保养日期" width="120" />
        <el-table-column prop="result" label="保养结果" show-overflow-tooltip />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }"><el-tag :type="row.status === 'COMPLETED' ? 'success' : 'warning'">{{ row.status === 'COMPLETED' ? '已完成' : '进行中' }}</el-tag></template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="计划名称"><el-input v-model="form.planName" /></el-form-item>
        <el-form-item label="设备">
          <el-select v-model="form.instrumentId" placeholder="请选择" filterable>
            <el-option v-for="item in instruments" :key="item.id" :label="item.instrumentName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="周期类型">
          <el-select v-model="form.maintenanceType">
            <el-option label="每日" value="DAILY" /><el-option label="每周" value="WEEKLY" />
            <el-option label="每月" value="MONTHLY" /><el-option label="每季" value="QUARTERLY" />
            <el-option label="每年" value="YEARLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="周期天数"><el-input-number v-model="form.cycleDays" :min="1" /></el-form-item>
        <el-form-item label="下次保养"><el-date-picker v-model="form.nextMaintenanceDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('plans')
const plans = ref([])
const records = ref([])
const instruments = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({})

const loadData = async () => {
  if (activeTab.value === 'plans') {
    const res = await request.get('/equipment/maintenance-plans')
    if (res.code === 200) plans.value = res.data
  } else {
    const res = await request.get('/equipment/maintenance-records')
    if (res.code === 200) records.value = res.data
  }
}
const loadInstruments = async () => {
  const res = await request.get('/instruments')
  if (res.code === 200) instruments.value = res.data
}
const handleAdd = () => { dialogTitle.value = '新增保养计划'; form.value = { maintenanceType: 'MONTHLY', cycleDays: 30 }; dialogVisible.value = true }
const handleEditPlan = (row) => { dialogTitle.value = '编辑保养计划'; form.value = { ...row }; dialogVisible.value = true }
const handleSave = async () => {
  if (!form.value.status) form.value.status = 'ACTIVE'
  const res = form.value.id ? await request.put(`/equipment/maintenance-plans/${form.value.id}`, form.value) : await request.post('/equipment/maintenance-plans', form.value)
  if (res.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; loadData() }
}
const handleExecute = async (row) => {
  await request.post(`/equipment/maintenance-plans/${row.id}/execute`)
  ElMessage.success('保养任务已执行'); loadData()
}
onMounted(() => { loadData(); loadInstruments() })
</script>
<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
