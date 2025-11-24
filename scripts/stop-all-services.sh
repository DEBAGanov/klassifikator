#!/bin/bash

# Скрипт для остановки всех микросервисов Klassifikator
# Использование: ./stop-all-services.sh

set -e

echo "🛑 Остановка микросервисов Klassifikator..."
echo ""

# Функция для остановки сервиса
stop_service() {
    SERVICE_NAME=$1
    PID_FILE="logs/$2.pid"
    
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p $PID > /dev/null 2>&1; then
            echo "  ├── Остановка $SERVICE_NAME (PID: $PID)..."
            kill $PID
            rm "$PID_FILE"
            echo "     └── ✅ Остановлен"
        else
            echo "  ├── $SERVICE_NAME не запущен"
            rm "$PID_FILE"
        fi
    else
        echo "  ├── $SERVICE_NAME - PID файл не найден"
    fi
}

# Остановка сервисов в обратном порядке
stop_service "API Gateway" "api-gateway"
stop_service "Integration Service" "integration-service"
stop_service "Media Service" "media-service"
stop_service "Template Service" "template-service"
stop_service "Content Service" "content-service"
stop_service "Landing Service" "landing-service"

echo ""
echo "📦 Остановка инфраструктуры (PostgreSQL, Redis, MinIO)..."
docker-compose down

echo ""
echo "✅ Все сервисы остановлены!"

