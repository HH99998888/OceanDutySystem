package cn.nmefc.ocean.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
@Data @TableName("duty_log")
public class DutyLog { @TableId private Long id; private String userName; private String dutyTime; private String moduleSummary; private String problem; private String solution; private String recoverTime; }
