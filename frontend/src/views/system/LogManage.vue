<template>
  <el-card><template #header>日志管理</template>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="操作日志" name="operation">
        <el-table :data="opList" border stripe><el-table-column prop="username" label="用户"/><el-table-column prop="operation" label="操作"/><el-table-column prop="method" label="方法"/><el-table-column prop="ip" label="IP"/><el-table-column prop="status" label="状态" width="80"/><el-table-column prop="costTime" label="耗时(ms)"/><el-table-column prop="createTime" label="时间"/></el-table>
        <el-pagination style="margin-top:15px" :current-page="opPage" :total="opTotal" layout="total, prev, pager, next" @current-change="p=>{opPage=p;loadOpLogs()}"/>
      </el-tab-pane>
      <el-tab-pane label="登录日志" name="login">
        <el-table :data="loginList" border stripe><el-table-column prop="username" label="用户"/><el-table-column prop="ip" label="IP"/><el-table-column prop="status" label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'">{{row.status===1?'成功':'失败'}}</el-tag></template></el-table-column><el-table-column prop="msg" label="消息"/><el-table-column prop="loginTime" label="时间"/></el-table>
        <el-pagination style="margin-top:15px" :current-page="loginPage" :total="loginTotal" layout="total, prev, pager, next" @current-change="p=>{loginPage=p;loadLoginLogs()}"/>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>
<script setup>
import {ref,onMounted,watch} from 'vue'
import request from '../../utils/request'
const activeTab=ref('operation'),opList=ref([]),opPage=ref(1),opTotal=ref(0),loginList=ref([]),loginPage=ref(1),loginTotal=ref(0)
function loadOpLogs(){request.get('/logs/operation',{params:{page:opPage.value-1,size:10}}).then(r=>{opList.value=r.data;opTotal.value=r.total})}
function loadLoginLogs(){request.get('/logs/login',{params:{page:loginPage.value-1,size:10}}).then(r=>{loginList.value=r.data;loginTotal.value=r.total})}
watch(activeTab,v=>{v==='operation'?loadOpLogs():loadLoginLogs()})
onMounted(()=>{loadOpLogs();loadLoginLogs()})
</script>
