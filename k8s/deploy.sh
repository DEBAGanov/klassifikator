#!/bin/bash

###############################################################################
# Скрипт развертывания Klassifikator в Timeweb Cloud Kubernetes
# Версия: 1.0
# Дата: 2025-11-10
###############################################################################

set -e  # Остановка при ошибке

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Проверка наличия необходимых инструментов
check_requirements() {
    info "Проверка необходимых инструментов..."
    
    if ! command -v kubectl &> /dev/null; then
        error "kubectl не установлен. Установите: https://kubernetes.io/docs/tasks/tools/"
        exit 1
    fi
    
    if ! command -v kustomize &> /dev/null; then
        warning "kustomize не установлен. Будет использован встроенный в kubectl."
    fi
    
    success "Все необходимые инструменты установлены"
}

# Проверка подключения к кластеру
check_cluster_connection() {
    info "Проверка подключения к Kubernetes кластеру..."
    
    if ! kubectl cluster-info &> /dev/null; then
        error "Не удалось подключиться к кластеру Kubernetes"
        error "Убедитесь, что kubeconfig настроен правильно"
        exit 1
    fi
    
    CLUSTER_NAME=$(kubectl config current-context)
    success "Подключено к кластеру: $CLUSTER_NAME"
}

# Создание namespace
create_namespace() {
    info "Создание namespace klassifikator..."
    
    if kubectl get namespace klassifikator &> /dev/null; then
        warning "Namespace klassifikator уже существует"
    else
        kubectl create namespace klassifikator
        success "Namespace klassifikator создан"
    fi
}

# Создание секретов
create_secrets() {
    info "Создание секретов..."
    
    # Проверка наличия .env файла
    if [ ! -f "../.env" ]; then
        error "Файл .env не найден в корне проекта"
        error "Создайте .env файл из env.example"
        exit 1
    fi
    
    # Создание секрета из .env
    if kubectl get secret klassifikator-secrets -n klassifikator &> /dev/null; then
        warning "Secret klassifikator-secrets уже существует. Обновление..."
        kubectl delete secret klassifikator-secrets -n klassifikator
    fi
    
    kubectl create secret generic klassifikator-secrets \
        --from-env-file=../.env \
        -n klassifikator
    
    success "Secret klassifikator-secrets создан"
    
    # Создание секрета для Google Credentials
    if [ ! -f "../config/google-credentials.json" ]; then
        error "Файл google-credentials.json не найден в папке config/"
        exit 1
    fi
    
    if kubectl get secret google-credentials -n klassifikator &> /dev/null; then
        warning "Secret google-credentials уже существует. Обновление..."
        kubectl delete secret google-credentials -n klassifikator
    fi
    
    kubectl create secret generic google-credentials \
        --from-file=google-credentials.json=../config/google-credentials.json \
        -n klassifikator
    
    success "Secret google-credentials создан"
}

# Применение манифестов
apply_manifests() {
    info "Применение Kubernetes манифестов..."
    
    # Используем kustomize для применения всех манифестов
    kubectl apply -k base/
    
    success "Манифесты применены"
}

# Ожидание готовности подов
wait_for_pods() {
    info "Ожидание готовности подов..."
    
    # Ожидание инфраструктуры
    info "Ожидание PostgreSQL..."
    kubectl wait --for=condition=ready pod -l app=postgres -n klassifikator --timeout=300s
    
    info "Ожидание Redis..."
    kubectl wait --for=condition=ready pod -l app=redis -n klassifikator --timeout=300s
    
    info "Ожидание MinIO..."
    kubectl wait --for=condition=ready pod -l app=minio -n klassifikator --timeout=300s
    
    # Ожидание микросервисов
    info "Ожидание микросервисов..."
    kubectl wait --for=condition=ready pod -l component=microservice -n klassifikator --timeout=600s
    
    success "Все поды готовы"
}

# Проверка статуса
check_status() {
    info "Проверка статуса развертывания..."
    
    echo ""
    info "=== Статус подов ==="
    kubectl get pods -n klassifikator
    
    echo ""
    info "=== Статус сервисов ==="
    kubectl get services -n klassifikator
    
    echo ""
    info "=== Статус Ingress ==="
    kubectl get ingress -n klassifikator
    
    echo ""
    info "=== PersistentVolumeClaims ==="
    kubectl get pvc -n klassifikator
}

# Получение URL для доступа
get_access_urls() {
    info "Получение URL для доступа..."
    
    INGRESS_IP=$(kubectl get ingress klassifikator-ingress -n klassifikator -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
    
    if [ -z "$INGRESS_IP" ]; then
        warning "IP адрес Ingress еще не назначен. Подождите несколько минут."
    else
        echo ""
        success "=== URL для доступа ==="
        echo "API Gateway: http://$INGRESS_IP (api.volzhck.ru)"
        echo "Лендинги: http://$INGRESS_IP (*.volzhck.ru)"
        echo ""
        info "Настройте DNS записи:"
        echo "  api.volzhck.ru -> $INGRESS_IP"
        echo "  *.volzhck.ru -> $INGRESS_IP"
    fi
}

# Основная функция
main() {
    echo ""
    info "========================================="
    info "  Развертывание Klassifikator в K8s"
    info "========================================="
    echo ""
    
    check_requirements
    check_cluster_connection
    create_namespace
    create_secrets
    apply_manifests
    wait_for_pods
    check_status
    get_access_urls
    
    echo ""
    success "========================================="
    success "  Развертывание завершено успешно!"
    success "========================================="
    echo ""
    
    info "Полезные команды:"
    echo "  kubectl get pods -n klassifikator -w"
    echo "  kubectl logs -f <pod-name> -n klassifikator"
    echo "  kubectl describe pod <pod-name> -n klassifikator"
    echo "  kubectl port-forward svc/api-gateway 8080:8080 -n klassifikator"
    echo ""
}

# Запуск
main

