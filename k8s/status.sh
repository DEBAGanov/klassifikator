#!/bin/bash

###############################################################################
# Скрипт для проверки статуса всех компонентов
###############################################################################

NAMESPACE="klassifikator"

GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
}

echo ""
info "========================================="
info "  Статус Klassifikator в K8s"
info "========================================="
echo ""

# Проверка namespace
info "=== Namespace ==="
kubectl get namespace $NAMESPACE
echo ""

# Статус подов
info "=== Поды ==="
kubectl get pods -n $NAMESPACE -o wide
echo ""

# Статус сервисов
info "=== Сервисы ==="
kubectl get services -n $NAMESPACE
echo ""

# Статус Ingress
info "=== Ingress ==="
kubectl get ingress -n $NAMESPACE
echo ""

# PVC
info "=== Persistent Volume Claims ==="
kubectl get pvc -n $NAMESPACE
echo ""

# Проверка health всех сервисов
info "=== Health Checks ==="
for service in landing-service content-service template-service media-service integration-service order-service api-gateway; do
    POD=$(kubectl get pod -n $NAMESPACE -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
    if [ -n "$POD" ]; then
        PORT=$(kubectl get pod -n $NAMESPACE $POD -o jsonpath='{.spec.containers[0].ports[0].containerPort}')
        STATUS=$(kubectl exec -n $NAMESPACE $POD -- curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/actuator/health 2>/dev/null || echo "ERROR")
        if [ "$STATUS" = "200" ]; then
            success "$service: OK"
        else
            echo "❌ $service: FAILED (HTTP $STATUS)"
        fi
    else
        echo "⚠️  $service: Pod not found"
    fi
done
echo ""

# Использование ресурсов
info "=== Использование ресурсов ==="
kubectl top pods -n $NAMESPACE 2>/dev/null || echo "Metrics server не установлен"
echo ""

success "========================================="
success "  Проверка завершена"
success "========================================="
echo ""

