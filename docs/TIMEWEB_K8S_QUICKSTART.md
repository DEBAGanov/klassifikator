# 🚀 Быстрый старт развертывания в Timeweb Cloud Kubernetes

**Дата создания**: 2025-11-17  
**Статус**: Production Ready

---

## 📋 Предварительные требования

### 1. Локальные инструменты
- ✅ Docker Desktop установлен
- ✅ kubectl установлен ([Установка](https://kubernetes.io/docs/tasks/tools/))
- ✅ Аккаунт в Timeweb Cloud

### 2. Доступы
- Kubeconfig файл от Timeweb Cloud
- Токен доступа к Container Registry Timeweb Cloud

---

## 🎯 Шаг 1: Настройка kubectl

### 1.1 Скачайте kubeconfig из Timeweb Cloud

1. Откройте [панель Timeweb Cloud Kubernetes](https://timeweb.cloud/my/kubernetes)
2. Выберите ваш кластер
3. Перейдите на вкладку **"Подключение"**
4. Скачайте файл `kubeconfig`

### 1.2 Настройте kubectl

```bash
# Создайте директорию для kubectl конфигов (если её нет)
mkdir -p ~/.kube

# Скопируйте скачанный kubeconfig
cp ~/Downloads/kubeconfig ~/.kube/config

# Или добавьте к существующему конфигу
export KUBECONFIG=~/.kube/config:~/Downloads/kubeconfig
kubectl config view --flatten > ~/.kube/config.new
mv ~/.kube/config.new ~/.kube/config

# Проверьте подключение
kubectl cluster-info
kubectl get nodes
```

**Ожидаемый результат:**
```
NAME                          STATUS   ROLES    AGE   VERSION
klassifikator-node-1          Ready    <none>   1d    v1.28.x
klassifikator-node-2          Ready    <none>   1d    v1.28.x
```

---

## 🐳 Шаг 2: Подготовка Docker Registry

### 2.1 Создайте Container Registry в Timeweb Cloud

1. Откройте [Container Registry](https://timeweb.cloud/my/container-registry)
2. Нажмите **"Создать registry"**
3. Имя: `klassifikator`
4. Скопируйте:
   - **Registry URL**: `12df3fb9-wise-cepheus.registry.twcstorage.ru`
   - **Username**: `12df3fb9-wise-cepheus`
   - **Token**: (нажмите "Показать токен")

### 2.2 Войдите в registry

```bash
# Войдите в Docker Registry Timeweb Cloud
docker login 12df3fb9-wise-cepheus.registry.twcstorage.ru

# Username: 12df3fb9-wise-cepheus
# Password: [вставьте токен из панели Timeweb]
```

---

## 🏗️ Шаг 3: Сборка и загрузка образов

### 3.1 Сборка всех образов

```bash
cd /Users/bagano/Downloads/Cursor/klassifikator

# Сборка образов для всех сервисов
./gradlew clean build

# Сборка Docker образов
docker build -t 12df3fb9-wise-cepheus.registry.twcstorage.ru/landing-service:latest -f landing-service/Dockerfile .
docker build -t 12df3fb9-wise-cepheus.registry.twcstorage.ru/content-service:latest -f content-service/Dockerfile .
docker build -t 12df3fb9-wise-cepheus.registry.twcstorage.ru/template-service:latest -f template-service/Dockerfile .
docker build -t 12df3fb9-wise-cepheus.registry.twcstorage.ru/media-service:latest -f media-service/Dockerfile .
docker build -t 12df3fb9-wise-cepheus.registry.twcstorage.ru/integration-service:latest -f integration-service/Dockerfile .
docker build -t 12df3fb9-wise-cepheus.registry.twcstorage.ru/order-service:latest -f order-service/Dockerfile .
docker build -t 12df3fb9-wise-cepheus.registry.twcstorage.ru/api-gateway:latest -f api-gateway/Dockerfile .
```

### 3.2 Загрузка образов в Registry

```bash
# Push всех образов
docker push 12df3fb9-wise-cepheus.registry.twcstorage.ru/landing-service:latest
docker push 12df3fb9-wise-cepheus.registry.twcstorage.ru/content-service:latest
docker push 12df3fb9-wise-cepheus.registry.twcstorage.ru/template-service:latest
docker push 12df3fb9-wise-cepheus.registry.twcstorage.ru/media-service:latest
docker push 12df3fb9-wise-cepheus.registry.twcstorage.ru/integration-service:latest
docker push 12df3fb9-wise-cepheus.registry.twcstorage.ru/order-service:latest
docker push 12df3fb9-wise-cepheus.registry.twcstorage.ru/api-gateway:latest
```

**Ожидаемый вывод:**
```
The push refers to repository [12df3fb9-wise-cepheus.registry.twcstorage.ru/landing-service]
latest: digest: sha256:xxxxx size: 1234
```

---

## 🔐 Шаг 4: Создание секретов в Kubernetes

### 4.1 Создайте imagePullSecret

```bash
kubectl create secret docker-registry timeweb-registry \
  --docker-server=12df3fb9-wise-cepheus.registry.twcstorage.ru \
  --docker-username=12df3fb9-wise-cepheus \
  --docker-password=<ваш-токен> \
  --namespace=klassifikator
```

### 4.2 Проверьте .env файл

```bash
cd /Users/bagano/Downloads/Cursor/klassifikator

# Убедитесь, что .env существует и заполнен
cat .env

# Если нет, создайте из шаблона
cp env.example .env
nano .env
```

**Обязательные поля в .env:**
```env
DB_PASSWORD=strong_password_here
REDIS_PASSWORD=redis_password_here
MINIO_ROOT_PASSWORD=minio_password_here
GOOGLE_SHEETS_SPREADSHEET_ID=1KS2TOS5ZKxONDmUaVoiwb3tyu3Y1DlGQaME2KM4vItQ
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
TELEGRAM_CHAT_ID=your_chat_id
```

---

## 🚀 Шаг 5: Развертывание приложения

### 5.1 Автоматическое развертывание

```bash
cd /Users/bagano/Downloads/Cursor/klassifikator/k8s

# Запустите скрипт развертывания
chmod +x deploy.sh
./deploy.sh
```

### 5.2 Или ручное развертывание

```bash
cd /Users/bagano/Downloads/Cursor/klassifikator/k8s

# 1. Создайте namespace
kubectl create namespace klassifikator

# 2. Создайте секреты из .env
kubectl create secret generic klassifikator-secrets \
  --from-env-file=../.env \
  -n klassifikator

# 3. Создайте секрет для Google Credentials
kubectl create secret generic google-credentials \
  --from-file=google-credentials.json=../config/google-credentials.json \
  -n klassifikator

# 4. Примените манифесты
kubectl apply -k base/

# 5. Проверьте статус
kubectl get pods -n klassifikator -w
```

---

## 🔍 Шаг 6: Проверка развертывания

### 6.1 Проверка подов

```bash
# Статус всех подов
kubectl get pods -n klassifikator

# Ожидаемый вывод:
# NAME                                   READY   STATUS    RESTARTS   AGE
# postgres-xxx                           1/1     Running   0          5m
# redis-xxx                              1/1     Running   0          5m
# minio-xxx                              1/1     Running   0          5m
# landing-service-xxx                    1/1     Running   0          3m
# content-service-xxx                    1/1     Running   0          3m
# template-service-xxx                   1/1     Running   0          3m
# media-service-xxx                      1/1     Running   0          3m
# integration-service-xxx                1/1     Running   0          3m
# order-service-xxx                      1/1     Running   0          3m
# api-gateway-xxx                        1/1     Running   0          3m
```

### 6.2 Проверка логов (если есть ошибки)

```bash
# Логи конкретного сервиса
kubectl logs -f deployment/landing-service -n klassifikator

# Логи всех микросервисов
kubectl logs -f -l component=microservice -n klassifikator --prefix=true

# Описание пода (для диагностики)
kubectl describe pod <pod-name> -n klassifikator
```

### 6.3 Проверка Ingress

```bash
# Получите IP адрес Load Balancer
kubectl get ingress klassifikator-ingress -n klassifikator

# Ожидаемый вывод:
# NAME                     CLASS   HOSTS                   ADDRESS         PORTS     AGE
# klassifikator-ingress    nginx   volzhck.ru,*.volzhck.ru 89.223.127.140  80, 443   5m
```

✅ **У вас уже настроено!** IP: `89.223.127.140`

---

## 🌐 Шаг 7: Проверка DNS и работы сервисов

### 7.1 Проверка DNS

```bash
# Проверьте DNS записи
dig api.volzhck.ru +short
# Должно вернуть: 89.223.127.140

dig test.volzhck.ru +short
# Должно вернуть: 89.223.127.140
```

✅ **У вас уже настроено!**

### 7.2 Проверка HTTP доступа

```bash
# Проверка API Gateway
curl -I http://api.volzhck.ru/actuator/health

# Проверка тестового лендинга (нужно создать организацию)
curl -I http://test.volzhck.ru
```

### 7.3 Проверка HTTPS (после получения SSL)

```bash
# Проверка SSL сертификата
kubectl get certificate -n klassifikator

# Описание сертификата
kubectl describe certificate klassifikator-tls -n klassifikator
```

Cert-manager автоматически получит SSL сертификаты от Let's Encrypt в течение 5-10 минут.

---

## ✅ Шаг 8: Создание тестовых данных

### 8.1 Создайте организацию

```bash
curl -X POST http://api.volzhck.ru/api/v1/organizations \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Тестовая Компания",
    "phone": "+79001234567",
    "email": "test@example.com",
    "address": "г. Волжский, ул. Мира, 10"
  }'
```

### 8.2 Создайте лендинг

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

### 8.3 Проверьте лендинг

```bash
# Откройте в браузере
open http://test.volzhck.ru
```

---

## 🐛 Troubleshooting

### Проблема: Поды не запускаются

**Проверка:**
```bash
kubectl get pods -n klassifikator
kubectl describe pod <pod-name> -n klassifikator
kubectl logs <pod-name> -n klassifikator
```

**Частые причины:**
1. **ImagePullBackOff** - неправильный imagePullSecret
   ```bash
   kubectl delete secret timeweb-registry -n klassifikator
   kubectl create secret docker-registry timeweb-registry \
     --docker-server=12df3fb9-wise-cepheus.registry.twcstorage.ru \
     --docker-username=12df3fb9-wise-cepheus \
     --docker-password=<правильный-токен> \
     --namespace=klassifikator
   
   kubectl rollout restart deployment -n klassifikator
   ```

2. **CrashLoopBackOff** - ошибка в приложении
   ```bash
   kubectl logs <pod-name> -n klassifikator
   ```

3. **Pending** - недостаточно ресурсов
   ```bash
   kubectl describe pod <pod-name> -n klassifikator
   # Проверьте Events в конце вывода
   ```

### Проблема: Домены не работают

**Проверка Ingress:**
```bash
kubectl get ingress -n klassifikator
kubectl describe ingress klassifikator-ingress -n klassifikator
```

**Проверка Ingress Controller:**
```bash
kubectl get pods -n ingress-nginx
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller
```

**Проверка SSL сертификатов:**
```bash
kubectl get certificate -n klassifikator
kubectl describe certificate klassifikator-tls -n klassifikator
```

### Проблема: База данных не подключается

**Проверка PostgreSQL:**
```bash
kubectl get pod -l app=postgres -n klassifikator
kubectl logs -l app=postgres -n klassifikator

# Проверка соединения из микросервиса
kubectl exec -n klassifikator deployment/landing-service -- nc -zv postgres-service 5432
```

---

## 📊 Полезные команды

### Мониторинг

```bash
# Статус всех ресурсов
kubectl get all -n klassifikator

# Использование ресурсов
kubectl top pods -n klassifikator
kubectl top nodes

# Логи в реальном времени
kubectl logs -f <pod-name> -n klassifikator

# События
kubectl get events -n klassifikator --sort-by='.lastTimestamp'
```

### Управление

```bash
# Перезапуск сервиса
kubectl rollout restart deployment/landing-service -n klassifikator

# Масштабирование
kubectl scale deployment landing-service --replicas=2 -n klassifikator

# Обновление образа
kubectl set image deployment/landing-service \
  landing-service=12df3fb9-wise-cepheus.registry.twcstorage.ru/landing-service:v2 \
  -n klassifikator

# Откат обновления
kubectl rollout undo deployment/landing-service -n klassifikator
```

### Отладка

```bash
# Exec в под
kubectl exec -it <pod-name> -n klassifikator -- /bin/bash

# Port-forward для локального доступа
kubectl port-forward svc/api-gateway 8080:8080 -n klassifikator

# Копирование файлов
kubectl cp <pod-name>:/path/to/file ./local-file -n klassifikator
```

---

## 📞 Поддержка

### Документация
- [KUBERNETES_DEPLOYMENT.md](/docs/KUBERNETES_DEPLOYMENT.md) - Полная документация
- [TESTING_GUIDE.md](/docs/TESTING_GUIDE.md) - Руководство по тестированию
- [Timeweb Cloud Kubernetes](https://timeweb.cloud/docs/k8s)

### Контакты
- Техподдержка Timeweb Cloud: [https://timeweb.cloud/support](https://timeweb.cloud/support)
- Email: zq97483@timeweb.cloud

---

## ✅ Чеклист готовности

- [ ] kubectl подключен к кластеру
- [ ] Container Registry создан и настроен
- [ ] Все Docker образы собраны
- [ ] Все образы загружены в Registry
- [ ] imagePullSecret создан
- [ ] .env файл настроен
- [ ] google-credentials.json загружен
- [ ] Все поды в статусе Running
- [ ] Ingress показывает IP адрес
- [ ] DNS записи настроены
- [ ] SSL сертификаты получены
- [ ] API Gateway отвечает
- [ ] Тестовый лендинг создан и работает

---

**Версия**: 1.0  
**Дата последнего обновления**: 2025-11-17  
**Автор**: DevOps Team

