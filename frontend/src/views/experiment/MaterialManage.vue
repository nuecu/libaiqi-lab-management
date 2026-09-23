<template>
  <div class="material-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>耗材领用管理</span>
          <el-button type="primary" @click="handleAdd">新增领用申请</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="申请人"><el-input v-model="searchForm.applicantName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待审批" value="PENDING" />
            <el-option label="已批准" value="APPROVED" />
            <el-option label="已领取" value="COLLECTED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="page = 1; loadData()">搜索</el-button>
          <el-button @click="searchForm = {}; loadData()">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border>
        <el-table-column prop="applicantName" label="申请人" />
        <el-table-column prop="laboratoryName" label="实验室" />
        <el-table-column prop="applyTime" label="申请时间" width="160" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="{ PENDING: 'warning', APPROVED: 'success', COLLECTED: 'info', REJECTED: 'danger' }[row.status]">
              {{ { PENDING: '待审批', APPROVED: '已批准', COLLECTED: '已领取', REJECTED: '已拒绝' }[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">详情</el-button>
            <el-button v-permission="'experiment:approve'" v-if="row.status === 'PENDING'" size="small" type="success" @click="handleApprove(row)">批准</el-button>
            <el-button v-permission="'experiment:approve'" v-if="row.status === 'APPROVED'" size="small" type="primary" @click="handleCollect(row)">确认领取</el-button>
            <el-button v-permission="'experiment:approve'" v-if="row.status === 'PENDING'" size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" style="margin-top: 16px" />
    </el-card>
    <el-dialog v-model="detailVisible" title="领用详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请人">{{ detailData.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="实验室">{{ detailData.laboratoryName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detailData.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailData.status }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailData.items || []" border style="margin-top: 16px">
        <el-table-column prop="consumableName" label="耗材名称" />
        <el-table-column prop="specification" label="规格" />
        <el-table-column prop="requestQuantity" label="数量" />
        <el-table-column prop="unit" label="单位" />
      </el-table>
    </el-dialog>
    <el-dialog v-model="dialogVisible" title="新增领用申请" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="实验室"><el-select v-model="form.laboratoryId" placeholder="请选择"><el-option v-for="item in laboratories" :key="item.id" :label="item.labName" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
        <el-divider>领用耗材</el-divider>
        <div v-for="(item, index) in form.items" :key="index" style="display: flex; gap: 8px; margin-bottom: 8px">
          <el-select v-model="item.consumableId" placeholder="选择耗材" filterable style="flex: 2">
            <el-option v-for="c in consumables" :key="c.id" :label="c.consumableName" :value="c.id" />
          </el-select>
          <el-input-number v-model="item.quantity" :min="1" placeholder="数量" style="flex: 1" />
          <el-button type="danger" :icon="Delete" circle @click="form.items.splice(index, 1)" />
        </div>
        <el-button type="primary" link @click="form.items.push({ consumableId: null, quantity: 1 })">+ 添加耗材</el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

const searchForm = ref({})
const tableData = ref([])
const laboratories = ref([])
const consumables = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const form = ref({ items: [] })
const detailData = ref({})

const loadData = async () => {
  const res = await request.get('/experiments/requisitions', { params: { ...searchForm.value, page: page.value - 1, size: size.value } })
  if (res.code === 200) { tableData.value = res.data; total.value = res.total || 0 }
}
const loadOptions = async () => {
  const [lRes, cRes] = await Promise.all([request.get('/laboratories'), request.get('/consumables')])
  if (lRes.code === 200) laboratories.value = lRes.data
  if (cRes.code === 200) consumables.value = cRes.data
}
const handleAdd = () => { form.value = { items: [{ consumableId: null, quantity: 1 }] }; dialogVisible.value = true }
const handleSave = async () => {
  const res = await request.post('/experiments/requisitions', form.value)
  if (res.code === 200) { ElMessage.success('提交成功'); dialogVisible.value = false; loadData() }
}
const handleApprove = async (row) => {
  await request.put(`/experiments/requisitions/${row.id}/approve`)
  ElMessage.success('已批准'); loadData()
}
const handleReject = async (row) => {
  await request.put(`/experiments/requisitions/${row.id}/reject`)
  ElMessage.success('已拒绝'); loadData()
}
const handleCollect = async (row) => {
  await request.put(`/experiments/requisitions/${row.id}/collect`)
  ElMessage.success('已确认领取'); loadData()
}
const handleView = async (row) => {
  const res = await request.get(`/experiments/requisitions/${row.id}`)
  if (res.code === 200) detailData.value = res.data
  detailVisible.value = true
}
onMounted(() => { loadData(); loadOptions() })
</script>
<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
