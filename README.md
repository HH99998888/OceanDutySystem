# 海洋预报发布值班监控系统

前后端分离的 MVP 骨架，面向海洋预报网站、业务模块更新时间和值班日志的日常监控。

## 快速启动

```bash
docker compose up --build
```

- 前端：`http://localhost:8080`
- 后端接口：`http://localhost:8081/api`
- 接口文档：`http://localhost:8081/swagger-ui/index.html`

开发模式请分别进入 `backend` 与 `frontend` 目录，执行 `mvn spring-boot:run` 和 `npm install && npm run dev`。

默认使用 SQLite 文件数据库，首次启动会创建数据表与演示监控配置。
