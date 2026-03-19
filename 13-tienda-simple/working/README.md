# Tienda Online Simple - Working Directory

## 🚀 Inicio Rápido

### 1. Configuración Inicial

**application.properties**
```properties
# Database H2
spring.datasource.url=jdbc:h2:mem:tienda
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Server
server.port=8080
```

**pom.xml** (dependencias necesarias)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### 2. Estructura de Directorios

```
working/
├── src/main/java/com/proyecto3/tienda/
│   ├── entity/
│   │   ├── Product.java         ← Empieza aquí
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   └── OrderStatus.java
│   ├── repository/
│   │   ├── ProductRepository.java
│   │   ├── OrderRepository.java
│   │   └── OrderItemRepository.java
│   ├── dto/
│   │   ├── ProductDTO.java
│   │   ├── OrderDTO.java
│   │   ├── OrderItemDTO.java
│   │   ├── CreateOrderRequest.java
│   │   └── CreateOrderItemRequest.java
│   ├── service/
│   │   ├── ProductService.java
│   │   └── OrderService.java
│   ├── controller/
│   │   ├── ProductController.java
│   │   └── OrderController.java
│   ├── exception/
│   │   ├── ResourceNotFoundException.java
│   │   ├── InsufficientStockException.java
│   │   ├── InvalidOrderStateException.java
│   │   └── GlobalExceptionHandler.java
│   └── TiendaApplication.java
└── src/main/resources/
    └── application.properties
```

### 3. Orden de Desarrollo Sugerido

1. **Entidades** → Product, OrderStatus (enum), Order, OrderItem
2. **Repositorios** → ProductRepository, OrderRepository, OrderItemRepository
3. **DTOs** → ProductDTO, OrderItemDTO, CreateOrderItemRequest, OrderDTO, CreateOrderRequest
4. **Excepciones** → ResourceNotFoundException, InsufficientStockException, InvalidOrderStateException, GlobalExceptionHandler
5. **Servicios** → ProductService, OrderService
6. **Controladores** → ProductController, OrderController

---

## 📡 API Endpoints Esperados

### Products

#### 1. Crear Producto
```http
POST /api/products
Content-Type: application/json
```
[Ver JSON en Postman →](postman_collection.json#crear-producto)

**Request Body**
```json
{
  "name": "Laptop HP Pavilion",
  "description": "Laptop con procesador Intel i5, 8GB RAM, 256GB SSD",
  "price": 599.99,
  "stock": 15
}
```

**Response 201 Created**
```json
{
  "id": 1,
  "name": "Laptop HP Pavilion",
  "description": "Laptop con procesador Intel i5, 8GB RAM, 256GB SSD",
  "price": 599.99,
  "stock": 15,
  "createdAt": "2025-02-06T10:00:00"
}
```

#### 2. Listar Productos
```http
GET /api/products
```
[Ver JSON en Postman →](postman_collection.json#listar-productos)

**Response 200 OK**
```json
[
  {
    "id": 1,
    "name": "Laptop HP Pavilion",
    "description": "Laptop con procesador Intel i5, 8GB RAM, 256GB SSD",
    "price": 599.99,
    "stock": 15,
    "createdAt": "2025-02-06T10:00:00"
  },
  {
    "id": 2,
    "name": "Mouse Logitech G502",
    "description": "Mouse gaming con sensor óptico de 16000 DPI",
    "price": 49.99,
    "stock": 30,
    "createdAt": "2025-02-06T10:05:00"
  }
]
```

#### 3. Obtener Producto por ID
```http
GET /api/products/1
```
[Ver JSON en Postman →](postman_collection.json#obtener-producto)

**Response 200 OK**
```json
{
  "id": 1,
  "name": "Laptop HP Pavilion",
  "description": "Laptop con procesador Intel i5, 8GB RAM, 256GB SSD",
  "price": 599.99,
  "stock": 15,
  "createdAt": "2025-02-06T10:00:00"
}
```

**Response 404 Not Found**
```json
{
  "timestamp": "2025-02-06T10:30:00",
  "message": "Producto no encontrado con ID: 999",
  "status": 404
}
```

#### 4. Actualizar Producto
```http
PUT /api/products/1
Content-Type: application/json
```
[Ver JSON en Postman →](postman_collection.json#actualizar-producto)

**Request Body**
```json
{
  "name": "Laptop HP Pavilion 15",
  "description": "Laptop con procesador Intel i5 11va gen, 16GB RAM, 512GB SSD",
  "price": 699.99,
  "stock": 20
}
```

**Response 200 OK**
```json
{
  "id": 1,
  "name": "Laptop HP Pavilion 15",
  "description": "Laptop con procesador Intel i5 11va gen, 16GB RAM, 512GB SSD",
  "price": 699.99,
  "stock": 20,
  "createdAt": "2025-02-06T10:00:00"
}
```

#### 5. Eliminar Producto
```http
DELETE /api/products/1
```
[Ver JSON en Postman →](postman_collection.json#eliminar-producto)

**Response 204 No Content**

---

### Orders

#### 1. Crear Pedido
```http
POST /api/orders
Content-Type: application/json
```
[Ver JSON en Postman →](postman_collection.json#crear-pedido)

**Request Body**
```json
{
  "customerName": "Juan Pérez García",
  "customerEmail": "juan.perez@example.com",
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

**Response 201 Created**
```json
{
  "id": 1,
  "orderNumber": "ORD-20250206-0001",
  "customerName": "Juan Pérez García",
  "customerEmail": "juan.perez@example.com",
  "orderDate": "2025-02-06T11:00:00",
  "status": "PENDING",
  "total": 1249.97,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Laptop HP Pavilion 15",
      "quantity": 2,
      "priceAtPurchase": 599.99,
      "subtotal": 1199.98
    },
    {
      "id": 2,
      "productId": 2,
      "productName": "Mouse Logitech G502",
      "quantity": 1,
      "priceAtPurchase": 49.99,
      "subtotal": 49.99
    }
  ]
}
```

**Response 400 Bad Request (Stock Insuficiente)**
```json
{
  "timestamp": "2025-02-06T11:05:00",
  "message": "Stock insuficiente para producto: Laptop HP Pavilion 15. Disponible: 1, Requerido: 2",
  "status": 400
}
```

#### 2. Listar Pedidos
```http
GET /api/orders
```
[Ver JSON en Postman →](postman_collection.json#listar-pedidos)

**Response 200 OK**
```json
[
  {
    "id": 1,
    "orderNumber": "ORD-20250206-0001",
    "customerName": "Juan Pérez García",
    "customerEmail": "juan.perez@example.com",
    "orderDate": "2025-02-06T11:00:00",
    "status": "PENDING",
    "total": 1249.97,
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Laptop HP Pavilion 15",
        "quantity": 2,
        "priceAtPurchase": 599.99,
        "subtotal": 1199.98
      },
      {
        "id": 2,
        "productId": 2,
        "productName": "Mouse Logitech G502",
        "quantity": 1,
        "priceAtPurchase": 49.99,
        "subtotal": 49.99
      }
    ]
  }
]
```

#### 3. Obtener Pedido por ID
```http
GET /api/orders/1
```
[Ver JSON en Postman →](postman_collection.json#obtener-pedido)

**Response 200 OK** (igual que el item individual del listado)

**Response 404 Not Found**
```json
{
  "timestamp": "2025-02-06T11:10:00",
  "message": "Pedido no encontrado con ID: 999",
  "status": 404
}
```

#### 4. Actualizar Estado del Pedido
```http
PATCH /api/orders/1/status?status=CONFIRMED
```
[Ver JSON en Postman →](postman_collection.json#actualizar-estado)

**Response 200 OK**
```json
{
  "id": 1,
  "orderNumber": "ORD-20250206-0001",
  "customerName": "Juan Pérez García",
  "customerEmail": "juan.perez@example.com",
  "orderDate": "2025-02-06T11:00:00",
  "status": "CONFIRMED",
  "total": 1249.97,
  "items": [...]
}
```

**Response 400 Bad Request (Estado Inválido)**
```json
{
  "timestamp": "2025-02-06T11:15:00",
  "message": "No se puede cambiar el estado de un pedido cancelado",
  "status": 400
}
```

#### 5. Cancelar Pedido
```http
DELETE /api/orders/1
```
[Ver JSON en Postman →](postman_collection.json#cancelar-pedido)

**Response 200 OK**
```json
{
  "id": 1,
  "orderNumber": "ORD-20250206-0001",
  "status": "CANCELLED",
  "message": "Pedido cancelado. Stock devuelto a los productos."
}
```

**Response 400 Bad Request**
```json
{
  "timestamp": "2025-02-06T11:20:00",
  "message": "No se puede cancelar un pedido en estado: SHIPPED",
  "status": 400
}
```

---

## 🧪 Secuencia de Pruebas

### Flujo Completo de Prueba

1. **Crear 3 productos** con stock suficiente
   ```bash
   POST /api/products → Laptop (stock: 10)
   POST /api/products → Mouse (stock: 20)
   POST /api/products → Teclado (stock: 15)
   ```

2. **Listar productos** para verificar creación
   ```bash
   GET /api/products
   ```

3. **Crear pedido válido** (suficiente stock)
   ```bash
   POST /api/orders
   {
     "customerName": "Juan Pérez",
     "customerEmail": "juan@example.com",
     "items": [
       {"productId": 1, "quantity": 2},
       {"productId": 2, "quantity": 1}
     ]
   }
   ```

4. **Verificar reducción de stock**
   ```bash
   GET /api/products/1 → stock debe ser 8 (10 - 2)
   GET /api/products/2 → stock debe ser 19 (20 - 1)
   ```

5. **Intentar pedido con stock insuficiente**
   ```bash
   POST /api/orders
   {
     "customerName": "María López",
     "customerEmail": "maria@example.com",
     "items": [
       {"productId": 1, "quantity": 15}  // Solo quedan 8
     ]
   }
   → Debe retornar 400 Bad Request
   ```

6. **Actualizar estado del pedido**
   ```bash
   PATCH /api/orders/1/status?status=CONFIRMED
   PATCH /api/orders/1/status?status=SHIPPED
   ```

7. **Cancelar pedido** (solo si es PENDING o CONFIRMED)
   ```bash
   DELETE /api/orders/1
   ```

8. **Verificar devolución de stock**
   ```bash
   GET /api/products/1 → stock debe volver a 10
   GET /api/products/2 → stock debe volver a 20
   ```

---

## 📋 Checklist de Desarrollo

### Entidades
- [ ] Product con validaciones
- [ ] OrderStatus (enum con 5 estados)
- [ ] Order con generación automática de orderNumber
- [ ] OrderItem con cálculo de subtotal

### Repositorios
- [ ] ProductRepository extends JpaRepository
- [ ] OrderRepository extends JpaRepository
- [ ] OrderItemRepository extends JpaRepository

### DTOs
- [ ] ProductDTO
- [ ] OrderItemDTO con productName
- [ ] CreateOrderItemRequest (solo productId y quantity)
- [ ] OrderDTO con items completos
- [ ] CreateOrderRequest con items

### Excepciones
- [ ] ResourceNotFoundException
- [ ] InsufficientStockException
- [ ] InvalidOrderStateException
- [ ] GlobalExceptionHandler con @RestControllerAdvice

### Servicios
- [ ] ProductService (CRUD básico)
- [ ] OrderService con lógica de negocio:
  - [ ] Validar stock disponible
  - [ ] Reducir stock al crear pedido
  - [ ] Capturar priceAtPurchase
  - [ ] Calcular subtotales y total
  - [ ] Devolver stock al cancelar
  - [ ] @Transactional en métodos críticos

### Controladores
- [ ] ProductController (5 endpoints)
- [ ] OrderController (5 endpoints)

---

## 💡 Notas Importantes

### Generación de orderNumber
```java
// Formato: ORD-YYYYMMDD-XXXX
// Ejemplo: ORD-20250206-0001
String orderNumber = String.format("ORD-%s-%04d", 
    LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
    countOrdersToday + 1
);
```

### Cálculo de Total
```java
// En OrderService al crear pedido
BigDecimal total = order.getItems().stream()
    .map(OrderItem::getSubtotal)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
order.setTotal(total);
```

### Control de Stock (Transaccional)
```java
@Transactional
public OrderDTO createOrder(CreateOrderRequest request) {
    // 1. Validar stock para TODOS los productos primero
    // 2. Si alguno falla, lanzar excepción (rollback automático)
    // 3. Si todos pasan, reducir stock y crear pedido
    // 4. La transacción asegura atomicidad
}
```

---

## 📦 Colección Postman

Importa la colección `postman_collection.json` incluida en este directorio con todos los endpoints configurados y ejemplos de datos.

**Incluye**:
- Variables de entorno (baseUrl)
- Todos los endpoints documentados
- Ejemplos de success y error cases
- Tests automáticos para validar respuestas

---

¿Listo para empezar? Crea `Product.java` y envíamelo para revisión 🚀