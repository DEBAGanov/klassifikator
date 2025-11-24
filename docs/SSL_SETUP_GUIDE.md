# 🔐 Руководство по настройке SSL сертификатов

**Дата:** 2025-11-22
**Статус:** ✅ SSL сертификаты работают!

## ✅ Успешно выпущенные сертификаты:

1. ✅ **api.volzhck.ru** - READY
2. ✅ **modernissimo.volzhck.ru** - READY *(проверено, работает!)*
3. ✅ **sushi-era.volzhck.ru** - READY
4. ✅ **november.volzhck.ru** - READY
5. ⏳ **dodo.volzhck.ru** - в процессе (требуется обновить DNS)
6. ⏳ **volzhck.ru** - в процессе (требуется обновить DNS)

## 🎯 Что было сделано:

### Проблема:
Let's Encrypt **не поддерживает wildcard сертификаты** (`*.volzhck.ru`) через HTTP-01 challenge.

### Решение:
Создали **отдельные сертификаты для каждого поддомена** с использованием HTTP-01 challenge.

## 📋 DNS настройки:

### Правильные IP адреса для A-записей:

```
Timeweb Cloud Kubernetes Ingress IP:
- 82.97.240.172
- 31.130.147.150
```

### Настройте A-записи в DNS:

```
api.volzhck.ru         A  89.223.127.140  (уже настроено ✅)
modernissimo.volzhck.ru A  89.223.127.140  (уже настроено ✅)
sushi-era.volzhck.ru    A  89.223.127.140  (уже настроено ✅)
november.volzhck.ru     A  89.223.127.140  (уже настроено ✅)
dodo.volzhck.ru         A  89.223.127.140  (требуется обновить!)
volzhck.ru              A  89.223.127.140  (требуется обновить!)
```

**Текущие неправильные записи:**
- `dodo.volzhck.ru` → 178.253.43.111 ❌
- `volzhck.ru` → 178.253.43.111 ❌

## 🔧 Автоматическое обновление сертификатов:

cert-manager автоматически обновляет сертификаты за 30 дней до истечения.

**Мониторинг сертификатов:**
```bash
# Проверка всех сертификатов
kubectl get certificate -n klassifikator

# Детали конкретного сертификата
kubectl describe certificate <name> -n klassifikator

# Проверка challenges (процесс выпуска)
kubectl get challenges -n klassifikator
```

## 🆕 Добавление нового лендинга:

Когда создаете новый лендинг (например, `newshop.volzhck.ru`):

1. **Обновите Ingress** - добавьте новый блок:

```yaml
tls:
- hosts:
  - newshop.volzhck.ru
  secretName: newshop-volzhck-ru-tls
# ...
rules:
- host: newshop.volzhck.ru
  http:
    paths:
    - path: /
      pathType: Prefix
      backend:
        service:
          name: template-service
          port:
            number: 8083
```

2. **Примените изменения:**
```bash
kubectl apply -f k8s/base/ingress-with-individual-certs.yaml
```

3. **Дождитесь выпуска сертификата (1-2 минуты):**
```bash
kubectl get certificate -n klassifikator -w
```

4. **Настройте DNS A-запись:**
```
newshop.volzhck.ru  A  89.223.127.140
```

## ⚡ Альтернативное решение (Wildcard через DNS-01):

Если вам нужно автоматически выпускать сертификаты для **всех** новых поддоменов без обновления Ingress:

### Требования:
- API доступ к вашему DNS провайдеру (например, Cloudflare, Route53, etc.)
- Настройка DNS-01 challenge в cert-manager

### Пример для Cloudflare:

1. Получите API Token в Cloudflare
2. Создайте Secret:
```bash
kubectl create secret generic cloudflare-api-token \
  --from-literal=api-token=YOUR_TOKEN \
  -n cert-manager
```

3. Обновите ClusterIssuer:
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
        cloudflare:
          apiTokenSecretRef:
            name: cloudflare-api-token
            key: api-token
```

4. Обновите Ingress для использования wildcard:
```yaml
tls:
- hosts:
  - "*.volzhck.ru"
  secretName: wildcard-volzhck-ru-tls
```

## 🔍 Проверка работы SSL:

```bash
# Проверка через curl
curl -I https://modernissimo.volzhck.ru

# Проверка сертификата
openssl s_client -connect modernissimo.volzhck.ru:443 -servername modernissimo.volzhck.ru < /dev/null 2>&1 | grep -A 5 "Certificate chain"

# Проверка через браузер
# Откройте: https://modernissimo.volzhck.ru
# Нажмите на замочек → должен быть зеленый, сертификат от Let's Encrypt
```

## ✅ Итог:

**SSL сертификаты успешно настроены для 4 лендингов!**
- modernissimo.volzhck.ru ✅
- sushi-era.volzhck.ru ✅  
- november.volzhck.ru ✅
- api.volzhck.ru ✅

**Осталось обновить DNS для:**
- dodo.volzhck.ru (IP: 178.253.43.111 → 89.223.127.140)
- volzhck.ru (IP: 178.253.43.111 → 89.223.127.140)

После обновления DNS сертификаты для этих доменов выпустятся автоматически в течение 1-2 минут.

---

**Документация cert-manager:** https://cert-manager.io/docs/
**Let's Encrypt:** https://letsencrypt.org/

