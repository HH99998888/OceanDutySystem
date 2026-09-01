<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../api/http'
const rows=ref([]), dialog=ref(false), form=reactive({userName:'',dutyTime:'',moduleSummary:'',problem:'',solution:'',recoverTime:''})
const load=async()=>rows.value=(await http.get('/duty-logs')).data
const save=async()=>{await http.post('/duty-logs',form);dialog.value=false;await load()}
onMounted(load)
</script>
<template><section class="title-row"><h2>值班日志</h2><el-button type="primary" @click="dialog=true">新增日志</el-button></section><el-table :data="rows" stripe><el-table-column prop="dutyTime" label="值班时间"/><el-table-column prop="userName" label="值班人员"/><el-table-column prop="moduleSummary" label="模块状态"/><el-table-column prop="problem" label="问题"/><el-table-column prop="solution" label="处理措施"/><el-table-column prop="recoverTime" label="恢复时间"/></el-table><el-dialog v-model="dialog" title="新增值班日志"><el-form label-width="90px"><el-form-item label="值班人员"><el-input v-model="form.userName"/></el-form-item><el-form-item label="值班时间"><el-input v-model="form.dutyTime" placeholder="2026-08-31 08:00"/></el-form-item><el-form-item label="模块状态"><el-input v-model="form.moduleSummary" type="textarea" placeholder="例如：海浪警报正常；海冰警报异常"/></el-form-item><el-form-item label="问题"><el-input v-model="form.problem" type="textarea"/></el-form-item><el-form-item label="处理措施"><el-input v-model="form.solution" type="textarea"/></el-form-item><el-form-item label="恢复时间"><el-input v-model="form.recoverTime" placeholder="2026-08-31 10:30"/></el-form-item></el-form><template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog></template>
<style scoped>.title-row{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px}</style>
