#!/bin/sh
# =============================================================================
# post-edit-check.sh
# PostToolUse hook — 文件编辑或写入后，输出相应的编译/构建验证提示。
#
# Claude Code 通过 stdin 以 JSON 形式传入 hook 输入，其中包含被操作的
# 文件路径。我们对原始 JSON 做扩展名匹配，避免依赖 jq。
#
# 此 hook 始终返回退出码 0（非阻塞）。仅起提示作用，不做阻止或修改。
# =============================================================================

# 从 stdin 读取完整 JSON 输入
INPUT=$(cat)

# =============================================================================
# 通过匹配 JSON 中的文件扩展名来判断文件类型。
# 直接对原始 JSON 字符串做匹配。
# =============================================================================

# --- Java 源文件 ---
echo "$INPUT" | grep -qE '\.java"'
if [ $? -eq 0 ]; then
  echo "[编辑后检查] 修改了 Java 文件。"
  echo "  验证命令：cd mall-backend && mvnw compile"
  echo "  （快速语法/类型检查，无需数据库连接）"
  exit 0
fi

# --- MyBatis XML Mapper ---
echo "$INPUT" | grep -qE '[Mm]apper\.xml"|/mapper/[^"]*\.xml"'
if [ $? -eq 0 ]; then
  echo "[编辑后检查] 修改了 MyBatis Mapper XML 文件。"
  echo "  XML 语法错误会在运行时才暴露。若要提前发现 Mapper 绑定问题："
  echo "  验证命令：cd mall-backend && mvnw test"
  exit 0
fi

# --- Spring 应用配置 ---
echo "$INPUT" | grep -qE 'application[^"]*\.yml"'
if [ $? -eq 0 ]; then
  echo "[编辑后检查] 修改了 Spring 配置 YAML 文件。"
  echo "  请手动检查缩进 — YAML 对空白字符敏感。"
  echo "  验证命令：cd mall-backend && mvnw compile"
  exit 0
fi

# --- Vue 单文件组件 ---
echo "$INPUT" | grep -qE '\.vue"'
if [ $? -eq 0 ]; then
  echo "[编辑后检查] 修改了 Vue 组件文件。"
  echo "  验证命令：cd mall-frontend && npm run build"
  echo "  （可捕捉模板编译错误和缺失导入）"
  exit 0
fi

# --- JavaScript / TypeScript ---
echo "$INPUT" | grep -qE '\.(js|ts|jsx|tsx)"'
if [ $? -eq 0 ]; then
  echo "[编辑后检查] 修改了 JavaScript/TypeScript 文件。"
  echo "  验证命令：cd mall-frontend && npm run build"
  exit 0
fi

# --- SCSS / CSS ---
echo "$INPUT" | grep -qE '\.(scss|css)"'
if [ $? -eq 0 ]; then
  echo "[编辑后检查] 修改了样式文件。"
  echo "  验证命令：cd mall-frontend && npm run build"
  echo "  （Vite 会报出 SCSS 编译错误）"
  exit 0
fi

# --- Docker / Compose / Nginx ---
echo "$INPUT" | grep -qiE 'dockerfile|docker-compose|nginx\.conf'
if [ $? -eq 0 ]; then
  echo "[编辑后检查] 修改了基础设施配置文件。"
  echo "  语法验证：docker compose config"
  echo "  完整测试：docker compose up -d --build"
  exit 0
fi

# --- Maven POM ---
echo "$INPUT" | grep -qE 'pom\.xml"'
if [ $? -eq 0 ]; then
  echo "[编辑后检查] 修改了 Maven POM 文件。"
  echo "  验证命令：cd mall-backend && mvnw compile"
  echo "  （新增依赖会在首次编译时自动下载）"
  exit 0
fi

# --- package.json ---
echo "$INPUT" | grep -qE 'package\.json"'
if [ $? -eq 0 ]; then
  echo "[编辑后检查] 修改了 package.json。"
  echo "  若新增了依赖：cd mall-frontend && npm install"
  echo "  然后验证：npm run build"
  exit 0
fi

# --- .env.example ---
echo "$INPUT" | grep -qE '\.env\.example"'
if [ $? -eq 0 ]; then
  echo "[编辑后检查] 修改了 .env.example。"
  echo "  若变更影响本地环境，请手动将 .env.example 复制为 .env。"
  exit 0
fi

# =============================================================================
# 兜底 — 未匹配到特定文件类型
# =============================================================================
echo "[编辑后检查] 文件已修改。如需要请手动确认是否需要执行构建验证。"
exit 0
