#!/bin/bash

###############################################################################
# Скрипт удаления Klassifikator из Kubernetes
###############################################################################

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

main() {
    echo ""
    warning "========================================="
    warning "  Удаление Klassifikator из K8s"
    warning "========================================="
    echo ""
    
    read -p "Вы уверены, что хотите удалить все ресурсы? (yes/no): " confirm
    
    if [ "$confirm" != "yes" ]; then
        info "Отменено пользователем"
        exit 0
    fi
    
    info "Удаление ресурсов..."
    
    # Удаление через kustomize
    kubectl delete -k base/ || true
    
    # Удаление namespace (это удалит все оставшиеся ресурсы)
    info "Удаление namespace klassifikator..."
    kubectl delete namespace klassifikator || true
    
    success "========================================="
    success "  Все ресурсы удалены"
    success "========================================="
    echo ""
    
    warning "ВНИМАНИЕ: PersistentVolumes могут остаться!"
    info "Проверьте: kubectl get pv"
    echo ""
}

main

