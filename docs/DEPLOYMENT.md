# Инструкция по развёртыванию Klassifikator на Timeweb Cloud

**Дата создания**: 2025-11-05  
**Версия**: 1.0

---

## 📋 Содержание

1. [Требования](#требования)
2. [Подготовка сервера](#подготовка-сервера)
3. [Настройка проекта](#настройка-проекта)
4. [Развёртывание через Docker Compose](#развёртывание-через-docker-compose)
5. [Настройка доменов и SSL](#настройка-доменов-и-ssl)
6. [Первый запуск и проверка](#первый-запуск-и-проверка)
7. [Мониторинг и обслуживание](#мониторинг-и-обслуживание)
8. [Обновление системы](#обновление-системы)
9. [Резервное копирование](#резервное-копирование)
10. [Troubleshooting](#troubleshooting)

---

## 🎯 Требования

### Сервер на Timeweb Cloud

- **ОС**: Ubuntu 20.04+ или Debian 11+
- **RAM**: минимум 4 GB (рекомендуется 8 GB)
- **CPU**: минимум 2 ядра (рекомендуется 4 ядра)
- **Диск**: минимум 40 GB SSD
- **Сеть**: Публичный IP адрес

### Программное обеспечение

- Docker 24.0+
- Docker Compose 2.20+
- Git
- OpenSSL (для SSL сертификатов)

### Домены

- Основной домен: `volzhck.ru`
- Поддомены создаются вручную в панели Timeweb Cloud

---

## 🖥️ Подготовка сервера

### Шаг 1: Подключение к серверу

```bash
ssh root@your-server-ip
```

### Шаг 2: Обновление системы

```bash
apt update && apt upgrade -y
```

### Шаг 3: Установка Docker

```bash
# Установка зависимостей
apt install -y apt-transport-https ca-certificates curl software-properties-common

# Добавление репозитория Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# Установка Docker
apt update
apt install -y docker-ce docker-ce-cli containerd.io

# Проверка установки
docker --version
docker-compose --version
```

### Шаг 4: Установка дополнительных инструментов

```bash
apt install -y git htop curl wget nano
```

### Шаг 5: Настройка firewall (UFW)

```bash
# Установка UFW
apt install -y ufw

# Базовые правила
ufw default deny incoming
ufw default allow outgoing

# Разрешить SSH
ufw allow 22/tcp

# Разрешить HTTP/HTTPS
ufw allow 80/tcp
ufw allow 443/tcp

# Включить firewall
ufw enable
ufw status
```

---

## ⚙️ Настройка проекта

### Шаг 1: Клонирование репозитория

```bash
cd /opt
git clone https://github.com/your-username/klassifikator.git
cd klassifikator
```

### Шаг 2: Создание директорий

```bash
mkdir -p config logs backups
chmod 755 config logs backups
```

### Шаг 3: Настройка переменных окружения

Создайте `.env` файл из шаблона:

```bash
cp env.example .env
nano .env
```

**Заполните следующие обязательные поля:**

```env
# Database
DB_PASSWORD=your_secure_db_password_here

# Redis
REDIS_PASSWORD=your_secure_redis_password_here

# MinIO
MINIO_ROOT_PASSWORD=your_secure_minio_password_here

# Google Sheets
GOOGLE_SHEETS_SPREADSHEET_ID=1KS2TOS5ZKxONDmUaVoiwb3tyu3Y1DlGQaME2KM4vItQ

# Telegram
TELEGRAM_BOT_TOKEN=1234567890:ABCdefGHIjklMNOpqrsTUVwxyz
TELEGRAM_CHAT_ID=-1001234567890

# Server
SERVER_IP=your_timeweb_server_ip
```

**Генерация безопасных паролей:**

```bash
# Генерация случайных паролей
openssl rand -base64 32
```

### Шаг 4: Настройка Google Sheets API

1. **Создайте проект в Google Cloud Console:**
   - https://console.cloud.google.com/

2. **Включите Google Sheets API:**
   - APIs & Services → Enable APIs and Services → Google Sheets API

3. **Создайте Service Account:**
   - IAM & Admin → Service Accounts → Create Service Account
   - Скачайте JSON ключ

4. **Разместите credentials:**
   ```bash
   nano config/google-credentials.json
   # Вставьте содержимое скачанного JSON файла
   ```

5. **Дайте доступ к таблице:**
   - Откройте Google Sheets
   - Поделиться → Добавить email из Service Account

### Шаг 5: Настройка Telegram Bot

1. **Создайте бота:**
   - Найдите @BotFather в Telegram
   - Отправьте `/newbot`
   - Следуйте инструкциям
   - Сохраните Token

2. **Получите Chat ID:**
   ```bash
   # Отправьте сообщение боту, затем выполните:
   curl https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates
   # Найдите "chat":{"id": -1001234567890}
   ```

---

## 🚀 Развёртывание через Docker Compose

### Вариант 1: Автоматический деплой (рекомендуется)

```bash
./deploy.sh
```

Скрипт автоматически:
- Проверит наличие `.env`
- Остановит старые контейнеры
- Соберёт Docker образы
- Запустит все сервисы
- Проверит здоровье сервисов

### Вариант 2: Ручной деплой

```bash
# Остановка старых контейнеров
docker-compose -f docker-compose.prod.yml down

# Сборка образов
docker-compose -f docker-compose.prod.yml build --no-cache

# Запуск инфраструктуры
docker-compose -f docker-compose.prod.yml up -d postgres redis minio

# Ожидание готовности
sleep 30

# Запуск приложений
docker-compose -f docker-compose.prod.yml up -d

# Проверка статуса
docker-compose -f docker-compose.prod.yml ps
```

### Проверка логов

```bash
# Все сервисы
docker-compose -f docker-compose.prod.yml logs -f

# Конкретный сервис
docker-compose -f docker-compose.prod.yml logs -f landing-service

# Последние 100 строк
docker-compose -f docker-compose.prod.yml logs --tail=100
```

---

## 🌐 Настройка доменов и SSL

### Шаг 1: Настройка основного домена

1. **Откройте панель Timeweb Cloud:**
   - https://timeweb.cloud/my/domains

2. **Настройте A-запись для volzhck.ru:**
   - Тип: A
   - Имя: @
   - Значение: IP вашего сервера
   - TTL: 300

3. **Проверка DNS:**
   ```bash
   dig volzhck.ru +short
   nslookup volzhck.ru
   ```

### Шаг 2: Создание поддоменов

Для каждого лендинга создайте поддомен:

1. **В панели Timeweb Cloud:**
   - Домены → volzhck.ru → Поддомены
   - Создать поддомен

2. **Укажите:**
   - Имя: `modernissimo` (для modernissimo.volzhck.ru)
   - Сервер: выберите ваш облачный сервер
   - Сохранить

3. **SSL сертификат выдастся автоматически!**

### Шаг 3: Проверка работы

```bash
# HTTP доступ
curl -I http://volzhck.ru
curl -I http://modernissimo.volzhck.ru

# HTTPS доступ (после выдачи SSL)
curl -I https://volzhck.ru
curl -I https://modernissimo.volzhck.ru
```

---

## ✅ Первый запуск и проверка

### Проверка сервисов

```bash
# Проверка здоровья всех сервисов
for port in 8080 8081 8082 8083 8084 8085 8086; do
  echo "Checking port $port..."
  curl -f http://localhost:$port/actuator/health || echo "Failed"
done
```

### Проверка базы данных

```bash
# Подключение к PostgreSQL
docker exec -it klassifikator-postgres psql -U klassifikator -d klassifikator

# Проверка таблиц
\dt

# Выход
\q
```

### Проверка Redis

```bash
# Подключение к Redis
docker exec -it klassifikator-redis redis-cli -a your_redis_password

# Проверка
PING
INFO

# Выход
exit
```

### Проверка MinIO

Откройте в браузере:
```
http://your-server-ip:9001
```

Логин: значение из `MINIO_ROOT_USER` (.env)  
Пароль: значение из `MINIO_ROOT_PASSWORD` (.env)

### Создание первого лендинга

1. **Добавьте строку в Google Sheets:**
   - Название: Тестовая компания
   - Домен: test.volzhck.ru
   - Заполните остальные поля

2. **Создайте поддомен в Timeweb Cloud:**
   - test.volzhck.ru → ваш сервер

3. **Запустите синхронизацию:**
   ```bash
   curl -X POST "http://your-server-ip:8085/api/v1/integration/google-sheets/sync-all?sheetName=Organizations"
   ```

4. **Проверьте лендинг:**
   ```bash
   curl http://test.volzhck.ru
   # или в браузере
   open http://test.volzhck.ru
   ```

---

## 📊 Мониторинг и обслуживание

### Мониторинг Docker контейнеров

```bash
# Статус контейнеров
docker-compose -f docker-compose.prod.yml ps

# Использование ресурсов
docker stats

# Логи
docker-compose -f docker-compose.prod.yml logs -f --tail=50
```

### Мониторинг системы

```bash
# Использование диска
df -h

# Использование RAM
free -h

# Нагрузка CPU
htop

# Сетевые соединения
netstat -tulpn | grep LISTEN
```

### Health Check эндпоинты

- API Gateway: http://localhost:8080/actuator/health
- Landing Service: http://localhost:8081/actuator/health
- Content Service: http://localhost:8082/actuator/health
- Template Service: http://localhost:8083/actuator/health
- Media Service: http://localhost:8084/actuator/health
- Integration Service: http://localhost:8085/actuator/health
- Order Service: http://localhost:8086/actuator/health

### Метрики (Prometheus)

- http://localhost:8080/actuator/prometheus

---

## 🔄 Обновление системы

### Обновление кода

```bash
cd /opt/klassifikator

# Остановка сервисов
docker-compose -f docker-compose.prod.yml down

# Получение обновлений
git pull origin main

# Пересборка и запуск
./deploy.sh
```

### Rolling Update (без простоя)

```bash
# Обновление по одному сервису
docker-compose -f docker-compose.prod.yml up -d --no-deps --build landing-service
docker-compose -f docker-compose.prod.yml up -d --no-deps --build content-service
# и так далее...
```

### Откат на предыдущую версию

```bash
# Просмотр коммитов
git log --oneline

# Откат
git checkout <commit-hash>

# Пересборка
./deploy.sh
```

---

## 💾 Резервное копирование

### Автоматический бэкап базы данных

```bash
# Запуск бэкапа
./scripts/backup.sh
```

Файлы бэкапов сохраняются в `./backups/`

### Настройка автоматических бэкапов через cron

```bash
# Редактирование crontab
crontab -e

# Добавить строку (бэкап каждый день в 2:00 ночи)
0 2 * * * cd /opt/klassifikator && ./scripts/backup.sh >> logs/backup.log 2>&1
```

### Восстановление из бэкапа

```bash
# Распаковка
gunzip backups/klassifikator_20251105_020000.sql.gz

# Восстановление
cat backups/klassifikator_20251105_020000.sql | docker exec -i klassifikator-postgres psql -U klassifikator -d klassifikator
```

### Бэкап volumes

```bash
# Остановка контейнеров
docker-compose -f docker-compose.prod.yml down

# Бэкап volumes
docker run --rm -v klassifikator_postgres_data:/data -v $(pwd)/backups:/backup alpine tar czf /backup/postgres_data.tar.gz /data
docker run --rm -v klassifikator_redis_data:/data -v $(pwd)/backups:/backup alpine tar czf /backup/redis_data.tar.gz /data
docker run --rm -v klassifikator_minio_data:/data -v $(pwd)/backups:/backup alpine tar czf /backup/minio_data.tar.gz /data

# Запуск контейнеров
docker-compose -f docker-compose.prod.yml up -d
```

---

## 🔧 Troubleshooting

### Проблема: Контейнеры не запускаются

**Решение:**
```bash
# Проверка логов
docker-compose -f docker-compose.prod.yml logs

# Проверка ресурсов
df -h
free -h

# Очистка неиспользуемых ресурсов
docker system prune -a
```

### Проблема: Не работает база данных

**Решение:**
```bash
# Проверка логов PostgreSQL
docker logs klassifikator-postgres

# Перезапуск
docker-compose -f docker-compose.prod.yml restart postgres

# Проверка соединения
docker exec klassifikator-postgres pg_isready -U klassifikator
```

### Проблема: Ошибки 502 Bad Gateway в Nginx

**Решение:**
```bash
# Проверка статуса upstream сервисов
curl http://localhost:8080/actuator/health
curl http://localhost:8083/actuator/health

# Перезапуск Nginx
docker-compose -f docker-compose.prod.yml restart nginx

# Проверка конфигурации Nginx
docker exec klassifikator-nginx nginx -t
```

### Проблема: Высокое использование RAM

**Решение:**
```bash
# Проверка использования памяти контейнерами
docker stats --no-stream

# Уменьшение лимитов Java heap
# Отредактируйте Dockerfile и добавьте:
# -Xmx512m -Xms256m

# Пересборка
docker-compose -f docker-compose.prod.yml build --no-cache
docker-compose -f docker-compose.prod.yml up -d
```

### Проблема: Медленная работа

**Решение:**
```bash
# Проверка размера логов
du -sh logs/

# Очистка старых логов
find logs/ -name "*.log" -mtime +7 -delete

# Проверка кэша Redis
docker exec klassifikator-redis redis-cli -a your_redis_password INFO memory

# Очистка кэша (если нужно)
docker exec klassifikator-redis redis-cli -a your_redis_password FLUSHALL
```

### Проблема: Не работает синхронизация с Google Sheets

**Решение:**
```bash
# Проверка credentials
cat config/google-credentials.json

# Проверка логов Integration Service
docker logs klassifikator-integration-service --tail=100

# Тестовый запрос
curl -X POST "http://localhost:8085/api/v1/integration/google-sheets/sync-all"

# Проверка доступа к Google Sheets API
docker exec klassifikator-integration-service curl https://sheets.googleapis.com/v4/spreadsheets/YOUR_SHEET_ID
```

---

## 📚 Полезные команды

### Docker Compose

```bash
# Просмотр всех контейнеров
docker-compose -f docker-compose.prod.yml ps

# Остановка всех контейнеров
docker-compose -f docker-compose.prod.yml down

# Остановка с удалением volumes
docker-compose -f docker-compose.prod.yml down -v

# Перезапуск конкретного сервиса
docker-compose -f docker-compose.prod.yml restart landing-service

# Пересборка конкретного сервиса
docker-compose -f docker-compose.prod.yml up -d --no-deps --build landing-service

# Масштабирование
docker-compose -f docker-compose.prod.yml up -d --scale landing-service=2
```

### Docker

```bash
# Удаление всех остановленных контейнеров
docker container prune

# Удаление всех неиспользуемых образов
docker image prune -a

# Удаление всех неиспользуемых volumes
docker volume prune

# Полная очистка системы
docker system prune -a --volumes
```

### Логи

```bash
# Просмотр логов в реальном времени
docker-compose -f docker-compose.prod.yml logs -f

# Логи конкретного сервиса
docker logs -f klassifikator-landing-service

# Поиск в логах
docker logs klassifikator-landing-service 2>&1 | grep ERROR

# Экспорт логов
docker logs klassifikator-landing-service > logs/landing-service-debug.log
```

---

## 🎯 Чек-лист для production

- [ ] `.env` файл настроен с безопасными паролями
- [ ] Google Sheets credentials размещены в `config/`
- [ ] Telegram bot настроен
- [ ] DNS записи настроены для всех доменов
- [ ] SSL сертификаты получены (автоматически от Timeweb)
- [ ] Firewall настроен (UFW)
- [ ] Автоматические бэкапы настроены (cron)
- [ ] Мониторинг настроен
- [ ] Логи ротируются
- [ ] Все сервисы в статусе "healthy"
- [ ] Тестовый лендинг создан и работает
- [ ] Google Sheets синхронизация работает
- [ ] Telegram уведомления работают

---

## 📞 Поддержка

- **Документация**: `/docs/`
- **Логи**: `/logs/`
- **Бэкапы**: `/backups/`

---

**Версия**: 1.0  
**Дата последнего обновления**: 2025-11-05

