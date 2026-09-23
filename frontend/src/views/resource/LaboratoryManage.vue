<template>
  <div class="laboratory-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>实验室管理</span>
          <el-button v-permission="'resource:laboratory:manage'" type="primary" @click="handleAdd">新增实验室</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="实验室名称">
          <el-input v-model="searchForm.name" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="所属楼栋">
          <el-input v-model="searchForm.buildingName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="page = 1; loadData()">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="buildings" border style="margin-bottom: 20px" row-key="id">
        <el-table-column prop="buildingName" label="楼栋名称" />
        <el-table-column prop="buildingCode" label="楼栋编号" />
        <el-table-column prop="address" label="位置" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-permission="'resource:laboratory:manage'" size="small" @click="handleEditBuilding(row)">编辑</el-button>
            <el-button v-permission="'resource:laboratory:manage'" size="small" type="danger" @click="handleDeleteBuilding(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-table :data="tableData" border row-key="id">
        <el-table-column prop="labName" label="实验室名称" />
        <el-table-column prop="labCode" label="编号" />
        <el-table-column prop="buildingName" label="所属楼栋" />
        <el-table-column prop="roomNumber" label="房间号" />
        <el-table-column prop="capacity" label="容量" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button v-permission="'resource:laboratory:manage'" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-permission="'resource:laboratory:manage'" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" style="margin-top: 16px" />
    </el-card>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="实验室名称"><el-input v-model="form.labName" /></el-form-item>
        <el-form-item label="编号"><el-input v-model="form.labCode" /></el-form-item>
        <el-form-item label="所属楼栋"><el-input v-model="form.buildingId" /></el-form-item>
        <el-form-item label="房间号"><el-input v-model="form.roomNumber" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="1" /></el-form-item>
        <el-form-item label="面积(㎡)"><el-input-number v-model="form.areaSqm" :min="0" /></el-form-item>
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

const searchForm = ref({ name: '', buildingName: '' })
const tableData = ref([])
const buildings = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({})

const loadData = async () => {
  const res = await request.get('/laboratories', { params: { ...searchForm.value, page: page.value - 1, size: size.value } })
  if (res.code === 200) { tableData.value = res.data; total.value = res.total || 0 }
}
const loadBuildings = async () => {
  const res = await request.get('/laboratories/buildings')
  if (res.code === 200) buildings.value = res.data
}
const resetSearch = () => { searchForm.value = { name: '', buildingName: '' }; page.value = 1; loadData() }
const handleAdd = () => { dialogTitle.value = '新增实验室'; form.value = { status: 1 }; dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑实验室'; form.value = { ...row }; dialogVisible.value = true }
const handleSave = async () => {
  const res = form.value.id ? await request.put(`/laboratories/${form.value.id}`, form.value) : await request.post('/laboratories', form.value)
  if (res.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; loadData() }
}
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/laboratories/${row.id}`)
    ElMessage.success('删除成功'); loadData()
  })
}
const handleEditBuilding = (row) => { /* 编辑楼栋逻辑 */ }
const handleDeleteBuilding = (row) => { /* 删除楼栋逻辑 */ }

onMounted(() => { loadData(); loadBuildings() })
</script>
<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
