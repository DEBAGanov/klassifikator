#!/bin/bash

###############################################################################
# Скрипт тестирования Klassifikator API локально
# Версия: 1.0
###############################################################################

# Цвета
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
success() { echo -e "${GREEN}✅ $1${NC}"; }
warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
error() { echo -e "${RED}❌ $1${NC}"; }

API_URL="http://localhost:8080"
LANDING_SERVICE_URL="http://localhost:8081"

echo ""
info "========================================="
info "  Тестирование Klassifikator API"
info "========================================="
echo ""

# 1. Проверяем health endpoints
info "1. Проверка health endpoints..."
echo ""

info "API Gateway (port 8080):"
curl -s "$API_URL/actuator/health" | jq '.' || warning "API Gateway недоступен"
echo ""

info "Landing Service (port 8081):"
curl -s "$LANDING_SERVICE_URL/actuator/health" | jq '.' || warning "Landing Service недоступен"
echo ""

# 2. Создаем организацию
info "2. Создание тестовой организации..."
ORG_RESPONSE=$(curl -s -X POST "$API_URL/api/v1/organizations" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Organization",
    "inn": "1234567890",
    "contactEmail": "test@example.com",
    "contactPhone": "+7 900 123-45-67"
  }')

echo "$ORG_RESPONSE" | jq '.'

ORG_ID=$(echo "$ORG_RESPONSE" | jq -r '.id')
if [ "$ORG_ID" != "null" ] && [ -n "$ORG_ID" ]; then
    success "Организация создана с ID: $ORG_ID"
else
    error "Не удалось создать организацию"
    exit 1
fi
echo ""

# 3. Создаем лендинг
info "3. Создание тестового лендинга..."
LANDING_RESPONSE=$(curl -s -X POST "$API_URL/api/v1/landings" \
  -H "Content-Type: application/json" \
  -d "{
    \"organizationId\": \"$ORG_ID\",
    \"domain\": \"test-$(date +%s).local\",
    \"title\": \"Test Landing Page\",
    \"description\": \"This is a test landing page\",
    \"templateId\": 1,
    \"themeSettings\": {
      \"primaryColor\": \"#007bff\",
      \"secondaryColor\": \"#6c757d\"
    }
  }")

echo "$LANDING_RESPONSE" | jq '.'

LANDING_ID=$(echo "$LANDING_RESPONSE" | jq -r '.id')
if [ "$LANDING_ID" != "null" ] && [ -n "$LANDING_ID" ]; then
    success "Лендинг создан с ID: $LANDING_ID"
else
    error "Не удалось создать лендинг"
    echo "$LANDING_RESPONSE"
fi
echo ""

# 4. Получаем список лендингов организации
info "4. Получение списка лендингов организации..."
curl -s "$API_URL/api/v1/landings/organization/$ORG_ID" | jq '.'
echo ""

# 5. Получаем информацию о лендинге
if [ "$LANDING_ID" != "null" ] && [ -n "$LANDING_ID" ]; then
    info "5. Получение информации о лендинге..."
    curl -s "$API_URL/api/v1/landings/$LANDING_ID" | jq '.'
    echo ""
fi

echo ""
success "========================================="
success "  Тестирование завершено!"
success "========================================="
echo ""
info "Созданные ресурсы:"
info "  • Организация ID: $ORG_ID"
info "  • Лендинг ID: $LANDING_ID"
echo ""
