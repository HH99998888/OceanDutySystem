package cn.nmefc.ocean.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/** 智能网格 FTP 的只读连接与目录映射，均从部署环境获取。 */
@Data @Component @ConfigurationProperties(prefix = "ocean.grid-ftp")
public class GridFtpProperties {
  private boolean enabled; private String host; private int port = 21; private String username; private String password;
  private String outputDirectory = "/OutputData"; private String rootDirectory = "/";
  private String windDirectory = "wind"; private String waveDirectory = "wave"; private String currentDirectory = "current"; private String sstDirectory = "sst"; private String tideDirectory = "storm_tide";
}
