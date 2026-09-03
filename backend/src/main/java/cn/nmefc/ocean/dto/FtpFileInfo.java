package cn.nmefc.ocean.dto;
/** 一处 FTP 目录内按起报时间选出的最新数据文件。 */
public record FtpFileInfo(String directory, String fileName, String startTime, String lastModifiedTime) { }
