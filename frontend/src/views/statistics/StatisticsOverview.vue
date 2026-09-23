<template>
  <div class="statistics-overview">
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="仪器总数" :value="stats.instrumentCount"><template #suffix>台</template></el-statistic></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="耗材种类" :value="stats.consumableCount"><template #suffix>种</template></el-statistic></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="本月预约" :value="stats.monthReservations"><template #suffix>次</template></el-statistic></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="实验室数量" :value="stats.laboratoryCount"><template #suffix>间</template></el-statistic></el-card></el-col>
    </el-row>
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="12">
        <el-card>
          <template #header><span>预约趋势（近30天）</span></template>
          <div ref="reservationChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>仪器使用率排行</span></template>
          <div ref="usageChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header><span>耗材领用统计</span></template>
          <div ref="consumableChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>预约状态分布</span></template>
          <div ref="labChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>数据导出</span>
        </div>
      </template>
      <el-space>
        <el-button type="primary" @click="exportData('reservations')">导出预约报表</el-button>
        <el-button type="success" @click="exportData('instruments')">导出仪器报表</el-button>
        <el-button type="warning" @click="exportData('consumables')">导出耗材报表</el-button>
      </el-space>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import request from '@/utils/request'

const stats = ref({ instrumentCount: 0, consumableCount: 0, monthReservations: 0, laboratoryCount: 0 })
const reservationChartRef = ref(null)
const usageChartRef = ref(null)
const consumableChartRef = ref(null)
const labChartRef = ref(null)
let charts = []

const loadStats = async () => {
  const res = await request.get('/statistics/dashboard')
  if (res.code === 200) {
    stats.value = {
      instrumentCount: res.data.totalInstruments || 0,
      consumableCount: res.data.totalConsumables || 0,
      monthReservations: res.data.monthReservations || 0,
      laboratoryCount: res.data.totalLaboratories || 0
    }
  }
}

const initCharts = async () => {
  await nextTick()
  // 预约趋势（近30天）- 使用真实数据
  const trendRes = await request.get('/statistics/reservation-trend').catch(() => ({ data: [] }))
  const trendData = trendRes.data || []
  const rChart = echarts.init(reservationChartRef.value)
  rChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: trendData.map(d => d.date ? d.date.substring(5) : ''), axisLabel: { fontSize: 11, rotate: 45 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ data: trendData.map(d => d.count || 0), type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, itemStyle: { color: '#409EFF' } }]
  })
  charts.push(rChart)

  // 仪器使用率 - 使用instrument stats代替
  const uChart = echarts.init(usageChartRef.value)
  const uRes = await request.get('/statistics/instruments')
  const uData = uRes.code === 200 ? uRes.data : {}
  uChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: ['空闲', '使用中', '故障', '维护中'] },
    series: [{ data: [uData.idle || 0, uData.inUse || 0, uData.fault || 0, uData.maintenance || 0], type: 'bar' }]
  })
  charts.push(uChart)

  // 耗材领用 - 使用consumable stats代替
  const cChart = echarts.init(consumableChartRef.value)
  const cRes = await request.get('/statistics/consumables')
  const cData = cRes.code === 200 ? cRes.data : {}
  cChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', data: [
      { name: '总类型', value: cData.totalTypes || 0 },
      { name: '低库存', value: cData.lowStockCount || 0 },
      { name: '高库存', value: cData.overStockCount || 0 }
    ], radius: ['40%', '70%'] }]
  })
  charts.push(cChart)

  // 预约状态分布 - 替代实验室使用率
  const resvRes = await request.get('/statistics/reservations').catch(() => ({ data: {} }))
  const resvData = resvRes.data || {}
  const lChart = echarts.init(labChartRef.value)
  lChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    series: [{
      type: 'pie', radius: ['40%', '70%'], center: ['50%', '45%'],
      label: { show: false },
      data: [
        { value: resvData.pending || 0, name: '待审核', itemStyle: { color: '#E6A23C' } },
        { value: resvData.approved || 0, name: '已批准', itemStyle: { color: '#67C23A' } },
        { value: resvData.inUse || 0, name: '使用中', itemStyle: { color: '#13C2C2' } },
        { value: resvData.rejected || 0, name: '已驳回', itemStyle: { color: '#F56C6C' } },
        { value: resvData.cancelled || 0, name: '已取消', itemStyle: { color: '#909399' } },
        { value: resvData.completed || 0, name: '已完成', itemStyle: { color: '#409EFF' } }
      ]
    }]
  })
  charts.push(lChart)
}

const exportData = async (type) => {
  ElMessage.info('导出功能开发中...')
}

const handleResize = () => { charts.forEach(c => c.resize()) }

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
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
