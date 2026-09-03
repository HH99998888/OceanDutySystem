package cn.nmefc.ocean.service;

import cn.nmefc.ocean.config.AlarmDatabaseProperties;
import cn.nmefc.ocean.dto.EnvironmentForecastItem;
import cn.nmefc.ocean.dto.EnvironmentForecastResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/** 基于 hyyj 库创建时间的环境预报发布检查。 */
@Slf4j @Service @RequiredArgsConstructor
public class EnvironmentForecastService {
    private final AlarmDatabaseProperties properties;

    public EnvironmentForecastResult latestForecasts() {
        if (!properties.isEnabled() || blank(properties.getUrl()) || blank(properties.getUsername()) || blank(properties.getPassword())) {
            return new EnvironmentForecastResult(false, "环境预报数据库未配置", List.of());
        }
        try (Connection connection = DriverManager.getConnection(properties.getUrl(), properties.getUsername(), properties.getPassword())) {
            LocalDateTime now = LocalDateTime.now();
            List<EnvironmentForecastItem> items = new ArrayList<>();
            items.add(checkDaily(connection, "海区预报", "SELECT MAX(create_date) FROM cms_forecast_area_firststage", now, LocalTime.of(15, 30)));
            items.add(checkDaily(connection, "近岸预报", "SELECT MAX(create_date) FROM cms_forecast_nearshoreseaarea", now, LocalTime.of(9, 0)));
            items.add(checkPeriod(connection, "周预报", "SELECT MAX(create_date) FROM cms_article WHERE category_id=?", "1190087799167778816", now, now.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atStartOfDay(), "每周日 00:00"));
            items.add(checkPeriod(connection, "月预报", "SELECT MAX(create_date) FROM cms_article WHERE category_id=?", "1190087852779372544", now, now.toLocalDate().withDayOfMonth(1).atStartOfDay(), "每月首日 00:00"));
            return new EnvironmentForecastResult(true, "查询成功", items);
        } catch (Exception exception) {
            log.warn("环境预报数据库查询失败: {}", exception.getClass().getSimpleName());
            return new EnvironmentForecastResult(false, "数据库连接或查询失败，请检查部署环境配置", List.of());
        }
    }

    private EnvironmentForecastItem checkDaily(Connection connection, String name, String sql, LocalDateTime now, LocalTime deadline) throws Exception {
        Timestamp latest = latestTimestamp(connection, sql, null);
        boolean publishedToday = latest != null && latest.toLocalDateTime().toLocalDate().equals(now.toLocalDate());
        String message = publishedToday ? "已发布当日预报" : now.toLocalTime().isBefore(deadline) ? "尚未到发布时限" : "超过发布时限仍未发布当日预报";
        return item(name, latest, publishedToday ? "NORMAL" : now.toLocalTime().isBefore(deadline) ? "WARNING" : "ABNORMAL", message);
    }

    private EnvironmentForecastItem checkPeriod(Connection connection, String name, String sql, String categoryId, LocalDateTime now, LocalDateTime periodStart, String deadlineText) throws Exception {
        Timestamp latest = latestTimestamp(connection, sql, categoryId);
        boolean published = latest != null && !latest.toLocalDateTime().isBefore(periodStart);
        String message = published ? "已发布本周期预报文件" : now.isBefore(periodStart) ? "尚未到发布时限" : "超过" + deadlineText + "仍未发布本周期预报文件";
        return item(name, latest, published ? "NORMAL" : now.isBefore(periodStart) ? "WARNING" : "ABNORMAL", message);
    }

    private Timestamp latestTimestamp(Connection connection, String sql, String categoryId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (categoryId != null) statement.setString(1, categoryId);
            try (ResultSet result = statement.executeQuery()) { result.next(); return result.getTimestamp(1); }
        }
    }
    private EnvironmentForecastItem item(String name, Timestamp time, String status, String message) { return new EnvironmentForecastItem(name, time == null ? null : time.toLocalDateTime().toString().replace('T', ' '), status, message); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
}
