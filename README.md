# 实验室管理系统 (Lab Management System)

一个面向高校实验室日常管理的**前后端分离 Web 系统**，覆盖用户与角色权限、设备管理、预约审批、实验记录、危化品管理、公告发布、数据统计与 AI 智能问答等 8 大模块。

## 技术栈

**后端**
- Java 21 + Spring Boot 3.2.5
- Spring Security + JWT（登录鉴权 / 细粒度权限）
- Spring Data JPA + MySQL 8.0
- Redis（权限缓存 / 高频数据缓存）
- AOP（操作日志切面）
- DeepSeek OpenAI-compatible API（受限知识库智能问答）

**前端**
- Vue 3 + Vite + Element Plus
- Pinia（状态管理）+ Vue Router（异步路由守卫）

**运行方式**
- 前后端分离独立运行：后端由 Spring Boot 内嵌 Tomcat 启动，前端由 Vite 构建 / 预览
- 开发环境通过 Vite dev server 将 `/api` 代理到后端服务

## 核心功能

| 模块 | 说明 |
|---|---|
| 用户与权限 | RBAC 模型（4 类角色 / 40+ 权限码），JWT 鉴权，接口层 `@PreAuthorize` 细粒度控制 |
| 设备管理 | 设备台账、状态跟踪、报修与保养流程 |
| 预约审批 | 预约规则校验（提前天数 / 时长 / 次数 / 资格等级），悲观锁防超订，签到签退全流程 |
| 实验记录 | 与预约一一对应，双向状态同步，终态拦截 |
| 危化品 | 领用 / 归还登记，库存与操作留痕 |
| 公告管理 | 分类 CRUD、发布 / 撤回、富文本详情 |
| 数据统计 | 学生个人视图与全局看板分离，按 `currentUserId` 隔离 |
| AI 助手 | 受限知识库问答，越界问题可判定拒答并留痕 |

## 项目亮点

1. **权限体系**：`JwtAuthenticationFilter` 登录时装载权限码 + `ROLE_AUTHENTICATED` 到 `SecurityContext`；读写拆分（读用 `ROLE_AUTHENTICATED`、写用 `xxx:manage` 权限码）；`PermissionResolverService` 刻意只依赖 Repository 避免循环依赖。
2. **防超订**：`findByIdForUpdate` 悲观锁 + 事务边界，避免并发预约同一时段。
3. **AI 受限问答**：系统提示词锁定领域 + 仅注入命中的知识库 + `[OUT_OF_SCOPE]` 标记 → 后端识别替换标准拒答话术 + 每轮落 `ai_chat_message`，可审计可判定。
4. **操作日志**：AOP `@Around` 拦截所有写方法 + `@LogAction` 注解，异步 `@Async` 落库，采集 userId / operation / method / params / ip / cost。
5. **前端权限指令**：全局 `v-permission` 指令 + `stores/permission.js` 的 `hasPermission`，无权限码自动移除 DOM；路由守卫 `async` 化，登录后首次导航 `await loadPermissions()` 自愈。
6. **定时任务**：`@EnableScheduling` + `ReservationScheduledService` 的 `@Scheduled` 任务自动扫描并标记逾期未签到 / 爽约的预约，同步更新预约与实验日志状态，幂等可重复执行。

## 快速开始

### 环境要求
- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 6+
- Node.js 18+

### 后端启动

```bash
# 1. 初始化数据库
mysql -uroot -p < sql/init_data.sql

# 2. 配置环境变量（application.properties 已改为读取环境变量）
export DB_PASSWORD=your_mysql_password
export JWT_SECRET=your_jwt_secret
export DEEPSEEK_API_KEY=your_deepseek_key  # 可选，AI 助手功能需要

# 3. 启动
mvn spring-boot:run
```

### 前端启动

```bash
cd frontend
npm install
npm run dev   # http://localhost:5173
```

### 默认账号

种子密码统一为 `root`，包含管理员 / 教师 / 学生 / 维护人员四类角色账号，具体见 `sql/init_data.sql`。

## 项目结构

```
.
├── src/main/java/org/example/libaiqi2023021108/
│   ├── config/          # SecurityConfig / DeepSeekProperties / RedisConfig
│   ├── controller/      # 8 大模块 REST 接口
│   ├── service/         # 业务层
│   ├── repository/      # JPA Repository
│   ├── entity/          # 实体
│   ├── security/        # JwtAuthenticationFilter / SecurityUtils
│   ├── aspect/          # 操作日志 AOP
│   └── exception/       # GlobalExceptionHandler
├── frontend/
│   ├── src/views/       # 页面组件（24 个子页面）
│   ├── src/stores/      # Pinia（含 permission.js）
│   ├── src/router/      # 路由与守卫
│   └── vite.config.js
├── sql/                 # 初始化脚本
└── pom.xml
```

## 运行说明

本项目按上文「快速开始」在本地运行：后端 `mvn spring-boot:run`（内嵌 Tomcat 提供 REST 接口），前端 `npm run dev`（Vite 开发服务器通过 `/api` 代理访问后端）。容器化 / 反向代理等生产部署方案作为后续规划，当前仓库未包含相关配置。

## License

MIT

---

**说明**：本项目为个人学习与工程实践作品，代码结构、权限设计、AI 集成方案等均可在面试中就任一模块深入展开。
