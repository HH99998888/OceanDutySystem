package cn.nmefc.ocean.dto;
import java.util.List;
public record GridFtpResult(boolean available, String message, List<GridFtpItem> items) { }
