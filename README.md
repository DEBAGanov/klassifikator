# Klassifikator - Платформа для управления лендингами

Микросервисная платформа для быстрого создания и управления лендингами организаций с интеграцией Google Sheets и автоматической синхронизацией контента.

## 📋 Содержание

- [Архитектура](#архитектура)
- [Технологии](#технологии)
- [Требования](#требования)
- [Установка и запуск](#установка-и-запуск)
- [API документация](#api-документация)
- [Конфигурация](#конфигурация)
- [Разработка](#разработка)

## 🏗️ Архитектура

Проект построен на микросервисной архитектуре:

```
API Gateway (8080) - Единая точка входа
    ├── Landing Service (8081) - Управление лендингами
    ├── Content Service (8082) - Управление контентом
    ├── Template Service (8083) - Управление шаблонами
    ├── Media Service (8084) - Управление медиафайлами (S3)
    └── Integration Service (8085) - Google Sheets + Telegram
```

### Модули

- **common** - Общие entity, конфигурации, утилиты
- **landing-service** - CRUD для лендингов и организаций
- **content-service** - Управление контентом, товарами, акциями
- **template-service** - Управление шаблонами лендингов
- **media-service** - Загрузка и хранение медиафайлов в S3
- **integration-service** - Интеграция с Google Sheets и Telegram
- **api-gateway** - Роутинг запросов, CORS, (будущее: JWT auth)

## 🛠️ Технологии

### Backend
- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Cloud Gateway** - API Gateway
- **Spring Data JPA** - ORM
- **MapStruct** - Маппинг DTO ↔ Entity
- **Lombok** - Уменьшение boilerplate кода
- **Gradle** - Сборка проекта

### База данных
- **PostgreSQL 15** - Основная БД
- **Flyway** - Миграции схемы БД
- **Redis** - Кэширование

### Хранилище и интеграции
- **AWS SDK S3** - Хранение медиафайлов (Timeweb Cloud S3)
- **Google Sheets API** - Синхронизация данных
- **Telegram Bot API** - Уведомления

### DevOps
- **Docker & Docker Compose** - Контейнеризация
- **GitHub Actions** - CI/CD (планируется)

## 📦 Требования

- **Java 21** или выше
- **Docker** и **Docker Compose**
- **Gradle 8.x** (wrapper включен)
- **PostgreSQL 15** (через Docker)
- **Redis 7** (через Docker)

## 🚀 Установка и запуск

### 1. Клонирование репозитория

```bash
git clone https://github.com/your-username/klassifikator.git
cd klassifikator
```

### 2. Запуск инфраструктуры (PostgreSQL, Redis, MinIO)

```bash
docker-compose up -d
```

Это запустит:
- PostgreSQL на порту `5432`
- Redis на порту `6379`
- MinIO (S3) на портах `9000` (API) и `9001` (Console)

### 3. Сборка проекта

```bash
./gradlew build
```

### 4. Запуск микросервисов

#### Вариант 1: Запуск всех сервисов вручную

```bash
# Terminal 1 - Landing Service
./gradlew :landing-service:bootRun

# Terminal 2 - Content Service
./gradlew :content-service:bootRun

# Terminal 3 - Template Service
./gradlew :template-service:bootRun

# Terminal 4 - Media Service
./gradlew :media-service:bootRun

# Terminal 5 - Integration Service
./gradlew :integration-service:bootRun

# Terminal 6 - API Gateway
./gradlew :api-gateway:bootRun
```

#### Вариант 2: Запуск через JAR файлы

```bash
# Сборка JAR файлов
./gradlew bootJar

# Запуск сервисов
java -jar landing-service/build/libs/landing-service-0.0.1-SNAPSHOT.jar &
java -jar content-service/build/libs/content-service-0.0.1-SNAPSHOT.jar &
java -jar template-service/build/libs/template-service-0.0.1-SNAPSHOT.jar &
java -jar media-service/build/libs/media-service-0.0.1-SNAPSHOT.jar &
java -jar integration-service/build/libs/integration-service-0.0.1-SNAPSHOT.jar &
java -jar api-gateway/build/libs/api-gateway-0.0.1-SNAPSHOT.jar &
```

### 5. Проверка работоспособности

```bash
# Проверка API Gateway
curl http://localhost:8080/actuator/health

# Проверка Landing Service
curl http://localhost:8081/actuator/health

# Проверка Content Service
curl http://localhost:8082/actuator/health

# Проверка Template Service
curl http://localhost:8083/actuator/health

# Проверка Media Service
curl http://localhost:8084/actuator/health

# Проверка Integration Service
curl http://localhost:8085/actuator/health
```

## 📚 API документация

### API Gateway (порт 8080)

Все запросы идут через API Gateway:

```
http://localhost:8080/api/v1/...
```

**Всего API эндпоинтов**: 41

- Landing Service: 8 эндпоинтов
- Content Service: 12 эндпоинтов
- Template Service: 10 эндпоинтов
- Media Service: 5 эндпоинтов
- Integration Service: 6 эндпоинтов

### Landing Service

**Базовый URL**: `/api/v1/landings`

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/v1/landings` | Создать лендинг |
| GET | `/api/v1/landings/{id}` | Получить лендинг по ID |
| GET | `/api/v1/landings/domain/{domain}` | Получить лендинг по домену |
| GET | `/api/v1/landings/organization/{id}` | Получить лендинги организации |
| GET | `/api/v1/landings` | Получить все лендинги |
| PUT | `/api/v1/landings/{id}` | Обновить лендинг |
| DELETE | `/api/v1/landings/{id}` | Удалить лендинг |
| POST | `/api/v1/landings/{id}/publish` | Опубликовать лендинг |

### Content Service

**Базовый URL**: `/api/v1/content`, `/api/v1/products`, `/api/v1/promotions`

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/v1/content/organization/{id}/full` | Получить весь контент организации |
| GET | `/api/v1/content/organization/{id}` | Получить базовый контент |
| POST | `/api/v1/content` | Создать/обновить контент |
| GET | `/api/v1/products/organization/{id}` | Получить товары организации |
| POST | `/api/v1/products` | Создать товар |
| PUT | `/api/v1/products/{id}` | Обновить товар |
| DELETE | `/api/v1/products/{id}` | Удалить товар |
| GET | `/api/v1/promotions/organization/{id}` | Получить акции организации |
| POST | `/api/v1/promotions` | Создать акцию |
| PUT | `/api/v1/promotions/{id}` | Обновить акцию |
| DELETE | `/api/v1/promotions/{id}` | Удалить акцию |

### Template Service

**Базовый URL**: `/api/v1/templates`

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/v1/templates` | Создать шаблон |
| GET | `/api/v1/templates/{id}` | Получить шаблон |
| GET | `/api/v1/templates?activeOnly=true` | Получить активные шаблоны |
| PUT | `/api/v1/templates/{id}` | Обновить шаблон |
| DELETE | `/api/v1/templates/{id}` | Удалить шаблон |
| GET | `/api/v1/templates/{id}/render?organizationId={id}` | Рендер шаблона с данными |
| POST | `/api/v1/templates/{id}/render-with-data` | Рендер с кастомными данными |
| POST | `/api/v1/templates/{id}/compile` | Предкомпиляция шаблона |
| DELETE | `/api/v1/templates/{id}/cache` | Очистка кэша шаблона |

### Media Service

**Базовый URL**: `/api/v1/media`

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/v1/media/upload` | Загрузить файл (multipart) |
| GET | `/api/v1/media/{id}` | Получить информацию о файле |
| GET | `/api/v1/media/organization/{id}` | Получить файлы организации |
| GET | `/api/v1/media/{id}/url` | Получить URL файла |
| DELETE | `/api/v1/media/{id}` | Удалить файл |

### Integration Service

**Базовый URL**: `/api/v1/integration`

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/v1/integration/google-sheets/sync` | Создать синхронизацию |
| GET | `/api/v1/integration/google-sheets/sync/organization/{id}` | Получить настройки синхронизации |
| POST | `/api/v1/integration/google-sheets/sync/organization/{id}/trigger` | Запустить синхронизацию |
| GET | `/api/v1/integration/google-sheets/read` | Прочитать данные из таблицы |
| POST | `/api/v1/integration/telegram/send` | Отправить уведомление |
| POST | `/api/v1/integration/telegram/order/{id}/notify` | Уведомить о заказе |

## ⚙️ Конфигурация

### Переменные окружения

Создайте `.env` файл в корне проекта:

```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/klassifikator_dev
DATABASE_USERNAME=klassifikator
DATABASE_PASSWORD=klassifikator_dev_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# S3 (Timeweb Cloud)
S3_ENDPOINT=https://s3.timeweb.com
S3_ACCESS_KEY=your-access-key
S3_SECRET_KEY=your-secret-key
S3_BUCKET_NAME=klassifikator
S3_REGION=ru-1
S3_BASE_URL=https://klassifikator.s3.timeweb.com

# Google Sheets
GOOGLE_SHEETS_CREDENTIALS_PATH=./credentials.json
GOOGLE_SHEETS_APP_NAME=Klassifikator
GOOGLE_SHEETS_SYNC_INTERVAL=1800000

# Telegram Bot
TELEGRAM_BOT_TOKEN=your-bot-token
TELEGRAM_API_URL=https://api.telegram.org/bot
```

### Google Sheets API

1. Перейдите в [Google Cloud Console](https://console.cloud.google.com/)
2. Создайте новый проект
3. Включите Google Sheets API
4. Создайте Service Account
5. Скачайте `credentials.json`
6. Поместите файл в корень проекта

### Telegram Bot

1. Создайте бота через [@BotFather](https://t.me/BotFather)
2. Получите токен
3. Добавьте токен в `.env` файл

### Timeweb Cloud S3

1. Войдите в [панель Timeweb](https://timeweb.cloud/)
2. Создайте S3 хранилище
3. Получите Access Key и Secret Key
4. Добавьте credentials в `.env` файл

## 🎨 Шаблоны лендингов

### Доступные шаблоны

1. **Landing Basic** (`templates/landing-basic/`)
   - Адаптивный дизайн (mobile-first)
   - 9 секций (Header, Hero, About, Promotions, Products, Reviews, Gallery, Contacts, Footer)
   - Handlebars синтаксис для динамических данных
   - Интеграция с Yandex Maps
   - Модальное окно заявок
   - Lighthouse Score 95+

### Рендеринг шаблонов

Проект использует **Handlebars** для рендеринга HTML с динамическими данными:

```handlebars
<!-- Переменные -->
<h1>{{title}}</h1>
<p>{{description}}</p>

<!-- Условия -->
{{#if promotions}}
  <div class="promotions">...</div>
{{/if}}

<!-- Циклы -->
{{#each products}}
  <div class="product">
    <h3>{{name}}</h3>
    <p>{{formatPrice price}} ₽</p>
  </div>
{{/each}}
```

**Кастомные хелперы**:
- `{{formatPrice price}}` - форматирование цен
- `{{formatDate date}}` - форматирование дат
- `{{#ifCond v1 v2 ">"}}` - условные сравнения
- `{{truncate text 100}}` - обрезка текста

Подробнее: [docs/TEMPLATE_RENDERING.md](docs/TEMPLATE_RENDERING.md)

## 🧪 Тестирование

### Unit тесты

```bash
./gradlew test
```

### Integration тесты

```bash
./gradlew integrationTest
```

### Проверка покрытия кода

```bash
./gradlew jacocoTestReport
```

Отчет будет доступен в `build/reports/jacoco/test/html/index.html`

### Тестирование рендеринга

```bash
# Создать тестовый шаблон
curl -X POST http://localhost:8083/api/v1/templates \
  -H "Content-Type: application/json" \
  -d @templates/landing-basic/template.json

# Отрендерить с данными
curl http://localhost:8083/api/v1/templates/1/render?organizationId=1 > test.html
```

## 🔧 Разработка

### Структура проекта

```
klassifikator/
├── common/                      # Общий модуль
│   └── src/main/java/.../common/
│       ├── model/entity/       # JPA entities
│       └── config/             # Общие конфигурации
├── landing-service/            # Микросервис лендингов
├── content-service/            # Микросервис контента
├── template-service/           # Микросервис шаблонов
├── media-service/              # Микросервис медиа
├── integration-service/        # Микросервис интеграций
├── api-gateway/                # API Gateway
├── docs/                       # Документация
│   ├── Project.md             # Описание проекта
│   ├── Tasktracker.md         # Трекер задач
│   ├── Diary.md               # Дневник разработки
│   └── qa.md                  # Вопросы и ответы
├── docker-compose.yml          # Docker Compose конфигурация
├── build.gradle                # Root Gradle конфигурация
└── settings.gradle             # Gradle модули
```

### Добавление нового микросервиса

1. Добавьте модуль в `settings.gradle`:
```gradle
include 'new-service'
```

2. Создайте `new-service/build.gradle`:
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
}

dependencies {
    implementation project(':common')
    implementation 'org.springframework.boot:spring-boot-starter-web'
    // ... другие зависимости
}
```

3. Создайте структуру пакетов и Application класс

4. Добавьте роут в API Gateway

### Code Style

Проект следует [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

Проверка стиля:
```bash
./gradlew checkstyleMain
```

## 📊 Мониторинг

### Actuator Endpoints

Каждый сервис предоставляет actuator endpoints:

- `/actuator/health` - Статус здоровья
- `/actuator/info` - Информация о приложении
- `/actuator/metrics` - Метрики

### Логирование

Логи доступны в консоли и файлах:
- `logs/application.log` - Основные логи
- `logs/error.log` - Логи ошибок

Уровни логирования настраиваются в `application.yml`

## 🤝 Вклад в проект

1. Fork проекта
2. Создайте feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit изменения (`git commit -m 'Add some AmazingFeature'`)
4. Push в branch (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

## 📝 Лицензия

Этот проект лицензирован под MIT License - см. файл [LICENSE](LICENSE) для деталей

## 👥 Авторы

- **Baganov** - *Initial work*

## 🙏 Благодарности

- Spring Boot Team
- Google Sheets API
- Telegram Bot API
- AWS SDK Team

## 📞 Контакты

- Email: your-email@example.com
- Telegram: @your_telegram

---

**Версия**: 0.0.1-SNAPSHOT  
**Дата**: 2025-11-02  
**Статус**: В разработке (60% базовой инфраструктуры)
