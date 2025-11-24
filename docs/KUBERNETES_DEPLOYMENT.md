# Развертывание Klassifikator в Timeweb Cloud Kubernetes

**Дата создания**: 2025-11-10  
**Версия**: 1.0

---

## 📋 Содержание

1. [Введение](#введение)
2. [Требования](#требования)
3. [Подготовка](#подготовка)
4. [Создание кластера в Timeweb Cloud](#создание-кластера-в-timeweb-cloud)
5. [Настройка kubectl](#настройка-kubectl)
6. [Развертывание приложения](#развертывание-приложения)
7. [Настройка DNS и SSL](#настройка-dns-и-ssl)
8. [Мониторинг и управление](#мониторинг-и-управление)
9. [Масштабирование](#масштабирование)
10. [Обновление приложения](#обновление-приложения)
11. [Резервное копирование](#резервное-копирование)
12. [Troubleshooting](#troubleshooting)
13. [Стоимость](#стоимость)

---

## 🎯 Введение

Данное руководство описывает процесс развертывания системы **Klassifikator** в **Timeweb Cloud Kubernetes** с использованием минимальных ресурсов для оптимизации затрат.

### Преимущества Kubernetes

- ✅ **Автомасштабирование** - автоматическое управление ресурсами
- ✅ **Отказоустойчивость** - автоматический перезапуск упавших подов
- ✅ **Балансировка нагрузки** - встроенная
- ✅ **Rolling updates** - обновления без простоя
- ✅ **Управление секретами** - безопасное хранение credentials

---

## 🎯 Требования

### Локальное окружение

- **kubectl** 1.28+ ([Установка](https://kubernetes.io/docs/tasks/tools/))
- **kustomize** (опционально, встроен в kubectl)
- **Docker** (для сборки образов)
- **Git**

### Timeweb Cloud

- Аккаунт в [Timeweb Cloud](https://timeweb.cloud/)
- Баланс для оплаты кластера Kubernetes
- Домен (volzhck.ru)

### Минимальные ресурсы кластера

**Рекомендуемая конфигурация:**
- **Worker nodes**: 2 ноды
- **CPU на ноду**: 2 vCPU
- **RAM на ноду**: 4 GB
- **Диск**: 50 GB SSD
- **Итого**: 4 vCPU, 8 GB RAM

**Стоимость**: ~2000-3000 руб/месяц

---

## ⚙️ Подготовка

### 1. Клонирование репозитория

```bash
git clone https://github.com/your-username/klassifikator.git
cd klassifikator
```

### 2. Настройка переменных окружения

Создайте `.env` файл:

```bash
cp env.example .env
nano .env
```

Заполните обязательные поля:

```env
# Database
DB_PASSWORD=your_secure_password_here

# Redis
REDIS_PASSWORD=your_redis_password_here

# MinIO
MINIO_ROOT_PASSWORD=your_minio_password_here

# Google Sheets
GOOGLE_SHEETS_SPREADSHEET_ID=1KS2TOS5ZKxONDmUaVoiwb3tyu3Y1DlGQaME2KM4vItQ

# Telegram
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
TELEGRAM_CHAT_ID=your_telegram_chat_id
```

### 3. Настройка Google Credentials

Разместите файл `google-credentials.json` в папке `config/`:

```bash
mkdir -p config
# Скопируйте ваш google-credentials.json в config/
```

### 4. Сборка Docker образов

```bash
# Сборка всех образов
docker-compose -f docker-compose.prod.yml build

# Или сборка отдельных сервисов
docker build -t klassifikator/landing-service:latest -f landing-service/Dockerfile .
docker build -t klassifikator/content-service:latest -f content-service/Dockerfile .
# ... и так далее для всех сервисов
```

### 5. Push образов в Docker Registry

**Вариант 1: Docker Hub**

```bash
# Логин в Docker Hub
docker login

# Тегирование образов
docker tag klassifikator/landing-service:latest your-username/klassifikator-landing:latest
docker tag klassifikator/content-service:latest your-username/klassifikator-content:latest
# ... для всех сервисов

# Push образов
docker push your-username/klassifikator-landing:latest
docker push your-username/klassifikator-content:latest
# ... для всех сервисов
```

**Вариант 2: Timeweb Cloud Container Registry**

```bash
# Создайте Container Registry в панели Timeweb Cloud
# Получите credentials для доступа

# Логин
docker login registry.timeweb.cloud

# Тегирование и push
docker tag klassifikator/landing-service:latest registry.timeweb.cloud/your-project/landing-service:latest
docker push registry.timeweb.cloud/your-project/landing-service:latest
```

**Обновите манифесты** с правильными именами образов в `k8s/base/*-deployment.yaml`

---

## 🚀 Создание кластера в Timeweb Cloud

### Шаг 1: Вход в панель управления

1. Откройте [https://timeweb.cloud/my/kubernetes](https://timeweb.cloud/my/kubernetes)
2. Нажмите **"Создать кластер"**

### Шаг 2: Выбор конфигурации

**Базовые настройки:**
- **Имя кластера**: klassifikator-prod
- **Версия Kubernetes**: 1.28+ (последняя stable)
- **Регион**: Москва (или ближайший к вам)

**Конфигурация worker nodes:**
- **Количество нод**: 2
- **Тип**: Standard
- **CPU**: 2 vCPU
- **RAM**: 4 GB
- **Диск**: 50 GB SSD

**Сеть:**
- **Сетевой плагин (CNI)**: Calico (рекомендуется)
- **Балансировщик нагрузки**: Включить

### Шаг 3: Дополнительные настройки

**Аддоны (включить):**
- ✅ **Nginx Ingress Controller** - для маршрутизации трафика
- ✅ **cert-manager** - для автоматических SSL сертификатов
- ✅ **Metrics Server** - для мониторинга ресурсов
- ⬜ CSI S3 - опционально
- ⬜ Velero - опционально (для бэкапов)

### Шаг 4: Создание

Нажмите **"Создать кластер"**

⏱️ Время создания: ~10-15 минут

---

## 🔧 Настройка kubectl

### Шаг 1: Скачивание kubeconfig

1. В панели Timeweb Cloud откройте ваш кластер
2. Перейдите на вкладку **"Подключение"**
3. Скачайте файл `kubeconfig`

### Шаг 2: Настройка kubectl

```bash
# Сохраните kubeconfig
mkdir -p ~/.kube
cp ~/Downloads/kubeconfig ~/.kube/config-klassifikator

# Установите переменную окружения
export KUBECONFIG=~/.kube/config-klassifikator

# Или объедините с существующим config
KUBECONFIG=~/.kube/config:~/.kube/config-klassifikator kubectl config view --flatten > ~/.kube/config.new
mv ~/.kube/config.new ~/.kube/config
```

### Шаг 3: Проверка подключения

```bash
# Проверка подключения
kubectl cluster-info

# Просмотр нод
kubectl get nodes

# Вывод должен показать 2 ноды в статусе Ready
```

---

## 🚢 Развертывание приложения

### Автоматическое развертывание

```bash
cd k8s
./deploy.sh
```

Скрипт автоматически:
1. ✅ Проверит наличие kubectl
2. ✅ Создаст namespace `klassifikator`
3. ✅ Создаст секреты из `.env` и `google-credentials.json`
4. ✅ Применит все Kubernetes манифесты
5. ✅ Дождется готовности всех подов
6. ✅ Выведет статус и URL для доступа

### Ручное развертывание

Если хотите выполнить шаги вручную:

```bash
cd k8s

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

### Проверка развертывания

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

# Логи конкретного сервиса
kubectl logs -f deployment/landing-service -n klassifikator

# Health check
kubectl exec -n klassifikator deployment/landing-service -- \
  curl -s http://localhost:8081/actuator/health
```

---

## 🌐 Настройка DNS и SSL

### Шаг 1: Получение IP адреса Load Balancer

```bash
kubectl get ingress klassifikator-ingress -n klassifikator

# Вывод:
# NAME                     CLASS   HOSTS                   ADDRESS         PORTS     AGE
# klassifikator-ingress    nginx   volzhck.ru,*.volzhck.ru 123.45.67.89   80, 443   5m
```

Запишите IP адрес (например, `123.45.67.89`)

### Шаг 2: Настройка DNS в Timeweb Cloud

1. Откройте [https://timeweb.cloud/my/domains](https://timeweb.cloud/my/domains)
2. Выберите домен `volzhck.ru`
3. Добавьте DNS записи:

**A-запись для основного домена:**
- Тип: `A`
- Имя: `@`
- Значение: `123.45.67.89` (IP Load Balancer)
- TTL: `300`

**A-запись для API:**
- Тип: `A`
- Имя: `api`
- Значение: `123.45.67.89`
- TTL: `300`

**A-запись для wildcard поддоменов:**
- Тип: `A`
- Имя: `*`
- Значение: `123.45.67.89`
- TTL: `300`

### Шаг 3: Проверка DNS

```bash
# Проверка основного домена
dig volzhck.ru +short
nslookup volzhck.ru

# Проверка API
dig api.volzhck.ru +short

# Проверка wildcard
dig test.volzhck.ru +short
```

⏱️ Время распространения DNS: 5-30 минут

### Шаг 4: Автоматическое получение SSL сертификатов

Cert-manager автоматически получит SSL сертификаты от Let's Encrypt.

Проверка статуса:

```bash
# Проверка ClusterIssuer
kubectl get clusterissuer

# Проверка сертификатов
kubectl get certificate -n klassifikator

# Детали сертификата
kubectl describe certificate klassifikator-tls -n klassifikator
```

Сертификаты будут готовы через 2-5 минут после настройки DNS.

### Шаг 5: Проверка HTTPS

```bash
# Проверка основного домена
curl -I https://volzhck.ru

# Проверка API
curl -I https://api.volzhck.ru

# Проверка лендинга
curl -I https://test.volzhck.ru
```

---

## 📊 Мониторинг и управление

### Просмотр статуса

```bash
# Использование утилиты
cd k8s
./status.sh

# Или вручную
kubectl get all -n klassifikator
kubectl top pods -n klassifikator
kubectl top nodes
```

### Просмотр логов

```bash
# Использование утилиты
cd k8s
./logs.sh landing-service

# Или вручную
kubectl logs -f deployment/landing-service -n klassifikator

# Логи всех микросервисов
kubectl logs -f -l component=microservice -n klassifikator --prefix=true
```

### Health Checks

```bash
# Проверка всех health endpoints
for service in landing content template media integration order; do
  echo "Checking ${service}-service..."
  kubectl exec -n klassifikator deployment/${service}-service -- \
    curl -s http://localhost:808X/actuator/health | jq .
done
```

### Метрики

```bash
# Использование ресурсов подами
kubectl top pods -n klassifikator --sort-by=memory

# Использование ресурсов нодами
kubectl top nodes
```

### Dashboard (опционально)

Установка Kubernetes Dashboard:

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml

# Создание пользователя для доступа
kubectl create serviceaccount dashboard-admin -n kubernetes-dashboard
kubectl create clusterrolebinding dashboard-admin \
  --clusterrole=cluster-admin \
  --serviceaccount=kubernetes-dashboard:dashboard-admin

# Получение токена
kubectl -n kubernetes-dashboard create token dashboard-admin

# Port-forward
kubectl port-forward -n kubernetes-dashboard service/kubernetes-dashboard 8443:443

# Откройте в браузере: https://localhost:8443
```

---

## 📈 Масштабирование

### Горизонтальное масштабирование (HPA)

Создайте HorizontalPodAutoscaler:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: landing-service-hpa
  namespace: klassifikator
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: landing-service
  minReplicas: 1
  maxReplicas: 5
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

Применение:

```bash
kubectl apply -f hpa.yaml
kubectl get hpa -n klassifikator -w
```

### Ручное масштабирование

```bash
# Увеличение количества реплик
kubectl scale deployment landing-service --replicas=3 -n klassifikator

# Проверка
kubectl get pods -n klassifikator -l app=landing-service
```

### Вертикальное масштабирование

Увеличение ресурсов для конкретного сервиса:

```bash
# Редактирование deployment
kubectl edit deployment landing-service -n klassifikator

# Измените resources:
#   requests:
#     cpu: 200m
#     memory: 512Mi
#   limits:
#     cpu: 1000m
#     memory: 1Gi
```

---

## 🔄 Обновление приложения

### Rolling Update

```bash
# Обновление образа
kubectl set image deployment/landing-service \
  landing-service=klassifikator/landing-service:v2.0 \
  -n klassifikator

# Проверка статуса обновления
kubectl rollout status deployment/landing-service -n klassifikator

# История обновлений
kubectl rollout history deployment/landing-service -n klassifikator
```

### Откат обновления

```bash
# Откат к предыдущей версии
kubectl rollout undo deployment/landing-service -n klassifikator

# Откат к конкретной ревизии
kubectl rollout undo deployment/landing-service --to-revision=2 -n klassifikator
```

### Blue-Green Deployment

```bash
# Создание новой версии (green)
kubectl apply -f landing-service-v2-deployment.yaml

# Переключение трафика (обновление Service selector)
kubectl patch service landing-service -n klassifikator \
  -p '{"spec":{"selector":{"version":"v2"}}}'

# Удаление старой версии (blue)
kubectl delete deployment landing-service-v1 -n klassifikator
```

---

## 💾 Резервное копирование

### Бэкап PostgreSQL

```bash
# Создание бэкапа
kubectl exec -n klassifikator deployment/postgres -- \
  pg_dump -U klassifikator klassifikator | gzip > backup-$(date +%Y%m%d).sql.gz

# Восстановление
gunzip < backup-20251110.sql.gz | \
  kubectl exec -i -n klassifikator deployment/postgres -- \
  psql -U klassifikator klassifikator
```

### Бэкап PersistentVolumes (с Velero)

Установка Velero:

```bash
# Установка Velero CLI
brew install velero

# Установка Velero в кластер
velero install \
  --provider aws \
  --plugins velero/velero-plugin-for-aws:v1.8.0 \
  --bucket klassifikator-backups \
  --secret-file ./credentials-velero \
  --backup-location-config region=ru-1,s3ForcePathStyle="true",s3Url=https://s3.timeweb.cloud

# Создание бэкапа
velero backup create klassifikator-backup --include-namespaces klassifikator

# Восстановление
velero restore create --from-backup klassifikator-backup
```

### Бэкап манифестов

```bash
# Экспорт всех ресурсов
kubectl get all,configmap,secret,pvc,ingress -n klassifikator -o yaml > backup-manifests.yaml

# Или использование kustomize
kubectl kustomize k8s/base/ > backup-kustomize.yaml
```

---

## 🔧 Troubleshooting

### Проблема: Поды не запускаются

**Диагностика:**

```bash
# Статус подов
kubectl get pods -n klassifikator

# Детали пода
kubectl describe pod <pod-name> -n klassifikator

# Логи
kubectl logs <pod-name> -n klassifikator

# События
kubectl get events -n klassifikator --sort-by='.lastTimestamp'
```

**Частые причины:**
- Недостаточно ресурсов на нодах
- Ошибки в образах
- Проблемы с секретами
- Недоступность PersistentVolumes

### Проблема: Ошибки подключения к базе данных

```bash
# Проверка статуса PostgreSQL
kubectl get pod -n klassifikator -l app=postgres

# Проверка логов
kubectl logs -n klassifikator -l app=postgres

# Проверка соединения
kubectl exec -n klassifikator deployment/landing-service -- \
  nc -zv postgres-service 5432

# Проверка переменных окружения
kubectl exec -n klassifikator deployment/landing-service -- env | grep DB
```

### Проблема: Ingress не работает

```bash
# Проверка Ingress
kubectl get ingress -n klassifikator
kubectl describe ingress klassifikator-ingress -n klassifikator

# Проверка Ingress Controller
kubectl get pods -n ingress-nginx
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller

# Проверка сертификатов
kubectl get certificate -n klassifikator
kubectl describe certificate klassifikator-tls -n klassifikator
```

### Проблема: Высокое использование ресурсов

```bash
# Проверка использования
kubectl top pods -n klassifikator --sort-by=memory
kubectl top nodes

# Увеличение лимитов
kubectl edit deployment <service-name> -n klassifikator

# Или масштабирование нод в панели Timeweb Cloud
```

### Проблема: Медленная работа

**Оптимизация:**

1. **Включите кэширование в Redis**
2. **Настройте connection pooling для БД**
3. **Увеличьте ресурсы для микросервисов**
4. **Добавьте HPA для автомасштабирования**
5. **Оптимизируйте запросы к БД**

---

## 💰 Стоимость

### Расчет стоимости Timeweb Cloud Kubernetes

**Минимальная конфигурация:**

| Компонент | Спецификация | Цена/месяц |
|-----------|-------------|------------|
| Worker Node 1 | 2 vCPU, 4 GB RAM, 50 GB SSD | ~1000 руб |
| Worker Node 2 | 2 vCPU, 4 GB RAM, 50 GB SSD | ~1000 руб |
| Load Balancer | Включен | ~300 руб |
| Persistent Storage | 32 GB (10+2+20) | ~100 руб |
| **Итого** | | **~2400 руб/месяц** |

**Дополнительные расходы:**
- Трафик: ~50 руб/100 GB
- Container Registry: ~100 руб/месяц (опционально)
- Бэкапы: ~50 руб/месяц (опционально)

**Итого с запасом: ~2600-3000 руб/месяц**

### Оптимизация затрат

1. **Используйте Spot Instances** (если доступно) - экономия до 70%
2. **Настройте автомасштабирование нод** - платите только за используемые ресурсы
3. **Используйте HDD вместо SSD** для некритичных данных
4. **Настройте lifecycle policies** для удаления старых бэкапов
5. **Оптимизируйте Docker образы** - уменьшите размер

---

## 📚 Полезные команды

### Общие

```bash
# Переключение контекста
kubectl config use-context klassifikator-prod

# Текущий контекст
kubectl config current-context

# Список всех ресурсов
kubectl get all -n klassifikator

# Удаление всех ресурсов
kubectl delete namespace klassifikator
```

### Debugging

```bash
# Запуск временного пода для отладки
kubectl run -it --rm debug --image=alpine --restart=Never -n klassifikator -- sh

# Exec в работающий под
kubectl exec -it <pod-name> -n klassifikator -- /bin/bash

# Port-forward для локального доступа
kubectl port-forward -n klassifikator svc/api-gateway 8080:8080

# Копирование файлов
kubectl cp <pod-name>:/path/to/file ./local-file -n klassifikator
```

### Мониторинг

```bash
# Watch за изменениями
kubectl get pods -n klassifikator -w

# События в реальном времени
kubectl get events -n klassifikator -w

# Логи нескольких подов
kubectl logs -f -l app=landing-service -n klassifikator --all-containers=true
```

---

## 🎓 Дополнительные ресурсы

### Документация

- [Timeweb Cloud Kubernetes](https://timeweb.cloud/docs/k8s)
- [Kubernetes Official Docs](https://kubernetes.io/docs/)
- [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)

### Обучение

- [Kubernetes Basics](https://kubernetes.io/docs/tutorials/kubernetes-basics/)
- [Timeweb Cloud Academy](https://timeweb.cloud/academy)

---

## ✅ Чек-лист для production

- [ ] Кластер создан с 2+ нодами
- [ ] kubectl настроен и подключен
- [ ] Все Docker образы собраны и загружены в registry
- [ ] `.env` файл настроен с безопасными паролями
- [ ] Google Credentials размещены
- [ ] Все поды в статусе Running
- [ ] Health checks проходят успешно
- [ ] DNS записи настроены
- [ ] SSL сертификаты получены
- [ ] Ingress работает корректно
- [ ] Тестовый лендинг создан и доступен
- [ ] Мониторинг настроен
- [ ] Бэкапы настроены
- [ ] Документация актуализирована

---

## 📞 Поддержка

- **Техподдержка Timeweb Cloud**: [https://timeweb.cloud/support](https://timeweb.cloud/support)
- **Документация проекта**: `/docs/`
- **Issues**: GitHub Issues

---

**Версия**: 1.0  
**Дата последнего обновления**: 2025-11-10  
**Автор**: DevOps Team


