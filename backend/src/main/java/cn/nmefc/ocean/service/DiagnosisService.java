package cn.nmefc.ocean.service;
import cn.nmefc.ocean.domain.ServerCheck;
import cn.nmefc.ocean.mapper.ServerCheckMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;
/** 三层异常诊断入口；部署时接入生产业务库和只读数据目录。 */
@Service @RequiredArgsConstructor
public class DiagnosisService {
  private final ServerCheckMapper serverCheckMapper;
  public Map<String,Object> diagnose(Long moduleId) { var result=new LinkedHashMap<String,Object>(); var checks=serverCheckMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ServerCheck>().eq(ServerCheck::getModuleId,moduleId)); result.put("moduleId",moduleId); result.put("database","待配置业务数据库只读数据源"); result.put("serverFiles",checks); result.put("conclusion",checks.isEmpty()?"待配置服务器数据目录，无法完成第三层诊断":"已获取服务器目录检查记录"); return result; }
}
