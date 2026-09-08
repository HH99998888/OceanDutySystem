# 海洋预报发布值班监控系统：一期架构设计

> 本文定义一期 MVP 的边界与实现约定。外部业务库、FTP 和页面解析规则均以配置接入，避免把账号、密码或业务库结构写入代码仓库。

## 1. 总体架构

```text
微信浏览器 / PC 浏览器
          │ HTTPS
          ▼
Vue 3 + Element Plus（Nginx 静态站点）
          │ /api（反向代理）
          ▼
Spring Boot REST API ── Quartz（5/10 分钟任务）
          │                 │
          ├── SQLite（监控配置、检查记录、值班日志）
          ├── HttpClient / Jsoup（网站与页面探测）
          └── 可选：业务数据库、FTP、Redis
```

一期以 SQLite 保存本地值班数据；可选业务库只读访问。生产部署时应由 Nginx 终止 TLS，业务库凭据通过环境变量或 Secret 注入。

## 2. 功能模块与权限边界

| 模块 | 一期内容 | 管理员 | 值班人员 |
| --- | --- | --- | --- |
| 监控总览 | 网站、模块状态与异常高亮 | 查看、立即检测 | 查看、立即检测 |
| 网站监控 | HTTP 状态、耗时、错误信息 | 配置（下一迭代） | 查看 |
| 发布监控 | 页面更新时间、业务库和 FTP 数据时效 | 配置规则（下一迭代） | 查看、诊断 |
| 值班日志 | 新增、查询、CSV 导出值班处置记录 | 查询、管理、导出 | 新增、查询、导出 |
| 任务调度 | 网站 5 分钟、模块 10 分钟 | 配置（下一迭代） | 无 |
| 用户与认证 | 管理员/值班人员、登录 | 管理 | 登录 |

当前代码已实现前四项的基础主链路与前两类调度任务。认证、用户管理与配置 CRUD 是二期接入点，不应在未经确认的情况下使用默认弱密码上线。

## 3. 数据模型（ER）

```text
monitor_site 1 ──── * monitor_module
     │                     │
     └─────────── * monitor_record
                         ▲
monitor_module ──────────┘

duty_log（独立的值班处置记录）
server_check（模块对应的目录/文件检查快照）
```

| 表 | 关键字段 | 用途 |
| --- | --- | --- |
| `monitor_site` | `site_name`, `site_url`, `status`, `http_status` | 站点监控配置和最近状态 |
| `monitor_module` | `site_id`, `module_url`, `expected_time`, `update_time` | 发布模块与更新时间 |
| `monitor_record` | `site_id`, `module_id`, `check_time`, `status`, `detail` | 检查审计记录 |
| `duty_log` | `user_name`, `duty_time`, `problem`, `solution` | 值班交接与处置日志 |
| `server_check` | `module_id`, `directory`, `file_name`, `modify_time` | 文件/目录检查记录 |

建议二期新增 `sys_user`、`sys_role`、`monitor_rule` 和 `daily_report`，并将时间字段迁移为统一的 UTC 时间戳或 `datetime` 类型。

## 4. 后端结构

```text
backend/src/main/java/cn/nmefc/ocean/
├── config/       # Web、外部数据源配置
├── controller/   # REST API 入口
├── domain/       # SQLite 实体
├── dto/          # 外部数据源返回模型
├── mapper/       # MyBatis-Plus Mapper
├── service/      # 探测、诊断、数据时效业务
└── task/         # Quartz Job 与调度配置
```

接口前缀固定为 `/api`。控制器只承接参数和输出；探测与诊断逻辑放在 `service`；调度只调用服务，不承载业务规则。

## 5. 前端结构

```text
frontend/src/
├── api/          # Axios 实例与接口调用
├── router/       # Vue Router 路由
├── views/        # 页面：总览、值班日志
├── App.vue       # 导航与响应式壳层
└── main.js       # Vue / Element Plus / Pinia 启动
```

页面以卡片为最小信息单元，窄屏下强制单列；异常优先显示在页面顶部。后续新增页面应保持 `/views` 按业务域拆分，并抽取 `components/` 复用组件。

## 6. API（一期）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/dashboard` | 站点、模块及异常汇总 |
| GET | `/api/sites` | 监控站点列表 |
| POST | `/api/sites/check` | 立即检查全部站点 |
| GET | `/api/modules` | 监控模块列表 |
| POST | `/api/modules/check` | 立即检查全部模块 |
| GET | `/api/modules/{id}/diagnosis` | 模块异常诊断 |
| GET/POST | `/api/duty-logs` | 查询/新增值班日志 |
| GET | `/api/duty-logs/export` | 导出 UTF-8 CSV 值班日志 |
| GET | `/api/alarm-database/latest` | 可选灾害预警库时效 |
| GET | `/api/environment-forecasts/latest` | 可选环境预报库时效 |
| GET | `/api/grid-data/latest` | 可选智能网格库时效 |
| GET | `/api/grid-files/latest` | 可选 FTP 文件时效 |

交互式 OpenAPI 文档在 `/swagger-ui/index.html`。二期统一引入 `ApiResponse<T>`、分页参数、Bean Validation 与全局异常返回格式，避免破坏一期接口。

## 7. 页面原型

```text
┌ 海洋预报发布值班监控系统 ─── 监控总览 | 值班日志 ┐
│ [异常：站点名称 / 错误 / 检测时间]                  │
│ 网站监控             [站点卡片] [站点卡片]          │
│ 发布数据与业务模块   [状态卡片] [状态卡片]          │
│ 值班日志             表格 + 新增日志弹窗            │
└────────────────────────────────────────────────────┘
```

移动端：导航与标题纵向排列，所有卡片一列显示，保留状态标签和检测时间，避免横向滚动。

## 8. 核心实现约定

- `MonitoringService` 使用 Java `HttpClient` 探测站点，20 秒超时，记录 HTTP 状态、耗时和异常信息。
- 模块页使用 Jsoup 访问并识别常见“更新时间/发布时间”文本，识别失败为 `WARNING` 而非误报为正常。
- Quartz 调度任务调用同一服务逻辑；手工“立即检测”与定时检测结果一致。
- 所有外部连接配置均使用 `OCEAN_*` 环境变量；不得提交真实账号、密码和内网地址。

## 9. Docker 部署

```bash
cp .env.example .env
docker compose up --build -d
```

`backend` 暴露 8081，`frontend` 暴露 8080。SQLite 数据使用 `ocean-data` 命名卷持久化。生产环境应限制 8081 仅供反向代理访问、配置 HTTPS，并定期备份数据卷。

## 10. 后续迭代顺序

1. 接入 Spring Security + JWT、用户/角色/权限与审计日志。
2. 实现网站和模块配置 CRUD；将页面选择器、时效阈值、告警级别配置化。
3. 增加告警通知（企业微信/短信/邮件）与告警确认、恢复闭环。
4. 增加日报生成、检查历史趋势、ECharts 可用率和响应时间图。
5. 将 SQLite 替换为 PostgreSQL/MySQL（高可用场景），以 Redis 缓存实时状态。
