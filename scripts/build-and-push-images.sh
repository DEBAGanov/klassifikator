#!/bin/bash

###############################################################################
# Скрипт сборки и загрузки Docker образов в Timeweb Cloud Container Registry
# Версия: 1.0
# Дата: 2025-11-17
###############################################################################

set -e  # Остановка при ошибке

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Конфигурация
REGISTRY="12df3fb9-wise-cepheus.registry.twcstorage.ru"
REGISTRY_USER="12df3fb9-wise-cepheus"

# Список микросервисов
SERVICES=(
    "api-gateway"
    "landing-service"
    "content-service"
    "template-service"
    "media-service"
    "integration-service"
    "order-service"
)

# Функции для вывода
info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
}

warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

error() {
    echo -e "${RED}❌ $1${NC}"
}

# Проверка наличия Docker
check_docker() {
    info "Проверка Docker..."
    
    if ! command -v docker &> /dev/null; then
        error "Docker не установлен. Установите Docker Desktop."
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        error "Docker не запущен. Запустите Docker Desktop."
        exit 1
    fi
    
    success "Docker готов"
}

# Вход в registry
docker_login() {
    info "Вход в Timeweb Cloud Container Registry..."
    
    if [ -z "$TIMEWEB_REGISTRY_TOKEN" ]; then
        warning "Переменная TIMEWEB_REGISTRY_TOKEN не установлена"
        info "Введите токен доступа к registry:"
        read -s TIMEWEB_REGISTRY_TOKEN
        echo
    fi
    
    echo "$TIMEWEB_REGISTRY_TOKEN" | docker login "$REGISTRY" -u "$REGISTRY_USER" --password-stdin
    
    success "Успешный вход в registry"
}

# Сборка Gradle проекта
build_gradle() {
    info "Сборка Gradle проекта..."
    
    cd "$(dirname "$0")/.."
    
    ./gradlew clean build -x test
    
    success "Gradle сборка завершена"
}

# Сборка Docker образа
build_docker_image() {
    local service=$1
    
    info "Сборка Docker образа для $service (linux/amd64, БЕЗ КЭША)..."
    
    docker build \
        --no-cache \
        --platform linux/amd64 \
        -t "$REGISTRY/$service:latest" \
        -t "$REGISTRY/$service:$(date +%Y%m%d-%H%M%S)" \
        -f "$service/Dockerfile" \
        .
    
    success "Образ $service собран"
}

# Загрузка образа в registry
push_docker_image() {
    local service=$1
    
    info "Загрузка образа $service в registry..."
    
    docker push "$REGISTRY/$service:latest"
    docker push "$REGISTRY/$service" --all-tags
    
    success "Образ $service загружен"
}

# Сборка всех образов
build_all_images() {
    info "Сборка всех Docker образов..."
    echo ""
    
    for service in "${SERVICES[@]}"; do
        build_docker_image "$service"
        echo ""
    done
    
    success "Все образы собраны"
}

# Загрузка всех образов
push_all_images() {
    info "Загрузка всех образов в registry..."
    echo ""
    
    for service in "${SERVICES[@]}"; do
        push_docker_image "$service"
        echo ""
    done
    
    success "Все образы загружены"
}

# Проверка образов
verify_images() {
    info "Проверка локальных образов..."
    echo ""
    
    docker images | grep "$REGISTRY"
    
    echo ""
    success "Проверка завершена"
}

# Основная функция
main() {
    echo ""
    info "========================================="
    info "  Сборка и загрузка Docker образов"
    info "  Registry: $REGISTRY"
    info "========================================="
    echo ""
    
    check_docker
    docker_login
    build_gradle
    build_all_images
    push_all_images
    verify_images
    
    echo ""
    success "========================================="
    success "  Все образы успешно собраны и загружены!"
    success "========================================="
    echo ""
    
    info "Следующий шаг: Разверните приложение в Kubernetes"
    echo "  cd k8s"
    echo "  ./deploy.sh"
    echo ""
}

# Обработка аргументов
case "${1:-}" in
    --build-only)
        check_docker
        build_gradle
        build_all_images
        ;;
    --push-only)
        check_docker
        docker_login
        push_all_images
        ;;
    --service)
        if [ -z "${2:-}" ]; then
            error "Укажите имя сервиса: --service <service-name>"
            exit 1
        fi
        check_docker
        docker_login
        build_gradle
        build_docker_image "$2"
        push_docker_image "$2"
        ;;
    --help)
        echo "Использование:"
        echo "  $0                  # Сборка и загрузка всех образов"
        echo "  $0 --build-only     # Только сборка образов"
        echo "  $0 --push-only      # Только загрузка образов"
        echo "  $0 --service <name> # Сборка и загрузка одного сервиса"
        echo "  $0 --help           # Показать эту справку"
        ;;
    *)
        main
        ;;
esac

