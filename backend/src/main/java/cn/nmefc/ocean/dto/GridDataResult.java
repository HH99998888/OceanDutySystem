package cn.nmefc.ocean.dto;
import java.util.List;
public record GridDataResult(boolean available, String message, List<GridDataItem> items) { }
