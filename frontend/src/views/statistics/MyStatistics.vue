<template>
  <div class="my-statistics">
    <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
      <template #title>本页仅统计{{ userStore.userInfo?.realName || '我' }}本人的预约、实验与领用数据</template>
    </el-alert>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="我的预约总数" :value="stats.totalReservations"><template #suffix>次</template></el-statistic></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="本月预约" :value="stats.monthReservations"><template #suffix>次</template></el-statistic></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="我的实验记录" :value="stats.totalExperiments"><template #suffix>条</template></el-statistic></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="我的领用申请" :value="stats.totalRequisitions"><template #suffix>单</template></el-statistic></el-card></el-col>
    </el-row>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="12">
        <el-card>
          <template #header><span>我的预约趋势（近30天）</span></template>
          <div ref="trendChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>我的预约状态分布</span></template>
          <div ref="statusChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header><span>我的实验记录与预约通过率</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="预约通过率">{{ stats.approvalRate || '0.0%' }}</el-descriptions-item>
            <el-descriptions-item label="已完成预约">{{ stats.completed || 0 }}</el-descriptions-item>
            <el-descriptions-item label="待审批预约">{{ stats.pending || 0 }}</el-descriptions-item>
            <el-descriptions-item label="使用中预约">{{ stats.inUse || 0 }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>我的耗材领用情况</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="领用申请总数">{{ stats.totalRequisitions || 0 }}</el-descriptions-item>
            <el-descriptions-item label="待审批">{{ stats.reqPending || 0 }}</el-descriptions-item>
            <el-descriptions-item label="已批准">{{ stats.reqApproved || 0 }}</el-descriptions-item>
            <el-descriptions-item label="已发放">{{ stats.reqIssued || 0 }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const stats = ref({})
const trendChartRef = ref(null)
const statusChartRef = ref(null)
let charts = []

const loadStats = async () => {
  const res = await request.get('/statistics/my-overview')
  if (res.code === 200) stats.value = res.data || {}
}

const initCharts = async () => {
  await nextTick()
  const trendRes = await request.get('/statistics/my-trend').catch(() => ({ data: [] }))
  const trendData = trendRes.data || []
  const tChart = echarts.init(trendChartRef.value)
  tChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: trendData.map(d => d.date ? d.date.substring(5) : ''), axisLabel: { fontSize: 11, rotate: 45 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ data: trendData.map(d => d.count || 0), type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, itemStyle: { color: '#409EFF' } }]
  })
  charts.push(tChart)

  const s = stats.value
  const pieData = [
    { value: s.pending || 0, name: '待审批', itemStyle: { color: '#E6A23C' } },
    { value: s.approved || 0, name: '已批准', itemStyle: { color: '#67C23A' } },
    { value: s.inUse || 0, name: '使用中', itemStyle: { color: '#13C2C2' } },
    { value: s.completed || 0, name: '已完成', itemStyle: { color: '#409EFF' } },
    { value: s.rejected || 0, name: '已驳回', itemStyle: { color: '#F56C6C' } },
    { value: s.cancelled || 0, name: '已取消', itemStyle: { color: '#909399' } }
  ]
  const stChart = echarts.init(statusChartRef.value)
  stChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    series: [{ type: 'pie', radius: ['40%', '70%'], center: ['50%', '45%'], label: { show: false }, data: pieData }]
  })
  charts.push(stChart)
}

const handleResize = () => charts.forEach(c => c.resize())

onMounted(async () => {
  await loadStats()
  await initCharts()
  window.addEventListener('resize', handleResize)
})
onBeforeUnmount(() => {
  charts.forEach(c => c.dispose())
  window.removeEventListener('resize', handleResize)
})
</script>
<style scoped>
.my-statistics { padding: 4px; }
</style>
