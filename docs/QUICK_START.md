# 🚀 Быстрый старт Klassifikator

**Версия**: 1.0  
**Дата**: 2025-11-03

---

## ⚡ Запуск за 5 минут

### 1. Предварительные требования

- ✅ Java 17+
- ✅ Docker & Docker Compose
- ✅ 8GB RAM минимум

### 2. Запуск системы

```bash
# Клонируйте репозиторий (если еще не сделано)
cd /Users/bagano/Downloads/Cursor/klassifikator

# Запустите все сервисы одной командой
./start-services.sh
```

Скрипт автоматически:
- Проверит Java и Docker
- Запустит PostgreSQL, Redis, MinIO
- Соберет проект
- Запустит все 7 микросервисов
- Проверит их готовность

### 3. Загрузка тестовых данных

```bash
# Подключитесь к PostgreSQL
docker exec -it klassifikator-postgres psql -U klassifikator_user -d klassifikator_dev

# Выполните скрипт
\i /path/to/test-data.sql

# Или через командную строку
docker exec -i klassifikator-postgres psql -U klassifikator_user -d klassifikator_dev < test-data.sql
```

### 4. Проверка работы

```bash
# Тестирование Order Service
./test-order-service.sh

# Или вручную
curl http://localhost:8080/actuator/health
```

### 5. Доступ к сервисам

| Сервис | URL | Описание |
|--------|-----|----------|
| **API Gateway** | http://localhost:8080 | Точка входа для всех запросов |
| **Landing Service** | http://localhost:8081 | Управление лендингами |
| **Content Service** | http://localhost:8082 | Управление контентом |
| **Template Service** | http://localhost:8083 | Управление шаблонами |
| **Media Service** | http://localhost:8084 | Управление медиафайлами |
| **Integration Service** | http://localhost:8085 | Интеграции (Telegram, Google Sheets) |
| **Order Service** | http://localhost:8086 | Управление заказами |
| **PostgreSQL** | localhost:5432 | База данных |
| **Redis** | localhost:6379 | Кэш |
| **MinIO** | http://localhost:9000 | S3 хранилище |

---

## 📝 Примеры использования

### Создание организации

```bash
curl -X POST http://localhost:8080/api/v1/organizations \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Моя Компания",
    "phone": "+79001234567",
    "email": "info@mycompany.ru",
    "isActive": true
  }'
```

### Создание товара

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "organizationId": 1,
    "name": "Товар 1",
    "description": "Описание товара",
    "price": 1000.00,
    "isActive": true
  }'
```

### Создание заказа

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "organizationId": 1,
    "landingId": 1,
    "customerName": "Иван Иванов",
    "customerPhone": "+79001234567",
    "items": [
      {"productId": 1, "quantity": 2}
    ]
  }'
```

### Рендеринг лендинга

```bash
curl "http://localhost:8080/api/v1/templates/1/render?organizationId=1"
```

---

## 🛑 Остановка системы

```bash
./stop-services.sh
```

---

## 📚 Дополнительная документация

- [Полное руководство по тестированию](docs/TESTING_GUIDE.md)
- [Документация Order Service](docs/ORDER_SERVICE.md)
- [Интеграция формы заказа](docs/ORDER_FORM_INTEGRATION.md)
- [Рендеринг шаблонов](docs/TEMPLATE_RENDERING.md)
- [Архитектура проекта](docs/Project.md)
- [Трекер задач](docs/Tasktracker.md)
- [Дневник разработки](docs/Diary.md)

---

## 🐛 Проблемы?

### Порт уже занят

```bash
# Найти процесс на порту
lsof -i :8080

# Убить процесс
kill -9 <PID>
```

### PostgreSQL не запускается

```bash
# Перезапустить Docker Compose
docker compose restart postgres

# Проверить логи
docker logs klassifikator-postgres
```

### Сервис не отвечает

```bash
# Проверить логи сервиса
tail -f logs/Order\ Service.log

# Перезапустить конкретный сервис
kill <PID>
java -jar order-service/build/libs/order-service.jar &
```

---

## 💡 Полезные команды

```bash
# Просмотр всех запущенных сервисов
ps aux | grep java

# Просмотр логов всех сервисов
tail -f logs/*.log

# Очистка кэша Redis
docker exec klassifikator-redis redis-cli FLUSHALL

# Подключение к PostgreSQL
docker exec -it klassifikator-postgres psql -U klassifikator_user -d klassifikator_dev

# Просмотр таблиц
\dt

# Выход из psql
\q
```

---

## 🎯 Следующие шаги

1. ✅ Протестируйте Order Service: `./test-order-service.sh`
2. ✅ Откройте лендинг в браузере
3. ✅ Попробуйте создать заказ через форму
4. ✅ Настройте Telegram бота для уведомлений
5. ⏳ Настройте Google Sheets интеграцию

---

**Нужна помощь?** Смотрите [TESTING_GUIDE.md](docs/TESTING_GUIDE.md)

