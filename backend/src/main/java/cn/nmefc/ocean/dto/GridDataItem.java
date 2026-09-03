package cn.nmefc.ocean.dto;
/** 智能网格产品的最新更新时间及其时效状态。 */
public record GridDataItem(String name, String latestUpdateTime, String version, String reportDate, String status, String rule, String message) { }
