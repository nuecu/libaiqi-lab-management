<template>
  <div class="inventory-check">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存盘点管理</span>
          <el-button type="primary" @click="handleAdd">发起盘点</el-button>
        </div>
      </template>
      <el-table :data="tableData" border>
        <el-table-column prop="checkNo" label="盘点单号" />
        <el-table-column prop="checkName" label="盘点名称" />
        <el-table-column prop="consumableName" label="盘点耗材" />
        <el-table-column prop="checkerName" label="盘点人" />
        <el-table-column prop="checkDate" label="盘点日期" width="120" />
        <el-table-column prop="systemQuantity" label="系统数量" />
        <el-table-column prop="actualQuantity" label="实际数量" />
        <el-table-column prop="difference" label="差异" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'warning'">
              {{ { PENDING: '进行中', COMPLETED: '已完成' }[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看详情</el-button>
            <el-button v-if="row.status !== 'COMPLETED'" size="small" type="success" @click="handleComplete(row)">完成盘点</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" style="margin-top: 16px" />
    </el-card>
    <el-dialog v-model="dialogVisible" title="发起盘点" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="盘点名称"><el-input v-model="form.checkName" /></el-form-item>
        <el-form-item label="耗材">
          <el-select v-model="form.consumableId" placeholder="请选择" filterable>
            <el-option v-for="c in consumables" :key="c.id" :label="c.consumableName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="盘点日期"><el-date-picker v-model="form.checkDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="系统数量"><el-input-number v-model="form.systemQuantity" :min="0" /></el-form-item>
        <el-form-item label="实际数量"><el-input-number v-model="form.actualQuantity" :min="0" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">开始盘点</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="detailVisible" title="盘点详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="盘点单号">{{ detailRow.checkNo }}</el-descriptions-item>
        <el-descriptions-item label="盘点名称">{{ detailRow.checkName }}</el-descriptions-item>
        <el-descriptions-item label="盘点耗材">{{ detailRow.consumableName }}</el-descriptions-item>
        <el-descriptions-item label="盘点人">{{ detailRow.checkerName }}</el-descriptions-item>
        <el-descriptions-item label="盘点日期">{{ detailRow.checkDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ { PENDING: '进行中', COMPLETED: '已完成' }[detailRow.status] || detailRow.status }}</el-descriptions-item>
        <el-descriptions-item label="系统数量">{{ detailRow.systemQuantity }}</el-descriptions-item>
        <el-descriptions-item label="实际数量">{{ detailRow.actualQuantity }}</el-descriptions-item>
        <el-descriptions-item label="差异">{{ detailRow.difference }}</el-descriptions-item>
        <el-descriptions-item label="损耗原因">{{ detailRow.lossReason }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const consumables = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const form = ref({})
const detailRow = ref({})

const loadData = async () => {
  const res = await request.get('/experiments/inventory-checks', { params: { page: page.value - 1, size: size.value } })
  if (res.code === 200) { tableData.value = res.data; total.value = res.total || 0 }
}
const loadConsumables = async () => {
  const res = await request.get('/consumables')
  if (res.code === 200) consumables.value = res.data
}
const handleAdd = () => { form.value = {}; dialogVisible.value = true }
const handleSave = async () => {
  const res = await request.post('/experiments/inventory-checks', form.value)
  if (res.code === 200) { ElMessage.success('盘点已发起'); dialogVisible.value = false; loadData() }
}
const handleComplete = async (row) => {
  await request.put(`/experiments/inventory-checks/${row.id}/complete`)
  ElMessage.success('盘点已完成'); loadData()
}
const handleView = (row) => {
  detailRow.value = row
  detailVisible.value = true
}
onMounted(() => { loadData(); loadConsumables() })
</script>
<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
