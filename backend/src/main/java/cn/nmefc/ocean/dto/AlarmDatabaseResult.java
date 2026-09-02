package cn.nmefc.ocean.dto;

import java.util.List;
/** 独立数据源的安全查询结果；连接失败不会影响主监控系统可用性。 */
public record AlarmDatabaseResult(boolean available, String message, List<AlarmPublication> alarms) { }
