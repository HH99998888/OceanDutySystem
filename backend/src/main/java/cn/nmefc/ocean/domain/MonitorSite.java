package cn.nmefc.ocean.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data @TableName("monitor_site")
public class MonitorSite { @TableId private Long id; private String siteName; private String siteUrl; private String siteType; private Status status; private Long responseTime; private Integer httpStatus; private String lastCheckTime; private String errorMessage; }
