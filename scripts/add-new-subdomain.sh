#!/bin/bash

# Скрипт для автоматического добавления SSL сертификата для нового поддомена
# Использование: ./add-new-subdomain.sh newshop

set -e

if [ -z "$1" ]; then
    echo "❌ Ошибка: не указан поддомен"
    echo "Использование: $0 <subdomain>"
    echo "Пример: $0 newshop"
    exit 1
fi

SUBDOMAIN=$1
DOMAIN="volzhck.ru"
FULL_DOMAIN="${SUBDOMAIN}.${DOMAIN}"
SECRET_NAME="${SUBDOMAIN}-volzhck-ru-tls"
NAMESPACE="klassifikator"

echo "🔧 Добавление SSL сертификата для ${FULL_DOMAIN}..."
echo ""

# Проверяем существует ли уже этот сертификат
if kubectl get certificate "${SECRET_NAME}" -n "${NAMESPACE}" &> /dev/null; then
    echo "⚠️  Сертификат для ${FULL_DOMAIN} уже существует!"
    kubectl get certificate "${SECRET_NAME}" -n "${NAMESPACE}"
    exit 0
fi

# Создаем временный файл с манифестом
TMP_FILE=$(mktemp)
cat > "${TMP_FILE}" << EOF
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: ${SECRET_NAME}
  namespace: ${NAMESPACE}
spec:
  secretName: ${SECRET_NAME}
  issuerRef:
    name: letsencrypt-prod
    kind: ClusterIssuer
  dnsNames:
  - ${FULL_DOMAIN}
  renewBefore: 720h  # 30 дней до истечения
EOF

# Применяем Certificate
echo "📝 Создание Certificate..."
kubectl apply -f "${TMP_FILE}"
rm "${TMP_FILE}"

# Обновляем Ingress - добавляем TLS
echo "🔐 Добавление TLS в Ingress..."

# Получаем текущий Ingress
kubectl get ingress klassifikator-ingress -n "${NAMESPACE}" -o yaml > /tmp/ingress-backup.yaml

# Создаем патч для TLS
TLS_PATCH=$(cat << EOF
{
  "spec": {
    "tls": [
      {
        "hosts": ["${FULL_DOMAIN}"],
        "secretName": "${SECRET_NAME}"
      }
    ]
  }
}
EOF
)

# Создаем патч для rules
RULE_PATCH=$(cat << EOF
{
  "spec": {
    "rules": [
      {
        "host": "${FULL_DOMAIN}",
        "http": {
          "paths": [
            {
              "path": "/",
              "pathType": "Prefix",
              "backend": {
                "service": {
                  "name": "template-service",
                  "port": {
                    "number": 8083
                  }
                }
              }
            }
          ]
        }
      }
    ]
  }
}
EOF
)

# Применяем патчи (добавляем в существующие массивы)
kubectl patch ingress klassifikator-ingress -n "${NAMESPACE}" --type=json -p="[
  {\"op\": \"add\", \"path\": \"/spec/tls/-\", \"value\": {\"hosts\": [\"${FULL_DOMAIN}\"], \"secretName\": \"${SECRET_NAME}\"}},
  {\"op\": \"add\", \"path\": \"/spec/rules/-\", \"value\": {\"host\": \"${FULL_DOMAIN}\", \"http\": {\"paths\": [{\"path\": \"/\", \"pathType\": \"Prefix\", \"backend\": {\"service\": {\"name\": \"template-service\", \"port\": {\"number\": 8083}}}}]}}}
]"

echo "✅ Конфигурация обновлена!"
echo ""
echo "⏳ Ожидание выпуска сертификата (это может занять 1-2 минуты)..."
echo ""

# Ждем готовности сертификата с таймаутом 5 минут
if kubectl wait --for=condition=ready certificate "${SECRET_NAME}" \
    -n "${NAMESPACE}" --timeout=300s 2>/dev/null; then
    
    echo ""
    echo "🎉 Успешно! SSL сертификат для ${FULL_DOMAIN} готов!"
    echo ""
    echo "📋 Информация о сертификате:"
    kubectl get certificate "${SECRET_NAME}" -n "${NAMESPACE}"
    echo ""
    echo "🌐 Проверьте в браузере: https://${FULL_DOMAIN}"
    echo "🔒 Сертификат должен быть валидным (зеленый замочек)"
    echo ""
else
    echo ""
    echo "⚠️  Выпуск сертификата занимает больше времени чем ожидалось..."
    echo ""
    echo "📊 Текущий статус:"
    kubectl get certificate "${SECRET_NAME}" -n "${NAMESPACE}"
    echo ""
    echo "🔍 Проверьте детали:"
    echo "kubectl describe certificate ${SECRET_NAME} -n ${NAMESPACE}"
    echo ""
    echo "🔍 Проверьте challenges:"
    echo "kubectl get challenges -n ${NAMESPACE}"
    echo ""
fi

echo "📝 Не забудьте создать DNS A-запись:"
echo "   ${FULL_DOMAIN}  A  89.223.127.140"
echo ""

