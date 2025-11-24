#!/bin/bash

###############################################################################
# Скрипт запуска всех микросервисов Klassifikator
# Версия: 2.0
# Дата: 2025-11-03
###############################################################################

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Функция для вывода цветного текста
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Функция проверки доступности порта
check_port() {
    local port=$1
    local service=$2
    
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        log_warning "Порт $port уже занят ($service)"
        return 1
    else
        log_info "Порт $port свободен ($service)"
        return 0
    fi
}

# Функция ожидания готовности сервиса
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=0
    
    log_info "Ожидание запуска $service_name..."
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            log_success "$service_name готов!"
            return 0
        fi
        
        attempt=$((attempt + 1))
        echo -n "."
        sleep 2
    done
    
    echo ""
    log_error "$service_name не запустился за $((max_attempts * 2)) секунд"
    return 1
}

# Заголовок
echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║         Klassifikator - Запуск микросервисов               ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Проверка наличия Java
log_info "Проверка Java..."
if ! command -v java &> /dev/null; then
    log_error "Java не найдена. Установите JDK 17 или выше."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    log_error "Требуется Java 17 или выше. Текущая версия: $JAVA_VERSION"
    exit 1
fi
log_success "Java версия: $JAVA_VERSION"

# Проверка Docker
log_info "Проверка Docker..."
if ! command -v docker &> /dev/null; then
    log_error "Docker не найден. Установите Docker."
    exit 1
fi
log_success "Docker найден"

# Запуск Docker Compose (PostgreSQL, Redis, MinIO)
log_info "Запуск инфраструктуры (PostgreSQL, Redis, MinIO)..."
if docker compose ps | grep -q "Up"; then
    log_warning "Docker Compose уже запущен"
else
    docker compose up -d
    log_success "Docker Compose запущен"
fi

# Ожидание готовности PostgreSQL
log_info "Ожидание готовности PostgreSQL..."
sleep 5
if docker exec klassifikator-postgres pg_isready -U klassifikator_user > /dev/null 2>&1; then
    log_success "PostgreSQL готов"
else
    log_error "PostgreSQL не готов"
    exit 1
fi

# Ожидание готовности Redis
log_info "Ожидание готовности Redis..."
if docker exec klassifikator-redis redis-cli ping > /dev/null 2>&1; then
    log_success "Redis готов"
else
    log_error "Redis не готов"
    exit 1
fi

echo ""
log_info "Проверка портов микросервисов..."

# Проверка портов
check_port 8081 "Landing Service"
check_port 8082 "Content Service"
check_port 8083 "Template Service"
check_port 8084 "Media Service"
check_port 8085 "Integration Service"
check_port 8086 "Order Service"
check_port 8080 "API Gateway"

echo ""
log_info "Сборка проекта..."
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    log_error "Ошибка сборки проекта"
    exit 1
fi
log_success "Проект собран успешно"

echo ""
log_info "Запуск микросервисов..."

# Создание директории для логов
mkdir -p logs

# Функция запуска сервиса
start_service() {
    local service_name=$1
    local jar_path=$2
    local port=$3
    local log_file="logs/${service_name}.log"
    
    log_info "Запуск $service_name на порту $port..."
    
    nohup java -jar "$jar_path" > "$log_file" 2>&1 &
    local pid=$!
    echo $pid > "logs/${service_name}.pid"
    
    log_success "$service_name запущен (PID: $pid)"
}

# Запуск сервисов по порядку
start_service "Landing Service" "landing-service/build/libs/landing-service.jar" 8081
sleep 3

start_service "Content Service" "content-service/build/libs/content-service.jar" 8082
sleep 3

start_service "Template Service" "template-service/build/libs/template-service.jar" 8083
sleep 3

start_service "Media Service" "media-service/build/libs/media-service.jar" 8084
sleep 3

start_service "Integration Service" "integration-service/build/libs/integration-service.jar" 8085
sleep 3

start_service "Order Service" "order-service/build/libs/order-service.jar" 8086
sleep 3

start_service "API Gateway" "api-gateway/build/libs/api-gateway.jar" 8080
sleep 5

echo ""
log_info "Проверка готовности сервисов..."

# Проверка health endpoints
wait_for_service "http://localhost:8081/actuator/health" "Landing Service"
wait_for_service "http://localhost:8082/actuator/health" "Content Service"
wait_for_service "http://localhost:8083/actuator/health" "Template Service"
wait_for_service "http://localhost:8084/actuator/health" "Media Service"
wait_for_service "http://localhost:8085/actuator/health" "Integration Service"
wait_for_service "http://localhost:8086/actuator/health" "Order Service"
wait_for_service "http://localhost:8080/actuator/health" "API Gateway"

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║              Все сервисы успешно запущены!                 ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

log_info "Сервисы доступны по адресам:"
echo ""
echo "  🌐 API Gateway:         http://localhost:8080"
echo "  📄 Landing Service:     http://localhost:8081"
echo "  📝 Content Service:     http://localhost:8082"
echo "  🎨 Template Service:    http://localhost:8083"
echo "  🖼️  Media Service:       http://localhost:8084"
echo "  🔗 Integration Service: http://localhost:8085"
echo "  🛒 Order Service:       http://localhost:8086"
echo ""
echo "  🗄️  PostgreSQL:          localhost:5432"
echo "  🔴 Redis:               localhost:6379"
echo "  📦 MinIO:               http://localhost:9000"
echo ""

log_info "Логи сервисов находятся в директории: ./logs/"
log_info "Для остановки всех сервисов используйте: ./stop-services.sh"

echo ""
log_success "Система готова к работе!"
echo ""

