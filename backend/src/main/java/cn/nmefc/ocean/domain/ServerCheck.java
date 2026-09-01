package cn.nmefc.ocean.domain;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
/** 服务器目录诊断结果；目录由管理员在部署环境配置。 */
@Data @TableName("server_check")
public class ServerCheck { @TableId private Long id; private Long moduleId; private String directory; private String fileName; private String modifyTime; private Long fileSize; private Long fileCount; private String fileStatus; private String checkTime; }
