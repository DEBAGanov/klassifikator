#!/bin/bash

###############################################################################
# Скрипт остановки всех микросервисов Klassifikator
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

# Заголовок
echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║        Klassifikator - Остановка микросервисов             ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Функция остановки сервиса
stop_service() {
    local service_name=$1
    local pid_file="logs/${service_name}.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        
        if ps -p $pid > /dev/null 2>&1; then
            log_info "Остановка $service_name (PID: $pid)..."
            kill $pid
            
            # Ожидание завершения процесса
            local count=0
            while ps -p $pid > /dev/null 2>&1 && [ $count -lt 10 ]; do
                sleep 1
                count=$((count + 1))
            done
            
            if ps -p $pid > /dev/null 2>&1; then
                log_warning "$service_name не остановился, принудительная остановка..."
                kill -9 $pid
            fi
            
            log_success "$service_name остановлен"
        else
            log_warning "$service_name не запущен (PID $pid не найден)"
        fi
        
        rm -f "$pid_file"
    else
        log_warning "$service_name: PID файл не найден"
    fi
}

# Остановка сервисов в обратном порядке
log_info "Остановка микросервисов..."
echo ""

stop_service "API Gateway"
stop_service "Order Service"
stop_service "Integration Service"
stop_service "Media Service"
stop_service "Template Service"
stop_service "Content Service"
stop_service "Landing Service"

echo ""
log_info "Остановка Docker Compose (PostgreSQL, Redis, MinIO)..."

if docker compose ps | grep -q "Up"; then
    docker compose down
    log_success "Docker Compose остановлен"
else
    log_warning "Docker Compose не запущен"
fi

echo ""
log_success "Все сервисы остановлены!"
echo ""

