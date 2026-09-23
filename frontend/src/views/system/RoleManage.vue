<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between;align-items:center"><span>角色管理</span><el-button type="primary" @click="showDialog()">新增角色</el-button></div></template>
    <el-table :data="list" border stripe><el-table-column prop="id" label="ID" width="60"/><el-table-column prop="roleCode" label="角色编码"/><el-table-column prop="roleName" label="角色名称"/><el-table-column prop="description" label="描述"/><el-table-column prop="status" label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'">{{row.status===1?'启用':'禁用'}}</el-tag></template></el-table-column><el-table-column label="操作" width="250"><template #default="{row}"><el-button size="small" @click="showDialog(row)">编辑</el-button><el-button size="small" type="warning" @click="showPermDialog(row)">分配权限</el-button><el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button></template></el-table-column></el-table>
    <el-dialog v-model="dialogVisible" :title="editId?'编辑角色':'新增角色'" width="500px">
      <el-form :model="form" label-width="80px"><el-form-item label="编码"><el-input v-model="form.roleCode" :disabled="!!editId"/></el-form-item><el-form-item label="名称"><el-input v-model="form.roleName"/></el-form-item><el-form-item label="描述"><el-input v-model="form.description" type="textarea"/></el-form-item><el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0"/></el-form-item></el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></template>
    </el-dialog>
    <el-dialog v-model="permDialogVisible" title="分配权限" width="500px">
      <el-tree ref="treeRef" :data="permissions" show-checkbox node-key="id" :default-checked-keys="checkedPerms" :props="{label:'permissionName'}"/>
      <template #footer><el-button @click="permDialogVisible=false">取消</el-button><el-button type="primary" @click="savePerms">确定</el-button></template>
    </el-dialog>
  </el-card>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import request from '../../utils/request'
import {ElMessage,ElMessageBox} from 'element-plus'
const list=ref([]),dialogVisible=ref(false),editId=ref(null),permDialogVisible=ref(false),permissions=ref([]),checkedPerms=ref([]),currentRoleId=ref(null)
const form=reactive({roleCode:'',roleName:'',description:'',status:1})
function loadData(){request.get('/roles').then(r=>{list.value=r.data})}
function showDialog(row){editId.value=row?row.id:null;Object.assign(form,row||{roleCode:'',roleName:'',description:'',status:1});dialogVisible.value=true}
async function handleSave(){if(editId.value){await request.put('/roles/'+editId.value,form)}else{await request.post('/roles',form)}ElMessage.success('操作成功');dialogVisible.value=false;loadData()}
function handleDelete(id){ElMessageBox.confirm('确认删除?').then(async()=>{await request.delete('/roles/'+id);ElMessage.success('删除成功');loadData()}).catch(()=>{})}
async function showPermDialog(row){currentRoleId.value=row.id;const [pRes,cRes]=await Promise.all([request.get('/permissions'),request.get('/roles/'+row.id+'/permissions')]);permissions.value=buildTree(pRes.data);checkedPerms.value=cRes.data;permDialogVisible.value=true}
function buildTree(data){return data.filter(d=>!d.parentId||d.parentId===0).map(d=>({...d,children:data.filter(c=>c.parentId===d.id)}))}
async function savePerms(){const tree=treeRef.value;const ids=[...tree.getCheckedKeys(),...tree.getHalfCheckedKeys()];await request.post('/roles/'+currentRoleId.value+'/permissions',{permissionIds:ids});ElMessage.success('权限分配成功');permDialogVisible.value=false}
const treeRef=ref()
onMounted(()=>loadData())
</script>
