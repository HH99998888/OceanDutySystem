package cn.nmefc.ocean.service;

import cn.nmefc.ocean.config.GridDatabaseProperties;
import cn.nmefc.ocean.config.AlarmDatabaseProperties;
import cn.nmefc.ocean.dto.GridDataItem;
import cn.nmefc.ocean.dto.GridDataResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** 中国海洋预报网智能网格产品的 PostgreSQL 时效检查。 */
@Slf4j @Service @RequiredArgsConstructor
public class GridDataService {
    private final GridDatabaseProperties properties;
    private final AlarmDatabaseProperties alarmDatabaseProperties;
    public GridDataResult latestGridData() {
        if (!properties.isEnabled()) return new GridDataResult(false, "未启用 OCEAN_GRID_DB_ENABLED", List.of());
        if (blank(properties.getUrl())) return new GridDataResult(false, "缺少 OCEAN_GRID_DB_URL", List.of());
        if (!properties.getUrl().trim().startsWith("jdbc:postgresql://")) return new GridDataResult(false, "OCEAN_GRID_DB_URL 格式应为 jdbc:postgresql://主机:端口/数据库", List.of());
        if (blank(properties.getUsername())) return new GridDataResult(false, "缺少 OCEAN_GRID_DB_USERNAME", List.of());
        if (blank(properties.getPassword())) return new GridDataResult(false, "缺少 OCEAN_GRID_DB_PASSWORD", List.of());
        try {
            // 显式注册驱动，兼容某些本地 Maven/IDE 启动环境未自动注册 JDBC Driver 的情况。
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException exception) { return new GridDataResult(false, "未加载 PostgreSQL JDBC 驱动，请重新执行 Maven 启动", List.of()); }
        try (Connection connection = DriverManager.getConnection(properties.getUrl().trim(), properties.getUsername(), properties.getPassword())) {
            LocalDateTime now = LocalDateTime.now(); List<GridDataItem> items = new ArrayList<>();
            items.add(check(connection, "风", "app_wind_speed_grid", now, LocalTime.of(7, 0), LocalTime.of(19, 0), 13, 13, "07:00-19:00 / 19:00-次日07:00 均为 13 小时"));
            items.add(check(connection, "海浪", "app_wave_height_grid", now, LocalTime.of(8, 0), LocalTime.of(22, 0), 15, 11, "08:00-22:00 为 15 小时，其他时段为 11 小时"));
            items.add(check(connection, "海流", "app_current_speed_grid", now, LocalTime.of(8, 30), LocalTime.of(17, 30), 12, 16, "08:30-17:30 为 12 小时，其他时段为 16 小时"));
            items.add(check(connection, "海温", "app_sst_grid", now, LocalTime.of(8, 30), LocalTime.of(17, 30), 12, 16, "08:30-17:30 为 12 小时，其他时段为 16 小时"));
            items.add(check(connection, "天文潮", "app_storm_tide_grid", now, LocalTime.MIDNIGHT, LocalTime.MIDNIGHT, 24, 24, "24 小时内必须更新"));
            return new GridDataResult(true, "查询成功", items);
        } catch (SQLException exception) {
            // SQLState 与数据库消息可定位表/字段/权限问题；不会记录 JDBC 地址、账号或密码。
            log.warn("智能网格数据库查询失败: SQLState={}, 错误码={}, 原因={}", exception.getSQLState(), exception.getErrorCode(), exception.getMessage());
            return new GridDataResult(false, "数据库查询失败（SQLState: " + exception.getSQLState() + "）", List.of());
        } catch (Exception exception) { log.warn("智能网格数据库查询失败: {}", exception.getClass().getSimpleName()); return new GridDataResult(false, "数据库连接或查询失败，请检查部署环境配置", List.of()); }
    }
    private GridDataItem check(Connection connection, String name, String table, LocalDateTime now, LocalTime start, LocalTime end, int dayHours, int nightHours, String rule) throws Exception {
        GridRecord record = latestRecord(connection, table); int threshold = inWindow(now.toLocalTime(), start, end) ? dayHours : nightHours;
        if (record.updateTime() == null) return new GridDataItem(name, null, null, null, "ABNORMAL", rule, "未查询到更新时间");
        String reportDate = reportDateForVersion(record.version());
        long ageMinutes = Duration.between(record.updateTime().toLocalDateTime(), now).toMinutes();
        boolean normal = ageMinutes <= threshold * 60L;
        String message = normal ? "最近更新距今 " + ageMinutes + " 分钟" : "超过 " + threshold + " 小时未更新";
        if (reportDate == null) message += "；未匹配到起报时间";
        return new GridDataItem(name, record.updateTime().toLocalDateTime().toString().replace('T', ' '), record.version(), reportDate, normal ? "NORMAL" : "ABNORMAL", rule, message);
    }
    private GridRecord latestRecord(Connection connection, String table) throws Exception { try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT version,update_date FROM " + table + " ORDER BY update_date DESC LIMIT 1")) { return rows.next() ? new GridRecord(rows.getString("version"), rows.getTimestamp("update_date")) : new GridRecord(null, null); } }
    /** 使用 PG 最新数据版本匹配 MySQL 版本控制表，获得实际起报时间。 */
    private String reportDateForVersion(String version) {
        if (blank(version) || !alarmDatabaseProperties.isEnabled() || blank(alarmDatabaseProperties.getUrl()) || blank(alarmDatabaseProperties.getUsername()) || blank(alarmDatabaseProperties.getPassword())) return null;
        String sql = "SELECT report_date FROM pro_version_controller WHERE local_version=? ORDER BY report_date DESC LIMIT 1";
        try (Connection connection = DriverManager.getConnection(alarmDatabaseProperties.getUrl(), alarmDatabaseProperties.getUsername(), alarmDatabaseProperties.getPassword()); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, version); try (ResultSet rows = statement.executeQuery()) { return rows.next() && rows.getTimestamp(1) != null ? rows.getTimestamp(1).toLocalDateTime().toString().replace('T', ' ') : null; }
        } catch (Exception exception) { log.warn("智能网格起报时间查询失败: {}", exception.getClass().getSimpleName()); return null; }
    }
    private boolean inWindow(LocalTime current, LocalTime start, LocalTime end) { return start.equals(end) || (!current.isBefore(start) && current.isBefore(end)); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private record GridRecord(String version, Timestamp updateTime) { }
}
