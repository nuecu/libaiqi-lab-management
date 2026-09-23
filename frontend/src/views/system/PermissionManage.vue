<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between;align-items:center"><span>权限管理</span><el-button type="primary" @click="showDialog()">新增权限</el-button></div></template>
    <el-table :data="treeData" row-key="id" border :tree-props="{children:'children'}"><el-table-column prop="permissionName" label="权限名称" width="200"/><el-table-column prop="permissionCode" label="权限编码"/><el-table-column prop="permissionType" label="类型" width="80"/><el-table-column prop="path" label="路由路径"/><el-table-column prop="component" label="组件"/><el-table-column prop="icon" label="图标" width="80"/><el-table-column prop="sortOrder" label="排序" width="70"/><el-table-column label="操作" width="200"><template #default="{row}"><el-button size="small" @click="showDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button></template></el-table-column></el-table>
    <el-dialog v-model="dialogVisible" :title="editId?'编辑权限':'新增权限'" width="500px">
      <el-form :model="form" label-width="80px"><el-form-item label="名称"><el-input v-model="form.permissionName"/></el-form-item><el-form-item label="编码"><el-input v-model="form.permissionCode"/></el-form-item><el-form-item label="类型"><el-select v-model="form.permissionType"><el-option label="菜单" value="MENU"/><el-option label="按钮" value="BUTTON"/><el-option label="接口" value="API"/></el-select></el-form-item><el-form-item label="父级ID"><el-input-number v-model="form.parentId" :min="0"/></el-form-item><el-form-item label="路由"><el-input v-model="form.path"/></el-form-item><el-form-item label="组件"><el-input v-model="form.component"/></el-form-item><el-form-item label="图标"><el-input v-model="form.icon"/></el-form-item><el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0"/></el-form-item></el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></template>
    </el-dialog>
  </el-card>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import request from '../../utils/request'
import {ElMessage,ElMessageBox} from 'element-plus'
const treeData=ref([]),dialogVisible=ref(false),editId=ref(null)
const form=reactive({permissionName:'',permissionCode:'',permissionType:'MENU',parentId:0,path:'',component:'',icon:'',sortOrder:0})
function loadData(){request.get('/permissions').then(r=>{treeData.value=buildTree(r.data)})}
function buildTree(data){return data.filter(d=>!d.parentId||d.parentId===0).map(d=>({...d,children:data.filter(c=>c.parentId===d.id)}))}
function showDialog(row){editId.value=row?row.id:null;Object.assign(form,row||{permissionName:'',permissionCode:'',permissionType:'MENU',parentId:0,path:'',component:'',icon:'',sortOrder:0});dialogVisible.value=true}
async function handleSave(){if(editId.value){await request.put('/permissions/'+editId.value,form)}else{await request.post('/permissions',form)}ElMessage.success('操作成功');dialogVisible.value=false;loadData()}
function handleDelete(id){ElMessageBox.confirm('确认删除?').then(async()=>{await request.delete('/permissions/'+id);ElMessage.success('删除成功');loadData()}).catch(()=>{})}
onMounted(()=>loadData())
</script>
