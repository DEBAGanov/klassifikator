#!/bin/bash

cd "$(dirname "$0")"

# Остановить старый процесс если запущен
if [ -f /tmp/landing-service.pid ]; then
    kill $(cat /tmp/landing-service.pid) 2>/dev/null || true
    rm /tmp/landing-service.pid
    sleep 2
fi

# Создать директорию для логов если не существует
mkdir -p logs

# Запуск с переменными окружения
DATABASE_URL="jdbc:postgresql://localhost:5432/klassifikator" \
DATABASE_USERNAME="klassifikator" \
DATABASE_PASSWORD="change_this_strong_password_123" \
REDIS_PASSWORD="change_this_redis_password_456" \
java -jar landing-service/build/libs/landing-service.jar > logs/landing-service.log 2>&1 &

echo $! > /tmp/landing-service.pid
echo "Landing Service started with PID: $(cat /tmp/landing-service.pid)"
echo "Logs: tail -f logs/landing-service.log"

