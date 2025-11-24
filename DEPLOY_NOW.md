# 🚀 Пошаговый план развертывания на Timeweb Cloud

**Дата**: 2025-11-17  
**Статус**: Готово к развертыванию  
**Время выполнения**: ~30-40 минут

---

## ✅ Что уже готово

- ✅ Все микросервисы разработаны и протестированы локально
- ✅ Kubernetes манифесты созданы и настроены
- ✅ DNS записи настроены (`*.volzhck.ru` → `89.223.127.140`)
- ✅ Документация и скрипты автоматизации
- ✅ Ingress с автоматическим SSL (cert-manager)

---

## 🎯 Что нужно сделать (6 шагов)

### ⚡ Шаг 1: Настройка kubectl (5 минут)

```bash
# 1. Откройте Timeweb Cloud панель
open https://timeweb.cloud/my/kubernetes

# 2. Выберите ваш кластер → вкладка "Подключение"
# 3. Скачайте kubeconfig

# 4. Настройте kubectl
cp ~/Downloads/kubeconfig ~/.kube/config

# 5. Проверьте подключение
kubectl cluster-info
kubectl get nodes

# Ожидаемый результат:
# NAME                STATUS   ROLES    AGE   VERSION
# node-1              Ready    <none>   1d    v1.28.x
# node-2              Ready    <none>   1d    v1.28.x
```

✅ **Критерий успеха**: Команда `kubectl get nodes` показывает ваши ноды в статусе Ready

---

### ⚡ Шаг 2: Подготовка Container Registry (3 минуты)

```bash
# 1. Откройте Container Registry
open https://timeweb.cloud/my/container-registry

# 2. Если registry уже создан, получите токен:
#    - Нажмите на registry
#    - Вкладка "Токены доступа"
#    - Скопируйте токен

# 3. Если registry НЕ создан:
#    - Нажмите "Создать registry"
#    - Имя: klassifikator
#    - Скопируйте URL, username и токен
```

**Запишите эти данные:**
- Registry URL: `12df3fb9-wise-cepheus.registry.twcstorage.ru`
- Username: `12df3fb9-wise-cepheus`
- Token: `____________________` (вставьте ваш)

✅ **Критерий успеха**: У вас есть токен доступа к registry

---

### ⚡ Шаг 3: Сборка и загрузка образов (15-20 минут)

```bash
cd /Users/bagano/Downloads/Cursor/klassifikator

# Установите токен в переменную окружения
export TIMEWEB_REGISTRY_TOKEN="ваш-токен-из-шага-2"

# Запустите автоматическую сборку и загрузку
./scripts/build-and-push-images.sh

# ☕ Время на кофе - процесс займет 15-20 минут
```

**Что происходит:**
1. Проверка Docker
2. Вход в registry
3. Сборка Gradle проекта (./gradlew clean build)
4. Сборка 7 Docker образов:
   - api-gateway
   - landing-service
   - content-service
   - template-service
   - media-service
   - integration-service
   - order-service
5. Загрузка всех образов в Timeweb Cloud Registry

✅ **Критерий успеха**: Скрипт завершился успешно, все 7 образов загружены

**Если что-то пошло не так:**
```bash
# Проверьте Docker
docker info

# Проверьте вход в registry
docker login 12df3fb9-wise-cepheus.registry.twcstorage.ru

# Соберите только один сервис для теста
./scripts/build-and-push-images.sh --service landing-service
```

---

### ⚡ Шаг 4: Создание imagePullSecret (1 минута)

```bash
# Создайте secret для доступа к registry
kubectl create secret docker-registry timeweb-registry \
  --docker-server=12df3fb9-wise-cepheus.registry.twcstorage.ru \
  --docker-username=12df3fb9-wise-cepheus \
  --docker-password="ваш-токен-из-шага-2" \
  --namespace=klassifikator

# Если namespace не существует, он будет создан автоматически на следующем шаге
# Если возникла ошибка "namespace not found", выполните:
kubectl create namespace klassifikator

# Затем повторите команду создания secret
```

✅ **Критерий успеха**: Secret создан без ошибок

---

### ⚡ Шаг 5: Проверка .env и credentials (2 минуты)

```bash
cd /Users/bagano/Downloads/Cursor/klassifikator

# Проверьте что .env существует
cat .env

# Проверьте что google-credentials.json существует
ls -l config/google-credentials.json
```

**Если .env НЕ существует:**
```bash
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

✅ **Критерий успеха**: Оба файла существуют и заполнены

---

### ⚡ Шаг 6: Развертывание! (5-10 минут)

```bash
cd /Users/bagano/Downloads/Cursor/klassifikator/k8s

# Запустите автоматическое развертывание
./deploy.sh

# Скрипт автоматически:
# - Создаст namespace klassifikator
# - Создаст секреты из .env и google-credentials.json
# - Применит все Kubernetes манифесты
# - Дождется готовности всех подов
# - Выведет статус и URL для доступа
```

**Что развернется:**
- PostgreSQL (база данных)
- Redis (кэш)
- MinIO (S3-хранилище)
- 7 микросервисов
- API Gateway
- Ingress с SSL

✅ **Критерий успеха**: Все поды в статусе Running

---

## 🔍 Шаг 7: Проверка развертывания (3 минуты)

```bash
cd /Users/bagano/Downloads/Cursor/klassifikator

# Запустите полную диагностику
./scripts/check-k8s-status.sh
```

**Скрипт проверит:**
- ✅ Статус всех подов
- ✅ Статус сервисов
- ✅ Статус Ingress и Load Balancer
- ✅ SSL сертификаты
- ✅ Health checks микросервисов
- ✅ Логи проблемных подов (если есть)
- ✅ Рекомендации по исправлению

✅ **Критерий успеха**: Все поды Running, Ingress показывает IP, health checks проходят

---

## 🧪 Шаг 8: Тестирование (5 минут)

### 1. Проверка API Gateway

```bash
# Проверка health endpoint
curl http://api.volzhck.ru/actuator/health

# Ожидаемый ответ:
# {"status":"UP"}
```

### 2. Создание тестовой организации

```bash
curl -X POST http://api.volzhck.ru/api/v1/organizations \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Тестовая Компания",
    "phone": "+79001234567",
    "email": "test@example.com",
    "address": "г. Волжский, ул. Мира, 10"
  }'

# Скопируйте ID из ответа (например: {"id":1,...})
```

### 3. Создание тестового лендинга

```bash
curl -X POST http://api.volzhck.ru/api/v1/landings \
  -H "Content-Type: application/json" \
  -d '{
    "organizationId": 1,
    "domain": "test.volzhck.ru",
    "templateId": 1,
    "status": "ACTIVE"
  }'
```

### 4. Проверка лендинга в браузере

```bash
# Откройте в браузере
open http://test.volzhck.ru

# Или проверьте curl
curl -I http://test.volzhck.ru
```

✅ **Критерий успеха**: Лендинг открывается в браузере

---

## ✨ Готово!

Поздравляю! Ваше приложение развернуто и работает!

### 🌐 URL для доступа

- **API**: http://api.volzhck.ru
- **Swagger**: http://api.volzhck.ru/swagger-ui.html
- **Health**: http://api.volzhck.ru/actuator/health
- **Лендинги**: http://[любое-имя].volzhck.ru

### 📊 Полезные команды

```bash
# Статус подов (реальное время)
watch kubectl get pods -n klassifikator

# Логи сервиса
kubectl logs -f deployment/landing-service -n klassifikator

# Полная диагностика
./scripts/check-k8s-status.sh

# Перезапуск сервиса
kubectl rollout restart deployment/landing-service -n klassifikator

# Port-forward для локального доступа
kubectl port-forward svc/api-gateway 8080:8080 -n klassifikator
```

---

## ❓ Что делать если что-то не работает?

### Проблема: Поды не запускаются (ImagePullBackOff)

```bash
# 1. Проверьте что secret создан
kubectl get secret timeweb-registry -n klassifikator

# 2. Если нет, создайте с правильным токеном
kubectl create secret docker-registry timeweb-registry \
  --docker-server=12df3fb9-wise-cepheus.registry.twcstorage.ru \
  --docker-username=12df3fb9-wise-cepheus \
  --docker-password="правильный-токен" \
  --namespace=klassifikator

# 3. Перезапустите deployments
kubectl rollout restart deployment -n klassifikator
```

### Проблема: Поды падают (CrashLoopBackOff)

```bash
# 1. Посмотрите логи
kubectl logs <pod-name> -n klassifikator

# 2. Проверьте секреты
kubectl get secret klassifikator-secrets -n klassifikator

# 3. Если нужно, пересоздайте секреты
cd /Users/bagano/Downloads/Cursor/klassifikator/k8s
./undeploy.sh
./deploy.sh
```

### Проблема: SSL сертификаты не получаются

```bash
# Обычно сертификаты получаются автоматически за 5-10 минут
# Проверьте статус
kubectl describe certificate klassifikator-tls -n klassifikator

# Проверьте cert-manager
kubectl get pods -n cert-manager
```

### Проблема: API не отвечает

```bash
# 1. Проверьте что под работает
kubectl get pod -l app=api-gateway -n klassifikator

# 2. Проверьте логи
kubectl logs -l app=api-gateway -n klassifikator

# 3. Проверьте Ingress
kubectl get ingress -n klassifikator
kubectl describe ingress klassifikator-ingress -n klassifikator
```

---

## 📚 Документация

- 📖 [Полное руководство](/docs/TIMEWEB_K8S_QUICKSTART.md)
- 🏗️ [Архитектура проекта](/docs/Project.md)
- 🧪 [Руководство по тестированию](/docs/TESTING_GUIDE.md)
- 📊 [Статус системы](/docs/SYSTEM_STATUS.md)
- 📝 [Дневник разработки](/docs/Diary.md)

---

## 🆘 Нужна помощь?

1. Запустите диагностику: `./scripts/check-k8s-status.sh`
2. Проверьте документацию в `/docs/`
3. Проверьте логи подов: `kubectl logs <pod-name> -n klassifikator`
4. Техподдержка Timeweb Cloud: https://timeweb.cloud/support

---

**Удачи с развертыванием! 🚀**

---

**Версия**: 1.0  
**Дата**: 2025-11-17  
**Автор**: DevOps Team

