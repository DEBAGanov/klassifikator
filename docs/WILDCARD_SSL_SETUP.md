# 🔐 Автоматические SSL сертификаты для ВСЕХ поддоменов (Wildcard)

## 🎯 Цель: Полная автоматизация SSL

**Что вы получите:**
- ✅ Автоматическое продление сертификатов (за 30 дней до истечения)
- ✅ Автоматические сертификаты для ВСЕХ новых поддоменов
- ✅ Один wildcard сертификат `*.volzhck.ru` вместо отдельных
- ✅ Не нужно обновлять Ingress при добавлении нового лендинга!

---

## 📋 Вариант 1: Cloudflare DNS (РЕКОМЕНДУЕТСЯ)

Если ваш домен `volzhck.ru` использует Cloudflare DNS:

### Шаг 1: Получите Cloudflare API Token

1. Войдите в [Cloudflare Dashboard](https://dash.cloudflare.com/)
2. Перейдите: **My Profile → API Tokens**
3. Нажмите **Create Token**
4. Выберите шаблон: **Edit zone DNS**
5. Настройте разрешения:
   ```
   Zone / DNS / Edit
   Zone Resources: Include / Specific Zone / volzhck.ru
   ```
6. Создайте токен и **СОХРАНИТЕ ЕГО** (показывается только один раз!)

### Шаг 2: Создайте Secret с API Token

```bash
kubectl create secret generic cloudflare-api-token-secret \
  --from-literal=api-token=YOUR_CLOUDFLARE_API_TOKEN_HERE \
  -n cert-manager
```

### Шаг 3: Примените Wildcard конфигурацию

```bash
# Создайте ClusterIssuer с DNS-01
kubectl apply -f k8s/base/certificate-wildcard-dns01.yaml

# Проверьте статус
kubectl get clusterissuer letsencrypt-prod-dns

# Примените новый Ingress с wildcard
kubectl apply -f k8s/base/ingress-wildcard.yaml

# Дождитесь выпуска сертификата (3-5 минут)
kubectl get certificate -n klassifikator -w
```

### Шаг 4: Проверка

```bash
# Сертификат должен быть READY
kubectl get certificate wildcard-volzhck-ru -n klassifikator

# Проверка через curl
curl -I https://modernissimo.volzhck.ru
curl -I https://any-new-subdomain.volzhck.ru  # Любой новый поддомен!
```

---

## 📋 Вариант 2: Другие DNS провайдеры

cert-manager поддерживает множество DNS провайдеров:

### Timeweb DNS API (если есть)

```yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod-dns
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: your-email@example.com
    privateKeySecretRef:
      name: letsencrypt-prod-dns
    solvers:
    - dns01:
        webhook:
          groupName: acme.timeweb.cloud
          solverName: timeweb
          config:
            apiTokenSecretRef:
              name: timeweb-api-token
              key: token
```

### Route53 (AWS)

```yaml
solvers:
- dns01:
    route53:
      region: us-east-1
      accessKeyID: YOUR_ACCESS_KEY
      secretAccessKeySecretRef:
        name: aws-secret
        key: secret-access-key
```

### Google Cloud DNS

```yaml
solvers:
- dns01:
    cloudDNS:
      project: your-project-id
      serviceAccountSecretRef:
        name: clouddns-sa
        key: key.json
```

### Yandex Cloud DNS

```yaml
solvers:
- dns01:
    webhook:
      groupName: acme.yandex.cloud
      solverName: yandexcloud
      config:
        folderId: your-folder-id
        serviceAccountSecretRef:
          name: yandex-sa
          key: key.json
```

**Полный список:** https://cert-manager.io/docs/configuration/acme/dns01/

---

## 📋 Вариант 3: Текущая конфигурация (HTTP-01)

Если у вас нет доступа к DNS API, текущая конфигурация уже автоматизирована:

### ✅ Автоматическое продление

- cert-manager автоматически продлевает за 30 дней
- Не требует ваших действий
- Проверка: `kubectl get certificate -n klassifikator`

### 🆕 Добавление нового лендинга

**Автоматизация через скрипт:**

Создайте файл `scripts/add-new-subdomain.sh`:

```bash
#!/bin/bash

# Использование: ./add-new-subdomain.sh newshop

SUBDOMAIN=$1
DOMAIN="volzhck.ru"
FULL_DOMAIN="${SUBDOMAIN}.${DOMAIN}"

echo "🔧 Добавление SSL сертификата для ${FULL_DOMAIN}..."

# Добавляем TLS блок в Ingress
kubectl patch ingress klassifikator-ingress -n klassifikator --type=json -p="[
  {
    \"op\": \"add\",
    \"path\": \"/spec/tls/-\",
    \"value\": {
      \"hosts\": [\"${FULL_DOMAIN}\"],
      \"secretName\": \"${SUBDOMAIN}-${DOMAIN//./-}-tls\"
    }
  }
]"

# Добавляем rule в Ingress
kubectl patch ingress klassifikator-ingress -n klassifikator --type=json -p="[
  {
    \"op\": \"add\",
    \"path\": \"/spec/rules/-\",
    \"value\": {
      \"host\": \"${FULL_DOMAIN}\",
      \"http\": {
        \"paths\": [{
          \"path\": \"/\",
          \"pathType\": \"Prefix\",
          \"backend\": {
            \"service\": {
              \"name\": \"template-service\",
              \"port\": {
                \"number\": 8083
              }
            }
          }
        }]
      }
    }
  }
]"

echo "✅ Конфигурация обновлена!"
echo "⏳ Ожидание выпуска сертификата (1-2 минуты)..."

# Ждем готовности сертификата
kubectl wait --for=condition=ready certificate "${SUBDOMAIN}-${DOMAIN//./-}-tls" \
  -n klassifikator --timeout=300s

echo "🎉 SSL сертификат для ${FULL_DOMAIN} готов!"
echo "🌐 Проверьте: https://${FULL_DOMAIN}"
```

**Использование:**

```bash
chmod +x scripts/add-new-subdomain.sh
./scripts/add-new-subdomain.sh newshop
```

---

## 🔍 Мониторинг сертификатов

### Статус всех сертификатов

```bash
kubectl get certificate -n klassifikator

# С автообновлением
kubectl get certificate -n klassifikator -w
```

### Детали конкретного сертификата

```bash
kubectl describe certificate wildcard-volzhck-ru -n klassifikator

# Дата истечения
kubectl get secret wildcard-volzhck-ru-tls -n klassifikator -o jsonpath='{.data.tls\.crt}' | base64 -d | openssl x509 -noout -dates
```

### Логи cert-manager

```bash
# Общие логи
kubectl logs -n cert-manager -l app=cert-manager --tail=100

# Логи продления
kubectl logs -n cert-manager -l app=cert-manager | grep -i "renew"
```

### Webhook для уведомлений (опционально)

Настройте webhook для уведомлений об истечении сертификатов:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: cert-manager-webhook
  namespace: cert-manager
data:
  webhook-url: "https://your-webhook-url.com/cert-expiry"
```

---

## 📊 Сравнение подходов

| Параметр | HTTP-01 (текущий) | DNS-01 (wildcard) |
|----------|-------------------|-------------------|
| **Автопродление** | ✅ Да | ✅ Да |
| **Новые поддомены** | ⚠️ Нужен скрипт | ✅ Автоматически |
| **Настройка** | ✅ Простая | ⚠️ Нужен DNS API |
| **Wildcard** | ❌ Нет | ✅ Да |
| **Зависимость** | HTTP доступ | DNS API |
| **Скорость** | ⚡ 1-2 мин | ⏱️ 3-5 мин |

---

## ✅ Рекомендация

### Если у вас Cloudflare или другой DNS с API:
👉 **Используйте Вариант 1 (DNS-01 wildcard)** - полная автоматизация!

### Если нет доступа к DNS API:
👉 **Оставайтесь на HTTP-01** + используйте скрипт `add-new-subdomain.sh`

---

## 🚀 Итоговая автоматизация

### С wildcard сертификатом (DNS-01):

```bash
# 1. Настройте один раз
kubectl apply -f k8s/base/certificate-wildcard-dns01.yaml
kubectl apply -f k8s/base/ingress-wildcard.yaml

# 2. Добавляете новый лендинг в Google Sheets
# Например: pizza.volzhck.ru

# 3. Создаете A-запись в DNS
# pizza.volzhck.ru → 89.223.127.140

# 4. ВСЁ! Сертификат работает автоматически! 🎉
# https://pizza.volzhck.ru - сразу с SSL!
```

**Никаких обновлений Ingress не требуется!**

---

## 🔧 Устранение проблем

### Сертификат не выпускается

```bash
# Проверьте Order
kubectl get order -n klassifikator

# Проверьте Challenge
kubectl get challenges -n klassifikator

# Логи
kubectl describe order <order-name> -n klassifikator
kubectl describe challenge <challenge-name> -n klassifikator
```

### DNS-01 не работает

```bash
# Проверьте API Token
kubectl get secret cloudflare-api-token-secret -n cert-manager

# Проверьте DNS записи
nslookup _acme-challenge.volzhck.ru

# Логи cert-manager
kubectl logs -n cert-manager -l app=cert-manager --tail=200 | grep -i dns
```

---

**Документация:**
- cert-manager: https://cert-manager.io/docs/
- DNS-01 challenges: https://cert-manager.io/docs/configuration/acme/dns01/
- Let's Encrypt: https://letsencrypt.org/docs/

