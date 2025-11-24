# 🎉 Google Sheets Integration - Успешно настроено!

**Дата**: 19 ноября 2025  
**Статус**: ✅ РАБОТАЕТ

---

## 📊 Что работает

### 1. ✅ Google Sheets API интеграция
- Credentials загружены в Kubernetes Secret
- Integration Service подключается к Google Sheets API
- Читает данные из таблицы: https://docs.google.com/spreadsheets/d/1KS2TOS5ZKxONDmUaVoiwb3tyu3Y1DlGQaME2KM4vItQ

### 2. ✅ Автоматическое создание организаций
Из Google Sheets созданы организации:
- **Модернисимо** (ID: 2) - Мебель, Компания
- **Суши-Эра** (ID: 3) - Общепит, Компания

### 3. ✅ Автоматическое создание лендингов
Созданы лендинги:
- `modernissimo.volzhck.ru` (ID: 2, status: ACTIVE)
- `sushi-era.volzhck.ru` (ID: 3, status: ACTIVE)

### 4. ✅ Все сервисы работают
```
✅ API Gateway - Running
✅ Landing Service - Running
✅ Content Service - Running (исправлен Redis)
✅ Template Service - Running
✅ Media Service - Running
✅ Integration Service - Running (с Google Sheets!)
✅ PostgreSQL - Running
✅ Redis - Running
✅ MinIO - Running
```

---

## 🔧 Что было исправлено

### 1. Integration Service - Google Sheets credentials
**Проблема**: `./credentials.json (No such file or directory)`

**Решение**:
- Загружен правильный `credentials.json` из `/config/google-credentials.json`
- Создан Kubernetes Secret: `google-credentials`
- Смонтирован как volume в `/app/config/credentials.json`
- Добавлена конфигурация в `application-prod.yml`:
  ```yaml
  google:
    sheets:
      credentials-path: ${GOOGLE_APPLICATION_CREDENTIALS:/app/config/credentials.json}
      application-name: Klassifikator
  ```

### 2. Content Service - Redis connection
**Проблема**: `UnknownHostException: redis`

**Решение**:
Исправлен `content-service/src/main/resources/application-prod.yml`:
```yaml
# Было:
host: ${SPRING_REDIS_HOST:redis}
port: ${SPRING_REDIS_8082:6379}

# Стало:
host: ${REDIS_HOST:redis}
port: ${REDIS_PORT:6379}
```

---

## 📡 API Endpoints

### Синхронизация из Google Sheets
```bash
curl -X POST "https://api.volzhck.ru/api/v1/integration/google-sheets/sync-all?spreadsheetId=1KS2TOS5ZKxONDmUaVoiwb3tyu3Y1DlGQaME2KM4vItQ&sheetName=Organizations" \
  -H "Content-Type: application/json"
```

**Ответ**:
```json
{
  "total": 10,
  "created": 2,
  "failed": 0,
  "updated": 0,
  "status": "SUCCESS"
}
```

### Проверка организаций
```bash
curl https://api.volzhck.ru/api/v1/organizations | jq '.[] | {id, name, category}'
```

### Проверка лендингов
```bash
curl https://api.volzhck.ru/api/v1/landings | jq '.[] | {id, domain, status}'
```

---

## 🌐 Домены

В Timeweb Cloud настроены поддомены:
- ✅ `modernissimo.volzhck.ru` → 31.130.147.150
- ✅ `production-test.volzhck.ru` → 31.130.147.150  
- ✅ `sushi-era.volzhck.ru` → 31.130.147.150

---

## ⚠️ Что нужно доработать

### Рендеринг лендингов по поддоменам

**Текущая ситуация**:
- Данные есть в БД ✅
- Лендинги созданы ✅
- Template Service запущен ✅
- **НО**: Template Service не отдает HTML по поддоменам ❌

**Что нужно**:
1. Реализовать контроллер в Template Service:
   ```java
   @GetMapping("/{subdomain}.volzhck.ru")
   public String renderLanding(@PathVariable String subdomain) {
       // 1. Получить лендинг по subdomain
       // 2. Получить данные организации
       // 3. Получить шаблон
       // 4. Рендерить HTML
       return html;
   }
   ```

2. Или использовать Nginx для проксирования:
   ```
   modernissimo.volzhck.ru → Template Service → renderLanding("modernissimo")
   ```

---

## 📋 Google Sheets структура

### Лист "Organizations"

| Столбец | Пример | Обязательное |
|---------|--------|--------------|
| Домен | `modernissimo.volzhck.ru` | ✅ Да |
| Название | "Модернисимо" | ✅ Да |
| Категория | "Мебель" | Нет |
| Тип | "Компания" | Нет |
| Телефон | "+7 900 123 45 67" | Нет |
| Адрес | "г. Волжск, ул Ленина, 52" | Нет |
| ... | ... | ... |

### Service Account Email
```
klassifikator-sheets@klassifikator-477110.iam.gserviceaccount.com
```

Этот email добавлен в Google Таблицу с правами **Редактор**.

---

## 🧪 Тестирование

### 1. Добавить новую организацию
1. Откройте Google Таблицу
2. Добавьте новую строку с данными:
   ```
   Домен: newcompany.volzhck.ru
   Название: Новая Компания
   Категория: IT
   ```
3. Создайте поддомен в Timeweb Cloud: `newcompany.volzhck.ru`
4. Запустите синхронизацию (или подождите 30 мин автосинхронизации)
5. Проверьте: `curl https://api.volzhck.ru/api/v1/landings`

### 2. Обновить данные существующей организации
1. Измените данные в Google Таблице (например, телефон)
2. Запустите синхронизацию
3. Данные обновятся в БД

---

## 🎯 Следующие шаги

### Приоритет 1: Рендеринг лендингов
- [ ] Реализовать контроллер в Template Service для отдачи HTML
- [ ] Настроить Ingress для маршрутизации поддоменов на Template Service
- [ ] Протестировать: открыть `https://modernissimo.volzhck.ru` в браузере

### Приоритет 2: Автоматизация
- [ ] Настроить автоматическую синхронизацию каждые 30 минут
- [ ] Добавить webhook от Google Sheets для мгновенной синхронизации

### Приоритет 3: Мониторинг
- [ ] Логирование синхронизаций
- [ ] Уведомления об ошибках синхронизации
- [ ] Dashboard со статистикой

---

## 🎊 Резюме

**Klassifikator успешно интегрирован с Google Sheets!**

Теперь вы можете:
- ✅ Добавлять организации в Google Таблицу
- ✅ Создавать поддомены в Timeweb Cloud
- ✅ Автоматически получать лендинги через API

**Финальный шаг**: доработать Template Service для отдачи HTML по поддоменам.

---

**Версия**: 2.0  
**Дата создания**: 2025-11-19  
**Автор**: AI Assistant

