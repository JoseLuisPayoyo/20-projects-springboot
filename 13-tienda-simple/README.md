# Proyecto 3: Tienda Online Simple

## 📋 Descripción General

Sistema de gestión para una tienda online básica que permite administrar productos, crear pedidos y gestionar líneas de pedido con control de stock automático.

## 🎯 Objetivos de Aprendizaje

- Implementar relaciones **OneToMany bidireccionales**
- Trabajar con **entidades intermedias** (OrderItem)
- Realizar **cálculos agregados** (totales de pedidos)
- Gestionar **transacciones** y **actualizaciones de stock**
- Aplicar **DTOs** para peticiones y respuestas
- Validar datos con **Bean Validation**
- Manejar **errores de negocio** (stock insuficiente, producto no encontrado)

## 📊 Modelo de Datos

### Entidades

#### Product
```
- id: Long (PK)
- name: String (NOT NULL)
- description: String
- price: BigDecimal (NOT NULL, >= 0)
- stock: Integer (NOT NULL, >= 0)
- createdAt: LocalDateTime
```

#### Order
```
- id: Long (PK)
- orderNumber: String (UNIQUE, generado automáticamente)
- customerName: String (NOT NULL)
- customerEmail: String (NOT NULL, formato email)
- orderDate: LocalDateTime (NOT NULL)
- status: OrderStatus (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- total: BigDecimal (calculado automáticamente)
- items: List<OrderItem> (OneToMany)
```

#### OrderItem
```
- id: Long (PK)
- order: Order (ManyToOne)
- product: Product (ManyToOne)
- quantity: Integer (NOT NULL, > 0)
- priceAtPurchase: BigDecimal (precio del producto al momento de la compra)
- subtotal: BigDecimal (calculado: quantity * priceAtPurchase)
```

### Relaciones

- **Order → OrderItem**: OneToMany bidireccional (mappedBy = "order")
- **Product → OrderItem**: OneToMany bidireccional (mappedBy = "product")

## 🔧 Requisitos Técnicos

### Dependencias Maven
```xml
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-validation
- h2 (runtime)
- lombok
```

### Estructura de Paquetes
```
com.proyecto3.tienda
├── entity
│   ├── Product.java
│   ├── Order.java
│   ├── OrderItem.java
│   └── OrderStatus.java (enum)
├── repository
│   ├── ProductRepository.java
│   ├── OrderRepository.java
│   └── OrderItemRepository.java
├── service
│   ├── ProductService.java
│   └── OrderService.java
├── controller
│   ├── ProductController.java
│   └── OrderController.java
├── dto
│   ├── ProductDTO.java
│   ├── OrderDTO.java
│   ├── OrderItemDTO.java
│   ├── CreateOrderRequest.java
│   └── CreateOrderItemRequest.java
└── exception
    ├── ResourceNotFoundException.java
    ├── InsufficientStockException.java
    └── GlobalExceptionHandler.java
```

## 🎯 Funcionalidades Principales

### Gestión de Productos

1. **Crear producto** (POST /api/products)
   - Validar nombre, precio y stock
   - Stock inicial >= 0

2. **Listar productos** (GET /api/products)
   - Devolver todos los productos disponibles
   - Incluir información de stock

3. **Obtener producto por ID** (GET /api/products/{id})
   - Lanzar excepción si no existe

4. **Actualizar producto** (PUT /api/products/{id})
   - Actualizar nombre, descripción, precio
   - El stock se gestiona mediante pedidos

5. **Eliminar producto** (DELETE /api/products/{id})
   - Solo si no tiene pedidos asociados
   - Soft delete opcional

### Gestión de Pedidos

1. **Crear pedido** (POST /api/orders)
   - Recibir: customerName, customerEmail, lista de items (productId, quantity)
   - Generar orderNumber automático (formato: ORD-YYYYMMDD-XXXX)
   - Validar stock disponible para cada producto
   - Capturar precio actual del producto en priceAtPurchase
   - Calcular subtotales y total automáticamente
   - Reducir stock de productos
   - Estado inicial: PENDING
   - **Operación transaccional**

2. **Listar pedidos** (GET /api/orders)
   - Devolver todos los pedidos con sus items
   - Incluir información del producto en cada item

3. **Obtener pedido por ID** (GET /api/orders/{id})
   - Incluir items completos con detalles del producto

4. **Actualizar estado del pedido** (PATCH /api/orders/{id}/status)
   - Cambiar entre estados: PENDING → CONFIRMED → SHIPPED → DELIVERED
   - Validar transiciones permitidas
   - No permitir cambios si está CANCELLED

5. **Cancelar pedido** (DELETE /api/orders/{id})
   - Solo si status = PENDING o CONFIRMED
   - Devolver stock a los productos
   - Cambiar status a CANCELLED

## ⚙️ Reglas de Negocio

### Control de Stock
- Al crear un pedido, validar que hay stock suficiente para todos los productos
- Si algún producto no tiene stock, rechazar el pedido completo
- Reducir el stock de forma atómica (dentro de la transacción)
- Al cancelar un pedido, devolver el stock

### Cálculos Automáticos
- **OrderItem.subtotal**: `quantity * priceAtPurchase`
- **Order.total**: suma de todos los subtotales de items
- Calcular en el Service antes de guardar

### Preservación de Precios
- `priceAtPurchase` guarda el precio del producto al momento de la compra
- Si el precio del producto cambia posteriormente, los pedidos históricos mantienen el precio original

### Validaciones
- Producto: name (not blank), price >= 0, stock >= 0
- Order: customerName (not blank), customerEmail (formato válido), status (not null)
- OrderItem: quantity > 0

## 📤 Ejemplos de Request/Response

### Crear Pedido
**POST /api/orders**
```json
{
  "customerName": "Juan Pérez",
  "customerEmail": "juan@example.com",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ]
}
```

**Response 201**
```json
{
  "id": 1,
  "orderNumber": "ORD-20250206-0001",
  "customerName": "Juan Pérez",
  "customerEmail": "juan@example.com",
  "orderDate": "2025-02-06T10:30:00",
  "status": "PENDING",
  "total": 89.97,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Laptop HP",
      "quantity": 2,
      "priceAtPurchase": 29.99,
      "subtotal": 59.98
    },
    {
      "id": 2,
      "productId": 3,
      "productName": "Mouse Logitech",
      "quantity": 1,
      "priceAtPurchase": 29.99,
      "subtotal": 29.99
    }
  ]
}
```

### Actualizar Estado
**PATCH /api/orders/1/status?status=CONFIRMED**

**Response 200**
```json
{
  "id": 1,
  "orderNumber": "ORD-20250206-0001",
  "status": "CONFIRMED",
  ...
}
```

## 🚨 Manejo de Errores

### Excepciones Personalizadas

1. **ResourceNotFoundException** (404)
   - Producto no encontrado
   - Pedido no encontrado

2. **InsufficientStockException** (400)
   - Stock insuficiente al crear pedido
   - Mensaje: "Stock insuficiente para producto: {nombre}. Disponible: {stock}, Requerido: {quantity}"

3. **InvalidOrderStateException** (400)
   - Intento de cancelar pedido ya enviado
   - Transición de estado inválida

### GlobalExceptionHandler
- Capturar excepciones y devolver respuestas JSON uniformes
- Incluir timestamp, mensaje, status code

## 🧪 Casos de Prueba Sugeridos

### Productos
- Crear producto válido
- Crear producto con precio negativo (debe fallar)
- Actualizar stock manualmente
- Eliminar producto sin pedidos

### Pedidos
- Crear pedido con stock suficiente
- Crear pedido con stock insuficiente (debe fallar)
- Verificar que el stock se reduce correctamente
- Cancelar pedido y verificar que el stock se recupera
- Verificar cálculo correcto de totales
- Actualizar estado del pedido correctamente

## 🎓 Conceptos Clave a Dominar

1. **Entidades intermedias**: OrderItem conecta Order y Product
2. **@Transactional**: asegurar atomicidad en operaciones de pedidos
3. **Cascade operations**: gestión automática de items al guardar order
4. **Cálculos en Service**: lógica de negocio fuera de controllers
5. **DTOs vs Entities**: separar modelo de dominio de API
6. **Manejo de excepciones**: respuestas HTTP apropiadas

## 📚 Recursos Adicionales

- [Spring Data JPA Relationships](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.entity-persistence)
- [Transaction Management](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)
- [Bean Validation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#validation-beanvalidation)

---

**Nivel de Complejidad**: Intermedio  
**Tiempo Estimado**: 4-6 horas  
**Proyecto Anterior**: Biblioteca (relaciones simples)  
**Siguiente Proyecto**: Sistema con autenticación