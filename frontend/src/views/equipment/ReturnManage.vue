<template>
  <div class="return-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>耗材归还</span>
          <el-button type="primary" @click="handleAdd">新增归还</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待处理" value="PENDING" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="page = 1; loadData()">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border stripe>
        <el-table-column prop="returnNo" label="归还单号" width="180" />
        <el-table-column prop="userName" label="归还人" />
        <el-table-column prop="consumableName" label="耗材名称" />
        <el-table-column prop="returnQuantity" label="归还数量" width="100" />
        <el-table-column prop="returnType" label="归还类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.returnType === 'REMAINING' ? '剩余归还' : '未用归还' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'warning'">{{ row.status === 'COMPLETED' ? '已完成' : '待处理' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiverName" label="接收人" />
        <el-table-column prop="createTime" label="归还时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="handleComplete(row)">完成</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" style="margin-top: 16px" />
    </el-card>
    <el-dialog v-model="dialogVisible" title="新增归还" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="耗材">
          <el-select v-model="form.consumableId" placeholder="请选择耗材" filterable>
            <el-option v-for="item in consumables" :key="item.id" :label="item.consumableName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="归还数量"><el-input-number v-model="form.returnQuantity" :min="1" /></el-form-item>
        <el-form-item label="归还类型">
          <el-select v-model="form.returnType" placeholder="请选择">
            <el-option label="剩余归还" value="REMAINING" />
            <el-option label="未用归还" value="UNUSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
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
import request from '@/utils/request'

const searchForm = ref({ status: '' })
const tableData = ref([])
const consumables = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const form = ref({})

const loadData = async () => {
  const params = { page: page.value - 1, size: size.value }
  if (searchForm.value.status) params.status = searchForm.value.status
  const res = await request.get('/material-returns', { params })
  if (res.code === 200) { tableData.value = res.data; total.value = res.total || 0 }
}
const loadConsumables = async () => {
  const res = await request.get('/consumables', { params: { page: 0, size: 1000 } })
  if (res.code === 200) consumables.value = res.data
}
const resetSearch = () => { searchForm.value = { status: "" }; page.value = 1; loadData() }
const handleAdd = () => { form.value = { returnType: 'REMAINING', returnQuantity: 1 }; dialogVisible.value = true }
const handleSave = async () => {
  const res = await request.post('/material-returns', form.value)
  if (res.code === 200) { ElMessage.success('归还提交成功'); dialogVisible.value = false; loadData() }
}
const handleComplete = (row) => {
  ElMessageBox.confirm('确认完成归还?', '提示', { type: 'info' }).then(async () => {
    await request.post(`/material-returns/${row.id}/complete`)
    ElMessage.success('已完成'); loadData()
  })
}
const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/material-returns/${id}`)
    ElMessage.success('删除成功'); loadData()
  })
}

onMounted(() => { loadData(); loadConsumables() })
</script>
<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
