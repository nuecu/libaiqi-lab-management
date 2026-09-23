<template>
  <el-card><template #header><div style="display:flex;justify-content:space-between;align-items:center"><span>耗材管理</span><el-button v-permission="'resource:consumable:manage'" type="primary" @click="showDialog()">新增耗材</el-button></div></template>
    <el-form :inline="true" style="margin-bottom:15px"><el-form-item><el-input v-model="keyword" placeholder="搜索耗材名称" clearable @clear="page = 1; loadData()" @keyup.enter="page = 1; loadData()"/></el-form-item><el-form-item><el-button type="primary" @click="page = 1; loadData()">搜索</el-button></el-form-item></el-form>
    <el-table :data="list" border stripe><el-table-column prop="consumableCode" label="编码" width="100"/><el-table-column prop="consumableName" label="名称"/><el-table-column prop="specification" label="规格"/><el-table-column prop="unit" label="单位" width="60"/><el-table-column prop="brand" label="品牌"/><el-table-column prop="currentStock" label="库存" width="70"/><el-table-column prop="minStock" label="预警值" width="70"/><el-table-column prop="isHazardous" label="危化品" width="80"><template #default="{row}"><el-tag :type="row.isHazardous?'danger':'info'">{{row.isHazardous?'是':'否'}}</el-tag></template></el-table-column><el-table-column label="操作" width="200"><template #default="{row}"><el-button v-permission="'resource:consumable:manage'" size="small" @click="showDialog(row)">编辑</el-button><el-button v-permission="'resource:consumable:manage'" size="small" type="danger" @click="handleDelete(row.id)">删除</el-button></template></el-table-column></el-table>
    <el-pagination style="margin-top:15px" :current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" @current-change="p=>{page=p;loadData()}"/>
    <el-dialog v-model="dialogVisible" :title="editId?'编辑耗材':'新增耗材'" width="600px">
      <el-form :model="form" label-width="80px"><el-form-item label="编码"><el-input v-model="form.consumableCode" :disabled="!!editId"/></el-form-item><el-form-item label="名称"><el-input v-model="form.consumableName"/></el-form-item><el-form-item label="规格"><el-input v-model="form.specification"/></el-form-item><el-form-item label="单位"><el-input v-model="form.unit"/></el-form-item><el-form-item label="品牌"><el-input v-model="form.brand"/></el-form-item><el-form-item label="库存"><el-input-number v-model="form.currentStock" :min="0"/></el-form-item><el-form-item label="预警值"><el-input-number v-model="form.minStock" :min="0"/></el-form-item><el-form-item label="危化品"><el-switch v-model="form.isHazardous"/></el-form-item><el-form-item label="存放位置"><el-input v-model="form.storageLocation"/></el-form-item></el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></template>
    </el-dialog>
  </el-card>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import request from '../../utils/request'
import {ElMessage,ElMessageBox} from 'element-plus'
const list=ref([]),page=ref(1),size=ref(10),total=ref(0),keyword=ref(''),dialogVisible=ref(false),editId=ref(null)
const form=reactive({consumableCode:'',consumableName:'',specification:'',unit:'',brand:'',currentStock:0,minStock:10,isHazardous:false,storageLocation:''})
function loadData(){request.get('/consumables',{params:{name:keyword.value,page:page.value-1,size:size.value}}).then(r=>{list.value=r.data;total.value=r.total})}
function showDialog(row){editId.value=row?row.id:null;Object.assign(form,row||{consumableCode:'',consumableName:'',specification:'',unit:'',brand:'',currentStock:0,minStock:10,isHazardous:false,storageLocation:''});dialogVisible.value=true}
async function handleSave(){if(editId.value){await request.put('/consumables/'+editId.value,form)}else{await request.post('/consumables',form)}ElMessage.success('操作成功');dialogVisible.value=false;loadData()}
function handleDelete(id){ElMessageBox.confirm('确认删除?').then(async()=>{await request.delete('/consumables/'+id);ElMessage.success('删除成功');loadData()}).catch(()=>{})}
onMounted(()=>loadData())
</script>
