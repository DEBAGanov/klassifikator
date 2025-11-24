#!/bin/bash

# =============================================================================
# Автоматический запуск Docker Compose для тестирования
# =============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🐳 Тестирование Klassifikator через Docker Compose${NC}\n"

# Проверка Docker
check_docker() {
    echo -e "${BLUE}Проверка Docker...${NC}"
    
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}❌ Docker не установлен!${NC}"
        exit 1
    fi
    
    if ! docker ps &> /dev/null; then
        echo -e "${YELLOW}⚠️  Docker daemon не запущен!${NC}"
        echo -e "${YELLOW}Пожалуйста, запустите Docker Desktop и попробуйте снова.${NC}"
        echo -e "${BLUE}Попытка запуска Docker Desktop...${NC}"
        open -a Docker 2>/dev/null || true
        echo -e "${YELLOW}Ожидание запуска Docker (30 секунд)...${NC}"
        sleep 30
        
        # Повторная проверка
        if ! docker ps &> /dev/null; then
            echo -e "${RED}❌ Docker всё ещё не запущен. Запустите Docker Desktop вручную.${NC}"
            exit 1
        fi
    fi
    
    echo -e "${GREEN}✅ Docker запущен${NC}\n"
}

# Остановка старых контейнеров
stop_old_containers() {
    echo -e "${BLUE}Остановка старых контейнеров...${NC}"
    docker-compose -f docker-compose.prod.yml down 2>/dev/null || true
    echo -e "${GREEN}✅ Готово${NC}\n"
}

# Сборка образов
build_images() {
    echo -e "${BLUE}Сборка Docker образов...${NC}"
    echo -e "${YELLOW}Это может занять 5-10 минут при первом запуске...${NC}\n"
    
    docker-compose -f docker-compose.prod.yml build --no-cache
    
    echo -e "${GREEN}✅ Образы собраны${NC}\n"
}

# Запуск инфраструктуры
start_infrastructure() {
    echo -e "${BLUE}Запуск инфраструктуры (PostgreSQL, Redis, MinIO)...${NC}"
    docker-compose -f docker-compose.prod.yml up -d postgres redis minio
    
    echo -e "${YELLOW}Ожидание готовности инфраструктуры (30 секунд)...${NC}"
    sleep 30
    
    # Проверка
    if docker ps | grep -q klassifikator-postgres; then
        echo -e "${GREEN}✅ PostgreSQL запущен${NC}"
    else
        echo -e "${RED}❌ PostgreSQL не запустился${NC}"
        exit 1
    fi
    
    if docker ps | grep -q klassifikator-redis; then
        echo -e "${GREEN}✅ Redis запущен${NC}"
    else
        echo -e "${RED}❌ Redis не запустился${NC}"
        exit 1
    fi
    
    echo ""
}

# Запуск всех сервисов
start_services() {
    echo -e "${BLUE}Запуск всех микросервисов...${NC}"
    docker-compose -f docker-compose.prod.yml up -d
    
    echo -e "${YELLOW}Ожидание запуска сервисов (60 секунд)...${NC}"
    sleep 60
    
    echo -e "${GREEN}✅ Сервисы запущены${NC}\n"
}

# Проверка health checks
check_health() {
    echo -e "${BLUE}Проверка health checks...${NC}\n"
    
    services=(
        "API Gateway:8080"
        "Landing Service:8081"
        "Content Service:8082"
        "Template Service:8083"
        "Media Service:8084"
        "Integration Service:8085"
        "Order Service:8086"
    )
    
    healthy=0
    total=${#services[@]}
    
    for service in "${services[@]}"; do
        IFS=':' read -r name port <<< "$service"
        
        if curl -f -s "http://localhost:${port}/actuator/health" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ ${name} healthy${NC}"
            ((healthy++))
        else
            echo -e "${YELLOW}⚠️  ${name} не отвечает (может быть ещё запускается)${NC}"
        fi
    done
    
    echo -e "\n${BLUE}Статус: ${healthy}/${total} сервисов healthy${NC}\n"
}

# Показать статус
show_status() {
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}✅ Развёртывание завершено!${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
    
    echo -e "${BLUE}📊 Статус контейнеров:${NC}"
    docker-compose -f docker-compose.prod.yml ps
    
    echo -e "\n${BLUE}🔗 Доступные сервисы:${NC}"
    echo -e "  - API Gateway:         http://localhost:8080"
    echo -e "  - Landing Service:     http://localhost:8081"
    echo -e "  - Content Service:     http://localhost:8082"
    echo -e "  - Template Service:    http://localhost:8083"
    echo -e "  - Media Service:       http://localhost:8084"
    echo -e "  - Integration Service: http://localhost:8085"
    echo -e "  - Order Service:       http://localhost:8086"
    echo -e "  - MinIO Console:       http://localhost:9001"
    
    echo -e "\n${BLUE}🧪 Тестирование:${NC}"
    echo -e "  # Синхронизация Google Sheets:"
    echo -e "  curl -X POST 'http://localhost:8085/api/v1/integration/google-sheets/sync-all?sheetName=Organizations'"
    echo -e ""
    echo -e "  # Список лендингов:"
    echo -e "  curl http://localhost:8081/api/v1/landings | jq ."
    echo -e ""
    echo -e "  # Просмотр логов:"
    echo -e "  docker-compose -f docker-compose.prod.yml logs -f"
    echo -e ""
    echo -e "  # Остановка:"
    echo -e "  docker-compose -f docker-compose.prod.yml down"
    echo ""
}

# Main
main() {
    check_docker
    stop_old_containers
    build_images
    start_infrastructure
    start_services
    check_health
    show_status
}

# Run
main

