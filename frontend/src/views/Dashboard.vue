<script setup>
import { onMounted, ref, computed } from 'vue'
import http from '../api/http'
const data=ref({sites:[],modules:[],abnormalSites:[],abnormalModules:[]}), loading=ref(false)
const tagType=s=>({NORMAL:'success',WARNING:'warning',ABNORMAL:'danger',UNKNOWN:'info'}[s]||'info')
const statusText=s=>({NORMAL:'正常',WARNING:'警告',ABNORMAL:'异常',UNKNOWN:'待检测'}[s]||s)
const refresh=async()=>{loading.value=true;try{data.value=(await http.get('/dashboard')).data}finally{loading.value=false}}
const check=async()=>{await Promise.all([http.post('/sites/check'),http.post('/modules/check')]);await refresh()}
const healthy=computed(()=>data.value.sites.filter(s=>s.status!=='ABNORMAL'))
onMounted(refresh)
</script>
<template><div v-loading="loading"><section class="title-row"><div><h2>监控总览</h2><p>实时掌握网站可用性和预报产品更新时间</p></div><el-button type="primary" @click="check">立即检测</el-button></section><el-alert v-for="site in data.abnormalSites" :key="site.id" type="error" :closable="false" show-icon class="alert" :title="`${site.siteName} 无法访问`" :description="`${site.errorMessage || '访问异常'} · ${site.lastCheckTime || '未检测'}`"/><el-alert v-for="item in data.abnormalModules" :key="`module-${item.id}`" type="error" :closable="false" show-icon class="alert" :title="`${item.moduleName} 数据异常`" :description="`${item.remark || '模块访问异常'} · ${item.lastCheckTime || '未检测'}`"/><h3>网站监控</h3><el-row :gutter="16"><el-col v-for="site in healthy" :key="site.id" :xs="24" :sm="12" :lg="8"><el-card class="card"><div class="card-head"><b>{{site.siteName}}</b><el-tag :type="tagType(site.status)">{{statusText(site.status)}}</el-tag></div><p>响应时间：{{site.responseTime == null ? '—' : site.responseTime + ' ms'}}</p><small>最近检测：{{site.lastCheckTime || '待检测'}}</small></el-card></el-col></el-row><h3>业务模块</h3><el-row :gutter="16"><el-col v-for="item in data.modules" :key="item.id" :xs="24" :sm="12" :lg="6"><el-card class="card"><div class="card-head"><b>{{item.moduleName}}</b><el-tag :type="tagType(item.status)">{{statusText(item.status)}}</el-tag></div><p>类别：{{item.moduleCategory}}</p><p>最后更新时间：{{item.updateTime || '待识别'}}</p><small>最近检查：{{item.lastCheckTime || '待检查'}}</small></el-card></el-col></el-row></div></template>
<style scoped>.title-row,.card-head{display:flex;align-items:center;justify-content:space-between}.title-row p{color:#7a8491}.alert{margin-bottom:12px}.card{margin-bottom:16px;min-height:112px}.card p{margin:18px 0 8px;color:#405266}small{color:#8894a4}h3{margin:30px 0 14px}</style>
