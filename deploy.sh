#!/usr/bin/env bash
# ===========================================
# springMall 部署脚本
# 用法: ./deploy.sh <test|prod>
# ===========================================
set -euo pipefail

ENV="${1:-}"

if [[ "$ENV" != "test" && "$ENV" != "prod" ]]; then
  echo "用法: ./deploy.sh <test|prod>"
  echo "示例:"
  echo "  ./deploy.sh test   # 部署测试环境"
  echo "  ./deploy.sh prod   # 部署生产环境"
  exit 1
fi

ENV_FILE=".env.${ENV}"
COMPOSE_OVERRIDE="docker-compose.${ENV}.yml"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "错误: 环境变量文件 $ENV_FILE 不存在"
  echo "请先执行: cp .env.example $ENV_FILE 并填入对应环境配置"
  exit 1
fi

echo "=========================================="
echo "  springMall 部署 - ${ENV} 环境"
echo "=========================================="

# 拉取最新代码
echo "[1/3] 拉取最新代码..."
git pull

# 构建镜像
echo "[2/3] 构建 Docker 镜像..."
docker compose -f docker-compose.yml -f "$COMPOSE_OVERRIDE" --env-file "$ENV_FILE" build

# 启动服务
echo "[3/3] 启动服务..."
docker compose -f docker-compose.yml -f "$COMPOSE_OVERRIDE" --env-file "$ENV_FILE" up -d

echo ""
echo "=========================================="
echo "  部署完成!"
echo "  前端: http://localhost:${FRONTEND_PORT:-26115}"
echo "  后端: http://localhost:${BACKEND_PORT:-25116}"
echo "=========================================="
echo ""
echo "查看日志:  docker compose -f docker-compose.yml -f $COMPOSE_OVERRIDE --env-file $ENV_FILE logs -f"
echo "停止服务:  docker compose -f docker-compose.yml -f $COMPOSE_OVERRIDE --env-file $ENV_FILE down"
echo "回滚操作:  git checkout HEAD~1 && ./deploy.sh $ENV"
