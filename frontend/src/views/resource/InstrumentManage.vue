<template>
  <el-card><template #header><div style="display:flex;justify-content:space-between;align-items:center"><span>仪器管理</span><el-button v-permission="'resource:instrument:manage'" type="primary" @click="showDialog()">新增仪器</el-button></div></template>
    <el-table :data="list" border stripe><el-table-column prop="instrumentCode" label="编码" width="100"/><el-table-column prop="instrumentName" label="名称"/><el-table-column prop="brand" label="品牌"/><el-table-column prop="model" label="型号"/><el-table-column prop="specification" label="规格"/><el-table-column prop="status" label="状态" width="90"><template #default="{row}"><el-tag :type="row.status==='IDLE'?'success':row.status==='FAULT'?'danger':row.status==='IN_USE'?'':'warning'">{{statusMap[row.status]||row.status}}</el-tag></template></el-table-column><el-table-column label="操作" width="200"><template #default="{row}"><el-button v-permission="'resource:instrument:manage'" size="small" @click="showDialog(row)">编辑</el-button><el-button v-permission="'resource:instrument:manage'" size="small" type="danger" @click="handleDelete(row.id)">删除</el-button></template></el-table-column></el-table>
    <el-pagination style="margin-top:15px" :current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" @current-change="p=>{page=p;loadData()}"/>
    <el-dialog v-model="dialogVisible" :title="editId?'编辑仪器':'新增仪器'" width="600px">
      <el-form :model="form" label-width="80px"><el-form-item label="编码"><el-input v-model="form.instrumentCode" :disabled="!!editId"/></el-form-item><el-form-item label="名称"><el-input v-model="form.instrumentName"/></el-form-item><el-form-item label="品牌"><el-input v-model="form.brand"/></el-form-item><el-form-item label="型号"><el-input v-model="form.model"/></el-form-item><el-form-item label="规格"><el-input v-model="form.specification" type="textarea"/></el-form-item><el-form-item label="状态"><el-select v-model="form.status"><el-option label="空闲" value="IDLE"/><el-option label="使用中" value="IN_USE"/><el-option label="故障" value="FAULT"/><el-option label="保养中" value="MAINTENANCE"/><el-option label="报废" value="SCRAPPED"/></el-select></el-form-item></el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></template>
    </el-dialog>
  </el-card>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import request from '../../utils/request'
import {ElMessage,ElMessageBox} from 'element-plus'
const list=ref([]),page=ref(1),size=ref(10),total=ref(0),dialogVisible=ref(false),editId=ref(null)
const statusMap={IDLE:'空闲',IN_USE:'使用中',FAULT:'故障',MAINTENANCE:'保养中',SCRAPPED:'报废'}
const form=reactive({instrumentCode:'',instrumentName:'',brand:'',model:'',specification:'',status:'IDLE'})
function loadData(){request.get('/instruments',{params:{page:page.value-1,size:size.value}}).then(r=>{list.value=r.data;total.value=r.total})}
function showDialog(row){editId.value=row?row.id:null;Object.assign(form,row||{instrumentCode:'',instrumentName:'',brand:'',model:'',specification:'',status:'IDLE'});dialogVisible.value=true}
async function handleSave(){if(editId.value){await request.put('/instruments/'+editId.value,form)}else{await request.post('/instruments',form)}ElMessage.success('操作成功');dialogVisible.value=false;loadData()}
function handleDelete(id){ElMessageBox.confirm('确认删除?').then(async()=>{await request.delete('/instruments/'+id);ElMessage.success('删除成功');loadData()}).catch(()=>{})}
onMounted(()=>loadData())
</script>
