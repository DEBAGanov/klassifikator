#!/bin/bash

###############################################################################
# Скрипт для просмотра логов сервисов
###############################################################################

NAMESPACE="klassifikator"

# Цвета
BLUE='\033[0;34m'
NC='\033[0m'

info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Если передан аргумент - показываем логи конкретного сервиса
if [ $# -eq 1 ]; then
    SERVICE=$1
    info "Логи сервиса: $SERVICE"
    kubectl logs -f -l app=$SERVICE -n $NAMESPACE --tail=100
else
    # Показываем список доступных сервисов
    info "Доступные сервисы:"
    echo ""
    kubectl get pods -n $NAMESPACE -o custom-columns=NAME:.metadata.name,STATUS:.status.phase
    echo ""
    info "Использование: ./logs.sh <service-name>"
    info "Например: ./logs.sh landing-service"
    echo ""
    info "Или для просмотра всех логов:"
    kubectl logs -f -l component=microservice -n $NAMESPACE --tail=50 --prefix=true
fi

