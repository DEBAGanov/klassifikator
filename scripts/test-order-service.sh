#!/bin/bash

###############################################################################
# Скрипт тестирования Order Service
# Версия: 1.0
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

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Заголовок
echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║           Тестирование Order Service                       ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

API_URL="http://localhost:8086"
GATEWAY_URL="http://localhost:8080"

# Проверка доступности Order Service
log_info "Проверка доступности Order Service..."
if curl -s "$API_URL/actuator/health" > /dev/null 2>&1; then
    log_success "Order Service доступен"
else
    log_error "Order Service недоступен. Запустите сервисы: ./start-services.sh"
    exit 1
fi

echo ""
log_info "=== Тест 1: Создание заказа ==="

ORDER_DATA='{
  "organizationId": 1,
  "landingId": 1,
  "customerName": "Тестовый Клиент",
  "customerPhone": "+79001234567",
  "customerEmail": "test@example.com",
  "deliveryAddress": "г. Москва, ул. Тестовая, д. 1, кв. 10",
  "comment": "Тестовый заказ - доставка с 10:00 до 18:00",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}'

log_info "Отправка POST запроса на создание заказа..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d "$ORDER_DATA")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "201" ]; then
    log_success "Заказ создан успешно (HTTP $HTTP_CODE)"
    echo ""
    echo "Ответ сервера:"
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
    
    # Извлечение ID заказа
    ORDER_ID=$(echo "$BODY" | jq -r '.id' 2>/dev/null)
    
    if [ -n "$ORDER_ID" ] && [ "$ORDER_ID" != "null" ]; then
        log_success "ID созданного заказа: $ORDER_ID"
        
        echo ""
        log_info "=== Тест 2: Получение заказа по ID ==="
        
        RESPONSE=$(curl -s -w "\n%{http_code}" "$API_URL/api/v1/orders/$ORDER_ID")
        HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
        BODY=$(echo "$RESPONSE" | sed '$d')
        
        if [ "$HTTP_CODE" = "200" ]; then
            log_success "Заказ получен успешно (HTTP $HTTP_CODE)"
            echo ""
            echo "Данные заказа:"
            echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
        else
            log_error "Ошибка получения заказа (HTTP $HTTP_CODE)"
            echo "$BODY"
        fi
        
        echo ""
        log_info "=== Тест 3: Обновление статуса заказа ==="
        
        RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH \
          "$API_URL/api/v1/orders/$ORDER_ID/status?status=PROCESSING")
        HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
        BODY=$(echo "$RESPONSE" | sed '$d')
        
        if [ "$HTTP_CODE" = "200" ]; then
            log_success "Статус заказа обновлен (HTTP $HTTP_CODE)"
            echo ""
            echo "Обновленный заказ:"
            echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
        else
            log_error "Ошибка обновления статуса (HTTP $HTTP_CODE)"
            echo "$BODY"
        fi
        
        echo ""
        log_info "=== Тест 4: Получение заказов организации ==="
        
        RESPONSE=$(curl -s -w "\n%{http_code}" \
          "$API_URL/api/v1/orders/organization/1?page=0&size=10")
        HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
        BODY=$(echo "$RESPONSE" | sed '$d')
        
        if [ "$HTTP_CODE" = "200" ]; then
            log_success "Заказы организации получены (HTTP $HTTP_CODE)"
            TOTAL=$(echo "$BODY" | jq -r '.totalElements' 2>/dev/null)
            log_info "Всего заказов: $TOTAL"
        else
            log_error "Ошибка получения заказов организации (HTTP $HTTP_CODE)"
        fi
        
        echo ""
        log_info "=== Тест 5: Получение заказов по статусу ==="
        
        RESPONSE=$(curl -s -w "\n%{http_code}" \
          "$API_URL/api/v1/orders/status/PROCESSING?page=0&size=10")
        HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
        BODY=$(echo "$RESPONSE" | sed '$d')
        
        if [ "$HTTP_CODE" = "200" ]; then
            log_success "Заказы по статусу получены (HTTP $HTTP_CODE)"
            TOTAL=$(echo "$BODY" | jq -r '.totalElements' 2>/dev/null)
            log_info "Заказов со статусом PROCESSING: $TOTAL"
        else
            log_error "Ошибка получения заказов по статусу (HTTP $HTTP_CODE)"
        fi
        
    else
        log_error "Не удалось извлечь ID заказа из ответа"
    fi
else
    log_error "Ошибка создания заказа (HTTP $HTTP_CODE)"
    echo "$BODY"
fi

echo ""
log_info "=== Тест 6: Проверка через API Gateway ==="

RESPONSE=$(curl -s -w "\n%{http_code}" "$GATEWAY_URL/api/v1/orders/organization/1?page=0&size=5")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "200" ]; then
    log_success "API Gateway работает корректно (HTTP $HTTP_CODE)"
else
    log_error "Ошибка доступа через API Gateway (HTTP $HTTP_CODE)"
fi

echo ""
log_info "=== Тест 7: Валидация - заказ без обязательных полей ==="

INVALID_ORDER='{
  "organizationId": 1,
  "items": []
}'

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d "$INVALID_ORDER")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "400" ] || [ "$HTTP_CODE" = "422" ]; then
    log_success "Валидация работает корректно (HTTP $HTTP_CODE)"
else
    log_error "Валидация не сработала (HTTP $HTTP_CODE)"
fi

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║              Тестирование завершено!                       ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

log_info "Проверьте логи Telegram для уведомлений о заказах"
log_info "Логи Order Service: ./logs/Order\\ Service.log"

echo ""

