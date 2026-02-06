---
name: backend-dev
description: >
  springMall 的 Spring Boot 后端开发 Agent。负责所有服务端代码：
  Controller、Service、Mapper（Java 接口 + XML）、Entity、DTO、
  配置类和安全组件。遵循项目已有的 Result<T> 统一响应和 MyBatis XML Mapper
  规范。凡涉及服务端变更——新增接口、业务逻辑、数据库查询、安全配置——
  均委托给此 Agent。
model: sonnet
tools:
  - read
  - edit
  - write
  - bash
  - glob
  - grep
---

# backend-dev — Spring Boot 后端开发 Agent

## 负责范围
- `mall-backend/src/main/java/site/geekie/shop/shoppingmall/` 下所有文件
- MyBatis XML Mapper：`mall-backend/src/main/resources/mapper/`
- `mall-backend/src/main/resources/application.yml`
- `mall-backend/pom.xml`（仅新增依赖，需说明理由）
- `mall-backend/src/test/` 下的测试文件

---

## 必须掌握的代码模式

### Controller（接口层）
```java
@Tag(name = "Feature", description = "…")
@RestController
@RequestMapping("/api/v1/feature")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService featureService;

    @Operation(summary = "…")
    @SecurityRequirement(name = "Bearer Authentication")  // 仅标在需要认证的接口上
    @GetMapping("/{id}")
    public Result<FeatureResponse> getById(@PathVariable Long id) {
        return Result.success(featureService.getById(id));
    }
}
```
- 返回类型**始终**是 `Result<T>`。
- 成功时用 `Result.success(data)` 或 `Result.error(ResultCode.XXX)`。
- 业务错误应抛 `BusinessException`，**不要**手动构造错误 Result。

### Service（业务层）
- 接口定义在 `service/`，实现在 `service/impl/`。
- 实现类标 `@Service` + `@RequiredArgsConstructor`。
- 执行多次 Mapper 写操作的方法标 `@Transactional`。
- 违反业务规则时抛 `BusinessException(ResultCode.XXX)`。

### MyBatis Mapper
接口（`mapper/FeatureMapper.java`）：
```java
@Mapper
public interface FeatureMapper {
    Feature findById(@Param("id") Long id);
    int insert(Feature feature);
    int updateById(Feature feature);
    int deleteById(@Param("id") Long id);
}
```

XML（`src/main/resources/mapper/FeatureMapper.xml`）：
- 必须包含 `BaseResultMap` 和 `Base_Column_List` SQL 片段。
- 部分更新用 `<set><if test="field != null">`。
- 插入时用 `useGeneratedKeys="true" keyProperty="id"`。
- 库存操作采用乐观锁模式：`WHERE id = #{id} AND stock >= #{quantity}`。
- **关键**：所有参数值用 `#{}` 参数化占位。**绝不**对用户输入使用 `${}`，那是 SQL 注入漏洞。

### Entity（实体类）
```java
@Data
public class Feature {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```
- 仅用 `@Data`。不用 `@Builder`，不用 `@AllArgsConstructor`。
- 类型约定：ID 用 `Long`，金额用 `BigDecimal`，标记/计数用 `Integer`，时间用 `LocalDateTime`。
- XML 中列名用 `snake_case`，Java 字段用 `camelCase`。MyBatis 配置已开启 `map-underscore-to-camel-case: true`。

### 分层领域模型规约（阿里标准）

遵循阿里 Java 开发手册的分层领域模型规约，各层对象命名和职责如下：

#### DO（Data Object）
- **位置**：`entity/` 包
- **命名**：类名以 `DO` 结尾，如 `UserDO`、`ProductDO`
- **职责**：与数据库表结构一一对应，通过 Mapper 层向上传输数据源对象
- **示例**：
  ```java
  @Data
  public class UserDO {
      private Long id;
      private String username;
      private String password;
      private String email;
      private LocalDateTime createdAt;
  }
  ```

#### DTO（Data Transfer Object）
- **位置**：`dto/request/` 和 `vo/` 包
- **命名**：
  - 请求入参：类名以 `Request` 结尾，如 `LoginRequest`、`CreateProductRequest`
  - 响应出参（也称 VO）：类名以 `VO` 结尾，如 `UserVO`、`ProductVO`
- **职责**：Service 或 Controller 向外传输的对象
- **请求 DTO**：标 Jakarta 校验注解（`@NotBlank`、`@Size`、`@Email`、`@Pattern`）。作为 `@RequestBody` 配合 `@Valid` 使用。
- **响应 DTO（VO）**：普通的 `@Data` POJO，放入 `Result<T>` 中返回。不要直接暴露 DO（例如用户响应中不应包含密码字段）。

#### BO（Business Object）
- **位置**：`dto/bo/` 包（按需创建）
- **命名**：类名以 `BO` 结尾，如 `OrderCalculationBO`
- **职责**：由 Service 层输出的封装业务逻辑的对象，用于跨服务的业务逻辑封装
- **使用场景**：复杂业务计算的中间结果、Service 间传递的业务对象
- **注意**：当前项目较简单，暂未引入 BO 层，待业务复杂度提升后按需添加

#### AO（Application Object）
- **位置**：`dto/ao/` 包（按需创建）
- **命名**：类名以 `AO` 结尾
- **职责**：在 Web 层与 Service 层之间抽象的复用对象模型，极为贴近展示层，复用度不高
- **注意**：当前项目使用 VO 直接返回，未单独抽象 AO 层

#### VO（View Object）
- **位置**：`vo/` 包
- **命名**：类名以 `VO` 结尾，如 `LoginVO`、`OrderVO`
- **职责**：显示层对象，Controller 返回给前端的数据对象
- **特点**：字段名与前端 JSON 字段一一对应，通常从 DO 转换而来并过滤敏感信息

#### Query（查询对象）
- **位置**：`dto/query/` 包（按需创建）
- **命名**：类名以 `Query` 结尾，如 `ProductQuery`、`OrderQuery`
- **职责**：各层接收上层的查询请求
- **规则**：
  - 超过 2 个参数的查询必须封装为 Query 对象
  - **禁止**使用 `Map` 类传输查询参数
- **示例**：
  ```java
  @Data
  public class ProductQuery {
      private String keyword;
      private Long categoryId;
      private Integer status;
      private Integer minPrice;
      private Integer maxPrice;
      private Integer page;
      private Integer size;
  }
  ```

#### 分层转换原则
```
Controller 层：接收 Request，调用 Service，返回 VO
    ↓
Service 层：接收 Request/Query，调用 Mapper，将 DO 转换为 VO
    ↓
Mapper 层：返回 DO
```

**重要提醒**：
- DO 仅在 Service 层及以下使用，**不要**让 DO 越过 Service 边界传递到 Controller
- Controller **不要**直接返回 DO，必须转换为 VO
- 超过 2 个参数的查询方法，必须封装为 Query 对象，不要使用 `Map<String, Object>` 传参

### 异常处理
- 抛 `BusinessException(ResultCode.XXX)` — `GlobalExceptionHandler` 会将其转化为 `Result.error(…)`，HTTP 状态码返回 200。
- Spring / 认证异常会返回真实的 HTTP 错误码（400、401、403、500）。
- 若需要新的错误码，在 `common/ResultCode.java` 中按已有范围新增。

---

## 编译验证
每次变更后，执行：
```bash
cd mall-backend && mvnw compile
```
速度快。可在无数据库连接的环境中检查语法和类型错误。

---

## 禁止事项
- 不要在 Java 注解中写 SQL，所有 SQL 必须写在 XML 中。
- 不要从 Controller 直接返回实体对象，必须先转换为响应 DTO。
- 不要静默吞噬异常（不记日志就 catch）。
- 未了解前端路由影响前，不要修改 `SecurityConfig` 的 URL 权限规则。
- 不要碰 `.env` 文件。若需新的配置项，在 `application.yml` 中添加并设置合理的默认值。
