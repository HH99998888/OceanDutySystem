package cn.nmefc.ocean.controller;
import cn.nmefc.ocean.domain.*;
import cn.nmefc.ocean.mapper.*;
import cn.nmefc.ocean.service.MonitoringService;
import cn.nmefc.ocean.service.DiagnosisService;
import cn.nmefc.ocean.service.AlarmDatabaseService;
import cn.nmefc.ocean.dto.AlarmDatabaseResult;
import cn.nmefc.ocean.dto.EnvironmentForecastResult;
import cn.nmefc.ocean.service.EnvironmentForecastService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class MonitorController {
  private final SiteMapper siteMapper; private final ModuleMapper moduleMapper; private final DutyLogMapper dutyLogMapper; private final MonitoringService monitoringService; private final DiagnosisService diagnosisService; private final AlarmDatabaseService alarmDatabaseService; private final EnvironmentForecastService environmentForecastService;
  @Operation(summary="仪表盘汇总") @GetMapping("/dashboard") public Map<String,Object> dashboard() { var sites=siteMapper.selectList(null); var modules=moduleMapper.selectList(null); return Map.of("sites",sites,"modules",modules,"abnormalSites",sites.stream().filter(s->s.getStatus()==Status.ABNORMAL).toList(),"abnormalModules",modules.stream().filter(m->m.getStatus()==Status.ABNORMAL).toList()); }
  @GetMapping("/sites") public List<MonitorSite> sites() { return siteMapper.selectList(null); }
  @PostMapping("/sites/check") public void checkSites() { monitoringService.checkAllSites(); }
  @GetMapping("/modules") public List<MonitorModule> modules() { return moduleMapper.selectList(null); }
  @PostMapping("/modules/check") public void checkModules() { monitoringService.checkAllModules(); }
  @GetMapping("/modules/{id}/diagnosis") public Map<String,Object> diagnosis(@PathVariable Long id) { return diagnosisService.diagnose(id); }
  @Operation(summary="灾害预警数据库最新发布记录") @GetMapping("/alarm-database/latest") public AlarmDatabaseResult latestAlarms() { return alarmDatabaseService.latestAlarms(); }
  @Operation(summary="环境预报数据库发布检查") @GetMapping("/environment-forecasts/latest") public EnvironmentForecastResult latestEnvironmentForecasts() { return environmentForecastService.latestForecasts(); }
  @GetMapping("/duty-logs") public List<DutyLog> logs() { return dutyLogMapper.selectList(null); }
  @PostMapping("/duty-logs") public DutyLog createLog(@RequestBody DutyLog log) { dutyLogMapper.insert(log); return log; }
}
