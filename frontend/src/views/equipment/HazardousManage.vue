<template>
  <div class="hazardous-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>危化品管控</span>
          <el-button type="primary" @click="handleAdd">新增记录</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="物品名称"><el-input v-model="searchForm.materialName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.operationType" placeholder="全部" clearable>
            <el-option label="领用" value="ISSUE" />
            <el-option label="归还" value="RETURN" />
            <el-option label="报废" value="DISPOSE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="page = 1; loadData()">搜索</el-button>
          <el-button @click="searchForm = {}; loadData()">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border>
        <el-table-column prop="materialName" label="物品名称" />
        <el-table-column prop="operationType" label="操作类型">
          <template #default="{ row }">{{ { ISSUE: '领用', RETURN: '归还', DISPOSE: '报废' }[row.operationType] || row.operationType }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" />
        <el-table-column prop="unit" label="单位" />
        <el-table-column prop="operatorName" label="操作人" />
        <el-table-column prop="operationTime" label="操作时间" width="160" />
        <el-table-column prop="storageLocation" label="存放位置" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" style="margin-top: 16px" />
    </el-card>
    <el-dialog v-model="dialogVisible" title="新增危化品记录" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="耗材">
          <el-select v-model="form.consumableId" placeholder="请选择危化品" filterable>
            <el-option v-for="c in hazardousConsumables" :key="c.id" :label="c.consumableName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="form.operationType"><el-option label="领用" value="ISSUE" /><el-option label="归还" value="RETURN" /><el-option label="报废" value="DISPOSE" /></el-select>
        </el-form-item>
        <el-form-item label="数量"><el-input-number v-model="form.quantity" :min="0" /></el-form-item>
        <el-form-item label="双人确认"><el-switch v-model="form.dualConfirm" /></el-form-item>
        <el-form-item label="资质校验"><el-switch v-model="form.qualificationVerified" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="detailVisible" title="危化品详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="物品名称">{{ detailData.materialName }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ { ISSUE: '领用', RETURN: '归还', DISPOSE: '报废' }[detailData.operationType] || detailData.operationType }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ detailData.quantity }} {{ detailData.unit }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detailData.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detailData.operationTime }}</el-descriptions-item>
        <el-descriptions-item label="存放位置">{{ detailData.storageLocation }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const searchForm = ref({})
const tableData = ref([])
const hazardousConsumables = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const form = ref({})
const detailData = ref({})

const loadData = async () => {
  const res = await request.get('/equipment/hazardous-records', { params: { ...searchForm.value, page: page.value - 1, size: size.value } })
  if (res.code === 200) { tableData.value = res.data; total.value = res.total || 0 }
}
const loadConsumables = async () => {
  const res = await request.get('/consumables', { params: { isHazardous: true } })
  if (res.code === 200) hazardousConsumables.value = res.data
}
const handleAdd = () => { form.value = { operationType: 'ISSUE', dualConfirm: false, qualificationVerified: false }; dialogVisible.value = true }
const handleSave = async () => {
  const res = await request.post('/equipment/hazardous-records', form.value)
  if (res.code === 200) { ElMessage.success('记录创建成功'); dialogVisible.value = false; loadData() }
}
const handleView = (row) => { detailData.value = row; detailVisible.value = true }
onMounted(() => { loadData(); loadConsumables() })
</script>
<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
