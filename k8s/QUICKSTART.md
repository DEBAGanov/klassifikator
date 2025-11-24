# 🚀 Быстрый старт: Развертывание Klassifikator в Timeweb Cloud Kubernetes

## 📋 Что нужно сделать

### 1️⃣ Создать кластер в Timeweb Cloud

1. Откройте https://timeweb.cloud/my/kubernetes
2. Нажмите **"Создать кластер"**
3. Выберите конфигурацию:
   - **Имя**: klassifikator-prod
   - **Версия K8s**: 1.28+ (последняя stable)
   - **Регион**: Москва
   - **Worker nodes**: 2 ноды
   - **Тип**: Standard
   - **CPU**: 2 vCPU на ноду
   - **RAM**: 4 GB на ноду
   - **Диск**: 50 GB SSD на ноду
   - **CNI**: Calico
4. Включите аддоны:
   - ✅ Nginx Ingress Controller
   - ✅ cert-manager
   - ✅ Metrics Server
5. Нажмите **"Создать"** (⏱️ ~10-15 минут)

**Стоимость**: ~2400-3000 руб/месяц

### 2️⃣ Настроить kubectl

```bash
# Скачайте kubeconfig из панели Timeweb Cloud
# Перейдите в кластер → Подключение → Скачать kubeconfig

# Сохраните kubeconfig
mkdir -p ~/.kube
cp ~/Downloads/kubeconfig ~/.kube/config-klassifikator

# Установите переменную окружения
export KUBECONFIG=~/.kube/config-klassifikator

# Проверка подключения
kubectl cluster-info
kubectl get nodes
# Должно показать 2 ноды в статусе Ready
```

### 3️⃣ Подготовить Docker образы

**Вариант A: Docker Hub (рекомендуется для начала)**

```bash
# Логин в Docker Hub
docker login

# Соберите и загрузите образы
cd /Users/bagano/Downloads/Cursor/klassifikator

# Сборка
docker-compose -f docker-compose.prod.yml build

# Тегирование (замените YOUR_USERNAME на ваш Docker Hub username)
docker tag klassifikator/landing-service:latest YOUR_USERNAME/klassifikator-landing:latest
docker tag klassifikator/landing-service:latest 12df3fb9-wise-cepheus.registry.twcstorage.ru/landing-service:latest

docker tag klassifikator/content-service:latest YOUR_USERNAME/klassifikator-content:latest
docker tag klassifikator/template-service:latest YOUR_USERNAME/klassifikator-template:latest
docker tag klassifikator/media-service:latest YOUR_USERNAME/klassifikator-media:latest
docker tag klassifikator/integration-service:latest YOUR_USERNAME/klassifikator-integration:latest
docker tag klassifikator/order-service:latest YOUR_USERNAME/klassifikator-order:latest
docker tag klassifikator/api-gateway:latest YOUR_USERNAME/klassifikator-gateway:latest

# Push
docker push YOUR_USERNAME/klassifikator-landing:latest
docker push YOUR_USERNAME/klassifikator-content:latest
docker push YOUR_USERNAME/klassifikator-template:latest
docker push YOUR_USERNAME/klassifikator-media:latest
docker push YOUR_USERNAME/klassifikator-integration:latest
docker push YOUR_USERNAME/klassifikator-order:latest
docker push YOUR_USERNAME/klassifikator-gateway:latest
```

**Обновите манифесты:**

```bash
# Замените в каждом *-deployment.yaml в папке k8s/base/
# image: klassifikator/landing-service:latest
# на
# image: YOUR_USERNAME/klassifikator-landing:latest

# Или используйте sed (замените YOUR_USERNAME):
cd k8s/base
sed -i '' 's|klassifikator/|YOUR_USERNAME/klassifikator-|g' *-deployment.yaml
```

**Вариант B: Timeweb Cloud Container Registry** (рекомендуется для production)

**Шаг B.1: Создание Container Registry в Timeweb Cloud**

1. Откройте https://timeweb.cloud/my/containers
2. Нажмите **"Создать реестр"** или **"Container Registry"**
3. Заполните форму:
   - **Имя реестра**: `klassifikator` (или любое другое)
   - **Регион**: Москва (или ближайший)
4. Нажмите **"Создать"**
5. После создания откройте реестр и перейдите в **"Настройки"** → **"Доступ"**
6. Скопируйте:
   - **URL реестра**: `registry.timeweb.cloud/YOUR_PROJECT`
   - **Логин**: обычно ваш email или username
   - **Пароль**: можно создать токен доступа

**Шаг B.2: Логин в Container Registry**

```bash
# Логин в реестр Timeweb Cloud
docker login registry.timeweb.cloud

# Введите:
# Username: ваш_email_или_username
# Password: ваш_пароль_или_токен

# Вы должны увидеть:
# Login Succeeded
```

**Шаг B.3: Тегирование и загрузка ВСЕХ образов**

```bash
# Перейдите в корень проекта
cd /Users/bagano/Downloads/Cursor/klassifikator

# ВАЖНО: Замените YOUR_PROJECT на имя вашего проекта из Timeweb Cloud
# Например: registry.timeweb.cloud/klassifikator/

# Тегирование всех образов
docker tag klassifikator/landing-service:latest registry.timeweb.cloud/YOUR_PROJECT/landing-service:latest
docker tag klassifikator/content-service:latest registry.timeweb.cloud/YOUR_PROJECT/content-service:latest
docker tag klassifikator/template-service:latest registry.timeweb.cloud/YOUR_PROJECT/template-service:latest
docker tag klassifikator/media-service:latest registry.timeweb.cloud/YOUR_PROJECT/media-service:latest
docker tag klassifikator/integration-service:latest registry.timeweb.cloud/YOUR_PROJECT/integration-service:latest
docker tag klassifikator/order-service:latest registry.timeweb.cloud/YOUR_PROJECT/order-service:latest
docker tag klassifikator/api-gateway:latest registry.timeweb.cloud/YOUR_PROJECT/api-gateway:latest

# Push всех образов (это займет 10-20 минут)
echo "Загрузка образов в реестр..."
docker push registry.timeweb.cloud/YOUR_PROJECT/landing-service:latest
docker push registry.timeweb.cloud/YOUR_PROJECT/content-service:latest
docker push registry.timeweb.cloud/YOUR_PROJECT/template-service:latest
docker push registry.timeweb.cloud/YOUR_PROJECT/media-service:latest
docker push registry.timeweb.cloud/YOUR_PROJECT/integration-service:latest
docker push registry.timeweb.cloud/YOUR_PROJECT/order-service:latest
docker push registry.timeweb.cloud/YOUR_PROJECT/api-gateway:latest

echo "✅ Все образы загружены!"
```

**Шаг B.4: Обновление манифестов**

```bash
# Перейдите в папку с манифестами
cd k8s/base

# ВАЖНО: Замените YOUR_PROJECT на имя вашего проекта
# Например, если ваш проект называется "klassifikator-prod":
# sed -i '' 's|klassifikator/|registry.timeweb.cloud/klassifikator-prod/|g' *-deployment.yaml

# Автоматическое обновление (для macOS):
sed -i '' 's|image: klassifikator/|image: registry.timeweb.cloud/YOUR_PROJECT/|g' *-deployment.yaml

# Для Linux используйте:
# sed -i 's|image: klassifikator/|image: registry.timeweb.cloud/YOUR_PROJECT/|g' *-deployment.yaml

# Проверьте изменения:
grep "image:" *-deployment.yaml

# Вы должны увидеть:
# landing-service-deployment.yaml:        image: registry.timeweb.cloud/YOUR_PROJECT/landing-service:latest
# content-service-deployment.yaml:        image: registry.timeweb.cloud/YOUR_PROJECT/content-service:latest
# и т.д.
```

**Шаг B.5: Создание ImagePullSecret (для приватного реестра)**

Если ваш реестр приватный, создайте секрет для доступа:

```bash
# Создайте секрет с credentials для Docker Registry
kubectl create secret docker-registry timeweb-registry \
  --docker-server=registry.timeweb.cloud \
  --docker-username=ваш_username \
  --docker-password=ваш_пароль \
  --docker-email=ваш_email \
  -n klassifikator

# Проверка:
kubectl get secret timeweb-registry -n klassifikator
```

Затем добавьте в каждый deployment (или сделайте это автоматически):

```bash
# Добавьте imagePullSecrets в spec.template.spec каждого deployment
# Это можно сделать вручную или через patch

# Пример для landing-service:
kubectl patch deployment landing-service -n klassifikator \
  -p '{"spec":{"template":{"spec":{"imagePullSecrets":[{"name":"timeweb-registry"}]}}}}'
```

### 4️⃣ Проверить .env и credentials (ОЧЕНЬ ВАЖНО!)

**Шаг 4.1: Проверка .env файла**

```bash
# Перейдите в корень проекта
cd /Users/bagano/Downloads/Cursor/klassifikator

# Проверьте наличие .env файла
ls -la .env

# Если файл не существует, создайте его:
cp env.example .env

# Откройте файл для редактирования
nano .env
# или
open .env
```

**Обязательные поля в .env:**

```env
# Database (придумайте надежный пароль!)
DB_NAME=klassifikator
DB_USER=klassifikator
DB_PASSWORD=ваш_надежный_пароль_123!@#

# Redis (придумайте надежный пароль!)
REDIS_PASSWORD=ваш_redis_пароль_456!@#

# MinIO (придумайте надежный пароль!)
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=ваш_minio_пароль_789!@#

# Google Sheets (ID таблицы из URL)
GOOGLE_SHEETS_SPREADSHEET_ID=1KS2TOS5ZKxONDmUaVoiwb3tyu3Y1DlGQaME2KM4vItQ

# Telegram (получите у @BotFather)
TELEGRAM_BOT_TOKEN=1234567890:ABCdefGHIjklMNOpqrsTUVwxyz
TELEGRAM_CHAT_ID=-1001234567890
```

**Как получить Telegram Bot Token:**

1. Найдите в Telegram бота **@BotFather**
2. Отправьте команду `/newbot`
3. Следуйте инструкциям (придумайте имя и username для бота)
4. Скопируйте полученный токен
5. Чтобы получить Chat ID:
   ```bash
   # Отправьте любое сообщение вашему боту в Telegram
   # Затем выполните:
   curl https://api.telegram.org/bot<ВАШ_ТОКЕН>/getUpdates
   
   # Найдите в ответе: "chat":{"id":-1001234567890}
   # Это и есть ваш CHAT_ID
   ```

**Шаг 4.2: Проверка Google Credentials**

```bash
# Проверьте наличие файла
ls -la config/google-credentials.json

# Если файл не существует:
mkdir -p config

# Создайте Service Account в Google Cloud Console:
# 1. Откройте https://console.cloud.google.com/
# 2. Создайте новый проект или выберите существующий
# 3. Перейдите в "APIs & Services" → "Credentials"
# 4. Нажмите "Create Credentials" → "Service Account"
# 5. Заполните форму и создайте
# 6. Нажмите на созданный Service Account
# 7. Перейдите в "Keys" → "Add Key" → "Create new key"
# 8. Выберите JSON и скачайте файл
# 9. Переименуйте скачанный файл в google-credentials.json
# 10. Переместите в папку config/

# Скопируйте файл (если он в Downloads):
cp ~/Downloads/your-project-xxxxx.json config/google-credentials.json

# Проверьте содержимое (должен быть валидный JSON):
cat config/google-credentials.json | head -n 5

# Вы должны увидеть что-то вроде:
# {
#   "type": "service_account",
#   "project_id": "your-project-id",
#   "private_key_id": "...",
#   "private_key": "-----BEGIN PRIVATE KEY-----\n..."
```

**ВАЖНО:** Дайте Service Account доступ к вашей Google Таблице:
1. Откройте вашу Google Таблицу
2. Нажмите **"Поделиться"**
3. Добавьте email из Service Account (найдите в google-credentials.json поле `client_email`)
4. Дайте права **"Редактор"**

### 5️⃣ Развернуть приложение (ГЛАВНЫЙ ЭТАП!)

**Шаг 5.1: Финальная проверка перед деплоем**

```bash
# Убедитесь, что вы в корне проекта
cd /Users/bagano/Downloads/Cursor/klassifikator

# Проверьте подключение к кластеру
kubectl cluster-info

# Вы должны увидеть:
# Kubernetes control plane is running at https://...
# CoreDNS is running at https://...

# Проверьте ноды
kubectl get nodes

# Вы должны увидеть 2 ноды в статусе Ready:
# NAME                   STATUS   ROLES    AGE   VERSION
# worker-node-1          Ready    <none>   10m   v1.28.x
# worker-node-2          Ready    <none>   10m   v1.28.x

# Проверьте наличие всех файлов
ls -la .env
ls -la config/google-credentials.json
ls -la k8s/deploy.sh
ls -la k8s/base/*.yaml
```

**Шаг 5.2: Запуск деплоя**

```bash
# Перейдите в папку k8s
cd k8s

# Сделайте скрипт исполняемым (если еще не сделали)
chmod +x deploy.sh

# ЗАПУСТИТЕ ДЕПЛОЙ!
./deploy.sh
```

**Что будет происходить (следите за выводом):**

```
=========================================
  Развертывание Klassifikator в K8s
=========================================

ℹ️  Проверка необходимых инструментов...
✅ Все необходимые инструменты установлены

ℹ️  Проверка подключения к Kubernetes кластеру...
✅ Подключено к кластеру: klassifikator-prod

ℹ️  Создание namespace klassifikator...
✅ Namespace klassifikator создан

ℹ️  Создание секретов...
✅ Secret klassifikator-secrets создан
✅ Secret google-credentials создан

ℹ️  Применение Kubernetes манифестов...
namespace/klassifikator created
configmap/klassifikator-config created
secret/klassifikator-secrets created
persistentvolumeclaim/postgres-pvc created
persistentvolumeclaim/redis-pvc created
persistentvolumeclaim/minio-pvc created
deployment.apps/postgres created
service/postgres-service created
deployment.apps/redis created
service/redis-service created
deployment.apps/minio created
service/minio-service created
deployment.apps/landing-service created
service/landing-service created
deployment.apps/content-service created
service/content-service created
deployment.apps/template-service created
service/template-service created
deployment.apps/media-service created
service/media-service created
deployment.apps/integration-service created
service/integration-service created
deployment.apps/order-service created
service/order-service created
deployment.apps/api-gateway created
service/api-gateway created
ingress.networking.k8s.io/klassifikator-ingress created
✅ Манифесты применены

ℹ️  Ожидание готовности подов...
ℹ️  Ожидание PostgreSQL...
pod/postgres-xxx condition met
ℹ️  Ожидание Redis...
pod/redis-xxx condition met
ℹ️  Ожидание MinIO...
pod/minio-xxx condition met
ℹ️  Ожидание микросервисов...
pod/landing-service-xxx condition met
pod/content-service-xxx condition met
pod/template-service-xxx condition met
pod/media-service-xxx condition met
pod/integration-service-xxx condition met
pod/order-service-xxx condition met
pod/api-gateway-xxx condition met
✅ Все поды готовы

ℹ️  Проверка статуса развертывания...
=== Статус подов ===
NAME                                   READY   STATUS    RESTARTS   AGE
postgres-xxx                           1/1     Running   0          5m
redis-xxx                              1/1     Running   0          5m
minio-xxx                              1/1     Running   0          5m
landing-service-xxx                    1/1     Running   0          3m
content-service-xxx                    1/1     Running   0          3m
template-service-xxx                   1/1     Running   0          3m
media-service-xxx                      1/1     Running   0          3m
integration-service-xxx                1/1     Running   0          3m
order-service-xxx                      1/1     Running   0          3m
api-gateway-xxx                        1/1     Running   0          3m

✅ Развертывание завершено успешно!
```

**⏱️ Время развертывания:** 5-10 минут

**Если возникли ошибки:**

```bash
# Проверьте логи конкретного пода
kubectl logs -f pod/landing-service-xxx -n klassifikator

# Проверьте описание пода
kubectl describe pod landing-service-xxx -n klassifikator

# Проверьте события
kubectl get events -n klassifikator --sort-by='.lastTimestamp'

# Проверьте статус всех ресурсов
kubectl get all -n klassifikator
```

### 6️⃣ Настроить DNS (КРИТИЧЕСКИ ВАЖНО!)

**Шаг 6.1: Получение IP Load Balancer**

```bash
# Получите IP Load Balancer
kubectl get ingress klassifikator-ingress -n klassifikator

# Вывод будет примерно таким:
# NAME                     CLASS   HOSTS                   ADDRESS         PORTS     AGE
# klassifikator-ingress    nginx   volzhck.ru,*.volzhck.ru 123.45.67.89   80, 443   5m

# Если в колонке ADDRESS пусто, подождите 1-2 минуты и повторите команду
```

**⚠️ ВАЖНО:** Запишите IP адрес (например, `123.45.67.89`)

**Шаг 6.2: Настройка DNS в Timeweb Cloud**

1. **Откройте панель управления доменами:**
   - Перейдите на https://timeweb.cloud/my/domains
   - Найдите и нажмите на домен `volzhck.ru`

2. **Перейдите в раздел DNS:**
   - Нажмите на вкладку **"DNS-записи"** или **"DNS"**
   - Вы увидите список существующих DNS записей

3. **Добавьте A-запись для основного домена:**
   - Нажмите **"Добавить запись"**
   - Заполните форму:
     - **Тип записи**: A
     - **Имя (поддомен)**: `@` (это означает корневой домен)
     - **Значение (IP адрес)**: `123.45.67.89` (ваш IP Load Balancer)
     - **TTL**: 300 (5 минут)
   - Нажмите **"Сохранить"**

4. **Добавьте A-запись для API:**
   - Нажмите **"Добавить запись"**
   - Заполните форму:
     - **Тип записи**: A
     - **Имя (поддомен)**: `api`
     - **Значение (IP адрес)**: `123.45.67.89` (тот же IP)
     - **TTL**: 300
   - Нажмите **"Сохранить"**

5. **Добавьте A-запись для wildcard (все поддомены):**
   - Нажмите **"Добавить запись"**
   - Заполните форму:
     - **Тип записи**: A
     - **Имя (поддомен)**: `*` (звездочка означает любой поддомен)
     - **Значение (IP адрес)**: `123.45.67.89` (тот же IP)
     - **TTL**: 300
   - Нажмите **"Сохранить"**

**Итоговые DNS записи должны выглядеть так:**

| Тип | Имя | Значение | TTL |
|-----|-----|----------|-----|
| A | @ | 123.45.67.89 | 300 |
| A | api | 123.45.67.89 | 300 |
| A | * | 123.45.67.89 | 300 |

**Шаг 6.3: Проверка DNS**

```bash
# Проверьте основной домен
dig volzhck.ru +short
# Должно вернуть: 123.45.67.89

# Проверьте API поддомен
dig api.volzhck.ru +short
# Должно вернуть: 123.45.67.89

# Проверьте wildcard (любой поддомен)
dig test.volzhck.ru +short
# Должно вернуть: 123.45.67.89

# Альтернативная проверка через nslookup
nslookup volzhck.ru
nslookup api.volzhck.ru
nslookup test.volzhck.ru
```

**⏱️ Время распространения DNS:** 5-30 минут (обычно 5-10 минут)

**Если DNS не работает:**
- Подождите еще 10-15 минут
- Очистите DNS кэш на вашем компьютере:
  ```bash
  # macOS
  sudo dscacheutil -flushcache; sudo killall -HUP mDNSResponder
  
  # Linux
  sudo systemd-resolve --flush-caches
  
  # Windows (в PowerShell от администратора)
  ipconfig /flushdns
  ```

### 7️⃣ Дождаться SSL сертификатов (АВТОМАТИЧЕСКИ!)

**Шаг 7.1: Мониторинг выдачи сертификатов**

```bash
# Проверка статуса сертификатов (с автообновлением)
kubectl get certificate -n klassifikator -w

# Вы увидите процесс:
# NAME                READY   SECRET              AGE
# klassifikator-tls   False   klassifikator-tls   30s   <- Сертификат запрашивается
# klassifikator-tls   False   klassifikator-tls   1m    <- Проверка домена
# klassifikator-tls   True    klassifikator-tls   2m    <- Готово! ✅

# Нажмите Ctrl+C чтобы остановить мониторинг
```

**Шаг 7.2: Проверка деталей сертификата**

```bash
# Детальная информация о сертификате
kubectl describe certificate klassifikator-tls -n klassifikator

# Вы должны увидеть в конце:
# Events:
#   Type    Reason     Age   From          Message
#   ----    ------     ----  ----          -------
#   Normal  Issuing    3m    cert-manager  Issuing certificate as Secret does not exist
#   Normal  Generated  3m    cert-manager  Stored new private key in temporary Secret resource
#   Normal  Requested  3m    cert-manager  Created new CertificateRequest resource
#   Normal  Issuing    2m    cert-manager  The certificate has been successfully issued
```

**Шаг 7.3: Проверка секрета с сертификатом**

```bash
# Проверьте, что секрет создан
kubectl get secret klassifikator-tls -n klassifikator

# Вывод:
# NAME                TYPE                DATA   AGE
# klassifikator-tls   kubernetes.io/tls   2      5m

# Проверьте содержимое (должны быть tls.crt и tls.key)
kubectl describe secret klassifikator-tls -n klassifikator
```

**⏱️ Время выдачи сертификатов:** 2-5 минут после настройки DNS

**Если сертификаты не выдаются:**

```bash
# Проверьте логи cert-manager
kubectl logs -n cert-manager deployment/cert-manager --tail=50

# Проверьте CertificateRequest
kubectl get certificaterequest -n klassifikator

# Проверьте Challenge (если используется HTTP-01)
kubectl get challenge -n klassifikator

# Проверьте ClusterIssuer
kubectl describe clusterissuer letsencrypt-prod
```

**Типичные проблемы:**
- DNS еще не распространился → подождите еще
- Неправильный email в ClusterIssuer → проверьте `k8s/base/ingress.yaml`
- Порт 80 недоступен → проверьте Ingress Controller

### 8️⃣ Проверить работу (ФИНАЛЬНАЯ ПРОВЕРКА!)

**Шаг 8.1: Проверка HTTP (без SSL)**

```bash
# Проверьте, что Ingress отвечает
curl -I http://api.volzhck.ru

# Вы должны увидеть редирект на HTTPS:
# HTTP/1.1 308 Permanent Redirect
# Location: https://api.volzhck.ru/
```

**Шаг 8.2: Проверка HTTPS (со SSL)**

```bash
# Проверьте API Gateway
curl -I https://api.volzhck.ru/actuator/health

# Ожидаемый ответ:
# HTTP/2 200
# content-type: application/vnd.spring-boot.actuator.v3+json
# ...

# Детальная проверка health
curl https://api.volzhck.ru/actuator/health | jq .

# Ожидаемый JSON:
# {
#   "status": "UP",
#   "components": {
#     "db": {
#       "status": "UP"
#     },
#     "redis": {
#       "status": "UP"
#     }
#   }
# }
```

**Шаг 8.3: Проверка всех микросервисов**

```bash
# Используйте утилиту status.sh
cd /Users/bagano/Downloads/Cursor/klassifikator/k8s
./status.sh

# Или проверьте вручную каждый сервис:
kubectl exec -n klassifikator deployment/landing-service -- \
  curl -s http://localhost:8081/actuator/health | jq .

kubectl exec -n klassifikator deployment/content-service -- \
  curl -s http://localhost:8082/actuator/health | jq .

kubectl exec -n klassifikator deployment/template-service -- \
  curl -s http://localhost:8083/actuator/health | jq .

kubectl exec -n klassifikator deployment/media-service -- \
  curl -s http://localhost:8084/actuator/health | jq .

kubectl exec -n klassifikator deployment/integration-service -- \
  curl -s http://localhost:8085/actuator/health | jq .

kubectl exec -n klassifikator deployment/order-service -- \
  curl -s http://localhost:8086/actuator/health | jq .

# Все должны вернуть: {"status":"UP"}
```

**Шаг 8.4: Проверка в браузере**

```bash
# Откройте в браузере (macOS)
open https://api.volzhck.ru/actuator/health

# Или (Linux)
xdg-open https://api.volzhck.ru/actuator/health

# Или просто откройте в браузере вручную:
# https://api.volzhck.ru/actuator/health
```

**Вы должны увидеть JSON с статусом UP!**

**Шаг 8.5: Создание тестового лендинга**

```bash
# Добавьте тестовую строку в Google Sheets
# (используйте вашу таблицу из GOOGLE_SHEETS_SPREADSHEET_ID)

# Запустите синхронизацию
curl -X POST "https://api.volzhck.ru/api/v1/integration/google-sheets/sync-all?sheetName=Organizations"

# Проверьте созданные организации
curl https://api.volzhck.ru/api/v1/organizations | jq .

# Проверьте созданные лендинги
curl https://api.volzhck.ru/api/v1/landings | jq .

# Откройте лендинг в браузере (замените test на ваш домен из таблицы)
open https://test.volzhck.ru
```

## 🎉 Готово!

**Ваш Klassifikator успешно развернут в Kubernetes!**

### ✅ Что у вас теперь работает:

1. ✅ **7 микросервисов** работают в Kubernetes
2. ✅ **PostgreSQL, Redis, MinIO** работают с persistent storage
3. ✅ **Автоматические SSL сертификаты** от Let's Encrypt
4. ✅ **Load Balancer** распределяет трафик
5. ✅ **Ingress** маршрутизирует запросы
6. ✅ **Health checks** мониторят состояние сервисов
7. ✅ **Автомасштабирование** (можно настроить HPA)
8. ✅ **Автоматическое создание лендингов** через Google Sheets

### 📊 Следующие шаги:

1. **Добавьте организации в Google Sheets**
2. **Создайте поддомены в Timeweb Cloud** (для каждого лендинга)
3. **Запустите синхронизацию** через API
4. **Проверьте лендинги** в браузере
5. **Настройте мониторинг** (Prometheus + Grafana)
6. **Настройте бэкапы** (Velero или скрипты)

### 🎓 Полезные команды для дальнейшей работы:

```bash
# Просмотр всех ресурсов
kubectl get all -n klassifikator

# Просмотр логов
kubectl logs -f deployment/landing-service -n klassifikator

# Проверка использования ресурсов
kubectl top pods -n klassifikator
kubectl top nodes

# Масштабирование
kubectl scale deployment landing-service --replicas=3 -n klassifikator

# Обновление образа
kubectl set image deployment/landing-service \
  landing-service=registry.timeweb.cloud/YOUR_PROJECT/landing-service:v2.0 \
  -n klassifikator

# Откат обновления
kubectl rollout undo deployment/landing-service -n klassifikator

# Удаление всего (если нужно)
cd k8s
./undeploy.sh
```

**Поздравляю! Вы успешно развернули микросервисное приложение в Kubernetes! 🚀**

## 📊 Полезные команды

```bash
# Статус всех компонентов
cd k8s
./status.sh

# Просмотр логов
./logs.sh landing-service

# Проверка подов
kubectl get pods -n klassifikator

# Проверка сервисов
kubectl get services -n klassifikator

# Проверка Ingress
kubectl get ingress -n klassifikator
```

## 🔧 Управление

### Масштабирование

```bash
# Увеличить реплики
kubectl scale deployment landing-service --replicas=3 -n klassifikator
```

### Обновление

```bash
# Обновить образ
kubectl set image deployment/landing-service \
  landing-service=YOUR_USERNAME/klassifikator-landing:v2.0 \
  -n klassifikator

# Проверка статуса
kubectl rollout status deployment/landing-service -n klassifikator
```

### Откат

```bash
# Откат к предыдущей версии
kubectl rollout undo deployment/landing-service -n klassifikator
```

## 🐛 Troubleshooting

### Поды не запускаются

```bash
# Детали пода
kubectl describe pod <pod-name> -n klassifikator

# Логи
kubectl logs <pod-name> -n klassifikator

# События
kubectl get events -n klassifikator --sort-by='.lastTimestamp'
```

### Проблемы с образами

```bash
# Проверьте, что образы доступны
docker pull YOUR_USERNAME/klassifikator-landing:latest

# Проверьте imagePullSecrets (если используете private registry)
kubectl get pods -n klassifikator -o jsonpath='{.items[0].spec.imagePullSecrets}'
```

### SSL не работает

```bash
# Проверка cert-manager
kubectl get pods -n cert-manager

# Проверка ClusterIssuer
kubectl get clusterissuer

# Проверка сертификата
kubectl describe certificate klassifikator-tls -n klassifikator

# Логи cert-manager
kubectl logs -n cert-manager deployment/cert-manager
```

## 📚 Дополнительная документация

- [Полное руководство](../docs/KUBERNETES_DEPLOYMENT.md) - подробная документация (500+ строк)
- [README](README.md) - описание структуры и команд
- [Документация проекта](../docs/Project.md) - архитектура системы

## 💰 Стоимость

**~2400-3000 руб/месяц** за минимальную конфигурацию:
- 2 worker nodes (2 vCPU, 4 GB RAM каждая)
- Load Balancer
- 32 GB Persistent Storage
- Трафик

## 📞 Поддержка

- Техподдержка Timeweb Cloud: https://timeweb.cloud/support
- Документация Timeweb K8s: https://timeweb.cloud/docs/k8s

---

**Версия**: 1.0  
**Дата**: 2025-11-10

