package cn.nmefc.ocean.task;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 集中管理监控任务计划；日报 Job 会在值班日报模块落地时接入。 */
@Configuration
public class QuartzConfig {
    @Bean public JobDetail siteCheckDetail() { return JobBuilder.newJob(SiteCheckJob.class).withIdentity("siteCheck").storeDurably().build(); }
    @Bean public Trigger siteCheckTrigger(JobDetail siteCheckDetail) { return TriggerBuilder.newTrigger().forJob(siteCheckDetail).withSchedule(CronScheduleBuilder.cronSchedule("0 */5 * ? * *")).build(); }
    @Bean public JobDetail moduleCheckDetail() { return JobBuilder.newJob(ModuleCheckJob.class).withIdentity("moduleCheck").storeDurably().build(); }
    @Bean public Trigger moduleCheckTrigger(JobDetail moduleCheckDetail) { return TriggerBuilder.newTrigger().forJob(moduleCheckDetail).withSchedule(CronScheduleBuilder.cronSchedule("0 */10 * ? * *")).build(); }
}
