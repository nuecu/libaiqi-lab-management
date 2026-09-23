<template>
  <div class="announcement-category" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告分类管理</span>
          <el-button v-permission="'announcement:category'" type="primary" @click="handleAdd">新增分类</el-button>
        </div>
      </template>
      <el-alert v-if="!loading && categories.length === 0" type="info" :closable="false"
                title="暂无分类数据，请点击「新增分类」创建" show-icon style="margin-bottom: 12px" />
      <el-table :data="categories" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="categoryName" label="分类名称" />
        <el-table-column prop="categoryCode" label="分类编码" width="160" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="count" label="公告数量" width="120">
          <template #default="{ row }">
            <el-tag type="info">{{ row.count || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canEdit" label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 'ACTIVE' ? 'warning' : 'success'" @click="handleToggle(row)">
              {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编码">
          <el-input v-model="form.categoryCode" placeholder="如：NOTICE、SAFETY" :disabled="!!form.id" />
          <div class="form-tip" v-if="!form.id">编码作为公告类型标识，创建后不可修改</div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入分类描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" active-value="ACTIVE" inactive-value="INACTIVE" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { usePermissionStore } from '@/stores/permission'

const permissionStore = usePermissionStore()
const canEdit = computed(() => permissionStore.hasPermission('announcement:category'))

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/announcement-categories')
    if (res.code === 200) categories.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增分类'
  form.value = { categoryName: '', categoryCode: '', description: '', status: 'ACTIVE' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑分类'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.categoryName || !form.value.categoryCode) {
    ElMessage.warning('请填写完整信息')
    return
  }
  const res = form.value.id
    ? await request.put(`/announcement-categories/${form.value.id}`, form.value)
    : await request.post('/announcement-categories', form.value)
  if (res.code === 200) {
    ElMessage.success(form.value.id ? '修改成功' : '新增成功')
    dialogVisible.value = false
    loadData()
  }
}

const handleToggle = async (row) => {
  const next = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const res = await request.put(`/announcement-categories/${row.id}`, { ...row, status: next })
  if (res.code === 200) { ElMessage.success(next === 'ACTIVE' ? '已启用' : '已停用'); loadData() }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除分类「${row.categoryName}」？`, '提示', { type: 'warning' }).then(async () => {
    const res = await request.delete(`/announcement-categories/${row.id}`)
    if (res.code === 200) { ElMessage.success('删除成功'); loadData() }
  })
}

onMounted(() => loadData())
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.form-tip { font-size: 12px; color: var(--el-text-color-secondary); line-height: 1.4; margin-top: 4px; }
</style>
