package cn.nmefc.ocean.service;

import cn.nmefc.ocean.domain.MonitorSite;
import cn.nmefc.ocean.domain.MonitorModule;
import cn.nmefc.ocean.domain.MonitorRecord;
import cn.nmefc.ocean.domain.Status;
import cn.nmefc.ocean.mapper.ModuleMapper;
import cn.nmefc.ocean.mapper.MonitorRecordMapper;
import cn.nmefc.ocean.mapper.SiteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 网站探测服务。模块网页内容解析规则将在各业务模块确认页面结构后补充。 */
@Slf4j @Service @RequiredArgsConstructor
public class MonitoringService {
    private final SiteMapper siteMapper; private final ModuleMapper moduleMapper; private final MonitorRecordMapper recordMapper;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    public void checkAllSites() { siteMapper.selectList(null).forEach(this::checkSite); }
    public void checkSite(MonitorSite site) {
        long started = System.currentTimeMillis();
        try {
            HttpResponse<Void> response = client.send(HttpRequest.newBuilder(URI.create(site.getSiteUrl())).timeout(Duration.ofSeconds(20)).GET().build(), HttpResponse.BodyHandlers.discarding());
            site.setResponseTime(System.currentTimeMillis() - started); site.setStatus(response.statusCode() < 400 ? Status.NORMAL : Status.ABNORMAL);
            site.setErrorMessage(response.statusCode() < 400 ? null : "HTTP " + response.statusCode());
        } catch (Exception e) { site.setStatus(Status.ABNORMAL); site.setResponseTime(System.currentTimeMillis() - started); site.setErrorMessage(e.getClass().getSimpleName() + ": " + e.getMessage()); log.warn("站点探测失败: {}", site.getSiteName(), e); }
        site.setLastCheckTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))); siteMapper.updateById(site);
    }
    /**
     * 先校验模块页可访问性并保留审计记录。各站页面的更新时间标签不同，
     * 后续可通过模块配置中的选择器启用 Jsoup 精确提取和时效阈值判断。
     */
    public void checkAllModules() { moduleMapper.selectList(null).forEach(this::checkModule); }
    private void checkModule(MonitorModule module) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        MonitorRecord record = new MonitorRecord(); record.setModuleId(module.getId()); record.setSiteId(module.getSiteId()); record.setCheckTime(now);
        try {
            HttpResponse<Void> response = client.send(HttpRequest.newBuilder(URI.create(module.getModuleUrl())).timeout(Duration.ofSeconds(20)).GET().build(), HttpResponse.BodyHandlers.discarding());
            module.setStatus(response.statusCode() < 400 ? Status.NORMAL : Status.ABNORMAL);
            module.setRemark(response.statusCode() < 400 ? "页面可访问，等待配置更新时间解析规则" : "HTTP " + response.statusCode());
        } catch (Exception e) { module.setStatus(Status.ABNORMAL); module.setRemark("模块访问失败：" + e.getClass().getSimpleName()); log.warn("模块探测失败: {}", module.getModuleName(), e); }
        module.setLastCheckTime(now); moduleMapper.updateById(module);
        record.setStatus(module.getStatus()); record.setDetail(module.getRemark()); recordMapper.insert(record);
    }
}
