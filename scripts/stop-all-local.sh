#!/bin/bash

echo "Остановка всех локальных сервисов..."

# Функция для остановки процесса
stop_service() {
    local service_name=$1
    local pid_file="/tmp/${service_name}.pid"
    if [ -f "$pid_file" ]; then
        echo "Остановка $service_name..."
        kill $(cat "$pid_file") 2>/dev/null || true
        rm "$pid_file"
    fi
}

stop_service "landing-service"
stop_service "content-service"
stop_service "template-service"
stop_service "integration-service"
stop_service "media-service"
stop_service "order-service"

echo "Все сервисы остановлены."

