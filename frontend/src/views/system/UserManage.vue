<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between;align-items:center"><span>用户管理</span><el-button type="primary" @click="showDialog()">新增用户</el-button></div></template>
    <el-form :inline="true" style="margin-bottom:15px"><el-form-item><el-input v-model="keyword" placeholder="搜索用户名/姓名" clearable @clear="page = 1; loadData()" @keyup.enter="page = 1; loadData()"/></el-form-item><el-form-item><el-button type="primary" @click="page = 1; loadData()">搜索</el-button></el-form-item></el-form>
    <el-table :data="list" border stripe><el-table-column prop="id" label="ID" width="60"/><el-table-column prop="username" label="用户名"/><el-table-column prop="realName" label="姓名"/><el-table-column prop="userType" label="类型"/><el-table-column prop="department" label="院系"/><el-table-column prop="phone" label="电话"/><el-table-column prop="status" label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'">{{row.status===1?'启用':'禁用'}}</el-tag></template></el-table-column><el-table-column label="操作" width="200"><template #default="{row}"><el-button size="small" @click="showDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button></template></el-table-column></el-table>
    <el-pagination style="margin-top:15px" :current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" @current-change="p=>{page=p;loadData()}"/>
    <el-dialog v-model="dialogVisible" :title="editId?'编辑用户':'新增用户'" width="500px">
      <el-form :model="form" label-width="80px"><el-form-item label="用户名"><el-input v-model="form.username" :disabled="!!editId"/></el-form-item><el-form-item label="密码"><el-input v-model="form.password" type="password" show-password/></el-form-item><el-form-item label="姓名"><el-input v-model="form.realName"/></el-form-item><el-form-item label="类型"><el-select v-model="form.userType"><el-option label="管理员" value="ADMIN"/><el-option label="教师" value="TEACHER"/><el-option label="学生" value="STUDENT"/><el-option label="维护人员" value="MAINTAINER"/></el-select></el-form-item><el-form-item label="院系"><el-input v-model="form.department"/></el-form-item><el-form-item label="电话"><el-input v-model="form.phone"/></el-form-item><el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0"/></el-form-item></el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></template>
    </el-dialog>
  </el-card>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import request from '../../utils/request'
import {ElMessage,ElMessageBox} from 'element-plus'
const list=ref([]),page=ref(1),size=ref(10),total=ref(0),keyword=ref(''),dialogVisible=ref(false),editId=ref(null)
const form=reactive({username:'',password:'',realName:'',userType:'STUDENT',department:'',phone:'',status:1})
function loadData(){request.get('/users',{params:{keyword:keyword.value,page:page.value-1,size:size.value}}).then(r=>{list.value=r.data;total.value=r.total})}
function showDialog(row){editId.value=row?row.id:null;Object.assign(form,row||{username:'',password:'',realName:'',userType:'STUDENT',department:'',phone:'',status:1});dialogVisible.value=true}
async function handleSave(){if(editId.value){await request.put('/users/'+editId.value,form)}else{await request.post('/users',form)}ElMessage.success('操作成功');dialogVisible.value=false;loadData()}
function handleDelete(id){ElMessageBox.confirm('确认删除?').then(async()=>{await request.delete('/users/'+id);ElMessage.success('删除成功');loadData()}).catch(()=>{})}
onMounted(()=>loadData())
</script>
