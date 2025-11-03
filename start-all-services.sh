#!/bin/bash

# Скрипт для запуска всех микросервисов Klassifikator
# Использование: ./start-all-services.sh

set -e

echo "🚀 Запуск микросервисов Klassifikator..."
echo ""

# Проверка Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker не установлен. Установите Docker и попробуйте снова."
    exit 1
fi

# Проверка Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose не установлен. Установите Docker Compose и попробуйте снова."
    exit 1
fi

# Запуск инфраструктуры
echo "📦 Запуск инфраструктуры (PostgreSQL, Redis, MinIO)..."
docker-compose up -d

echo "⏳ Ожидание запуска PostgreSQL (10 секунд)..."
sleep 10

# Сборка проекта
echo ""
echo "🔨 Сборка проекта..."
./gradlew build -x test

# Создание директории для логов
mkdir -p logs

# Запуск микросервисов
echo ""
echo "🎯 Запуск микросервисов..."

echo "  ├── Landing Service (8081)..."
nohup java -jar landing-service/build/libs/landing-service-0.0.1-SNAPSHOT.jar > logs/landing-service.log 2>&1 &
LANDING_PID=$!
echo "     └── PID: $LANDING_PID"

sleep 5

echo "  ├── Content Service (8082)..."
nohup java -jar content-service/build/libs/content-service-0.0.1-SNAPSHOT.jar > logs/content-service.log 2>&1 &
CONTENT_PID=$!
echo "     └── PID: $CONTENT_PID"

sleep 5

echo "  ├── Template Service (8083)..."
nohup java -jar template-service/build/libs/template-service-0.0.1-SNAPSHOT.jar > logs/template-service.log 2>&1 &
TEMPLATE_PID=$!
echo "     └── PID: $TEMPLATE_PID"

sleep 5

echo "  ├── Media Service (8084)..."
nohup java -jar media-service/build/libs/media-service-0.0.1-SNAPSHOT.jar > logs/media-service.log 2>&1 &
MEDIA_PID=$!
echo "     └── PID: $MEDIA_PID"

sleep 5

echo "  ├── Integration Service (8085)..."
nohup java -jar integration-service/build/libs/integration-service-0.0.1-SNAPSHOT.jar > logs/integration-service.log 2>&1 &
INTEGRATION_PID=$!
echo "     └── PID: $INTEGRATION_PID"

sleep 5

echo "  └── API Gateway (8080)..."
nohup java -jar api-gateway/build/libs/api-gateway-0.0.1-SNAPSHOT.jar > logs/api-gateway.log 2>&1 &
GATEWAY_PID=$!
echo "     └── PID: $GATEWAY_PID"

# Сохранение PID в файл
echo "$LANDING_PID" > logs/landing-service.pid
echo "$CONTENT_PID" > logs/content-service.pid
echo "$TEMPLATE_PID" > logs/template-service.pid
echo "$MEDIA_PID" > logs/media-service.pid
echo "$INTEGRATION_PID" > logs/integration-service.pid
echo "$GATEWAY_PID" > logs/api-gateway.pid

echo ""
echo "⏳ Ожидание запуска всех сервисов (30 секунд)..."
sleep 30

# Проверка здоровья сервисов
echo ""
echo "🏥 Проверка здоровья сервисов..."

check_health() {
    SERVICE_NAME=$1
    PORT=$2
    
    if curl -s http://localhost:$PORT/actuator/health | grep -q "UP"; then
        echo "  ✅ $SERVICE_NAME - OK"
        return 0
    else
        echo "  ❌ $SERVICE_NAME - FAILED"
        return 1
    fi
}

check_health "API Gateway" 8080
check_health "Landing Service" 8081
check_health "Content Service" 8082
check_health "Template Service" 8083
check_health "Media Service" 8084
check_health "Integration Service" 8085

echo ""
echo "✅ Все сервисы запущены!"
echo ""
echo "📊 Информация:"
echo "  - API Gateway: http://localhost:8080"
echo "  - Landing Service: http://localhost:8081"
echo "  - Content Service: http://localhost:8082"
echo "  - Template Service: http://localhost:8083"
echo "  - Media Service: http://localhost:8084"
echo "  - Integration Service: http://localhost:8085"
echo ""
echo "  - PostgreSQL: localhost:5432"
echo "  - Redis: localhost:6379"
echo "  - MinIO Console: http://localhost:9001"
echo ""
echo "📝 Логи доступны в директории logs/"
echo "🛑 Для остановки используйте: ./stop-all-services.sh"

