# Kubernetes Deployment для Klassifikator

Эта папка содержит все необходимые манифесты и скрипты для развертывания Klassifikator в Kubernetes.

## 📁 Структура

```
k8s/
├── base/                           # Базовые Kubernetes манифесты
│   ├── namespace.yaml             # Namespace klassifikator
│   ├── configmap.yaml             # Конфигурация приложения
│   ├── secrets.yaml               # Секреты (шаблон)
│   ├── postgres-pvc.yaml          # PVC для PostgreSQL
│   ├── redis-pvc.yaml             # PVC для Redis
│   ├── minio-pvc.yaml             # PVC для MinIO
│   ├── postgres-deployment.yaml   # PostgreSQL Deployment + Service
│   ├── redis-deployment.yaml      # Redis Deployment + Service
│   ├── minio-deployment.yaml      # MinIO Deployment + Service
│   ├── *-service-deployment.yaml  # Микросервисы Deployments + Services
│   ├── ingress.yaml               # Ingress для маршрутизации
│   └── kustomization.yaml         # Kustomize конфигурация
├── overlays/                       # Overlays для разных окружений
│   ├── dev/                       # Development конфигурация
│   └── prod/                      # Production конфигурация
├── helm/                           # Helm Chart (будущее)
├── deploy.sh                       # Скрипт автоматического развертывания
├── undeploy.sh                     # Скрипт удаления
├── status.sh                       # Проверка статуса
├── logs.sh                         # Просмотр логов
└── README.md                       # Этот файл
```

## 🚀 Быстрый старт

### Предварительные требования

1. **kubectl** установлен и настроен
2. **Кластер Kubernetes** создан в Timeweb Cloud
3. **Docker образы** собраны и загружены в registry
4. **`.env`** файл настроен в корне проекта
5. **`google-credentials.json`** размещен в `config/`

### Автоматическое развертывание

```bash
./deploy.sh
```

Скрипт автоматически:
- ✅ Создаст namespace
- ✅ Создаст секреты из `.env` и `google-credentials.json`
- ✅ Применит все манифесты
- ✅ Дождется готовности подов
- ✅ Выведет статус и URL

### Ручное развертывание

```bash
# 1. Создание namespace
kubectl create namespace klassifikator

# 2. Создание секретов
kubectl create secret generic klassifikator-secrets \
  --from-env-file=../.env \
  -n klassifikator

kubectl create secret generic google-credentials \
  --from-file=google-credentials.json=../config/google-credentials.json \
  -n klassifikator

# 3. Применение манифестов
kubectl apply -k base/

# 4. Проверка статуса
kubectl get pods -n klassifikator -w
```

## 📊 Управление

### Проверка статуса

```bash
./status.sh
```

Или вручную:

```bash
kubectl get all -n klassifikator
kubectl get pods -n klassifikator
kubectl get services -n klassifikator
kubectl get ingress -n klassifikator
```

### Просмотр логов

```bash
# Логи конкретного сервиса
./logs.sh landing-service

# Или вручную
kubectl logs -f deployment/landing-service -n klassifikator

# Логи всех микросервисов
kubectl logs -f -l component=microservice -n klassifikator --prefix=true
```

### Удаление

```bash
./undeploy.sh
```

## 🔧 Конфигурация

### Обновление секретов

```bash
# Удаление старых секретов
kubectl delete secret klassifikator-secrets -n klassifikator

# Создание новых
kubectl create secret generic klassifikator-secrets \
  --from-env-file=../.env \
  -n klassifikator

# Перезапуск подов для применения изменений
kubectl rollout restart deployment -n klassifikator
```

### Изменение ресурсов

Отредактируйте файлы `*-deployment.yaml` в папке `base/`:

```yaml
resources:
  requests:
    cpu: 100m      # Минимальные ресурсы
    memory: 256Mi
  limits:
    cpu: 500m      # Максимальные ресурсы
    memory: 512Mi
```

Примените изменения:

```bash
kubectl apply -k base/
```

### Масштабирование

```bash
# Увеличение реплик
kubectl scale deployment landing-service --replicas=3 -n klassifikator

# Или отредактируйте deployment
kubectl edit deployment landing-service -n klassifikator
```

## 🌐 Настройка DNS

После развертывания получите IP Load Balancer:

```bash
kubectl get ingress klassifikator-ingress -n klassifikator
```

Настройте DNS записи в Timeweb Cloud:

- `A` запись: `@` → `<LOAD_BALANCER_IP>`
- `A` запись: `api` → `<LOAD_BALANCER_IP>`
- `A` запись: `*` → `<LOAD_BALANCER_IP>`

## 🔒 SSL сертификаты

SSL сертификаты выдаются автоматически через cert-manager.

Проверка:

```bash
kubectl get certificate -n klassifikator
kubectl describe certificate klassifikator-tls -n klassifikator
```

## 📈 Мониторинг

### Использование ресурсов

```bash
# Поды
kubectl top pods -n klassifikator

# Ноды
kubectl top nodes
```

### Health Checks

```bash
# Проверка всех сервисов
for service in landing content template media integration order; do
  kubectl exec -n klassifikator deployment/${service}-service -- \
    curl -s http://localhost:808X/actuator/health
done
```

## 🔄 Обновление

### Rolling Update

```bash
# Обновление образа
kubectl set image deployment/landing-service \
  landing-service=klassifikator/landing-service:v2.0 \
  -n klassifikator

# Проверка статуса
kubectl rollout status deployment/landing-service -n klassifikator
```

### Откат

```bash
# Откат к предыдущей версии
kubectl rollout undo deployment/landing-service -n klassifikator

# История
kubectl rollout history deployment/landing-service -n klassifikator
```

## 💾 Резервное копирование

### База данных

```bash
# Создание бэкапа
kubectl exec -n klassifikator deployment/postgres -- \
  pg_dump -U klassifikator klassifikator | gzip > backup-$(date +%Y%m%d).sql.gz

# Восстановление
gunzip < backup-20251110.sql.gz | \
  kubectl exec -i -n klassifikator deployment/postgres -- \
  psql -U klassifikator klassifikator
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

### Проблемы с подключением

```bash
# Проверка сервисов
kubectl get svc -n klassifikator

# Проверка endpoints
kubectl get endpoints -n klassifikator

# Тест подключения
kubectl exec -n klassifikator deployment/landing-service -- \
  nc -zv postgres-service 5432
```

### Проблемы с Ingress

```bash
# Статус Ingress
kubectl describe ingress klassifikator-ingress -n klassifikator

# Логи Ingress Controller
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller
```

## 📚 Дополнительная документация

- [Полное руководство по развертыванию](../docs/KUBERNETES_DEPLOYMENT.md)
- [Документация проекта](../docs/Project.md)
- [Timeweb Cloud Kubernetes](https://timeweb.cloud/docs/k8s)

## 💰 Стоимость

**Минимальная конфигурация:**
- 2 worker nodes (2 vCPU, 4 GB RAM каждая)
- Load Balancer
- 32 GB Persistent Storage

**Итого: ~2400-3000 руб/месяц**

## 📞 Поддержка

- Техподдержка Timeweb Cloud: https://timeweb.cloud/support
- Issues: GitHub Issues

---

**Версия**: 1.0  
**Дата**: 2025-11-10

