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
| 用户与权限 | RBAC 模型（4 类角色 / 28 权限码），JWT 鉴权，接口层 `@PreAuthorize` 细粒度控制 |
| 设备管理 | 设备台账、状态跟踪、报修与保养流程 |
| 预约审批 | 预约规则校验（提前天数 / 时长 / 次数 / 资格等级），Redis 分布式锁防超订，签到签退全流程 |
| 实验记录 | 与预约一一对应，双向状态同步，终态拦截 |
| 危化品 | 领用 / 归还登记，库存与操作留痕 |
| 公告管理 | 分类 CRUD、发布 / 撤回、富文本详情 |
| 数据统计 | 学生个人视图与全局看板分离，按 `currentUserId` 隔离 |
| AI 助手 | 受限知识库问答，越界问题可判定拒答并留痕 |

## 项目亮点

1. **权限体系**：`JwtAuthenticationFilter` 登录时装载权限码 + `ROLE_AUTHENTICATED` 到 `SecurityContext`；读写拆分（读用 `ROLE_AUTHENTICATED`、写用 `xxx:manage` 权限码）；`PermissionResolverService` 刻意只依赖 Repository 避免循环依赖。
2. **防超订**：基于 Redis（Redisson）分布式锁包裹数据库事务，抢锁 → 事务内校验+落库 → 提交 → 释放锁，串行化同一资源的并发预约；锁释放前数据已持久化，避免多请求 / 多实例在锁间隙读到未提交数据而重复预约。
3. **AI 受限问答**：系统提示词锁定领域 + 仅注入命中的知识库 + `[OUT_OF_SCOPE]` 标记 → 后端识别替换标准拒答话术 + 每轮落 `ai_chat_message`，可审计可判定。
4. **操作日志**：AOP `@Around` 拦截所有写方法 + `@LogAction` 注解，异步 `@Async` 落库，采集 userId / operation / method / params / ip / cost。
5. **前端权限指令**：全局 `v-permission` 指令 + `stores/permission.js` 的 `hasPermission`，无权限码自动移除 DOM；路由守卫 `async` 化，登录后首次导航 `await loadPermissions()` 自愈。
6. **定时任务**：`@EnableScheduling` + `ReservationScheduledService` 的 `@Scheduled` 任务自动扫描并标记逾期未签到 / 爽约的预约，同步更新预约与实验日志状态，幂等可重复执行。

## 快速开始

> 前后端分离：后端 Spring Boot 内嵌 Tomcat 监听 8080；前端 Vue3 + Vite 监听 5173，并通过 Vite 代理把 `/api` 转发到 `http://localhost:8080`。

### 环境要求

| 依赖 | 版本 | 说明 |
|---|---|---|
| JDK | 21+ | 后端编译/运行 |
| Maven | 3.8+ | 后端构建（也可用自带 mvnw） |
| MySQL | 8.0+ | 主数据库 |
| Redis | 6+ | **必需**：集成 Redisson 分布式锁 + 权限缓存，未启动 Redis 后端会启动失败 |
| Node.js | 18+ | 前端构建 |

### 1. 克隆仓库

```bash
git clone https://github.com/nuecu/libaiqi-lab-management.git
cd libaiqi-lab-management
```

### 2. 初始化数据库

执行 `sql/init_data.sql`，它会创建 `lab_management` 库、建表并灌入种子数据（用户/角色/权限/设备/预约等），且可重复执行：

```bash
mysql -uroot -p < sql/init_data.sql
```

- 种子账号登录密码统一为 `root`。
- `sql/fix_missing_permissions.sql` 与 `sql/fix_missing_operation_permissions.sql` 是给「早期版本数据库」补权限码的幂等补丁；全新按本脚本初始化者无需执行，仅当旧库登录后写/审批/AI 操作报 403 时才补跑并清 Redis 缓存。

### 3. 启动 Redis

```bash
redis-server            # macOS / Linux
redis-server redis.windows.conf   # Windows
```

### 4. 启动后端（8080）

```bash
mvn spring-boot:run
# 或： mvn -DskipTests package && java -jar target/*.jar
```

所有配置均支持环境变量覆盖，本地默认（MySQL root、空密码）不改即可直接运行：

| 环境变量 | 默认 | 何时设置 |
|---|---|---|
| `DB_USERNAME` / `DB_PASSWORD` | root / (空) | MySQL 用户名/密码与默认不同时 |
| `REDIS_HOST` / `REDIS_PASSWORD` | localhost / (空) | Redis 非本机或有密码 |
| `JWT_SECRET` | 内置占位值 | 生产环境务必设置 |
| `DEEPSEEK_API_KEY` | (空) | 需要 AI 问答功能时填入（不填不影响其它功能） |

### 5. 启动前端（5173）

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173 。

### 默认账号

| 角色 | 用户名 | 密码 |
|---|---|---|
| 系统管理员 | admin | root |
| 教师 | teacher1 / teacher2 | root |
| 学生 | student1 / student2 / student3 | root |
| 设备维护员 | maintainer1 | root |

### 常见问题

- **后端启动即报连不上 Redis**：先确认 Redis 已在 6379 运行，Redisson 是启动硬依赖。
- **登录 500 / 数据库连不上**：确认 MySQL 已启动、库名 `lab_management`、必要时设置 `DB_USERNAME`/`DB_PASSWORD`。
- **新增/编辑/审批/发布/AI 返回 403**：旧库缺操作权限码，补跑 `sql/fix_missing_operation_permissions.sql` → `redis-cli flushall` → 重新登录。
- **改了角色/权限不生效**：权限缓存在 Redis `userPermissions`，`redis-cli flushall` 后重新登录即可。

## 项目结构

```
.
├── src/main/java/org/example/libaiqi2023021108/
│   ├── config/          # SecurityConfig / JwtAuthenticationFilter / RedisConfig / RedissonConfig / DeepSeekProperties
│   ├── controller/      # 8 大模块 REST 接口
│   ├── service/         # 业务层
│   ├── repository/      # JPA Repository
│   ├── entity/          # 实体
│   ├── common/          # SecurityUtils / 通用工具
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
