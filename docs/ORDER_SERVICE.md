# Order Service - Документация

**Версия**: 1.0  
**Дата создания**: 2025-11-03  
**Порт**: 8086

---

## 📋 Описание

Order Service - микросервис для управления заказами с лендингов. Обрабатывает создание заказов, управление статусами и отправку уведомлений в Telegram.

---

## 🏗️ Архитектура

### Компоненты

```
order-service/
├── controller/
│   └── OrderController.java          # REST API контроллер
├── service/
│   ├── OrderService.java             # Интерфейс сервиса
│   └── impl/
│       └── OrderServiceImpl.java     # Реализация бизнес-логики
├── repository/
│   ├── OrderRepository.java          # Репозиторий для заказов
│   ├── OrderItemRepository.java      # Репозиторий для позиций заказа
│   └── ProductRepository.java        # Репозиторий для товаров (read-only)
├── model/dto/
│   ├── OrderDto.java                 # DTO заказа
│   ├── OrderRequestDto.java          # DTO для создания заказа
│   ├── OrderItemDto.java             # DTO позиции заказа
│   └── OrderItemRequestDto.java      # DTO для создания позиции
├── mapper/
│   ├── OrderMapper.java              # Маппер заказов (MapStruct)
│   └── OrderItemMapper.java          # Маппер позиций (MapStruct)
├── exception/
│   ├── OrderNotFoundException.java   # Исключение: заказ не найден
│   └── ProductNotFoundException.java # Исключение: товар не найден
└── OrderApplication.java             # Главный класс приложения
```

---

## 🔌 API Endpoints

### 1. Создать заказ
```http
POST /api/v1/orders
Content-Type: application/json

{
  "organizationId": 1,
  "landingId": 1,
  "customerName": "Иван Иванов",
  "customerPhone": "+79001234567",
  "customerEmail": "ivan@example.com",
  "deliveryAddress": "г. Москва, ул. Ленина, д. 1",
  "comment": "Доставка с 10:00 до 18:00",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}
```

**Response**: `201 Created`
```json
{
  "id": 1,
  "organizationId": 1,
  "landingId": 1,
  "customerName": "Иван Иванов",
  "customerPhone": "+79001234567",
  "customerEmail": "ivan@example.com",
  "deliveryAddress": "г. Москва, ул. Ленина, д. 1",
  "comment": "Доставка с 10:00 до 18:00",
  "totalAmount": 45000.00,
  "status": "NEW",
  "createdAt": "2025-11-03T22:00:00",
  "updatedAt": "2025-11-03T22:00:00",
  "items": [
    {
      "id": 1,
      "orderId": 1,
      "productId": 1,
      "productName": "Стол офисный",
      "quantity": 2,
      "price": 15000.00,
      "totalPrice": 30000.00
    },
    {
      "id": 2,
      "orderId": 1,
      "productId": 2,
      "productName": "Стул офисный",
      "quantity": 1,
      "price": 15000.00,
      "totalPrice": 15000.00
    }
  ]
}
```

---

### 2. Получить заказ по ID
```http
GET /api/v1/orders/{id}
```

**Response**: `200 OK`

---

### 3. Получить заказы организации
```http
GET /api/v1/orders/organization/{organizationId}?page=0&size=20&sortBy=createdAt&sortDirection=DESC
```

**Response**: `200 OK` (Page<OrderDto>)

---

### 4. Получить заказы лендинга
```http
GET /api/v1/orders/landing/{landingId}?page=0&size=20&sortBy=createdAt&sortDirection=DESC
```

**Response**: `200 OK` (Page<OrderDto>)

---

### 5. Получить заказы по статусу
```http
GET /api/v1/orders/status/{status}?page=0&size=20&sortBy=createdAt&sortDirection=DESC
```

**Статусы**: `NEW`, `PROCESSING`, `COMPLETED`, `CANCELLED`

**Response**: `200 OK` (Page<OrderDto>)

---

### 6. Обновить статус заказа
```http
PATCH /api/v1/orders/{id}/status?status=PROCESSING
```

**Response**: `200 OK`

---

### 7. Отменить заказ
```http
POST /api/v1/orders/{id}/cancel
```

**Response**: `200 OK`

---

### 8. Удалить заказ
```http
DELETE /api/v1/orders/{id}
```

**Response**: `204 No Content`

---

## 📊 Модель данных

### Order (Заказ)
```java
{
  id: Long                    // ID заказа
  organizationId: Long        // ID организации
  landingId: Long             // ID лендинга
  customerName: String        // Имя клиента
  customerPhone: String       // Телефон клиента
  customerEmail: String       // Email клиента
  deliveryAddress: String     // Адрес доставки
  comment: String             // Комментарий к заказу
  totalAmount: BigDecimal     // Общая сумма заказа
  status: OrderStatus         // Статус заказа
  createdAt: LocalDateTime    // Дата создания
  updatedAt: LocalDateTime    // Дата обновления
  items: List<OrderItem>      // Позиции заказа
}
```

### OrderItem (Позиция заказа)
```java
{
  id: Long                    // ID позиции
  orderId: Long               // ID заказа
  productId: Long             // ID товара
  productName: String         // Название товара
  quantity: Integer           // Количество
  price: BigDecimal           // Цена за единицу
  totalPrice: BigDecimal      // Общая стоимость позиции
}
```

### OrderStatus (Статус заказа)
```java
enum OrderStatus {
  NEW,          // Новый заказ
  PROCESSING,   // В обработке
  COMPLETED,    // Выполнен
  CANCELLED     // Отменен
}
```

---

## 🔔 Интеграция с Telegram

При создании заказа автоматически отправляется уведомление в Telegram через Integration Service.

### Формат уведомления
```
🛒 Новый заказ #1

👤 Клиент: Иван Иванов
📞 Телефон: +79001234567
💰 Сумма: 45000.00 руб.
📝 Комментарий: Доставка с 10:00 до 18:00
```

### Асинхронная отправка
- Уведомление отправляется асинхронно
- Не блокирует создание заказа
- Ошибки логируются, но не прерывают процесс

---

## ✅ Валидация

### OrderRequestDto
- `organizationId` - обязательное поле (NotNull)
- `landingId` - обязательное поле (NotNull)
- `customerName` - обязательное поле (NotBlank)
- `customerPhone` - обязательное поле (NotBlank)
- `customerEmail` - валидация email (Email)
- `items` - обязательное поле, не пустой список (NotEmpty)

### OrderItemRequestDto
- `productId` - обязательное поле (NotNull)
- `quantity` - обязательное поле, минимум 1 (NotNull, Min(1))

---

## 🔄 Бизнес-логика

### Создание заказа
1. Валидация входных данных
2. Проверка существования товаров
3. Расчет стоимости каждой позиции (price × quantity)
4. Расчет общей суммы заказа
5. Сохранение заказа в БД
6. Сохранение позиций заказа
7. Асинхронная отправка уведомления в Telegram
8. Возврат созданного заказа

### Расчет суммы
```java
// Для каждой позиции
itemTotal = product.price × quantity

// Общая сумма заказа
totalAmount = sum(itemTotal)
```

---

## 🗄️ База данных

### Таблица: orders
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    landing_id BIGINT,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50) NOT NULL,
    customer_email VARCHAR(255),
    delivery_address TEXT,
    comment TEXT,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_orders_organizations FOREIGN KEY (organization_id) 
        REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_orders_landings FOREIGN KEY (landing_id) 
        REFERENCES landings(id) ON DELETE SET NULL
);

CREATE INDEX idx_orders_organization ON orders(organization_id);
CREATE INDEX idx_orders_landing ON orders(landing_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
```

### Таблица: order_items
```sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_items_orders FOREIGN KEY (order_id) 
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_products FOREIGN KEY (product_id) 
        REFERENCES products(id) ON DELETE RESTRICT
);

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);
```

---

## 🚀 Запуск

### Через JAR
```bash
java -jar order-service/build/libs/order-service.jar
```

### Переменные окружения
```bash
SERVER_PORT=8086
DB_URL=jdbc:postgresql://localhost:5432/klassifikator_dev
DB_USERNAME=klassifikator_user
DB_PASSWORD=klassifikator_password
REDIS_HOST=localhost
REDIS_PORT=6379
INTEGRATION_SERVICE_URL=http://localhost:8085
```

---

## 🧪 Тестирование

### Создание тестового заказа
```bash
curl -X POST http://localhost:8086/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "organizationId": 1,
    "landingId": 1,
    "customerName": "Тестовый Клиент",
    "customerPhone": "+79001234567",
    "customerEmail": "test@example.com",
    "deliveryAddress": "г. Москва, ул. Тестовая, д. 1",
    "comment": "Тестовый заказ",
    "items": [
      {"productId": 1, "quantity": 2}
    ]
  }'
```

### Получение заказа
```bash
curl http://localhost:8086/api/v1/orders/1
```

### Обновление статуса
```bash
curl -X PATCH "http://localhost:8086/api/v1/orders/1/status?status=PROCESSING"
```

---

## 📝 Технические долги

1. **Unit тесты**
   - Тесты для OrderService
   - Тесты для OrderController
   - Тесты для валидации

2. **Integration тесты**
   - Тестирование создания заказа через API
   - Тестирование интеграции с Telegram
   - Тестирование расчета суммы

3. **Обработка ошибок**
   - GlobalExceptionHandler
   - Обработка неактивных товаров
   - Обработка отсутствия товара в наличии

4. **Кэширование**
   - Кэширование списка заказов
   - Кэширование данных о товарах

---

## 📚 Зависимости

- **Spring Boot Starter Web** - REST API
- **Spring Boot Starter Data JPA** - работа с БД
- **Spring Boot Starter WebFlux** - WebClient для межсервисного взаимодействия
- **Spring Boot Starter Validation** - валидация данных
- **Spring Boot Starter Cache** - кэширование
- **PostgreSQL Driver** - драйвер БД
- **Redis** - кэш
- **MapStruct** - маппинг Entity ↔ DTO
- **Lombok** - уменьшение boilerplate кода

---

## 🔗 Связанные сервисы

- **Content Service** (8082) - данные о товарах
- **Integration Service** (8085) - отправка уведомлений в Telegram
- **API Gateway** (8080) - роутинг запросов

---

**Автор**: AI Assistant  
**Дата последнего обновления**: 2025-11-03

