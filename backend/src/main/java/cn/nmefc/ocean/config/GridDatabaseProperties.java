package cn.nmefc.ocean.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/** 智能网格 PostgreSQL 的只读连接配置，由部署环境变量注入。 */
@Data @Component @ConfigurationProperties(prefix = "ocean.grid-db")
public class GridDatabaseProperties { private boolean enabled; private String url; private String username; private String password; }
