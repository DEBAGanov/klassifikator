# 🧪 Локальное тестирование Docker Compose

## ⚠️ Требования

1. **Docker Desktop должен быть запущен!**
   - Откройте Docker Desktop приложение
   - Дождитесь полного запуска (иконка в трее должна быть зелёной)

2. **Проверка Docker:**
   ```bash
   docker ps
   # Должен вывести список контейнеров (может быть пустым)
   ```

## 🚀 Быстрый запуск

### Вариант 1: Автоматический (рекомендуется)

```bash
# 1. Убедитесь что Docker Desktop запущен
# 2. Запустите скрипт
./deploy.sh
```

### Вариант 2: Ручной запуск

```bash
# 1. Остановите старые контейнеры (если есть)
docker-compose -f docker-compose.prod.yml down

# 2. Соберите образы
docker-compose -f docker-compose.prod.yml build

# 3. Запустите инфраструктуру
docker-compose -f docker-compose.prod.yml up -d postgres redis minio

# 4. Подождите 30 секунд
sleep 30

# 5. Запустите все сервисы
docker-compose -f docker-compose.prod.yml up -d

# 6. Проверка статуса
docker-compose -f docker-compose.prod.yml ps

# 7. Просмотр логов
docker-compose -f docker-compose.prod.yml logs -f
```

## 📊 Проверка работы

### Health Checks

```bash
# Проверка всех сервисов
for port in 8080 8081 8082 8083 8084 8085 8086; do
  echo "Checking port $port..."
  curl -f http://localhost:$port/actuator/health && echo " ✅" || echo " ❌"
done
```

### Тестирование синхронизации Google Sheets

```bash
# Синхронизация всех организаций
curl -X POST "http://localhost:8085/api/v1/integration/google-sheets/sync-all?sheetName=Organizations" | jq .
```

### Проверка лендингов

```bash
# Список всех лендингов
curl http://localhost:8081/api/v1/landings | jq .

# Список организаций
curl http://localhost:8081/api/v1/organizations | jq .

# Рендеринг лендинга по домену
curl "http://localhost:8083/api/v1/templates/render-by-domain/modernissimo.volzhck.ru" | head -100
```

### Проверка через Nginx

```bash
# Лендинг через Nginx (если настроен /etc/hosts)
# Добавьте в /etc/hosts:
# 127.0.0.1 modernissimo.volzhck.ru
# 127.0.0.1 test.volzhck.ru

curl http://modernissimo.volzhck.ru
```

## 🔍 Полезные команды

```bash
# Просмотр логов конкретного сервиса
docker logs -f klassifikator-landing-service

# Перезапуск сервиса
docker-compose -f docker-compose.prod.yml restart landing-service

# Остановка всех сервисов
docker-compose -f docker-compose.prod.yml down

# Остановка с удалением volumes (⚠️ удалит данные!)
docker-compose -f docker-compose.prod.yml down -v

# Использование ресурсов
docker stats

# Просмотр всех контейнеров
docker ps -a
```

## 🐛 Troubleshooting

### Проблема: Docker daemon не запущен

**Решение:**
1. Откройте Docker Desktop
2. Дождитесь полного запуска
3. Проверьте: `docker ps`

### Проблема: Порт уже занят

**Решение:**
```bash
# Найти процесс
lsof -i :8081

# Остановить
kill -9 <PID>
```

### Проблема: Ошибка сборки Docker образов

**Решение:**
```bash
# Очистка кэша
docker system prune -a

# Пересборка
docker-compose -f docker-compose.prod.yml build --no-cache
```

### Проблема: Сервисы не запускаются

**Решение:**
```bash
# Проверка логов
docker-compose -f docker-compose.prod.yml logs landing-service

# Проверка .env файла
cat .env | grep -v "^#"

# Проверка подключения к БД
docker exec -it klassifikator-postgres psql -U klassifikator -d klassifikator -c "SELECT 1;"
```

## 📝 Следующие шаги

После успешного запуска:

1. ✅ Проверьте health checks всех сервисов
2. ✅ Запустите синхронизацию Google Sheets
3. ✅ Проверьте создание лендинга
4. ✅ Проверьте работу через Nginx
5. ✅ Протестируйте создание заказа

**Готово к production развёртыванию!** 🚀

