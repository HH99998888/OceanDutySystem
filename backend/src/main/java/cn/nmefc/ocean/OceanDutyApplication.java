package cn.nmefc.ocean;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@SpringBootApplication
public class OceanDutyApplication {
    public static void main(String[] args) {
        migrateLegacySqlite();
        SpringApplication.run(OceanDutyApplication.class, args);
    }

    /**
     * 旧版本已生成的 SQLite 文件需要在执行 data.sql 前补齐字段，
     * 这样既能修复乱码预置数据，也不会要求值班人员删除历史日志。
     */
    private static void migrateLegacySqlite() {
        try {
            Files.createDirectories(Path.of("data"));
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:data/ocean-duty.db")) {
                addColumnIfMissing(connection, "monitor_module", "module_category", "TEXT");
                addColumnIfMissing(connection, "monitor_module", "last_check_time", "TEXT");
                addColumnIfMissing(connection, "duty_log", "module_summary", "TEXT");
                addColumnIfMissing(connection, "server_check", "module_id", "INTEGER");
                addColumnIfMissing(connection, "server_check", "file_size", "INTEGER");
                addColumnIfMissing(connection, "server_check", "file_count", "INTEGER");
                addColumnIfMissing(connection, "server_check", "check_time", "TEXT");
            }
        } catch (Exception exception) { System.err.println("SQLite 历史结构迁移跳过: " + exception.getClass().getSimpleName()); }
    }
    private static void addColumnIfMissing(Connection connection, String table, String column, String type) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet columns = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (columns.next()) if (column.equals(columns.getString("name"))) return;
        }
        try (Statement statement = connection.createStatement()) { statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type); }
        catch (Exception ignored) { /* 新建数据库时表尚不存在，后续 schema.sql 会创建。 */ }
    }
}
