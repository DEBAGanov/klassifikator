# 🚀 Быстрый старт: Развертывание на Timeweb Cloud Kubernetes

Это краткое руководство для быстрого развертывания приложения Klassifikator на Timeweb Cloud Kubernetes.

## 📝 Подготовка

### 1. Установите инструменты
- ✅ Docker Desktop
- ✅ kubectl

### 2. Настройте kubectl для Timeweb Cloud

```bash
# Скачайте kubeconfig из панели Timeweb Cloud Kubernetes
# Затем:
cp ~/Downloads/kubeconfig ~/.kube/config

# Проверьте подключение
kubectl cluster-info
kubectl get nodes
```

## 🐳 Шаг 1: Сборка и загрузка образов

```bash
# Установите токен registry (получите из панели Timeweb Cloud)
export TIMEWEB_REGISTRY_TOKEN="ваш-токен"

# Запустите сборку и загрузку всех образов
./scripts/build-and-push-images.sh
```

Скрипт автоматически:
- Соберет Gradle проект
- Создаст Docker образы для всех 7 микросервисов
- Загрузит их в Timeweb Cloud Container Registry

⏱️ Время выполнения: ~15-20 минут

## 🔐 Шаг 2: Подготовка секретов

### 2.1 Проверьте .env файл

```bash
# Убедитесь что .env существует и заполнен
cat .env

# Если нет, создайте из шаблона
cp env.example .env
nano .env
```

### 2.2 Проверьте google-credentials.json

```bash
# Убедитесь что файл существует
ls -l config/google-credentials.json
```

### 2.3 Создайте imagePullSecret

```bash
kubectl create secret docker-registry timeweb-registry \
  --docker-server=12df3fb9-wise-cepheus.registry.twcstorage.ru \
  --docker-username=12df3fb9-wise-cepheus \
  --docker-password="ваш-токен" \
  --namespace=klassifikator
```

## 🚀 Шаг 3: Развертывание

```bash
cd k8s
./deploy.sh
```

Скрипт автоматически:
- Создаст namespace `klassifikator`
- Создаст секреты из .env и google-credentials.json
- Применит все Kubernetes манифесты
- Дождется готовности всех подов

⏱️ Время развертывания: ~5-10 минут

## ✅ Шаг 4: Проверка

### Быстрая проверка

```bash
# Используйте наш скрипт проверки
./scripts/check-k8s-status.sh
```

### Ручная проверка

```bash
# Статус подов
kubectl get pods -n klassifikator

# Статус Ingress
kubectl get ingress -n klassifikator

# Health check
kubectl get pods -n klassifikator -l component=microservice
```

**Ожидаемый результат:**
- Все поды в статусе `Running`
- Ingress показывает IP `89.223.127.140`
- DNS записи настроены: `api.volzhck.ru` и `*.volzhck.ru`

## 🌐 Шаг 5: Тестирование

### Проверьте API

```bash
curl http://api.volzhck.ru/actuator/health
```

### Создайте тестовый лендинг

```bash
# 1. Создайте организацию
curl -X POST http://api.volzhck.ru/api/v1/organizations \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Тестовая Компания",
    "phone": "+79001234567",
    "email": "test@example.com"
  }'

# 2. Создайте лендинг
curl -X POST http://api.volzhck.ru/api/v1/landings \
  -H "Content-Type: application/json" \
  -d '{
    "organizationId": 1,
    "domain": "test.volzhck.ru",
    "status": "ACTIVE"
  }'

# 3. Откройте в браузере
open http://test.volzhck.ru
```

## 🐛 Проблемы?

### ImagePullBackOff

```bash
# Проверьте secret
kubectl get secret timeweb-registry -n klassifikator

# Если нужно, пересоздайте с правильным токеном
kubectl delete secret timeweb-registry -n klassifikator
kubectl create secret docker-registry timeweb-registry \
  --docker-server=12df3fb9-wise-cepheus.registry.twcstorage.ru \
  --docker-username=12df3fb9-wise-cepheus \
  --docker-password="правильный-токен" \
  --namespace=klassifikator

# Перезапустите deployments
kubectl rollout restart deployment -n klassifikator
```

### CrashLoopBackOff

```bash
# Проверьте логи
kubectl logs <pod-name> -n klassifikator

# Проверьте секреты
kubectl get secret klassifikator-secrets -n klassifikator
kubectl get secret google-credentials -n klassifikator
```

### SSL сертификаты не получаются

```bash
# Проверьте cert-manager
kubectl get pods -n cert-manager

# Проверьте статус сертификата
kubectl describe certificate klassifikator-tls -n klassifikator

# Обычно сертификаты получаются автоматически за 5-10 минут
```

## 📊 Полезные команды

```bash
# Полная проверка статуса
./scripts/check-k8s-status.sh

# Мониторинг подов в реальном времени
watch kubectl get pods -n klassifikator

# Логи сервиса
kubectl logs -f deployment/landing-service -n klassifikator

# Перезапуск сервиса
kubectl rollout restart deployment/landing-service -n klassifikator

# Удаление всего развертывания
cd k8s && ./undeploy.sh
```

## 📚 Документация

- 📖 [Полная документация](/docs/KUBERNETES_DEPLOYMENT.md)
- 🚀 [Подробное руководство](/docs/TIMEWEB_K8S_QUICKSTART.md)
- 🧪 [Руководство по тестированию](/docs/TESTING_GUIDE.md)
- 📊 [Статус системы](/docs/SYSTEM_STATUS.md)

## 🆘 Поддержка

**Проблемы с развертыванием?**

1. Запустите полную диагностику: `./scripts/check-k8s-status.sh`
2. Проверьте логи проблемных подов: `kubectl logs <pod-name> -n klassifikator`
3. Проверьте документацию в `/docs/`
4. Обратитесь в поддержку Timeweb Cloud: https://timeweb.cloud/support

---

**✨ Готово!** Ваше приложение развернуто и работает на `https://api.volzhck.ru`

**Следующие шаги:**
1. Настройте автоматическую синхронизацию с Google Sheets
2. Создайте лендинги для ваших организаций
3. Настройте мониторинг и алерты

**Полезные ресурсы:**
- Панель управления: https://timeweb.cloud/my/kubernetes
- Container Registry: https://timeweb.cloud/my/container-registry
- Документация Timeweb: https://timeweb.cloud/docs/k8s

