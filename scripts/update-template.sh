#!/bin/bash

# Script to update template in PostgreSQL database via kubectl

set -e

NAMESPACE="klassifikator"
POD_NAME=$(kubectl get pods -n $NAMESPACE -l app=postgres -o jsonpath='{.items[0].metadata.name}')

echo "📦 Обновление шаблона в базе данных..."
echo "Pod: $POD_NAME"

# Read files
HTML=$(cat templates/landing-basic/index.html | sed "s/'/\'\'/g")
CSS=$(cat templates/landing-basic/combined-styles.css | sed "s/'/\'\'/g")  
JS=$(cat templates/landing-basic/combined-scripts.js | sed "s/'/\'\'/g")

# Create SQL update statement
SQL="UPDATE templates SET 
  name = 'Modern Business Template',
  description = 'Современный адаптивный шаблон с корзиной, слайдером и полным функционалом',
  version = '2.0.0',
  html_structure = '$HTML',
  css_styles = '$CSS',
  js_scripts = '$JS',
  config = '{\"features\": [\"slider\", \"cart\", \"gallery\", \"reviews\", \"maps\"]}',
  is_active = true,
  updated_at = NOW()
WHERE id = 1;"

# Execute SQL
echo "$SQL" | kubectl exec -i -n $NAMESPACE $POD_NAME -- psql -U klassifikator -d klassifikator

echo "✅ Шаблон обновлен успешно!"
echo "🔄 Перезапуск Template Service..."

kubectl rollout restart deployment template-service -n $NAMESPACE

echo "✅ Готово! Подождите 30 секунд и обновите страницу https://modernissimo.volzhck.ru"
