package cn.nmefc.ocean.service;

import cn.nmefc.ocean.config.GridFtpProperties;
import cn.nmefc.ocean.dto.FtpFileInfo;
import cn.nmefc.ocean.dto.GridFtpItem;
import cn.nmefc.ocean.dto.GridFtpResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Service;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 智能网格 FTP 输出目录与根目录文件的一致性检查。 */
@Slf4j @Service @RequiredArgsConstructor
public class GridFtpService {
  private static final Pattern START_TIME = Pattern.compile("(20\\d{6}(?:\\d{2}){0,3})");
  private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final GridFtpProperties properties;
  public GridFtpResult latestFiles() {
    if (!properties.isEnabled() || blank(properties.getHost()) || blank(properties.getUsername()) || blank(properties.getPassword())) return new GridFtpResult(false, "智能网格 FTP 未配置", List.of());
    FTPClient client = new FTPClient();
    try {
      client.setConnectTimeout(10_000); client.setDataTimeout(java.time.Duration.ofSeconds(20)); client.connect(properties.getHost(), properties.getPort());
      if (!client.login(properties.getUsername(), properties.getPassword())) return new GridFtpResult(false, "FTP 登录失败", List.of());
      client.enterLocalPassiveMode(); client.setFileType(FTP.BINARY_FILE_TYPE);
      List<GridFtpItem> items = new ArrayList<>();
      items.add(readElement(client, "风", properties.getWindDirectory())); items.add(readElement(client, "海浪", properties.getWaveDirectory()));
      items.add(readElement(client, "海流", properties.getCurrentDirectory())); items.add(readElement(client, "海温", properties.getSstDirectory())); items.add(readElement(client, "天文潮", properties.getTideDirectory()));
      return new GridFtpResult(true, "查询成功", items);
    } catch (Exception exception) { log.warn("智能网格 FTP 查询失败: {}", exception.getClass().getSimpleName()); return new GridFtpResult(false, "FTP 连接或目录查询失败，请检查部署环境配置", List.of()); }
    finally { try { if (client.isConnected()) { client.logout(); client.disconnect(); } } catch (Exception ignored) { } }
  }
  private GridFtpItem readElement(FTPClient client, String name, String folder) throws Exception { return new GridFtpItem(name, latestFile(client, join(properties.getOutputDirectory(), folder)), latestFile(client, join(properties.getRootDirectory(), folder))); }
  private FtpFileInfo latestFile(FTPClient client, String directory) throws Exception {
    FTPFile file = Arrays.stream(client.listFiles(directory)).filter(FTPFile::isFile).max(Comparator.comparing(this::sortKey).thenComparing(entry -> entry.getTimestamp() == null ? 0L : entry.getTimestamp().toInstant().toEpochMilli())).orElse(null);
    if (file == null) return new FtpFileInfo(directory, null, null, null);
    String start = extractStartTime(file.getName()); String modified = file.getTimestamp() == null ? null : DISPLAY_TIME.format(file.getTimestamp().toInstant().atZone(ZoneId.systemDefault()));
    return new FtpFileInfo(directory, file.getName(), start, modified);
  }
  private String sortKey(FTPFile file) { String value = extractRawStartTime(file.getName()); return value == null ? "" : value; }
  private String extractStartTime(String filename) { String value = extractRawStartTime(filename); if (value == null) return null; try { return switch (value.length()) { case 8 -> value.substring(0,4)+"-"+value.substring(4,6)+"-"+value.substring(6,8); case 10 -> value.substring(0,4)+"-"+value.substring(4,6)+"-"+value.substring(6,8)+" "+value.substring(8,10)+":00"; case 12 -> value.substring(0,4)+"-"+value.substring(4,6)+"-"+value.substring(6,8)+" "+value.substring(8,10)+":"+value.substring(10,12); default -> value.substring(0,4)+"-"+value.substring(4,6)+"-"+value.substring(6,8)+" "+value.substring(8,10)+":"+value.substring(10,12)+":"+value.substring(12,14); }; } catch (Exception ignored) { return value; } }
  private String extractRawStartTime(String filename) { Matcher matcher = START_TIME.matcher(filename); String latest = null; while (matcher.find()) latest = matcher.group(1); return latest; }
  private String join(String left, String right) { return (left.endsWith("/") ? left.substring(0, left.length()-1) : left) + "/" + right; }
  private boolean blank(String value) { return value == null || value.isBlank(); }
}
