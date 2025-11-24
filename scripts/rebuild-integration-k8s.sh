#!/bin/bash

# Script to rebuild and deploy Integration Service to Kubernetes

set -e

echo "=== Пересборка Integration Service в Kubernetes ==="

# Export Timeweb token
export TIMEWEB_REGISTRY_TOKEN="eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCIsImtpZCI6IjFrYnhacFJNQGJSI0tSbE1xS1lqIn0.eyJ1c2VyIjoienE5NzQ4MyIsInR5cGUiOiJhcGlfa2V5IiwiYXBpX2tleV9pZCI6IjljMmY1MGJmLTliYmYtNDdhOC1iNGIwLWEyZTNkZjg4NDAxZCIsImlhdCI6MTc2MzQwOTE1OX0.BEqbEvqXP_DVoRY6cN5cp9y0KvomK0HVlbd1CrFRU9K5ac_buM1UAc9l_fG1024g5qAvPmJVLMf7MU4_2QudKIHaHGPJ-nncUTYOOrAP9wnlcd0ZNRssCAPnZ9A0fRmjVh_gFgIo7JIsB1bgM1BXRLThOTpyF-PeBxnU4H5Wxzhw9-iuqHr4gKrrf86BF5TESJ4I01-Ao16lSlMzrIPQSFYfoxGFhCdgy-mtIcYH7weESQv1ZP9GSd6bJ3B3n3ljBEh69F_B66VYlRn04WslbSNiwuo5TqhwyhgcPI41tDHRMeWUoIo5MjpbdDUXXatKEp-lEaXDaR7O2Ou2cBoiUtB7JdAWqtyBBrvqGmyTBbEIovqdx7bHiEG6zT-ile45lf3t87CPRRI_-os3oO-kVrmaKMdRPDE1StvzK-7dhi-2GHlsxOxCs7-orM3JSN3i-YYQrwxxODbKuN_vU2CEiCX-WZ7TG3PUT5yxiYsSoWdf3gD8fJvlnZfDFWXcbxQ6"

# Build using existing Docker image
echo "1️⃣ Используем уже собранный образ..."
docker images | grep integration-service

# Scale down current deployment
echo "2️⃣ Останавливаем текущий deployment..."
kubectl scale deployment/integration-service -n klassifikator --replicas=0
sleep 5

# Delete old pods
echo "3️⃣ Удаляем старые поды..."
kubectl delete pods -n klassifikator -l app=integration-service --force --grace-period=0 2>/dev/null || true

# Scale up with new code
echo "4️⃣ Запускаем новый deployment..."
kubectl set image deployment/integration-service -n klassifikator \
  integration-service=cr.timeweb.cloud/zq97483/klassifikator-integration-service:latest

kubectl scale deployment/integration-service -n klassifikator --replicas=1

# Wait for pod to be ready
echo "5️⃣ Ожидание готовности пода..."
kubectl wait --for=condition=ready pod -n klassifikator -l app=integration-service --timeout=180s

# Check logs
echo "6️⃣ Проверка логов..."
sleep 10
kubectl logs -n klassifikator deployment/integration-service --tail=30

echo ""
echo "✅ Integration Service перезапущен!"

