<script setup>
import { onMounted, ref, computed } from 'vue'
import http from '../api/http'

const data = ref({ sites: [], modules: [], abnormalSites: [], abnormalModules: [] })
const alarmData = ref({ available: false, message: '灾害预警数据库未配置', alarms: [] })
const gridData = ref({ available: false, message: '智能网格数据库未配置', items: [] })
const ftpData = ref({ available: false, message: '智能网格 FTP 未配置', items: [] })
const loading = ref(false)
const tagType = status => ({ NORMAL: 'success', WARNING: 'warning', ABNORMAL: 'danger', UNKNOWN: 'info' }[status] || 'info')
const statusText = status => ({ NORMAL: '正常', WARNING: '警告', ABNORMAL: '异常', UNKNOWN: '待检测' }[status] || status)
const healthy = computed(() => data.value.sites.filter(site => site.status !== 'ABNORMAL'))
const healthyModules = computed(() => data.value.modules.filter(module => module.status !== 'ABNORMAL'))

const refresh = async () => {
  loading.value = true
  try {
    const [dashboard, alarms, grids, files] = await Promise.all([http.get('/dashboard'), http.get('/alarm-database/latest'), http.get('/grid-data/latest'), http.get('/grid-files/latest')])
    data.value = dashboard.data
    alarmData.value = alarms.data
    gridData.value = grids.data
    ftpData.value = files.data
  } catch {
    alarmData.value = { available: false, message: '无法读取灾害预警数据库', alarms: [] }
    gridData.value = { available: false, message: '无法读取智能网格数据库', items: [] }
    ftpData.value = { available: false, message: '无法读取智能网格 FTP', items: [] }
  } finally { loading.value = false }
}
const check = async () => { await http.post('/sites/check'); await http.post('/modules/check'); await refresh() }
onMounted(refresh)
</script>

<template>
  <div v-loading="loading">
    <section class="title-row"><div><h2>监控总览</h2><p>实时掌握网站可用性和预报产品更新时间</p></div><el-button type="primary" @click="check">立即检测</el-button></section>
    <el-alert v-for="site in data.abnormalSites" :key="site.id" type="error" :closable="false" show-icon class="alert" :title="`${site.siteName} 无法访问`" :description="`${site.errorMessage || '访问异常'} · ${site.lastCheckTime || '未检测'}`" />
    <el-alert v-for="item in data.abnormalModules" :key="`module-${item.id}`" type="error" :closable="false" show-icon class="alert" :title="`${item.moduleName} 数据异常`" :description="`${item.remark || '模块访问异常'} · ${item.lastCheckTime || '未检测'}`" />

    <h3>网站监控</h3>
    <el-row :gutter="16"><el-col v-for="site in healthy" :key="site.id" :xs="24" :sm="12" :lg="8"><el-card class="card"><div class="card-head"><b>{{ site.siteName }}</b><el-tag :type="tagType(site.status)">{{ statusText(site.status) }}</el-tag></div><p>响应时间：{{ site.responseTime == null ? '—' : `${site.responseTime} ms` }}</p><small>最近检测：{{ site.lastCheckTime || '待检测' }}</small></el-card></el-col></el-row>

    <h3>中国海洋预报网 · 灾害预警</h3>
    <el-row v-if="alarmData.available" :gutter="16"><el-col v-for="alarm in alarmData.alarms" :key="`${alarm.type}-${alarm.code}`" :xs="24" :sm="12" :lg="6"><el-card class="card"><div class="card-head"><b>{{ alarm.categoryName }}</b><el-tag type="success">已获取</el-tag></div><p>{{ alarm.title || '—' }}</p><small>编号：{{ alarm.code || '—' }}</small><br /><small>最新发布：{{ alarm.alarmDate || '—' }}</small></el-card></el-col></el-row>
    <el-empty v-else :description="alarmData.message" :image-size="54" />

    <h3>中国海洋预报网 · 智能网格</h3>
    <el-row v-if="gridData.available" :gutter="16"><el-col v-for="grid in gridData.items" :key="grid.name" :xs="24" :sm="12" :lg="6"><el-card class="card"><div class="card-head"><b>{{ grid.name }}</b><el-tag :type="tagType(grid.status)">{{ statusText(grid.status) }}</el-tag></div><p>更新时间：{{ grid.latestUpdateTime || '—' }}</p><small>版本：{{ grid.version || '—' }}</small><br /><small>起报时间：{{ grid.reportDate || '未匹配' }}</small><br /><small>{{ grid.message }}</small></el-card></el-col></el-row>
    <el-empty v-else :description="gridData.message" :image-size="54" />

    <h3>中国海洋预报网 · 智能网格 FTP 文件</h3>
    <el-row v-if="ftpData.available" :gutter="16"><el-col v-for="ftp in ftpData.items" :key="ftp.name" :xs="24" :sm="12" :lg="6"><el-card class="card ftp-card"><div class="card-head"><b>{{ ftp.name }}</b><el-tag type="success">已获取</el-tag></div><p>OutputData 起报：{{ ftp.outputData.startTime || '未识别' }}</p><small>文件：{{ ftp.outputData.fileName || '未找到' }}</small><br /><small>修改时间：{{ ftp.outputData.lastModifiedTime || '—' }}</small><p>根目录起报：{{ ftp.rootData.startTime || '未识别' }}</p><small>文件：{{ ftp.rootData.fileName || '未找到' }}</small><br /><small>修改时间：{{ ftp.rootData.lastModifiedTime || '—' }}</small></el-card></el-col></el-row>
    <el-empty v-else :description="ftpData.message" :image-size="54" />

    <h3>业务模块</h3>
    <el-row :gutter="16"><el-col v-for="item in healthyModules" :key="item.id" :xs="24" :sm="12" :lg="6"><el-card class="card"><div class="card-head"><b>{{ item.moduleName }}</b><el-tag :type="tagType(item.status)">{{ statusText(item.status) }}</el-tag></div><p>类别：{{ item.moduleCategory }}</p><p>最后更新时间：{{ item.updateTime || '待识别' }}</p><small>最近检查：{{ item.lastCheckTime || '待检查' }}</small></el-card></el-col></el-row>
  </div>
</template>

<style scoped>
.title-row,.card-head{display:flex;align-items:center;justify-content:space-between}.title-row p{color:#7a8491}.alert{margin-bottom:12px}.card{margin-bottom:16px;min-height:112px}.ftp-card{min-height:190px}.card p{margin:18px 0 8px;color:#405266}small{color:#8894a4;word-break:break-all}h3{margin:30px 0 14px}
</style>
