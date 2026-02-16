# Spring Mall Docker 部署文档

> **部署模式**: 混合模式 - 前后端容器化，连接本机 MySQL 数据库

## 目录

- [环境要求](#环境要求)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [详细配置](#详细配置)
- [部署步骤](#部署步骤)
- [常用命令](#常用命令)
- [故障排除](#故障排除)
- [生产环境建议](#生产环境建议)

---

## 环境要求

### 技术栈版本

| 组件 | 版本 |
|------|------|
| Docker | 20.10+ |
| Docker Compose | 2.0+ |
| JDK | 21 |
| Maven | 3.9.6 |
| Node.js | 20 (构建时) |
| MySQL | 8.0.37 |
| Nginx | Alpine |
| Spring Boot | 3.2.5 |
| Vue | 3.5+ |

### 服务器要求

- **最低配置**: 2 CPU / 4GB RAM / 40GB SSD
- **推荐配置**: 4 CPU / 8GB RAM / 100GB SSD
- **操作系统**: Ubuntu 22.04 / CentOS 8+ / Debian 11+

---

## 项目结构

```
springMall/
├── docker-compose.yml          # Docker Compose 主配置
├── .env.example                # 环境变量示例
├── .env                        # 环境变量配置（需自行创建）
├── init-sql/                   # MySQL 初始化脚本目录
│   └── init.sql               # 数据库初始化脚本
├── mall-backend/
│   ├── Dockerfile             # 后端 Docker 镜像配置
│   ├── .dockerignore          # Docker 构建忽略文件
│   └── src/main/resources/
│       └── application-docker.yml  # Docker 环境配置
└── mall-frontend/
    ├── Dockerfile             # 前端 Docker 镜像配置
    ├── .dockerignore          # Docker 构建忽略文件
    └── nginx.conf             # Nginx 配置
```

---

## 快速开始

### 1. 克隆项目

```bash
git clone <your-repo-url>
cd springMall
```

### 2. 确保本机 MySQL 已启动

```bash
# Windows: 确保 MySQL 服务已启动
# 确保 mall 数据库已存在且数据已初始化
```

### 3. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 文件，修改为本机数据库的用户名和密码
```

### 4. 启动服务

```bash
docker compose up -d --build
```

### 5. 访问服务

- **前端**: http://localhost:26115
- **后端 API**: http://localhost:26115/api/v1
- **Swagger UI**: http://localhost:26115/swagger-ui/

---

## 详细配置

### 环境变量说明 (.env)

```bash
# 本机 MySQL 数据库配置
DB_USERNAME=root                  # 本机数据库用户名
DB_PASSWORD=123456                # 本机数据库密码

# JWT 配置
JWT_SECRET=your-secret-key        # JWT 签名密钥（生产环境必须修改）
JWT_EXPIRATION=86400000           # Token 过期时间（毫秒，默认24小时）

# 支付宝配置（仅在启用支付功能时需要）
ALIPAY_APP_ID=your-sandbox-app-id        # 支付宝应用ID
ALIPAY_PRIVATE_KEY=your-private-key      # 商户私钥 (RSA2)
ALIPAY_PUBLIC_KEY=alipay-public-key      # 支付宝公钥
ALIPAY_NOTIFY_URL=http://...             # 异步通知地址（需外网可访问）
ALIPAY_RETURN_URL=http://...             # 同步返回地址
FRONTEND_URL=http://...                  # 前端 URL
```

### 端口映射

| 服务 | 容器内端口 | 宿主机端口 | 说明 |
|------|-----------|-----------|------|
| Frontend | 80 | 26115 | 前端 + Nginx 反向代理 |
| Backend | 8080 | 8080 | Spring Boot API |

> **注意**: 数据库使用本机 MySQL，无需容器化。确保本机 MySQL 服务已启动且 `mall` 数据库已创建。

---

## 部署步骤

### 方式一：完整构建部署

适用于首次部署或代码有更新时：

```bash
# 1. 停止现有服务
docker compose down

# 2. 重新构建并启动
docker compose up -d --build

# 3. 查看日志
docker compose logs -f
```

### 方式二：仅重启服务

适用于配置更新，无代码变更：

```bash
docker compose restart
```

### 方式三：单独构建某个服务

```bash
# 仅重新构建后端
docker compose up -d --build backend

# 仅重新构建前端
docker compose up -d --build frontend
```

---

## 常用命令

### 服务管理

```bash
# 启动所有服务
docker compose up -d

# 停止所有服务
docker compose down

# 重启所有服务
docker compose restart

# 查看服务状态
docker compose ps

# 查看所有日志
docker compose logs -f

# 查看特定服务日志
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql
```

### 容器操作

```bash
# 进入后端容器
docker exec -it mall-backend sh

# 进入前端容器
docker exec -it mall-frontend sh

# 进入 MySQL 容器
docker exec -it mall-mysql mysql -u root -p
```

### 镜像管理

```bash
# 查看镜像
docker images | grep mall

# 清理无用镜像
docker image prune -f

# 完全清理（包括数据卷）
docker compose down -v --rmi all
```

### 数据备份

```bash
# 备份 MySQL 数据
docker exec mall-mysql mysqldump -u root -p mall > backup_$(date +%Y%m%d).sql

# 恢复 MySQL 数据
docker exec -i mall-mysql mysql -u root -p mall < backup.sql
```

---

## 支付宝支付功能部署

### 概述

springMall 支持支付宝 PC 网站支付功能。此部分说明如何配置支付宝集成。

### 沙箱环境配置

#### 第一步：获取支付宝沙箱账号

1. 访问 [支付宝开放平台](https://openhome.alipay.com/)
2. 登录或注册开发者账号
3. 进入 [开发者中心](https://openhome.alipay.com/platform/home.htm)
4. 点击 "应用" > "我的应用" > "创建应用"
5. 选择 "PC 网站支付" 应用类型
6. 完成应用信息填写并提交（沙箱环境无需审核）

#### 第二步：获取应用密钥

1. 在应用管理页面选择你的应用
2. 点击 "开发设置" 标签
3. 在 "接口加签方式" 部分，点击 "生成密钥"
4. 选择 RSA2（推荐）加签方式
5. **重要**: 妥善保管以下三个信息：
   - **应用 ID** (ALIPAY_APP_ID)
   - **商户应用私钥** (ALIPAY_PRIVATE_KEY) - 需复制包含 `-----BEGIN RSA PRIVATE KEY-----` 的完整内容
   - **支付宝公钥** (ALIPAY_PUBLIC_KEY) - 从支付宝公钥证书中提取

#### 第三步：上传公钥到支付宝后台

1. 在 "开发设置" 页面的 "接口加签方式" 中
2. 上传你的 **商户应用公钥**（与私钥对应）
3. 点击保存

#### 第四步：配置 URL

在支付宝应用设置中配置以下 URL：

| 参数 | 值 | 说明 |
|-----|---|-----|
| 授权回调地址 | `http://localhost:26115/payment/result` | 支付成功后用户跳转地址 |
| 消息接收地址 | `http://localhost:25116/api/v1/payment/alipay/notify` | 支付宝异步通知地址（本地需内网穿透） |

### 本地开发配置

#### 配置环境变量

1. 复制 `.env.example` 为 `.env`：
   ```bash
   cp .env.example .env
   ```

2. 填写支付宝配置（从沙箱环境获取）：
   ```bash
   ALIPAY_APP_ID=2021000121677669
   ALIPAY_PRIVATE_KEY=MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDI...（完整的私钥）
   ALIPAY_PUBLIC_KEY=MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...（完整的公钥）
   ALIPAY_NOTIFY_URL=http://localhost:25116/api/v1/payment/alipay/notify
   ALIPAY_RETURN_URL=http://localhost:26115/payment/result
   FRONTEND_URL=http://localhost:26115
   ```

#### 使用内网穿透工具（ngrok）处理异步通知

由于本地开发环境无法从外网直接访问，需要使用内网穿透工具：

1. **安装 ngrok**
   ```bash
   # 从 https://ngrok.com/download 下载
   # 或使用 Homebrew (macOS)
   brew install ngrok
   ```

2. **启动本地后端**
   ```bash
   docker compose up -d backend
   ```

3. **运行 ngrok 转发**
   ```bash
   # 将 ngrok 转发到本地后端
   ngrok http 25116
   ```

4. **更新 .env 文件**
   ```bash
   # ngrok 会输出类似: https://xxxx.ngrok.io
   # 更新异步通知地址
   ALIPAY_NOTIFY_URL=https://xxxx.ngrok.io/api/v1/payment/alipay/notify
   ```

5. **重启后端容器**
   ```bash
   docker compose restart backend
   ```

### 生产环境配置

#### 关键要点

1. **HTTPS 支持**: 所有支付宝相关 URL 必须使用 HTTPS 协议
2. **异步通知地址**: 必须是外网可访问的地址（公网 IP 或域名）
3. **密钥安全**:
   - 生产环境私钥通过 CI/CD 管道或密钥管理系统安全注入
   - 绝不在代码库中硬编码私钥
   - 定期轮换密钥
   - 使用环境变量或密钥管理工具（如 HashiCorp Vault）

#### 部署步骤

1. **配置生产环境变量**
   ```bash
   # 在服务器上创建 .env 文件或使用系统环境变量
   export ALIPAY_APP_ID="production-app-id"
   export ALIPAY_PRIVATE_KEY="production-private-key"
   # ... 其他配置
   ```

2. **启动容器**
   ```bash
   docker compose up -d --build
   ```

3. **验证支付功能**
   ```bash
   # 查看后端日志是否有支付宝配置加载
   docker compose logs backend | grep -i alipay
   ```

### 数据库表说明

支付宝支付功能依赖以下数据库表：

#### mall_payment 表（支付记录）

存储每笔支付的记录，用于追踪支付状态：

| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键 |
| payment_no | VARCHAR(50) | 系统支付流水号（唯一） |
| order_no | VARCHAR(50) | 关联的订单号 |
| user_id | BIGINT | 支付用户 |
| amount | DECIMAL(10,2) | 支付金额 |
| payment_method | VARCHAR(20) | 支付方式（ALIPAY/WECHAT） |
| payment_status | VARCHAR(20) | 支付状态（PENDING/SUCCESS/FAILED/CLOSED） |
| trade_no | VARCHAR(100) | 支付宝交易号 |
| notify_time | DATETIME | 异步通知时间 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### mall_order 表（新增字段）

| 字段 | 类型 | 说明 |
|-----|------|------|
| payment_method | VARCHAR(20) | 支付方式（ALIPAY/WECHAT） |
| payment_time | DATETIME | 支付完成时间 |

### 支付流程

1. **用户创建订单** → 订单状态：UNPAID
2. **用户点击支付** → 系统生成支付记录 (payment_status: PENDING)
3. **用户在支付宝完成支付** → 支付宝发送异步通知
4. **后端处理异步通知** → 更新订单和支付记录状态
5. **用户重定向到结果页** → 显示支付结果

### 常见问题排查

#### 异步通知无法接收

**症状**: 支付完成后，订单状态仍为 UNPAID

**原因**:
1. ALIPAY_NOTIFY_URL 配置错误
2. 防火墙阻止支付宝 IP
3. 本地开发未使用内网穿透

**解决**:
```bash
# 查看后端日志
docker compose logs backend | tail -50

# 确认异步通知地址配置
docker compose exec backend cat /proc/1/environ | grep ALIPAY_NOTIFY_URL
```

#### 支付页面打不开

**症状**: 点击支付按钮，页面无反应

**原因**:
1. ALIPAY_APP_ID 配置错误
2. 私钥格式不正确
3. 后端服务未启动

**解决**:
```bash
# 检查后端是否正常运行
docker compose ps backend

# 查看后端错误日志
docker compose logs backend | grep -i error

# 测试后端健康检查
curl http://localhost:25116/api/v1/categories
```

---

## 故障排除

### 1. 后端无法连接数据库

**症状**: 后端日志显示 `Connection refused` 或 `Access denied`

**解决方案**:
```bash
# 检查 MySQL 是否正常启动
docker compose logs mysql

# 确认 MySQL 健康检查通过
docker compose ps

# 验证数据库连接
docker exec -it mall-mysql mysql -u mall -p -e "SELECT 1"
```

### 2. 前端无法访问后端 API

**症状**: 浏览器显示 502 或 API 请求失败

**解决方案**:
```bash
# 检查后端是否正常运行
docker compose logs backend

# 检查容器网络连通性
docker exec mall-frontend wget -qO- http://backend:8080/api/v1/categories

# 检查 Nginx 配置
docker exec mall-frontend cat /etc/nginx/conf.d/default.conf
```

### 3. 构建失败

**症状**: Docker build 过程中报错

**解决方案**:
```bash
# 清理 Docker 缓存后重新构建
docker compose build --no-cache

# 检查网络连接（可能是 npm/maven 下载超时）
# 可以配置镜像源加速
```

### 4. 容器内存不足

**症状**: 服务频繁重启或 OOM Killed

**解决方案**:
```bash
# 查看容器资源使用
docker stats

# 在 docker-compose.yml 中限制资源
# services:
#   backend:
#     deploy:
#       resources:
#         limits:
#           memory: 1G
```

---

## 生产环境建议

### 1. 安全配置

- [ ] 修改默认数据库密码
- [ ] 使用强随机 JWT 密钥
- [ ] 不暴露 MySQL 端口到公网
- [ ] 配置 HTTPS（使用 Let's Encrypt）
- [ ] 启用防火墙，仅开放必要端口

### 2. HTTPS 配置

创建 `nginx-ssl.conf`:

```nginx
server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;

    # ... 其他配置同 nginx.conf
}
```

### 3. 日志管理

```yaml
# docker-compose.yml 中添加日志限制
services:
  backend:
    logging:
      driver: "json-file"
      options:
        max-size: "100m"
        max-file: "3"
```

### 4. 监控建议

- 使用 Prometheus + Grafana 监控容器和应用指标
- 配置日志收集（ELK Stack 或 Loki）
- 设置告警通知

### 5. 备份策略

```bash
# 创建定时备份脚本 /opt/backup-mall.sh
#!/bin/bash
BACKUP_DIR=/backup/mysql
DATE=$(date +%Y%m%d_%H%M%S)
docker exec mall-mysql mysqldump -u root -proot123456 mall > $BACKUP_DIR/mall_$DATE.sql
# 保留最近 7 天的备份
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
```

```bash
# 添加到 crontab
0 2 * * * /opt/backup-mall.sh
```

---

## 版本信息

| 组件 | 版本 |
|------|------|
| Spring Boot | 3.2.5 |
| Spring MVC | 6.1.6 |
| MyBatis | 3.5.16 |
| MySQL | 8.0.37 |
| Maven | 3.9.6 |
| JDK | 21 |
| springdoc-openapi | 2.3.0 |
| Vue | 3.5+ |
| Vite | 7.2+ |
| Element Plus | 2.13+ |
| Node.js | 20 |
| Nginx | Alpine |

---

## 联系与支持

如有问题，请提交 Issue 或联系项目维护者。
