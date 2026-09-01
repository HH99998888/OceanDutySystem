package cn.nmefc.ocean.domain;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
/** 每次站点或模块检查的可追溯记录。 */
@Data @TableName("monitor_record")
public class MonitorRecord { @TableId private Long id; private Long siteId; private Long moduleId; private String checkTime; private Status status; private String detail; }
