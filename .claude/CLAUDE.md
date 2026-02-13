# springMall — Claude Code Agents 项目宪章

---

## 1. 项目架构概览

springMall 是一个全栈电商应用项目。

### 后端（`mall-backend/`）
- **框架**：Spring Boot 3.3.6，Java 21
- **ORM**：MyBatis，XML Mapper（`classpath:mapper/*.xml`）
- **安全**：Spring Security + JWT（JJWT 0.12.6），无状态会话
- **数据库**：MySQL 8，HikariCP 连接池
- **接口风格**：RESTful，所有路径以 `/api/v1/` 为前缀
- **响应格式**：统一 `Result<T>` 包装 — `{ code, message, data }`
- **校验**：Jakarta Bean Validation（`@Valid` 作用于 `@RequestBody`）
- **接口文档**：Swagger，由 springdoc-openapi 2.3.0 提供
- **构建**：Maven（`mvnw` 包装器）

包根路径：`site.geekie.shop.shoppingmall`

| 包目录 | 职责 |
|---|---|
| `controller/` | HTTP 接口层。返回 `Result<T>`，不写业务逻辑。 |
| `controller/admin/` | 管理员专用接口（需 ADMIN 角色）。 |
| `service/` | 业务逻辑接口定义。 |
| `service/impl/` | 业务逻辑实现。多写操作的方法标 `@Transactional`。 |
| `mapper/` | MyBatis Mapper 接口。仅用 `@Mapper` + `@Param`。 |
| `entity/` | 映射到数据库行的实体类。用 `@Data`，不用 `@Builder`。 |
| `dto/request/` | 已校验的请求入参。 |
| `dto/response/` | 放入 `Result<T>` 中返回给前端的响应对象。 |
| `common/` | `Result<T>`、`ResultCode` 枚举、`PageResult`、`OrderStatus`。 |
| `config/` | `SecurityConfig`、`SwaggerConfig`、CORS 配置。 |
| `security/` | `JwtTokenProvider`、`JwtAuthenticationFilter`、`UserDetailsServiceImpl`。 |
| `exception/` | `BusinessException`、`GlobalExceptionHandler`。 |
| `util/` | `OrderNoGenerator` 及其他工具类。 |

### 前端（`mall-frontend/`）
- **框架**：Vue 3（`<script setup>` / Composition API）
- **状态管理**：Pinia — Options 风格 `defineStore`（`state`/`getters`/`actions`）
- **UI 组件库**：Element Plus 2.13+
- **HTTP 客户端**：Axios，封装在 `src/api/request.js`
- **构建工具**：Vite 7 + Sass
- **生产构建**：`npm run build` → `dist/`
- **开发服务器**：`npm run dev`（端口 3000，`/api/` 代理到 localhost:8080）

| 目录 | 职责 |
|---|---|
| `src/api/` | Axios 接口调用模块，按业务领域一个文件（`product.js`、`cart.js`…） |
| `src/store/` | Pinia Store（`auth.js`、`cart.js`、`app.js`、`user.js`） |
| `src/views/` | 页面级组件。子目录：`user/`、`admin/`、`auth/` |
| `src/components/` | 可复用组件。`common/` 中有 `Loading.vue`、`Empty.vue` |
| `src/layouts/` | 布局包装组件（`UserLayout`、`AdminLayout`） |
| `src/router/` | Vue Router 配置 + 导航守卫 |
| `src/utils/` | 纯工具函数：`storage.js`、`format.js`、`constants.js`、`validate.js` |
| `src/assets/` | SCSS 变量、重置样式、公共样式 |

### 部署（项目根目录）
- `docker-compose.yml`：后端宿主端口 **25116**，前端宿主端口 **26115**
- 前端 Nginx 将 `/api/` 代理到 `backend:8080`
- 后端通过 `host.docker.internal` 连接宿主机 MySQL
- 密钥和凭据通过 `.env` → docker-compose 变量插值传入

---

## 2. Agent 团队花名册

| Agent | 模型 | 负责范围 | 委托时机 |
|---|---|---|---|
| `backend-dev` | sonnet | Controller、Service、Mapper、Entity、DTO、Config、Security | 任何服务端变更：新接口、业务逻辑、数据库查询、安全配置 |
| `frontend-dev` | sonnet | Vue 页面、组件、Pinia Store、Router、API 层、SCSS | 任何 UI 变更：新页面、组件、Store Action、API 对接 |
| `code-reviewer` | sonnet | 安全审查、OWASP 检查、代码规范一致性 | backend-dev 或 frontend-dev 完成变更之后。合并前必经环节。仅读不写。 |
| `test-validator` | haiku | 执行 `mvnw test` 和 `npm run build`，报告通过/失败 | code-reviewer 审批通过后。确认无回归。 |
| `devops-deploy` | sonnet | Dockerfile、docker-compose、nginx.conf、.env 管理 | 任何基础设施或部署相关变更。也在 test-validator 通过且涉及部署文件时启动。 |
| `doc-writer` | sonnet | `mall-backend/DevDoc/` 下所有开发文档 | backend-dev 完成变更后。生成阶段交付文档或规划/设计文档，也负责更新已有文档。 |

### 委托决策树
1. 纯基础设施 / Docker / 部署 → **devops-deploy**
2. 涉及 Java 源文件 → **backend-dev**
3. 涉及 Vue / JS / SCSS 文件 → **frontend-dev**
4. 同时涉及前后端 → **先 backend-dev**（API 合约是源头），再 **frontend-dev**，最后 **code-reviewer** 审查两者
5. backend-dev 完成变更后 → **doc-writer** 根据交接信息生成或更新 `DevDoc/` 文档（可与 code-reviewer 并行）

---
## 3. 编码规范

### Java（后端）
- **Lombok**：实体类用 `@Data`。Service 和 Controller 中用 `@RequiredArgsConstructor` 做依赖注入。
- **MyBatis**：所有 SQL 写在 `src/main/resources/mapper/` 下的 XML 文件中。**不要**在 Java 中用 `@Select`/`@Insert` 内联写 SQL。每个 XML 必须包含 `BaseResultMap` 和 `Base_Column_List` 片段。
- **Result<T>**：Controller 的每个方法都返回 `Result<T>`。用 `Result.success(data)`、`Result.success(message, data)` 或 `Result.error(ResultCode.XXX)`。不要直接返回原始对象或 `ResponseEntity`。
- **异常处理**：业务错误抛 `BusinessException(ResultCode)`，由 `GlobalExceptionHandler` 转化为 `Result` 响应。不要静默吞噬异常。
- **Swagger**：Controller 类标 `@Tag`。每个方法标 `@Operation`。需要认证的方法标 `@SecurityRequirement(name = "Bearer Authentication")`。
- **校验**：`@RequestBody` 参数标 `@Valid`。约束注解写在 DTO 字段上。
- **事务**：执行多次写操作的 Service 方法标 `@Transactional`。
- **分层领域模型规约**（阿里标准）：
  - **DO（Data Object）**：`entity/` 包，类名以 `DO` 结尾（如 `UserDO`）。与数据库表结构一一对应，通过 Mapper 层向上传输数据源对象。
  - **DTO（Data Transfer Object）**：`dto/request/` 和 `vo/` 包。Service 或 Controller 向外传输的对象。请求入参以 `Request` 结尾，响应出参以 `VO` 结尾。
  - **BO（Business Object）**：`dto/bo/` 包（按需创建）。由 Service 层输出的封装业务逻辑的对象。
  - **VO（View Object）**：`vo/` 包，类名以 `VO` 结尾（如 `UserVO`）。显示层对象，Controller 返回给前端的数据对象。
  - **Query**：`dto/query/` 包（按需创建）。数据查询对象，各层接收上层的查询请求。**超过 2 个参数的查询必须封装为 Query 对象，禁止使用 Map 传输查询参数**。
  - **分层转换原则**：DO 仅在 Service 层及以下使用。Controller **不要**直接返回 DO，必须转换为 VO。

### Vue（前端）
- **组件**：使用 `<script setup>`（Composition API）。从 `'vue'` 中导入 `ref`、`onMounted` 等。
- **Store**：使用 Options 风格的 `defineStore`（`state` 函数、`getters` 对象、`actions` 对象）。**不要**使用 setup 风格的 Store。
- **API 层**：`src/api/` 中按业务领域各一个文件。导入 `'@/api/request'` 的共享实例。导出命名的 async 函数。响应拦截器已自动解包 `Result<T>.data`，API 函数直接返回数据实体。
- **UI**：使用 Element Plus 组件（`el-input`、`el-button`、`el-table` 等）。用 `ElMessage` 做通知提示，`ElMessageBox` 做确认弹窗。
- **样式**：`<style scoped lang="scss">`。从 `'@/assets/styles/variables.scss'` 导入变量。
- **路由**：用动态 `import()` 懒加载路由。用 `meta: { requiresAuth, requiresAdmin }` 控制守卫。

---

## 4. 安全策略

以下规则不得违反，任何 Agent 均无权覆盖。

1. **绝不**修改、创建或 commit `.env` 文件。只能编辑 `.env.example`，并手动通知开发者复制。
2. **绝不** commit 密钥：JWT Secret、数据库密码、API Key。若暂存区中检测到，立即中止。
3. **绝不**在未写明回滚方案的情况下修改 `schema.sql` 或任何数据库迁移脚本。
4. **绝不**在未获得用户明确确认的情况下执行 `rm -rf`、`git reset --hard`、`git checkout .`、`DROP TABLE` 或 `format` 命令。（`block-dangerous.sh` hook 自动执行此拦截。）
5. `application.yml` 中的 JWT Secret 和数据库密码仅供本地开发使用。Docker 部署时由环境变量覆盖。保持此模式不变。
7. 后端 `Dockerfile` 的 HEALTHCHECK 指向 `/actuator/health`，但项目未引入 actuator 依赖。正确的健康检查端点是 `/api/v1/categories`（`docker-compose.yml` 中已正确使用）。不要仅为修补此问题而引入 actuator。

---

## 6. 团队协作流程

### 简单提问或修改
直接调用主agent进行回答

### 复杂功能交付流程
```
用户发起功能需求
    │
    ▼
主 Session — 分析需求范围
    │
    ├── 需要后端变更？  ──►  backend-dev 实现
    │                            │
    │                            ├──► doc-writer — 生成/更新 DevDoc
    │                            │
    ├── 需要前端变更？  ──►  frontend-dev 实现
    │                            │
    ▼                            ▼
    └──────────┬─────────────────┘
               ▼
        code-reviewer — 审查所有变更文件（仅读不写）
               │
               ▼
        test-validator — mvnw test + pnpm run build(用户请求测试时才触发)
               │
               ▼
  （若涉及基础设施）devops-deploy — 验证 Docker 配置(用户请求测试时才触发)
               │
               ▼
    主 Session 将结果汇报给用户
```

### 交接协议
- **backend-dev** 完成后 → 说明：修改了哪些文件、API 合约（接口路径、请求/响应结构）、是否需要数据库迁移。
- **frontend-dev** 在开始前读取上述交接信息，确保拿到正确的 API 结构。
- **doc-writer** 收到 backend-dev 交接信息后启动。自行读取代码验证接口细节，生成或更新 `mall-backend/DevDoc/` 文档。可与 code-reviewer 并行执行。
- **code-reviewer** 收到所有变更文件的清单，逐一阅读。不修改任何文件，仅输出按 BLOCKER / WARNING / INFO 分级的审查报告。
- **test-validator** 用户请求测试时才触发，不阅读源代码。仅运行构建和测试命令，报告退出码和错误输出。
- **devops-deploy** 用户请求测试时才触发。

### 并行与顺序执行规则
- `backend-dev` 和 `frontend-dev` **仅当** API 合约已存在（已记录在 `.claude/skills/API_document/references/api-endpoints.md` 中）时才可并行执行。
- 若 API 合约是新增的 → `backend-dev` 必须先完成。
- `code-reviewer` 必须在两位开发 Agent 都完成之后才运行，不可与之并行。
- `test-validator` 必须在 `code-reviewer` 之后运行，不可跳过。
