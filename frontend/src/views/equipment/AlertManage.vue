<template>
  <div class="alert-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>安全预警管理</span>
          <el-button type="primary" @click="handleAdd">新增预警</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="预警类型">
          <el-select v-model="searchForm.alertType" placeholder="全部" clearable>
            <el-option label="库存不足" value="INVENTORY_LOW" />
            <el-option label="危化品异常" value="HAZARDOUS_OVERUSE" />
            <el-option label="仪器超时" value="INSTRUMENT_OVERTIME" />
            <el-option label="未签到" value="NO_SHOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="级别">
          <el-select v-model="searchForm.alertLevel" placeholder="全部" clearable>
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="page = 1; loadData()">搜索</el-button>
          <el-button @click="searchForm = {}; loadData()">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border>
        <el-table-column prop="alertContent" label="预警内容" show-overflow-tooltip />
        <el-table-column prop="alertType" label="类型">
          <template #default="{ row }">{{ { INVENTORY_LOW: '库存不足', HAZARDOUS_OVERUSE: '危化品异常', INSTRUMENT_OVERTIME: '仪器超时', NO_SHOW: '未签到' }[row.alertType] || row.alertType }}</template>
        </el-table-column>
        <el-table-column prop="alertLevel" label="级别">
          <template #default="{ row }">
            <el-tag :type="{ LOW: 'info', MEDIUM: 'warning', HIGH: 'danger', CRITICAL: 'danger' }[row.alertLevel]">{{ { LOW: '低', MEDIUM: '中', HIGH: '高', CRITICAL: '紧急' }[row.alertLevel] || row.alertLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预警时间" width="160" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }"><el-tag :type="row.status === 'RESOLVED' ? 'success' : 'danger'">{{ { ACTIVE: '未处理', ACKNOWLEDGED: '已确认', RESOLVED: '已处理' }[row.status] || row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status !== 'RESOLVED'" size="small" type="success" @click="handleResolve(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" style="margin-top: 16px" />
    </el-card>
    <el-dialog v-model="dialogVisible" title="新增预警" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="预警内容"><el-input v-model="form.alertContent" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.alertType"><el-option label="库存不足" value="INVENTORY_LOW" /><el-option label="危化品异常" value="HAZARDOUS_OVERUSE" /><el-option label="仪器超时" value="INSTRUMENT_OVERTIME" /><el-option label="未签到" value="NO_SHOW" /></el-select>
        </el-form-item>
        <el-form-item label="级别">
          <el-select v-model="form.alertLevel"><el-option label="低" value="LOW" /><el-option label="中" value="MEDIUM" /><el-option label="高" value="HIGH" /><el-option label="紧急" value="CRITICAL" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="detailVisible" title="预警详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="预警内容">{{ detailData.alertContent }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detailData.alertType }}</el-descriptions-item>
        <el-descriptions-item label="级别">{{ detailData.alertLevel }}</el-descriptions-item>
        <el-descriptions-item label="预警时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ detailData.resolveTime }}</el-descriptions-item>
        <el-descriptions-item label="处理结果">{{ detailData.remark }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ { ACTIVE: '未处理', ACKNOWLEDGED: '已确认', RESOLVED: '已处理' }[detailData.status] || detailData.status }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const searchForm = ref({})
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const form = ref({})
const detailData = ref({})

const loadData = async () => {
  const res = await request.get('/equipment/alerts', { params: { ...searchForm.value, page: page.value - 1, size: size.value } })
  if (res.code === 200) { tableData.value = res.data; total.value = res.total || 0 }
}
const handleAdd = () => { form.value = { alertType: 'INVENTORY_LOW', alertLevel: 'MEDIUM' }; dialogVisible.value = true }
const handleSave = async () => {
  const res = await request.post('/equipment/alerts', form.value)
  if (res.code === 200) { ElMessage.success('预警创建成功'); dialogVisible.value = false; loadData() }
}
const handleResolve = (row) => {
  ElMessageBox.prompt('请输入处理结果', '处理预警', { type: 'info' }).then(async ({ value }) => {
    await request.put(`/equipment/alerts/${row.id}/resolve`, { resolveResult: value })
    ElMessage.success('已处理'); loadData()
  })
}
const handleView = (row) => { detailData.value = row; detailVisible.value = true }
onMounted(() => loadData())
</script>
<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
