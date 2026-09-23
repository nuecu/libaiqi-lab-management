<template>
  <div class="reservation-rules">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约规则管理</span>
          <el-button type="primary" @click="handleAdd">新增规则</el-button>
        </div>
      </template>
      <el-table :data="tableData" border>
        <el-table-column prop="ruleName" label="规则名称" />
        <el-table-column prop="resourceType" label="资源类型">
          <template #default="{ row }">{{ resourceTypeText(row.resourceType) }}</template>
        </el-table-column>
        <el-table-column prop="maxDurationHours" label="最大时长(h)" />
        <el-table-column prop="advanceDays" label="提前天数" />
        <el-table-column prop="maxTimesPerDay" label="每日最大次数" />
        <el-table-column prop="autoApprove" label="自动审批">
          <template #default="{ row }">
            <el-tag :type="row.autoApprove ? 'success' : 'info'">{{ row.autoApprove ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="规则名称"><el-input v-model="form.ruleName" /></el-form-item>
        <el-form-item label="资源类型">
          <el-select v-model="form.resourceType" placeholder="请选择">
            <el-option label="实验室" value="LABORATORY" />
            <el-option label="仪器" value="INSTRUMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大时长(h)"><el-input-number v-model="form.maxDurationHours" :min="1" :max="24" /></el-form-item>
        <el-form-item label="提前天数"><el-input-number v-model="form.advanceDays" :min="1" /></el-form-item>
        <el-form-item label="每日最大次数"><el-input-number v-model="form.maxTimesPerDay" :min="1" /></el-form-item>
        <el-form-item label="自动审批"><el-switch v-model="form.autoApprove" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({})
const resourceTypeText = (t) => ({ LABORATORY: '实验室', INSTRUMENT: '仪器' }[t] || t || '—')

const loadData = async () => {
  const res = await request.get('/reservations/rules')
  if (res.code === 200) tableData.value = res.data
}
const handleAdd = () => { dialogTitle.value = '新增规则'; form.value = { status: 1, autoApprove: false, resourceType: 'LABORATORY' }; dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑规则'; form.value = { ...row }; dialogVisible.value = true }
const handleSave = async () => {
  const res = form.value.id ? await request.put(`/reservations/rules/${form.value.id}`, form.value) : await request.post('/reservations/rules', form.value)
  if (res.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; loadData() }
}
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/reservations/rules/${row.id}`)
    ElMessage.success('删除成功'); loadData()
  })
}
onMounted(() => loadData())
</script>
<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
