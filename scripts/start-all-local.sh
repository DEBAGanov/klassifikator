#!/bin/bash

cd "$(dirname "$0")"

# Цвета для вывода
GREEN='\033[0.32m'
YELLOW='\033[0.33m'
RED='\033[0.31m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Запуск микросервисов Klassifikator ===${NC}"

# Общие переменные окружения
export DATABASE_URL="jdbc:postgresql://localhost:5432/klassifikator"
export DATABASE_USERNAME="klassifikator"
export DATABASE_PASSWORD="change_this_strong_password_123"
export REDIS_PASSWORD="change_this_redis_password_456"
export LANDING_SERVICE_URL="http://localhost:8081"
export CONTENT_SERVICE_URL="http://localhost:8082"
export TEMPLATE_SERVICE_URL="http://localhost:8083"
export INTEGRATION_SERVICE_URL="http://localhost:8085"

# Создать директории для логов
mkdir -p logs

# Функция для остановки процесса
stop_service() {
    local service_name=$1
    local pid_file="/tmp/${service_name}.pid"
    if [ -f "$pid_file" ]; then
        kill $(cat "$pid_file") 2>/dev/null || true
        rm "$pid_file"
    fi
}

# Функция для запуска сервиса
start_service() {
    local service_name=$1
    local jar_path=$2
    local port=$3
    
    echo -e "${YELLOW}Запуск $service_name на порту $port...${NC}"
    
    java -jar "$jar_path" > "logs/${service_name}.log" 2>&1 &
    local pid=$!
    echo $pid > "/tmp/${service_name}.pid"
    
    echo -e "${GREEN}✓ $service_name started (PID: $pid)${NC}"
}

# Остановить старые процессы
echo "Остановка старых процессов..."
stop_service "landing-service"
stop_service "content-service"
stop_service "template-service"
stop_service "integration-service"
sleep 2

# Запуск сервисов
start_service "landing-service" "landing-service/build/libs/landing-service.jar" "8081"
sleep 5

start_service "content-service" "content-service/build/libs/content-service.jar" "8082"
sleep 5

start_service "template-service" "template-service/build/libs/template-service.jar" "8083"
sleep 5

start_service "integration-service" "integration-service/build/libs/integration-service.jar" "8085"
sleep 5

echo -e "${GREEN}=== Все сервисы запущены ===${NC}"
echo ""
echo "Проверка здоровья сервисов через 10 секунд..."
sleep 10

echo ""
echo -e "${YELLOW}=== Health Checks ===${NC}"
curl -s http://localhost:8081/actuator/health && echo " - Landing Service: ✓" || echo " - Landing Service: ✗"
curl -s http://localhost:8082/actuator/health && echo " - Content Service: ✓" || echo " - Content Service: ✗"
curl -s http://localhost:8083/actuator/health && echo " - Template Service: ✓" || echo " - Template Service: ✗"
curl -s http://localhost:8085/actuator/health && echo " - Integration Service: ✓" || echo " - Integration Service: ✗"

echo ""
echo -e "${GREEN}=== Готово! ===${NC}"
echo "Логи находятся в ./logs/"
echo "Остановка: ./stop-all-local.sh"

