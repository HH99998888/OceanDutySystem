package cn.nmefc.ocean.service;

import cn.nmefc.ocean.config.AlarmDatabaseProperties;
import cn.nmefc.ocean.dto.AlarmDatabaseResult;
import cn.nmefc.ocean.dto.AlarmPublication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 读取 hyyj.cms_forecast_alarm 中每种警报的最新发布记录。
 * 数据库异常仅返回脱敏诊断信息，绝不记录 JDBC 地址、用户名或密码。
 */
@Slf4j @Service @RequiredArgsConstructor
public class AlarmDatabaseService {
    private final AlarmDatabaseProperties properties;

    public AlarmDatabaseResult latestAlarms() {
        if (!properties.isEnabled() || isBlank(properties.getUrl()) || isBlank(properties.getUsername()) || isBlank(properties.getPassword())) {
            return new AlarmDatabaseResult(false, "灾害预警数据库未配置", List.of());
        }
        Map<String, String> names = properties.getTypeNames();
        if (names.isEmpty()) return new AlarmDatabaseResult(false, "未配置警报类型映射", List.of());
        String placeholders = String.join(",", java.util.Collections.nCopies(names.size(), "?"));
        String sql = "SELECT a.type,a.title,a.code,a.alarm_date FROM cms_forecast_alarm a " +
                "JOIN (SELECT type,MAX(alarm_date) latest_date FROM cms_forecast_alarm WHERE type IN (" + placeholders + ") GROUP BY type) latest " +
                "ON a.type=latest.type AND a.alarm_date=latest.latest_date ORDER BY a.alarm_date DESC";
        try (Connection connection = DriverManager.getConnection(properties.getUrl(), properties.getUsername(), properties.getPassword());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1; for (String type : names.keySet()) statement.setString(index++, type);
            List<AlarmPublication> result = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(new AlarmPublication(rows.getString("type"), names.get(rows.getString("type")), rows.getString("title"), rows.getString("code"), String.valueOf(rows.getTimestamp("alarm_date"))));
            }
            return new AlarmDatabaseResult(true, "查询成功", result);
        } catch (Exception exception) {
            log.warn("灾害预警数据库查询失败: {}", exception.getClass().getSimpleName());
            return new AlarmDatabaseResult(false, "数据库连接或查询失败，请检查部署环境配置", List.of());
        }
    }
    private boolean isBlank(String value) { return value == null || value.isBlank(); }
}
