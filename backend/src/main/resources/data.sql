-- 初始监控站点配置。重复启动时按 URL 去重。
INSERT INTO monitor_site (site_name,site_url,site_type,status) SELECT '中国海洋预报网','https://www.oceanguide.org.cn/IndexHome','预报网站','UNKNOWN' WHERE NOT EXISTS (SELECT 1 FROM monitor_site WHERE site_url='https://www.oceanguide.org.cn/IndexHome');
INSERT INTO monitor_site (site_name,site_url,site_type,status) SELECT '国家海洋预报中心门户网站','https://www.nmefc.cn/','门户网站','UNKNOWN' WHERE NOT EXISTS (SELECT 1 FROM monitor_site WHERE site_url='https://www.nmefc.cn/');
INSERT INTO monitor_site (site_name,site_url,site_type,status) SELECT 'NEARGOOS网站','https://neargoos.nmefc.cn/#/index','业务网站','UNKNOWN' WHERE NOT EXISTS (SELECT 1 FROM monitor_site WHERE site_url='https://neargoos.nmefc.cn/#/index');
INSERT INTO monitor_site (site_name,site_url,site_type,status) SELECT 'MaCOM网站','https://macom.oceanguide.org.cn/','业务网站','UNKNOWN' WHERE NOT EXISTS (SELECT 1 FROM monitor_site WHERE site_url='https://macom.oceanguide.org.cn/');

-- 灾害预警与预报服务模块。更新时间解析规则将在确认页面 DOM 后写入 remark。
WITH modules(module_name,module_url,module_category) AS (
  VALUES
  ('台风海浪警报','https://www.nmefc.cn/zhyj/hljb/tfhljb','灾害预警'),
  ('温带海浪警报','https://www.nmefc.cn/zhyj/hljb/wdhljb','灾害预警'),
  ('台风风暴潮警报','https://www.nmefc.cn/zhyj/fbcjb/tffbcjb','灾害预警'),
  ('温带风暴潮警报','https://www.nmefc.cn/zhyj/fbcjb/wdfbcjb','灾害预警'),
  ('海冰警报','https://www.nmefc.cn/zhyj/hbjb','灾害预警'),
  ('海啸消息/警报','https://www.nmefc.cn/zhyj/hx','灾害预警'),
  ('海浪实况','https://www.nmefc.cn/ybfw/waveAnalysis','预报服务'),
  ('海浪综合预报','https://www.nmefc.cn/ybfw/waveForecast','预报服务'),
  ('海浪预报','https://www.nmefc.cn/ybfw/wave/Global','预报服务'),
  ('台风风暴潮预报','https://www.nmefc.cn/stormSurgeViews/typhoon','预报服务'),
  ('温带风暴潮预报','https://www.nmefc.cn/stormSurgeViews/temperate','预报服务'),
  ('海冰年预报','https://www.nmefc.cn/ybfw/IceForecast/IceForecast-Year','预报服务'),
  ('海冰月预报','https://www.nmefc.cn/ybfw/IceForecast/IceForecast-Moth','预报服务'),
  ('海冰旬预报','https://www.nmefc.cn/ybfw/IceForecast/IceForecast-Ten','预报服务'),
  ('海冰周预报','https://www.nmefc.cn/ybfw/IceForecast/IceForecast-Week','预报服务'),
  ('海冰预报','https://www.nmefc.cn/ybfw/IceNumerical/BoHuangHaiBusiness','预报服务'),
  ('极地预报','https://www.nmefc.cn/ybfw/polar/ArcticPolarIce','预报服务'),
  ('海流预报','https://www.nmefc.cn/ybfw/seacurrent/Global','预报服务'),
  ('海洋热浪预报','https://www.nmefc.cn/ybfw/heatWave/tensity','预报服务'),
  ('海温预报','https://www.nmefc.cn/ybfw/seatemp/Global','预报服务'),
  ('盐度预报','https://www.nmefc.cn/ybfw/salinity/Global','预报服务'),
  ('生态预报','https://www.nmefc.cn/ybfw/styb/WestNorthPacific','预报服务'),
  ('中尺度诊断产品','https://www.nmefc.cn/ybfw/diagnostic/scs','预报服务')
)
INSERT INTO monitor_module (site_id,module_name,module_url,module_category,status)
SELECT site.id, modules.module_name, modules.module_url, modules.module_category, 'UNKNOWN'
FROM monitor_site site CROSS JOIN modules
WHERE site.site_url='https://www.nmefc.cn/'
  AND NOT EXISTS (SELECT 1 FROM monitor_module current WHERE current.module_url=modules.module_url);
