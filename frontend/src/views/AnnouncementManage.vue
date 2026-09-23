<template>
  <div class="announcement-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button v-permission="'announcement:publish'" type="primary" @click="handleAdd">发布公告</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题"><el-input v-model="searchForm.title" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="已发布" :value="1" />
            <el-option label="草稿" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="handleView(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="announcementType" label="类型" width="120">
          <template #default="{ row }">{{ typeText(row.announcementType) }}</template>
        </el-table-column>
        <el-table-column prop="createByName" label="发布人" width="120">
          <template #default="{ row }">{{ row.createByName || row.createBy || '—' }}</template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag></template>
        </el-table-column>
        <el-table-column v-if="canPublish" label="操作" width="300">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status !== 1" size="small" type="success" @click="handlePublish(row)">发布</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" style="margin-top: 16px" />
    </el-card>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.announcementType">
            <el-option v-for="c in categories" :key="c.categoryCode" :label="c.categoryName" :value="c.categoryCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="是否置顶"><el-switch v-model="form.isTop" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">立即发布</el-radio>
            <el-radio :value="0">存为草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="公告详情" width="720px">
      <div class="detail">
        <h2 class="detail-title">{{ detail.title }}</h2>
        <div class="detail-meta">
          <el-tag size="small" effect="plain">{{ typeText(detail.announcementType) }}</el-tag>
          <el-tag size="small" :type="detail.status === 1 ? 'success' : 'info'">{{ detail.status === 1 ? '已发布' : '草稿' }}</el-tag>
          <span class="detail-meta-item">发布人：{{ detail.createByName || detail.createBy || '—' }}</span>
          <span class="detail-meta-item">发布时间：{{ detail.publishTime || '—' }}</span>
        </div>
        <el-divider />
        <div class="detail-content">{{ detail.content || '（暂无内容）' }}</div>
      </div>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
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
const canPublish = computed(() => permissionStore.hasPermission('announcement:publish'))

const searchForm = ref({ title: '', status: '' })
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({})
const detailVisible = ref(false)
const detail = ref({})
const categories = ref([])

const typeText = (t) => {
  const c = categories.value.find(x => x.categoryCode === t)
  return c ? c.categoryName : (t || '—')
}

const loadCategories = async () => {
  const res = await request.get('/announcement-categories/active').catch(() => null)
  if (res && res.code === 200) categories.value = res.data || []
}

const loadData = async () => {
  const params = { page: page.value - 1, size: size.value }
  if (searchForm.value.title) params.title = searchForm.value.title
  if (searchForm.value.status !== '' && searchForm.value.status !== null) params.status = searchForm.value.status
  const res = await request.get('/announcements', { params })
  if (res.code === 200) { tableData.value = res.data; total.value = res.total || 0 }
}
const handleSearch = () => { page.value = 1; loadData() }
const handleReset = () => { searchForm.value = { title: '', status: '' }; page.value = 1; loadData() }
const handleView = async (row) => {
  detail.value = { ...row }
  detailVisible.value = true
  const res = await request.get(`/announcements/${row.id}`)
  if (res.code === 200 && res.data) detail.value = res.data
}
const handleAdd = () => { dialogTitle.value = '发布公告'; form.value = { announcementType: (categories.value[0] && categories.value[0].categoryCode) || '', status: 1, isTop: false }; dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑公告'; form.value = { ...row }; dialogVisible.value = true }
const handleSave = async () => {
  if (!form.value.title) { ElMessage.warning('请输入标题'); return }
  const res = form.value.id
    ? await request.put(`/announcements/${form.value.id}`, form.value)
    : await request.post('/announcements', form.value)
  if (res.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; loadData() }
}
const handlePublish = async (row) => {
  ElMessageBox.confirm('确认发布该公告?', '提示', { type: 'info' }).then(async () => {
    const res = await request.put(`/announcements/${row.id}/publish`)
    if (res.code === 200) { ElMessage.success('已发布'); loadData() }
  })
}
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/announcements/${row.id}`)
    ElMessage.success('删除成功'); loadData()
  })
}
onMounted(() => { loadCategories(); loadData() })
</script>
<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
.detail-title { text-align: center; font-size: 20px; margin: 0 0 16px; }
.detail-meta { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; color: var(--el-text-color-secondary); font-size: 13px; }
.detail-meta-item { margin-left: 4px; }
.detail-content { white-space: pre-wrap; word-break: break-word; line-height: 1.8; color: var(--el-text-color-primary); }
</style>
