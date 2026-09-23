<template>
  <div>
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; gap: 15px">
            <el-icon :size="40" :color="item.color"><component :is="item.icon" /></el-icon>
            <div><div style="font-size: 24px; font-weight: bold">{{ stats[item.key] || 0 }}</div><div style="color: #999; font-size: 14px">{{ item.title }}</div></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12"><el-card header="仪器状态分布"><div ref="chartRef1" style="height: 300px"></div></el-card></el-col>
      <el-col :span="12"><el-card header="预约状态统计"><div ref="chartRef2" style="height: 300px"></div></el-card></el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import request from '../utils/request'

const stats = ref({})
const chartRef1 = ref()
const chartRef2 = ref()
const statCards = [
  { title: '耗材总数', key: 'totalConsumables', icon: 'Box', color: '#409EFF' },
  { title: '仪器总数', key: 'totalInstruments', icon: 'Monitor', color: '#67C23A' },
  { title: '预约总数', key: 'totalReservations', icon: 'Calendar', color: '#E6A23C' },
  { title: '待审预约', key: 'pendingReservations', icon: 'Clock', color: '#F56C6C' }
]

onMounted(async () => {
  const res = await request.get('/statistics/dashboard')
  stats.value = res.data || {}
  initCharts()
})

function initCharts() {
  const chart1 = echarts.init(chartRef1.value)
  chart1.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '5%' },
    series: [{ type: 'pie', radius: ['40%', '70%'], data: [
      { value: stats.value.totalInstruments || 0, name: '空闲', itemStyle: { color: '#67C23A' } },
      { value: 0, name: '使用中', itemStyle: { color: '#409EFF' } },
      { value: 0, name: '故障', itemStyle: { color: '#F56C6C' } },
      { value: 0, name: '保养中', itemStyle: { color: '#E6A23C' } }
    ]}]
  })
  const chart2 = echarts.init(chartRef2.value)
  chart2.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['待审核', '已通过', '已驳回', '已取消', '已完成'] },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: [
      { value: stats.value.pendingReservations || 0, itemStyle: { color: '#E6A23C' } },
      { value: stats.value.approvedReservations || 0, itemStyle: { color: '#67C23A' } },
      { value: 0, itemStyle: { color: '#F56C6C' } },
      { value: 0, itemStyle: { color: '#909399' } },
      { value: 0, itemStyle: { color: '#409EFF' } }
    ]}]
  })
}
</script>
