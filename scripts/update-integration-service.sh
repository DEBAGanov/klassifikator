#!/bin/bash

# Script to update Integration Service in Kubernetes

set -e

echo "=== Обновление Integration Service ==="

# Copy updated source code to the build pod
echo "1️⃣ Копирование исходного кода..."
kubectl delete configmap integration-service-source -n klassifikator --ignore-not-found
kubectl create configmap integration-service-source -n klassifikator \
  --from-file=GoogleSheetsServiceImpl.java=integration-service/src/main/java/com/baganov/klassifikator/integration/service/impl/GoogleSheetsServiceImpl.java \
  --from-file=GoogleSheetsDataProcessor.java=integration-service/src/main/java/com/baganov/klassifikator/integration/service/GoogleSheetsDataProcessor.java

# Restart Integration Service to pick up the new code
echo "2️⃣ Перезапуск Integration Service..."
kubectl rollout restart deployment/integration-service -n klassifikator

# Wait for rollout to complete
echo "3️⃣ Ожидание завершения развертывания..."
kubectl rollout status deployment/integration-service -n klassifikator --timeout=180s

# Check logs
echo "4️⃣ Проверка логов..."
sleep 5
kubectl logs -n klassifikator deployment/integration-service --tail=20

echo ""
echo "✅ Integration Service успешно обновлен!"
echo ""
echo "📋 Проверить статус:"
echo "   kubectl get pods -n klassifikator -l app=integration-service"
echo ""
echo "📋 Посмотреть логи:"
echo "   kubectl logs -n klassifikator deployment/integration-service -f"

