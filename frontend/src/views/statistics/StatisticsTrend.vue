<template>
  <div class="statistics-trend" v-loading="loading">
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>预约趋势分析</span>
              <el-radio-group v-model="trendRange" size="small" @change="loadAll">
                <el-radio-button label="week">近7天</el-radio-button>
                <el-radio-button label="month">近30天</el-radio-button>
                <el-radio-button label="quarter">近90天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="reservationTrendRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="12">
        <el-card>
          <template #header><span>耗材消耗趋势</span></template>
          <div ref="consumableTrendRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>实验次数趋势</span></template>
          <div ref="experimentTrendRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header><span>仪器使用率趋势</span></template>
          <div ref="instrumentTrendRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>审批效率趋势</span></template>
          <div ref="approvalTrendRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const trendRange = ref('month')
const loading = ref(false)
const reservationTrendRef = ref(null)
const consumableTrendRef = ref(null)
const experimentTrendRef = ref(null)
const instrumentTrendRef = ref(null)
const approvalTrendRef = ref(null)
let charts = []

// 将 yyyy-MM-dd 简化为 MM-DD 作为 x 轴标签
const fmtLabels = (dates) => (dates || []).map(d => (d && d.length >= 10 ? d.slice(5) : d))
const nz = (arr) => (Array.isArray(arr) ? arr : [])

const ensureChart = (el) => {
  if (!el) return null
  return echarts.getInstanceByDom(el) || echarts.init(el)
}

const render = (data) => {
  const labels = fmtLabels(data.dates)

  const c1 = ensureChart(reservationTrendRef.value)
  if (c1) c1.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增预约', '已完成', '已取消'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: labels, boundaryGap: false },
    yAxis: { type: 'value' },
    series: [
      { name: '新增预约', type: 'line', smooth: true, data: nz(data.newReservations), areaStyle: { opacity: 0.15 } },
      { name: '已完成', type: 'line', smooth: true, data: nz(data.completed), areaStyle: { opacity: 0.15 } },
      { name: '已取消', type: 'line', smooth: true, data: nz(data.cancelled), areaStyle: { opacity: 0.1 } }
    ]
  }, true)

  const c2 = ensureChart(consumableTrendRef.value)
  if (c2) c2.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['领用数量', '归还数量'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value' },
    series: [
      { name: '领用数量', type: 'bar', data: nz(data.issueQty), itemStyle: { color: '#409EFF' } },
      { name: '归还数量', type: 'bar', data: nz(data.returnQty), itemStyle: { color: '#67C23A' } }
    ]
  }, true)

  const c3 = ensureChart(experimentTrendRef.value)
  if (c3) c3.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value' },
    series: [{
      name: '实验次数', type: 'line', smooth: true, data: nz(data.experiments),
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#f56c6c' }, { offset: 1, color: 'rgba(245,108,108,0.05)' }]) },
      lineStyle: { color: '#f56c6c' }, itemStyle: { color: '#f56c6c' }
    }]
  }, true)

  const c4 = ensureChart(instrumentTrendRef.value)
  if (c4) c4.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>{a}: {c}%' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{
      name: '使用率', type: 'line', smooth: true, data: nz(data.instrumentRate),
      markLine: { data: [{ type: 'average', name: '平均值' }] }
    }]
  }, true)

  const c5 = ensureChart(approvalTrendRef.value)
  if (c5) c5.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['平均审批时长(小时)', '通过率(%)'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: labels },
    yAxis: [
      { type: 'value', name: '时长(h)' },
      { type: 'value', name: '通过率(%)', max: 100 }
    ],
    series: [
      { name: '平均审批时长(小时)', type: 'bar', data: nz(data.approvalHours), itemStyle: { color: '#E6A23C' } },
      { name: '通过率(%)', type: 'line', yAxisIndex: 1, smooth: true, data: nz(data.passRate), itemStyle: { color: '#67C23A' } }
    ]
  }, true)

  charts = [c1, c2, c3, c4, c5].filter(Boolean)
}

const loadAll = async () => {
  loading.value = true
  try {
    const res = await request.get('/statistics/trend-analysis', { params: { range: trendRange.value } })
    if (res.code === 200) render(res.data || {})
  } finally {
    loading.value = false
  }
}

const handleResize = () => { charts.forEach(c => c.resize()) }

onMounted(async () => {
  await nextTick()
  await loadAll()
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
