package cn.nmefc.ocean.service;

import cn.nmefc.ocean.domain.MonitorSite;
import cn.nmefc.ocean.domain.Status;
import cn.nmefc.ocean.mapper.ModuleMapper;
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
    private final SiteMapper siteMapper; private final ModuleMapper moduleMapper;
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
    public void checkAllModules() { moduleMapper.selectList(null).forEach(module -> { if (module.getStatus() == null) { module.setStatus(Status.UNKNOWN); moduleMapper.updateById(module); } }); }
}
