package cn.nmefc.ocean.dto;

/** cms_forecast_alarm 的最新警报发布信息，不包含敏感数据。 */
public record AlarmPublication(String type, String categoryName, String title, String code, String alarmDate) { }
