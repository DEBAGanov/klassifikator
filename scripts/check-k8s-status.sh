#!/bin/bash

###############################################################################
# Скрипт проверки статуса развертывания Klassifikator в Kubernetes
# Версия: 1.0
# Дата: 2025-11-17
###############################################################################

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

NAMESPACE="klassifikator"

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

header() {
    echo ""
    echo -e "${CYAN}============================================${NC}"
    echo -e "${CYAN}  $1${NC}"
    echo -e "${CYAN}============================================${NC}"
}

# Проверка kubectl
check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        error "kubectl не установлен"
        exit 1
    fi
    
    if ! kubectl cluster-info &> /dev/null; then
        error "Нет подключения к Kubernetes кластеру"
        echo ""
        info "Настройте kubectl подключение:"
        echo "  1. Скачайте kubeconfig из Timeweb Cloud"
        echo "  2. cp ~/Downloads/kubeconfig ~/.kube/config"
        echo "  3. kubectl cluster-info"
        exit 1
    fi
}

# Информация о кластере
cluster_info() {
    header "Информация о кластере"
    
    echo "Текущий контекст:"
    kubectl config current-context
    
    echo ""
    echo "Ноды кластера:"
    kubectl get nodes
    
    echo ""
    echo "Использование ресурсов нод:"
    kubectl top nodes || warning "Metrics Server не установлен"
}

# Проверка namespace
check_namespace() {
    header "Проверка namespace"
    
    if kubectl get namespace $NAMESPACE &> /dev/null; then
        success "Namespace '$NAMESPACE' существует"
    else
        error "Namespace '$NAMESPACE' не найден"
        info "Создайте namespace: kubectl create namespace $NAMESPACE"
        exit 1
    fi
}

# Статус подов
pod_status() {
    header "Статус подов"
    
    kubectl get pods -n $NAMESPACE
    
    echo ""
    
    # Подсчет подов по статусу
    TOTAL=$(kubectl get pods -n $NAMESPACE --no-headers 2>/dev/null | wc -l)
    RUNNING=$(kubectl get pods -n $NAMESPACE --no-headers 2>/dev/null | grep "Running" | wc -l)
    PENDING=$(kubectl get pods -n $NAMESPACE --no-headers 2>/dev/null | grep "Pending" | wc -l)
    FAILED=$(kubectl get pods -n $NAMESPACE --no-headers 2>/dev/null | grep -E "Error|CrashLoopBackOff|ImagePullBackOff" | wc -l)
    
    echo "Статистика подов:"
    echo "  Всего: $TOTAL"
    success "  Running: $RUNNING"
    [ $PENDING -gt 0 ] && warning "  Pending: $PENDING" || echo -e "  Pending: ${GREEN}0${NC}"
    [ $FAILED -gt 0 ] && error "  Failed: $FAILED" || echo -e "  Failed: ${GREEN}0${NC}"
    
    # Если есть проблемные поды
    if [ $FAILED -gt 0 ] || [ $PENDING -gt 0 ]; then
        echo ""
        warning "Обнаружены проблемные поды:"
        kubectl get pods -n $NAMESPACE | grep -E "Pending|Error|CrashLoopBackOff|ImagePullBackOff" || true
    fi
}

# Использование ресурсов
resource_usage() {
    header "Использование ресурсов"
    
    if kubectl top pods -n $NAMESPACE &> /dev/null; then
        kubectl top pods -n $NAMESPACE --sort-by=memory
    else
        warning "Metrics Server не установлен или еще не готов"
    fi
}

# Статус сервисов
service_status() {
    header "Статус сервисов"
    
    kubectl get services -n $NAMESPACE
}

# Статус Ingress
ingress_status() {
    header "Статус Ingress"
    
    kubectl get ingress -n $NAMESPACE
    
    echo ""
    
    # Получение IP адреса
    INGRESS_IP=$(kubectl get ingress klassifikator-ingress -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null)
    
    if [ -n "$INGRESS_IP" ]; then
        success "Load Balancer IP: $INGRESS_IP"
        echo ""
        info "URL для доступа:"
        echo "  API Gateway: http://$INGRESS_IP (api.volzhck.ru)"
        echo "  Лендинги: http://$INGRESS_IP (*.volzhck.ru)"
        
        # Проверка DNS
        echo ""
        info "Проверка DNS:"
        API_DNS=$(dig +short api.volzhck.ru 2>/dev/null | tail -n1)
        if [ "$API_DNS" == "$INGRESS_IP" ]; then
            success "  api.volzhck.ru -> $API_DNS"
        else
            warning "  api.volzhck.ru -> $API_DNS (ожидается $INGRESS_IP)"
        fi
    else
        warning "IP адрес Load Balancer еще не назначен"
    fi
}

# SSL сертификаты
certificate_status() {
    header "Статус SSL сертификатов"
    
    if kubectl get certificate -n $NAMESPACE &> /dev/null; then
        kubectl get certificate -n $NAMESPACE
        
        echo ""
        
        CERT_READY=$(kubectl get certificate klassifikator-tls -n $NAMESPACE -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}' 2>/dev/null)
        
        if [ "$CERT_READY" == "True" ]; then
            success "SSL сертификаты готовы"
        else
            warning "SSL сертификаты еще получаются (это может занять 5-10 минут)"
            info "Проверьте cert-manager:"
            echo "  kubectl get pods -n cert-manager"
            echo "  kubectl describe certificate klassifikator-tls -n $NAMESPACE"
        fi
    else
        warning "cert-manager не установлен или сертификаты не созданы"
    fi
}

# PersistentVolumeClaims
pvc_status() {
    header "Статус PersistentVolumeClaims"
    
    kubectl get pvc -n $NAMESPACE
}

# Секреты
secret_status() {
    header "Статус секретов"
    
    echo "Секреты в namespace:"
    kubectl get secrets -n $NAMESPACE
    
    echo ""
    
    # Проверка обязательных секретов
    if kubectl get secret klassifikator-secrets -n $NAMESPACE &> /dev/null; then
        success "Secret 'klassifikator-secrets' существует"
    else
        error "Secret 'klassifikator-secrets' не найден"
        info "Создайте: kubectl create secret generic klassifikator-secrets --from-env-file=.env -n $NAMESPACE"
    fi
    
    if kubectl get secret google-credentials -n $NAMESPACE &> /dev/null; then
        success "Secret 'google-credentials' существует"
    else
        error "Secret 'google-credentials' не найден"
        info "Создайте: kubectl create secret generic google-credentials --from-file=google-credentials.json=config/google-credentials.json -n $NAMESPACE"
    fi
    
    if kubectl get secret timeweb-registry -n $NAMESPACE &> /dev/null; then
        success "Secret 'timeweb-registry' существует"
    else
        warning "Secret 'timeweb-registry' не найден"
        info "Создайте: kubectl create secret docker-registry timeweb-registry --docker-server=... -n $NAMESPACE"
    fi
}

# Логи проблемных подов
problem_pod_logs() {
    local PROBLEM_PODS=$(kubectl get pods -n $NAMESPACE --no-headers 2>/dev/null | grep -E "Error|CrashLoopBackOff|ImagePullBackOff" | awk '{print $1}')
    
    if [ -n "$PROBLEM_PODS" ]; then
        header "Логи проблемных подов"
        
        for POD in $PROBLEM_PODS; do
            echo ""
            warning "Логи пода: $POD"
            kubectl logs --tail=20 $POD -n $NAMESPACE 2>&1 || \
            kubectl logs --tail=20 $POD -n $NAMESPACE --previous 2>&1 || \
            echo "Не удалось получить логи"
            echo ""
            echo "---"
        done
    fi
}

# Health checks
health_checks() {
    header "Health Checks микросервисов"
    
    SERVICES=("api-gateway" "landing-service" "content-service" "template-service" "media-service" "integration-service" "order-service")
    
    for SERVICE in "${SERVICES[@]}"; do
        POD=$(kubectl get pods -n $NAMESPACE -l app=$SERVICE -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
        
        if [ -n "$POD" ]; then
            STATUS=$(kubectl get pod $POD -n $NAMESPACE -o jsonpath='{.status.phase}' 2>/dev/null)
            
            if [ "$STATUS" == "Running" ]; then
                # Попытка проверить health endpoint
                HEALTH=$(kubectl exec $POD -n $NAMESPACE -- curl -s http://localhost:8080/actuator/health 2>/dev/null || echo "N/A")
                
                if echo "$HEALTH" | grep -q "UP"; then
                    success "$SERVICE: UP"
                else
                    warning "$SERVICE: Running (health check недоступен)"
                fi
            else
                error "$SERVICE: $STATUS"
            fi
        else
            error "$SERVICE: под не найден"
        fi
    done
}

# Рекомендации
recommendations() {
    header "Рекомендации"
    
    # Проверяем различные проблемы
    local HAS_ISSUES=false
    
    # Проверка на ImagePullBackOff
    if kubectl get pods -n $NAMESPACE 2>/dev/null | grep -q "ImagePullBackOff"; then
        HAS_ISSUES=true
        warning "Обнаружена проблема: ImagePullBackOff"
        info "Решение:"
        echo "  1. Проверьте, что образы загружены в registry:"
        echo "     docker images | grep 12df3fb9-wise-cepheus"
        echo "  2. Проверьте imagePullSecret:"
        echo "     kubectl get secret timeweb-registry -n $NAMESPACE"
        echo "  3. Пересоздайте secret с правильным токеном"
        echo ""
    fi
    
    # Проверка на CrashLoopBackOff
    if kubectl get pods -n $NAMESPACE 2>/dev/null | grep -q "CrashLoopBackOff"; then
        HAS_ISSUES=true
        warning "Обнаружена проблема: CrashLoopBackOff"
        info "Решение:"
        echo "  1. Проверьте логи пода:"
        echo "     kubectl logs <pod-name> -n $NAMESPACE"
        echo "  2. Проверьте переменные окружения и секреты"
        echo "  3. Проверьте подключение к базе данных"
        echo ""
    fi
    
    # Проверка SSL
    CERT_READY=$(kubectl get certificate klassifikator-tls -n $NAMESPACE -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}' 2>/dev/null)
    if [ "$CERT_READY" != "True" ]; then
        HAS_ISSUES=true
        warning "SSL сертификаты еще не получены"
        info "Подождите 5-10 минут или проверьте:"
        echo "  kubectl describe certificate klassifikator-tls -n $NAMESPACE"
        echo "  kubectl get pods -n cert-manager"
        echo ""
    fi
    
    if [ "$HAS_ISSUES" = false ]; then
        success "Проблем не обнаружено! Система работает корректно."
        echo ""
        info "Проверьте работу приложения:"
        echo "  curl http://api.volzhck.ru/actuator/health"
        echo "  open http://api.volzhck.ru"
    fi
}

# Основная функция
main() {
    clear
    
    echo -e "${CYAN}"
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║      Проверка статуса Klassifikator в Kubernetes          ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    
    check_kubectl
    cluster_info
    check_namespace
    pod_status
    service_status
    ingress_status
    certificate_status
    pvc_status
    secret_status
    resource_usage
    health_checks
    problem_pod_logs
    recommendations
    
    echo ""
    header "Завершено"
    info "Для мониторинга в реальном времени:"
    echo "  watch kubectl get pods -n $NAMESPACE"
    echo ""
    info "Для просмотра логов:"
    echo "  kubectl logs -f deployment/<service-name> -n $NAMESPACE"
    echo ""
}

# Обработка аргументов
case "${1:-}" in
    --pods)
        check_kubectl
        pod_status
        ;;
    --ingress)
        check_kubectl
        ingress_status
        certificate_status
        ;;
    --health)
        check_kubectl
        health_checks
        ;;
    --logs)
        check_kubectl
        problem_pod_logs
        ;;
    --help)
        echo "Использование:"
        echo "  $0           # Полная проверка статуса"
        echo "  $0 --pods    # Только статус подов"
        echo "  $0 --ingress # Только Ingress и SSL"
        echo "  $0 --health  # Только health checks"
        echo "  $0 --logs    # Только логи проблемных подов"
        echo "  $0 --help    # Показать эту справку"
        ;;
    *)
        main
        ;;
esac

