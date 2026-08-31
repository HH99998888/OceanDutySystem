package cn.nmefc.ocean.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data @TableName("monitor_module")
public class MonitorModule { @TableId private Long id; private Long siteId; private String moduleName; private String moduleUrl; private String moduleCategory; private String updateTime; private String expectedTime; private String lastCheckTime; private Status status; private String remark; }
