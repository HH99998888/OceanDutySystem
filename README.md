# 海洋预报发布值班监控系统

前后端分离的 MVP 骨架，面向海洋预报网站、业务模块更新时间和值班日志的日常监控。

完整的一期架构、ER、接口、页面原型、Docker 方案和迭代计划见 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)。

## 快速启动

```bash
docker compose up --build
```

- 前端：`http://localhost:8080`
- 后端接口：`http://localhost:8081/api`
- 接口文档：`http://localhost:8081/swagger-ui/index.html`

开发模式请分别进入 `backend` 与 `frontend` 目录，执行 `mvn spring-boot:run` 和 `npm install && npm run dev`。

默认使用 SQLite 文件数据库，首次启动会创建数据表与演示监控配置。

## 灾害预警 MySQL（可选）

`cms_forecast_alarm` 使用独立只读连接。复制 `.env.example` 并在服务器的密钥管理服务、Docker Secret 或进程环境中设置 `OCEAN_ALARM_DB_*`；真实 IP、账号和密码不得写入仓库。接口 `GET /api/alarm-database/latest` 按 `type` 返回每类最新的 `alarm_date`、`title` 和 `code`。

默认类型映射为：`wave`（海浪）、`storm`（风暴潮）、`bore`（海啸）、`ice`（海冰）。

环境预报检查接口为 `GET /api/environment-forecasts/latest`：海区预报每日 15:30、近岸预报每日 09:00、周预报每周日 00:00、月预报每月首日 00:00 后，若未查到对应周期的新 `create_date` 即返回异常。
