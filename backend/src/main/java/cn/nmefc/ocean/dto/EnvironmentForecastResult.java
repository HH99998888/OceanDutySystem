package cn.nmefc.ocean.dto;

import java.util.List;
public record EnvironmentForecastResult(boolean available, String message, List<EnvironmentForecastItem> forecasts) { }
