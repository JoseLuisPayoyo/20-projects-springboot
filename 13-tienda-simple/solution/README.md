# Tienda Online Simple - Solución Completa

## 📚 Documentación Técnica

Esta solución implementa un sistema de tienda online con gestión de productos, pedidos y control automático de stock utilizando Spring Boot, JPA y arquitectura REST.

---

## 🏗️ Arquitectura de la Solución

### Capas de la Aplicación

```
Presentation Layer (Controllers)
        ↓
Service Layer (Business Logic)
        ↓
Repository Layer (Data Access)
        ↓
Database (H2)
```

### Flujo de Creación de Pedido

```
1. Controller recibe CreateOrderRequest
2. Service valida stock disponible para cada producto
3. Si hay stock insuficiente → lanza InsufficientStockException
4. Service captura precio actual (priceAtPurchase)
5. Service crea OrderItems con subtotales calculados
6. Service reduce stock de productos
7. Service calcula total del pedido
8. Service guarda Order (cascade a OrderItems)
9. Repository persiste en transacción atómica
10. Service mapea a OrderDTO y retorna
11. Controller retorna 201 Created
```

---

## 📊 Modelo de Datos Detallado

### Product Entity

```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer stock;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Relación con OrderItem (no se usa típicamente en la lógica)
    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

**Notas técnicas**:
- `precision = 10, scale = 2`: acepta números hasta 99,999,999.99
- `@PrePersist`: hook de JPA para setear createdAt automáticamente
- Relación con OrderItem no tiene cascade (productos existen independientemente)

### Order Entity

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String orderNumber; // ORD-20250206-0001
    
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Column(nullable = false)
    private String customerName;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    @Column(nullable = false)
    private String customerEmail;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime orderDate;
    
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
    
    // Relación bidireccional con OrderItem
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }
    
    // Helper method para mantener sincronización bidireccional
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
```

**Notas técnicas**:
- `cascade = CascadeType.ALL`: al guardar Order se guardan items automáticamente
- `orphanRemoval = true`: si se elimina un item de la lista, se borra de BD
- `@Enumerated(EnumType.STRING)`: guarda enum como texto ("PENDING") no como número
- `addItem()`: método helper para mantener consistencia bidireccional

### OrderItem Entity

```java
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Relación ManyToOne con Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    // Relación ManyToOne con Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer quantity;
    
    // Precio capturado al momento de la compra
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;
    
    // Subtotal calculado (quantity * priceAtPurchase)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    // Método para calcular subtotal
    public void calculateSubtotal() {
        this.subtotal = this.priceAtPurchase.multiply(
            BigDecimal.valueOf(this.quantity)
        );
    }
}
```

**Notas técnicas**:
- `FetchType.LAZY`: no carga order/product hasta que se acceda explícitamente
- `priceAtPurchase`: preserva el precio histórico aunque el producto cambie
- `calculateSubtotal()`: método helper, llamado desde Service antes de guardar

### OrderStatus Enum

```java
public enum OrderStatus {
    PENDING,      // Pedido creado, pendiente de confirmación
    CONFIRMED,    // Pedido confirmado por el cliente
    SHIPPED,      // Pedido enviado
    DELIVERED,    // Pedido entregado
    CANCELLED     // Pedido cancelado
}
```

**Transiciones válidas**:
- PENDING → CONFIRMED, CANCELLED
- CONFIRMED → SHIPPED, CANCELLED
- SHIPPED → DELIVERED
- DELIVERED → (estado final)
- CANCELLED → (estado final)

---

## 🔧 Capa de Servicios

### ProductService

**Responsabilidades**:
- CRUD básico de productos
- Validaciones de negocio
- Mapeo Entity ↔ DTO

**Métodos principales**:
```java
@Service
@Transactional(readOnly = true)
public class ProductService {
    
    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        // Validar datos, crear entity, guardar, mapear a DTO
    }
    
    public List<ProductDTO> getAllProducts() {
        // Obtener todos, mapear a DTOs
    }
    
    public ProductDTO getProductById(Long id) {
        // Buscar por ID, lanzar excepción si no existe, mapear
    }
    
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        // Buscar, actualizar campos, guardar, mapear
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        // Buscar, eliminar (validar que no tenga pedidos activos)
    }
}
```

### OrderService

**Responsabilidades**:
- Creación de pedidos con validación de stock
- Actualización de estados con validación de transiciones
- Cancelación de pedidos con devolución de stock
- Cálculos de totales
- Gestión transaccional

**Método createOrder (CRÍTICO)**:
```java
@Transactional
public OrderDTO createOrder(CreateOrderRequest request) {
    // 1. Crear Order entity
    Order order = new Order();
    order.setCustomerName(request.getCustomerName());
    order.setCustomerEmail(request.getCustomerEmail());
    order.setOrderNumber(generateOrderNumber());
    order.setStatus(OrderStatus.PENDING);
    
    // 2. Procesar items y validar stock
    for (CreateOrderItemRequest itemRequest : request.getItems()) {
        Product product = productRepository.findById(itemRequest.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Producto no encontrado: " + itemRequest.getProductId()
            ));
        
        // VALIDACIÓN CRÍTICA DE STOCK
        if (product.getStock() < itemRequest.getQuantity()) {
            throw new InsufficientStockException(
                "Stock insuficiente para producto: " + product.getName() + 
                ". Disponible: " + product.getStock() + 
                ", Requerido: " + itemRequest.getQuantity()
            );
        }
        
        // 3. Crear OrderItem
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(itemRequest.getQuantity());
        item.setPriceAtPurchase(product.getPrice()); // ⭐ Captura precio actual
        item.calculateSubtotal(); // quantity * priceAtPurchase
        
        // 4. Reducir stock
        product.setStock(product.getStock() - itemRequest.getQuantity());
        productRepository.save(product);
        
        // 5. Agregar item al pedido
        order.addItem(item);
    }
    
    // 6. Calcular total del pedido
    BigDecimal total = order.getItems().stream()
        .map(OrderItem::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    order.setTotal(total);
    
    // 7. Guardar pedido (cascade guarda items automáticamente)
    Order savedOrder = orderRepository.save(order);
    
    // 8. Mapear a DTO y retornar
    return mapToDTO(savedOrder);
}
```

**Método updateOrderStatus**:
```java
@Transactional
public OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
    
    // Validar transición de estado
    if (order.getStatus() == OrderStatus.CANCELLED) {
        throw new InvalidOrderStateException(
            "No se puede cambiar el estado de un pedido cancelado"
        );
    }
    
    // Validar transiciones permitidas
    validateStatusTransition(order.getStatus(), newStatus);
    
    order.setStatus(newStatus);
    Order updated = orderRepository.save(order);
    return mapToDTO(updated);
}
```

**Método cancelOrder**:
```java
@Transactional
public OrderDTO cancelOrder(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
    
    // Solo se puede cancelar si está PENDING o CONFIRMED
    if (order.getStatus() != OrderStatus.PENDING && 
        order.getStatus() != OrderStatus.CONFIRMED) {
        throw new InvalidOrderStateException(
            "No se puede cancelar un pedido en estado: " + order.getStatus()
        );
    }
    
    // DEVOLVER STOCK
    for (OrderItem item : order.getItems()) {
        Product product = item.getProduct();
        product.setStock(product.getStock() + item.getQuantity());
        productRepository.save(product);
    }
    
    // Cambiar estado a CANCELLED
    order.setStatus(OrderStatus.CANCELLED);
    Order cancelled = orderRepository.save(order);
    return mapToDTO(cancelled);
}
```

**Generación de orderNumber**:
```java
private String generateOrderNumber() {
    // Formato: ORD-YYYYMMDD-XXXX
    String dateStr = LocalDate.now().format(
        DateTimeFormatter.BASIC_ISO_DATE
    );
    
    // Contar pedidos del día
    long countToday = orderRepository.countByOrderDateBetween(
        LocalDateTime.now().with(LocalTime.MIN),
        LocalDateTime.now().with(LocalTime.MAX)
    );
    
    return String.format("ORD-%s-%04d", dateStr, countToday + 1);
}
```

---

## 📦 DTOs y Mapping

### ProductDTO
```java
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private LocalDateTime createdAt;
}
```

### CreateOrderItemRequest
```java
public class CreateOrderItemRequest {
    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;
}
```

### OrderItemDTO
```java
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName; // ⭐ Desnormalizado para facilitar frontend
    private Integer quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal subtotal;
}
```

### CreateOrderRequest
```java
public class CreateOrderRequest {
    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String customerName;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String customerEmail;
    
    @NotEmpty(message = "El pedido debe tener al menos un item")
    @Valid // ⭐ Valida cada item de la lista
    private List<CreateOrderItemRequest> items;
}
```

### OrderDTO
```java
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal total;
    private List<OrderItemDTO> items; // ⭐ Items completos con detalles
}
```

**Mapeo Entity → DTO**:
```java
private OrderDTO mapToDTO(Order order) {
    OrderDTO dto = new OrderDTO();
    dto.setId(order.getId());
    dto.setOrderNumber(order.getOrderNumber());
    dto.setCustomerName(order.getCustomerName());
    dto.setCustomerEmail(order.getCustomerEmail());
    dto.setOrderDate(order.getOrderDate());
    dto.setStatus(order.getStatus());
    dto.setTotal(order.getTotal());
    
    // Mapear items
    List<OrderItemDTO> itemDTOs = order.getItems().stream()
        .map(this::mapItemToDTO)
        .collect(Collectors.toList());
    dto.setItems(itemDTOs);
    
    return dto;
}

private OrderItemDTO mapItemToDTO(OrderItem item) {
    OrderItemDTO dto = new OrderItemDTO();
    dto.setId(item.getId());
    dto.setProductId(item.getProduct().getId());
    dto.setProductName(item.getProduct().getName()); // ⭐ Incluir nombre
    dto.setQuantity(item.getQuantity());
    dto.setPriceAtPurchase(item.getPriceAtPurchase());
    dto.setSubtotal(item.getSubtotal());
    return dto;
}
```

---

## 🚨 Manejo de Excepciones

### Excepciones Personalizadas

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}

public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}
```

### GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        ResourceNotFoundException ex
    ) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
        InsufficientStockException ex
    ) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(
        InvalidOrderStateException ex
    ) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            message,
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

### ErrorResponse DTO

```java
@Data
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private String message;
    private int status;
}
```

---

## 🎯 Puntos Clave de la Implementación

### 1. Transaccionalidad
- `@Transactional` en métodos que modifican múltiples entidades
- Si alguna operación falla, todo hace rollback automático
- Especialmente crítico en `createOrder()` y `cancelOrder()`

### 2. Lazy Loading
- Relaciones ManyToOne son LAZY por defecto
- OrderItem.product no se carga hasta acceder a `getProduct()`
- Dentro de una transacción activa, funciona transparentemente
- Fuera de transacción puede causar `LazyInitializationException`

### 3. Cascade Operations
- `CascadeType.ALL` en Order → OrderItem
- Al guardar Order, se guardan items automáticamente
- No necesitas llamar `orderItemRepository.save()` explícitamente

### 4. Preservación de Precios
- `priceAtPurchase` captura el precio histórico
- Permite cambiar precios de productos sin afectar pedidos existentes
- Esencial para reportes y auditoría

### 5. Validaciones en Capas
- **Controller**: validaciones de formato con `@Valid`
- **Service**: validaciones de negocio (stock, estados)
- **Entity**: constraints de base de datos con `@Column(nullable = false)`

### 6. DTOs vs Entities
- Nunca exponer entities directamente en API
- DTOs permiten:
  - Controlar qué datos se exponen
  - Agregar campos calculados (productName en OrderItemDTO)
  - Prevenir lazy loading issues
  - Separar modelo de dominio de API

---

## 🧪 Testing Considerations

### Unit Tests
- Mockear repositories en service tests
- Verificar lógica de cálculos (totales, subtotales)
- Validar lanzamiento de excepciones

### Integration Tests
- Verificar flujo completo de creación de pedido
- Validar reducción y devolución de stock
- Comprobar transaccionalidad (rollback en caso de error)

### Casos Edge
- Pedido con 0 items → validación debe fallar
- Pedido con producto inexistente → ResourceNotFoundException
- Pedido con cantidad negativa → validación debe fallar
- Cancelar pedido ya enviado → InvalidOrderStateException
- Stock exacto (pedir exactamente lo disponible)
- Stock simultáneo (dos pedidos concurrentes del mismo producto)

---

## 📈 Posibles Mejoras

### Funcionales
- Paginación en listados de productos y pedidos
- Búsqueda y filtrado de productos
- Historial de cambios de estado de pedido
- Sistema de descuentos/cupones
- Métodos de pago
- Direcciones de envío

### Técnicas
- Auditoría con Spring Data Envers
- Soft delete en productos
- Cache con @Cacheable en productos
- Eventos de dominio (OrderCreatedEvent)
- Versionado optimista con @Version
- Índices de base de datos para búsquedas
- API documentada con Swagger/OpenAPI

### Arquitectura
- Separar módulos (catalog, order, customer)
- Event-driven con mensajería (Kafka/RabbitMQ)
- CQRS para separar escrituras de lecturas
- Saga pattern para transacciones distribuidas

---

## 📚 Conceptos Aprendidos

1. **Relaciones JPA**: OneToMany bidireccionales con entidad intermedia
2. **Cascade y Orphan Removal**: gestión automática de ciclo de vida
3. **Transacciones**: atomicidad en operaciones complejas
4. **Validaciones**: Bean Validation con @Valid
5. **Manejo de excepciones**: @RestControllerAdvice centralizado
6. **DTOs**: separación de capas y control de serialización
7. **Cálculos de negocio**: totales, subtotales, stock
8. **Estados y transiciones**: máquina de estados simple
9. **Preservación de datos históricos**: priceAtPurchase

---

**Nivel completado**: ⭐⭐⭐ Intermedio  
**Próximo paso**: Autenticación y autorización con Spring Security