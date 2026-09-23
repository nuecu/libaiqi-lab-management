<template>
  <div class="kb" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI 知识库管理（限定助手可回答的范围）</span>
          <el-button v-permission="'ai:knowledge:manage'" type="primary" @click="handleAdd">新增条目</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="按主题/关键词搜索" clearable style="width:240px" @keyup.enter="search" @clear="search" />
        <el-input v-model="query.category" placeholder="按分类筛选" clearable style="width:180px;margin-left:10px" @keyup.enter="search" @clear="search" />
        <el-button style="margin-left:10px" @click="search">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <el-alert type="warning" :closable="false" show-icon style="margin:10px 0"
        title="这里维护的条目就是 AI 被允许回答的全部系统知识范围；助手只会基于启用(ACTIVE)的条目作答，无关问题会被拒绝。" />

      <el-table :data="rows" border style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="question" label="主题/典型问题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="answer" label="标准答案" min-width="280" show-overflow-tooltip />
        <el-table-column prop="keywords" label="关键词" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canManage" label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 'ACTIVE' ? 'warning' : 'success'" @click="handleToggle(row)">
              {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pager" background layout="total, prev, pager, next, sizes"
                     :total="total" v-model:current-page="page" v-model:page-size="size"
                     :page-sizes="[10,20,50]" @size-change="loadData" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="分类"><el-input v-model="form.category" placeholder="如：预约管理、权限管理" /></el-form-item>
        <el-form-item label="主题/问题"><el-input v-model="form.question" placeholder="用户的典型问题，如：实验室怎么预约" /></el-form-item>
        <el-form-item label="标准答案"><el-input v-model="form.answer" type="textarea" :rows="5" placeholder="AI 被允许引用的权威答案" /></el-form-item>
        <el-form-item label="关键词"><el-input v-model="form.keywords" placeholder="检索关键词，逗号分隔，如：预约,实验室,申请" /></el-form-item>
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
const canManage = computed(() => permissionStore.hasPermission('ai:knowledge:manage'))

const rows = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = ref({ keyword: '', category: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/ai/knowledge', {
      params: { keyword: query.value.keyword || undefined, category: query.value.category || undefined, page: page.value - 1, size: size.value }
    })
    if (res.code === 200) { rows.value = res.data || []; total.value = res.total || 0 }
  } finally { loading.value = false }
}
const search = () => { page.value = 1; loadData() }
const resetSearch = () => { query.value = { keyword: '', category: '' }; page.value = 1; loadData() }

const handleAdd = () => { dialogTitle.value = '新增知识条目'; form.value = { category: '', question: '', answer: '', keywords: '', status: 'ACTIVE' }; dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑知识条目'; form.value = { ...row }; dialogVisible.value = true }

const handleSave = async () => {
  if (!form.value.question || !form.value.answer) { ElMessage.warning('请填写主题与答案'); return }
  const res = form.value.id
    ? await request.put(`/ai/knowledge/${form.value.id}`, form.value)
    : await request.post('/ai/knowledge', form.value)
  if (res.code === 200) { ElMessage.success(form.value.id ? '修改成功' : '新增成功'); dialogVisible.value = false; loadData() }
}

const handleToggle = async (row) => {
  const next = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const res = await request.put(`/ai/knowledge/${row.id}`, { ...row, status: next })
  if (res.code === 200) { ElMessage.success(next === 'ACTIVE' ? '已启用' : '已停用'); loadData() }
}
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除条目「${row.question}」？`, '提示', { type: 'warning' }).then(async () => {
    const res = await request.delete(`/ai/knowledge/${row.id}`)
    if (res.code === 200) { ElMessage.success('删除成功'); loadData() }
  })
}

onMounted(() => loadData())
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.toolbar { display: flex; align-items: center; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
