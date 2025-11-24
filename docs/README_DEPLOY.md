# 🚀 Быстрый старт - Развёртывание Klassifikator

> **Полная документация**: `docs/DEPLOYMENT.md`

## ⚡ Quick Start

### 1. Подготовка сервера (Timeweb Cloud)

```bash
# Подключение к серверу
ssh root@your-server-ip

# Установка Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Установка Git
apt install -y git

# Клонирование проекта
cd /opt
git clone https://github.com/your-username/klassifikator.git
cd klassifikator
```

### 2. Настройка

```bash
# Создание .env файла
cp env.example .env
nano .env

# Заполните обязательные поля:
# - DB_PASSWORD
# - REDIS_PASSWORD
# - MINIO_ROOT_PASSWORD
# - GOOGLE_SHEETS_SPREADSHEET_ID
# - TELEGRAM_BOT_TOKEN
# - TELEGRAM_CHAT_ID
```

### 3. Google Sheets API

```bash
# Создайте config/google-credentials.json
mkdir -p config
nano config/google-credentials.json

# Вставьте JSON от Google Service Account
```

### 4. Развёртывание

```bash
# Автоматический деплой
chmod +x deploy.sh
./deploy.sh
```

### 5. Настройка доменов в Timeweb Cloud

1. Откройте: https://timeweb.cloud/my/domains
2. Создайте A-запись для `volzhck.ru` → IP вашего сервера
3. Создайте поддомены для лендингов (например, `test.volzhck.ru`)
4. SSL сертификаты выдаются автоматически ✅

### 6. Проверка

```bash
# Проверка здоровья сервисов
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # Landing Service
curl http://localhost:8082/actuator/health  # Content Service
curl http://localhost:8083/actuator/health  # Template Service
curl http://localhost:8084/actuator/health  # Media Service
curl http://localhost:8085/actuator/health  # Integration Service
curl http://localhost:8086/actuator/health  # Order Service

# Синхронизация с Google Sheets
curl -X POST "http://localhost:8085/api/v1/integration/google-sheets/sync-all"

# Проверка лендинга
curl http://test.volzhck.ru
```

## 📦 Структура проекта

```
klassifikator/
├── api-gateway/              # API Gateway (Spring Cloud Gateway)
├── landing-service/          # Управление лендингами и организациями
├── content-service/          # Управление контентом
├── template-service/         # Рендеринг шаблонов
├── media-service/            # Работа с медиафайлами (S3)
├── integration-service/      # Google Sheets + Telegram
├── order-service/            # Обработка заказов
├── common/                   # Общие модели и утилиты
├── templates/                # HTML/CSS/JS шаблоны лендингов
│   └── landing-basic/
├── nginx/                    # Nginx конфигурация
│   ├── nginx.conf
│   └── conf.d/default.conf
├── docs/                     # Документация
│   ├── DEPLOYMENT.md         # 📖 Полная инструкция по деплою
│   ├── AUTO_LANDING_CREATION.md
│   ├── Project.md
│   └── Tasktracker.md
├── scripts/                  # Вспомогательные скрипты
│   └── backup.sh
├── docker-compose.prod.yml   # Docker Compose для production
├── deploy.sh                 # 🚀 Скрипт автоматического деплоя
├── env.example               # Шаблон переменных окружения
└── README.md
```

## 🎯 Основные команды

### Управление Docker Compose

```bash
# Запуск всех сервисов
docker-compose -f docker-compose.prod.yml up -d

# Остановка всех сервисов
docker-compose -f docker-compose.prod.yml down

# Просмотр логов
docker-compose -f docker-compose.prod.yml logs -f

# Перезапуск сервиса
docker-compose -f docker-compose.prod.yml restart landing-service

# Статус контейнеров
docker-compose -f docker-compose.prod.yml ps
```

### Мониторинг

```bash
# Использование ресурсов
docker stats

# Логи конкретного сервиса
docker logs -f klassifikator-landing-service

# Проверка базы данных
docker exec -it klassifikator-postgres psql -U klassifikator -d klassifikator
```

### Резервное копирование

```bash
# Создание бэкапа БД
./scripts/backup.sh

# Восстановление из бэкапа
gunzip backups/klassifikator_YYYYMMDD_HHMMSS.sql.gz
cat backups/klassifikator_YYYYMMDD_HHMMSS.sql | docker exec -i klassifikator-postgres psql -U klassifikator -d klassifikator
```

## 🔗 Полезные ссылки

- **Документация по деплою**: `docs/DEPLOYMENT.md`
- **Автоматическое создание лендингов**: `docs/AUTO_LANDING_CREATION.md`
- **Архитектура проекта**: `docs/Project.md`
- **Трекер задач**: `docs/Tasktracker.md`
- **Дневник разработки**: `docs/Diary.md`

## 📊 Порты сервисов

| Сервис | Порт | URL |
|--------|------|-----|
| Nginx | 80, 443 | http://volzhck.ru |
| API Gateway | 8080 | http://localhost:8080 |
| Landing Service | 8081 | http://localhost:8081 |
| Content Service | 8082 | http://localhost:8082 |
| Template Service | 8083 | http://localhost:8083 |
| Media Service | 8084 | http://localhost:8084 |
| Integration Service | 8085 | http://localhost:8085 |
| Order Service | 8086 | http://localhost:8086 |
| PostgreSQL | 5432 | localhost:5432 |
| Redis | 6379 | localhost:6379 |
| MinIO API | 9000 | http://localhost:9000 |
| MinIO Console | 9001 | http://localhost:9001 |

## 🎉 Готово!

После успешного развёртывания:

1. ✅ Все сервисы запущены
2. ✅ База данных настроена
3. ✅ Nginx проксирует запросы
4. ✅ Google Sheets интеграция работает
5. ✅ Telegram бот отправляет уведомления
6. ✅ Лендинги доступны по доменам

**Теперь можно создавать лендинги просто добавляя строки в Google Sheets!** 🚀

---

**Для получения помощи**: см. раздел Troubleshooting в `docs/DEPLOYMENT.md`

