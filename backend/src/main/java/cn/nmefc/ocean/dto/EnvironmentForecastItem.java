package cn.nmefc.ocean.dto;

/** 环境预报文件的最新创建时间及按值班规则计算出的状态。 */
public record EnvironmentForecastItem(String name, String latestCreateTime, String status, String message) { }
