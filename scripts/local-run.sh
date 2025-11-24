#!/bin/bash

###############################################################################
# Скрипт запуска Klassifikator локально для тестирования
# Версия: 1.0
###############################################################################

set -e

# Цвета
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
success() { echo -e "${GREEN}✅ $1${NC}"; }
error() { echo -e "${RED}❌ $1${NC}"; }
warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }

info "========================================="
info "  Запуск Klassifikator локально"
info "========================================="
echo ""

# Переходим в корневую директорию проекта
cd "$(dirname "$0")/.."

# 1. Запускаем инфраструктуру
info "Запуск инфраструктуры (PostgreSQL, Redis, MinIO)..."
docker-compose up -d postgres redis minio minio-setup
sleep 5
success "Инфраструктура запущена"

# 2. Собираем проект
info "Сборка проекта..."
./gradlew clean build -x test
success "Проект собран"

# 3. Устанавливаем переменные окружения
export SPRING_PROFILES_ACTIVE=dev
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/klassifikator_dev
export SPRING_DATASOURCE_USERNAME=klassifikator
export SPRING_DATASOURCE_PASSWORD=klassifikator_dev_password
export SPRING_DATA_REDIS_HOST=localhost
export SPRING_DATA_REDIS_PORT=6379
export S3_ENDPOINT=http://localhost:9000
export S3_ACCESS_KEY=minioadmin
export S3_SECRET_KEY=minioadmin
export S3_BUCKET_NAME=klassifikator

echo ""
info "========================================="
info "  Инфраструктура готова!"
info "========================================="
echo ""
info "PostgreSQL: localhost:5432"
info "Redis:      localhost:6379"
info "MinIO:      localhost:9000 (Console: localhost:9001)"
echo ""
info "Переменные окружения установлены:"
info "  SPRING_DATASOURCE_URL=$SPRING_DATASOURCE_URL"
info "  SPRING_DATA_REDIS_HOST=$SPRING_DATA_REDIS_HOST"
echo ""
warning "Для запуска сервисов используйте:"
echo ""
echo "  # API Gateway"
echo "  ./gradlew :api-gateway:bootRun"
echo ""
echo "  # Landing Service"
echo "  ./gradlew :landing-service:bootRun"
echo ""
echo "  # Content Service"
echo "  ./gradlew :content-service:bootRun"
echo ""
echo "  # Template Service"
echo "  ./gradlew :template-service:bootRun"
echo ""
info "Или запустите все сервисы в отдельных терминалах"
echo ""

