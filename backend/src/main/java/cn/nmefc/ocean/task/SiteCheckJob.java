package cn.nmefc.ocean.task;

import cn.nmefc.ocean.service.MonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Quartz 网站探测任务，每五分钟运行一次。 */
@Slf4j @Component
public class SiteCheckJob implements Job {
    @Autowired private MonitoringService monitoringService;
    @Override public void execute(JobExecutionContext context) { log.info("执行网站探测任务"); monitoringService.checkAllSites(); }
}
