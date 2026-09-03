package cn.nmefc.ocean.dto;
/** 每个智能网格要素在 OutputData 与根目录的文件对照。 */
public record GridFtpItem(String name, FtpFileInfo outputData, FtpFileInfo rootData) { }
