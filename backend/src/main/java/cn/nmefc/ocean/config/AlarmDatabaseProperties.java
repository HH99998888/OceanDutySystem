package cn.nmefc.ocean.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.Map;

/** 灾害预警 MySQL 的只读连接配置，全部由运行环境变量注入。 */
@Data @Component @ConfigurationProperties(prefix = "ocean.alarm-db")
public class AlarmDatabaseProperties {
    private boolean enabled;
    private String url;
    private String username;
    private String password;
    /** type 与页面展示名称的映射；实际库有新增类型时可在配置中心扩展。 */
    private Map<String, String> typeNames = new LinkedHashMap<>(Map.of(
            "wave", "海浪警报", "storm", "风暴潮警报", "tsunami", "海啸消息/警报", "ice", "海冰警报"));
}
