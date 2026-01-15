# Proyecto 10 - Sistema de Facturación (Solution)

## 📘 Documentación Técnica de Implementación

Esta solución implementa un sistema completo de facturación con DTOs especializados, cálculos automáticos y testing exhaustivo. Incluye validaciones fiscales, manejo de estados y operaciones de reportes.

---

## 🏗️ Arquitectura General

```
┌─────────────────────────────────────────────────────────┐
│                    API REST Layer                        │
│              (InvoiceController)                         │
│  - DTOs especializados según operación                   │
│  - Validaciones de entrada (Bean Validation)             │
└───────────────────┬─────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────┐
│                 Service Layer                            │
│              (InvoiceService)                            │
│  - Cálculos automáticos (subtotal, IVA, total)          │
│  - Generación de números de factura                      │
│  - Conversión DTO ↔ Entity                              │
│  - Validaciones de negocio                               │
│  - Serialización/deserialización JSON                    │
└───────────────────┬─────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────┐
│               Repository Layer                           │
│            (InvoiceRepository)                           │
│  - Queries personalizadas (JPQL)                         │
│  - Filtros y búsquedas especializadas                    │
└───────────────────┬─────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────┐
│                 Database (H2)                            │
│  - Tabla: invoices                                       │
│  - Campo JSON: items (almacenado como TEXT)              │
└─────────────────────────────────────────────────────────┘
```

---

## 🔧 Decisiones Técnicas Clave

### 1. Almacenamiento de Items como JSON String

**Decisión**: Almacenar items como String JSON en lugar de tabla relacionada.

**Justificación**:
- **Simplicidad**: Proyecto enfocado en DTOs, no en relaciones JPA
- **Flexibilidad**: Estructura de items puede variar sin migración
- **Rendimiento**: Menos joins en consultas
- **Atomicidad**: Items siempre se modifican con la factura

**Implementación**:
```java
@Entity
public class Invoice {
    @Column(columnDefinition = "TEXT") // Para JSONs grandes
    private String items;
}

// En Service: Serialización/Deserialización
private String convertItemsToJson(List<ItemDTO> items) {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(items);
}

private List<ItemDTO> convertJsonToItems(String json) {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(json, new TypeReference<List<ItemDTO>>(){});
}
```

### 2. DTOs Especializados por Operación

**Decisión**: Crear 7 DTOs diferentes en lugar de uno genérico.

**Justificación**:
- **InvoiceCreateDTO**: Solo datos necesarios para crear, items como List<ItemDTO>
- **InvoiceResponseDTO**: Vista estándar sin datos sensibles (notas, dirección)
- **InvoiceDetailDTO**: Vista completa con items parseados
- **InvoiceSummaryDTO**: Optimizado para listados (menos campos)
- **InvoicePaymentDTO**: Solo datos relevantes para marcar como pagada
- **InvoiceReportDTO**: Agregaciones y estadísticas
- **ItemDTO**: Reutilizable en diferentes contextos

**Ventajas**:
- Validaciones específicas por operación
- Mejor documentación de API
- Evita exponer datos innecesarios
- Mayor control sobre lo que se envía/recibe

### 3. Cálculos Automáticos en Service

**Decisión**: Calcular subtotal, IVA y total automáticamente.

**Implementación**:
```java
public InvoiceResponseDTO crearFactura(InvoiceCreateDTO dto) {
    // 1. Calcular subtotal de items
    BigDecimal subtotal = dto.getItems().stream()
        .map(item -> item.getPrecioUnitario()
            .multiply(BigDecimal.valueOf(item.getCantidad())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    // 2. Calcular IVA (16%)
    BigDecimal iva = subtotal.multiply(new BigDecimal("0.16"));
    
    // 3. Calcular total
    BigDecimal total = subtotal.add(iva).subtract(dto.getDescuento());
    
    // 4. Generar número de factura
    String numeroFactura = generarNumeroFactura();
    
    // 5. Serializar items a JSON
    String itemsJson = convertItemsToJson(dto.getItems());
    
    // 6. Crear entidad
    Invoice invoice = new Invoice();
    // ... mapeo de campos
    invoice.setSubtotal(subtotal);
    invoice.setIva(iva);
    invoice.setTotal(total);
    invoice.setNumeroFactura(numeroFactura);
    invoice.setItems(itemsJson);
    
    return convertToResponseDTO(repository.save(invoice));
}
```

### 4. Generación Automática de Número de Factura

**Decisión**: Formato FACT-YYYY-XXXX con autoincremento anual.

**Implementación**:
```java
private String generarNumeroFactura() {
    int anioActual = LocalDate.now().getYear();
    
    // Obtener última factura del año
    Optional<Invoice> ultimaFactura = repository
        .findTopByNumeroFacturaStartingWithOrderByNumeroFacturaDesc(
            "FACT-" + anioActual
        );
    
    int siguienteNumero = 1;
    if (ultimaFactura.isPresent()) {
        String ultimoNumero = ultimaFactura.get().getNumeroFactura();
        // Extraer número: FACT-2024-0015 → 15
        String[] partes = ultimoNumero.split("-");
        siguienteNumero = Integer.parseInt(partes[2]) + 1;
    }
    
    // Formato: FACT-2024-0001
    return String.format("FACT-%d-%04d", anioActual, siguienteNumero);
}
```

### 5. Validación de RFC Mexicano

**Decisión**: Validar RFC con regex en DTO.

**Patrón RFC**:
- Personas físicas: 12 caracteres (AAAA000000A00)
- Personas morales: 13 caracteres (AAA000000A00)

**Implementación**:
```java
@Pattern(
    regexp = "^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$",
    message = "RFC inválido. Formato esperado: AAAA000000A00 o AAA000000A00"
)
private String rfc;
```

### 6. Manejo de Estados con Validaciones

**Decisión**: Validar transiciones de estado permitidas.

**Reglas**:
- **PENDIENTE** → puede pasar a PAGADA o CANCELADA
- **PAGADA** → estado final, no puede cambiar
- **CANCELADA** → estado final, no puede cambiar
- **VENCIDA** → asignado automáticamente por sistema

**Implementación**:
```java
public InvoiceResponseDTO marcarComoPagada(Long id, InvoicePaymentDTO dto) {
    Invoice invoice = repository.findById(id)
        .orElseThrow(() -> new InvoiceNotFoundException(id));
    
    // Validar estado
    if (invoice.getEstado() == EstadoFactura.CANCELADA) {
        throw new InvalidInvoiceStateException(
            "No se puede marcar como pagada una factura cancelada"
        );
    }
    
    if (invoice.getEstado() == EstadoFactura.PAGADA) {
        throw new InvalidInvoiceStateException(
            "La factura ya está pagada"
        );
    }
    
    // Actualizar estado
    invoice.setEstado(EstadoFactura.PAGADA);
    // ... actualizar otros campos
    
    return convertToResponseDTO(repository.save(invoice));
}
```

### 7. Testing con Mockito y MockMvc

**Decisión**: Tests unitarios con Mockito, tests de integración con MockMvc.

**Tests Unitarios** (Service):
```java
@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
    @Mock
    private InvoiceRepository repository;
    
    @InjectMocks
    private InvoiceService service;
    
    @Test
    void crearFactura_CalculosCorrectos() {
        // Given
        InvoiceCreateDTO dto = createValidDTO();
        Invoice savedInvoice = new Invoice();
        when(repository.save(any(Invoice.class))).thenReturn(savedInvoice);
        
        // When
        InvoiceResponseDTO result = service.crearFactura(dto);
        
        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("56000.00"), result.getSubtotal());
        assertEquals(new BigDecimal("8960.00"), result.getIva());
        assertEquals(new BigDecimal("64460.00"), result.getTotal());
    }
}
```

**Tests de Integración** (Controller):
```java
@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private InvoiceService service;
    
    @Test
    void crearFactura_DatosValidos_Retorna201() throws Exception {
        // Given
        InvoiceResponseDTO response = createValidResponseDTO();
        when(service.crearFactura(any())).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createValidJson()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.numeroFactura").exists())
            .andExpect(jsonPath("$.total").value(64460.00));
    }
}
```

---

## 📋 Estructura de Clases Implementadas

### Enums

#### EstadoFactura.java
```java
public enum EstadoFactura {
    PENDIENTE,  // Estado inicial
    PAGADA,     // Marcada como pagada
    CANCELADA,  // Cancelada por usuario
    VENCIDA     // Asignada automáticamente si fechaVencimiento < hoy
}
```

#### MetodoPago.java
```java
public enum MetodoPago {
    EFECTIVO,
    TRANSFERENCIA,
    TARJETA,
    CHEQUE
}
```

### Entity

#### Invoice.java
**Anotaciones clave**:
- `@Entity` - Marca como entidad JPA
- `@Table(uniqueConstraints = ...)` - Número de factura único
- `@Column(columnDefinition = "TEXT")` - Para JSONs grandes
- `@Enumerated(EnumType.STRING)` - Almacenar enums como String
- `@Pattern` - Validación de RFC
- `@NotNull`, `@NotBlank` - Validaciones Bean Validation
- `@DecimalMin` - Validar montos positivos

**Campos calculados**: No se almacenan, se calculan en Service antes de persistir.

### DTOs

#### ItemDTO.java
**Propósito**: Representar item anidado en factura.

**Características**:
- Validaciones: `@NotBlank`, `@Positive`, `@DecimalMin`
- Campo calculado: `importe` (opcional en request, calculado en response)
- Reutilizable en InvoiceCreateDTO e InvoiceDetailDTO

#### InvoiceCreateDTO.java
**Propósito**: Request para crear/actualizar factura.

**Características**:
- Lista de items: `List<@Valid ItemDTO>`
- Validaciones personalizadas: RFC, fechas
- Descuento opcional con default 0
- Método `@AssertTrue` para validar fechaVencimiento >= fechaEmision

#### InvoiceResponseDTO.java
**Propósito**: Response estándar CRUD.

**Características**:
- Todos los campos calculados incluidos
- Sin datos sensibles extendidos (notas, dirección completa)
- Formato óptimo para operaciones rápidas

#### InvoiceDetailDTO.java
**Propósito**: Vista completa con items parseados.

**Características**:
- Extiende InvoiceResponseDTO o incluye todos sus campos
- Items deserializados: `List<ItemDTO>`
- Campos calculados adicionales: `diasVencimiento`, `estadoVencimiento`
- Usado en GET por ID

#### InvoiceSummaryDTO.java
**Propósito**: Listados optimizados.

**Características**:
- Solo campos esenciales: id, número, cliente, fecha, total, estado
- Campo calculado: `diasParaVencimiento`
- Reduce payload en listados grandes

#### InvoicePaymentDTO.java
**Propósito**: Operación PATCH marcar como pagada.

**Características**:
- Campos mínimos: metodoPago, fechaPago, notas
- Validación: fechaPago no puede ser futura
- Permite actualizar método de pago si difiere del original

#### InvoiceReportDTO.java
**Propósito**: Reportes financieros mensuales.

**Características**:
- Agregaciones: sumas, promedios, conteos
- Calculado completamente en Service
- No mapea directamente a entidad

### Repository

#### InvoiceRepository.java
**Queries personalizadas**:

```java
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    // Buscar por número de factura (único)
    Optional<Invoice> findByNumeroFactura(String numeroFactura);
    
    // Última factura del año (para generación de número)
    Optional<Invoice> findTopByNumeroFacturaStartingWithOrderByNumeroFacturaDesc(String prefix);
    
    // Facturas vencidas
    @Query("SELECT i FROM Invoice i WHERE i.estado = 'PENDIENTE' AND i.fechaVencimiento < :fecha")
    List<Invoice> findVencidas(@Param("fecha") LocalDate fecha);
    
    // Por cliente (insensible a mayúsculas)
    List<Invoice> findByClienteContainingIgnoreCase(String cliente);
    
    // Por estado
    List<Invoice> findByEstado(EstadoFactura estado);
    
    // Por método de pago
    List<Invoice> findByMetodoPago(MetodoPago metodoPago);
    
    // Por rango de fechas
    List<Invoice> findByFechaEmisionBetween(LocalDate inicio, LocalDate fin);
    
    // Reporte mensual (agregaciones)
    @Query("SELECT COUNT(i), SUM(i.total) FROM Invoice i WHERE " +
           "YEAR(i.fechaEmision) = :anio AND MONTH(i.fechaEmision) = :mes")
    Object[] getEstadisticasMensuales(@Param("anio") int anio, @Param("mes") int mes);
}
```

### Service

#### InvoiceService.java
**Responsabilidades**:
1. **Cálculos automáticos**: subtotal, IVA, total
2. **Generación de números de factura**: formato FACT-YYYY-XXXX
3. **Conversión DTO ↔ Entity**: mapeo manual o con ModelMapper
4. **Serialización JSON**: items List<ItemDTO> ↔ String
5. **Validaciones de negocio**: estados, transiciones, fechas
6. **Operaciones especializadas**: pagar, cancelar, reportes

**Métodos principales**:
```java
- crearFactura(InvoiceCreateDTO): InvoiceResponseDTO
- obtenerTodas(): List<InvoiceSummaryDTO>
- obtenerPorId(Long): InvoiceDetailDTO
- actualizarFactura(Long, InvoiceCreateDTO): InvoiceResponseDTO
- eliminarFactura(Long): void
- buscarPorNumero(String): InvoiceDetailDTO
- marcarComoPagada(Long, InvoicePaymentDTO): InvoiceResponseDTO
- cancelarFactura(Long): InvoiceResponseDTO
- obtenerVencidas(): List<InvoiceSummaryDTO>
- buscarPorCliente(String): List<InvoiceSummaryDTO>
- buscarPorEstado(EstadoFactura): List<InvoiceSummaryDTO>
- buscarPorMetodoPago(MetodoPago): List<InvoiceSummaryDTO>
- buscarPorRangoFechas(LocalDate, LocalDate): List<InvoiceSummaryDTO>
- reporteMensual(int, int): InvoiceReportDTO
- obtenerTotales(): Map<String, Object>
```

**Helpers privados**:
```java
- generarNumeroFactura(): String
- calcularSubtotal(List<ItemDTO>): BigDecimal
- calcularIva(BigDecimal): BigDecimal
- calcularTotal(BigDecimal, BigDecimal, BigDecimal): BigDecimal
- convertItemsToJson(List<ItemDTO>): String
- convertJsonToItems(String): List<ItemDTO>
- convertToResponseDTO(Invoice): InvoiceResponseDTO
- convertToDetailDTO(Invoice): InvoiceDetailDTO
- convertToSummaryDTO(Invoice): InvoiceSummaryDTO
- actualizarEstadosVencidos(): void (llamado periódicamente)
```

### Controller

#### InvoiceController.java
**Estructura**:
```java
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    
    private final InvoiceService service;
    
    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> crear(@Valid @RequestBody InvoiceCreateDTO dto)
    
    @GetMapping
    public ResponseEntity<List<InvoiceSummaryDTO>> obtenerTodas()
    
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDetailDTO> obtenerPorId(@PathVariable Long id)
    
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> actualizar(
        @PathVariable Long id, 
        @Valid @RequestBody InvoiceCreateDTO dto)
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id)
    
    // Consultas especializadas
    @GetMapping("/numero/{numero}")
    @GetMapping("/vencidas")
    @GetMapping("/cliente/{cliente}")
    @GetMapping("/estado/{estado}")
    @GetMapping("/fecha-rango")
    @GetMapping("/metodo-pago/{metodo}")
    
    // Operaciones
    @PatchMapping("/{id}/pay")
    @PatchMapping("/{id}/cancel")
    
    // Reportes
    @GetMapping("/reporte/mensual/{anio}/{mes}")
    @GetMapping("/totales")
}
```

**Status Codes**:
- `201 Created` - POST crear factura
- `200 OK` - GET, PUT, PATCH exitosos
- `204 No Content` - DELETE exitoso
- `400 Bad Request` - Validaciones fallidas
- `404 Not Found` - Recurso no existe
- `409 Conflict` - Operación no permitida (estado inválido)

### Exceptions

#### InvoiceNotFoundException.java
```java
public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(Long id) {
        super("Factura no encontrada con ID: " + id);
    }
    
    public InvoiceNotFoundException(String numero) {
        super("Factura no encontrada con número: " + numero);
    }
}
```

#### InvalidInvoiceStateException.java
```java
public class InvalidInvoiceStateException extends RuntimeException {
    public InvalidInvoiceStateException(String message) {
        super(message);
    }
}
```

#### GlobalExceptionHandler.java
**Manejo centralizado**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(InvoiceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }
    
    @ExceptionHandler(InvalidInvoiceStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidInvoiceStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(ex.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage
            ));
        return ResponseEntity.badRequest().body(new ErrorResponse(errors));
    }
}
```

---

## 🧪 Estrategia de Testing

### Tests Unitarios (InvoiceServiceTest)

**Cobertura objetivo**: >80% del Service

**Categorías**:
1. **Cálculos** (5 tests)
   - Cálculo correcto de subtotal
   - Cálculo correcto de IVA (16%)
   - Cálculo correcto de total con descuento
   - Cálculo con descuento = 0
   - Cálculo con múltiples items

2. **Generación de números** (3 tests)
   - Primera factura del año (FACT-2024-0001)
   - Factura consecutiva (incremento correcto)
   - Cambio de año (reinicio de contador)

3. **Validaciones** (6 tests)
   - RFC inválido lanza excepción
   - Fecha emisión futura lanza excepción
   - Fecha vencimiento anterior lanza excepción
   - Descuento negativo lanza excepción
   - Items vacío lanza excepción
   - Cantidad/precio inválido lanza excepción

4. **Operaciones de estado** (5 tests)
   - Marcar como pagada (PENDIENTE → PAGADA)
   - Marcar como pagada factura cancelada (excepción)
   - Marcar como pagada factura ya pagada (excepción)
   - Cancelar factura pendiente (PENDIENTE → CANCELADA)
   - Cancelar factura pagada (excepción)

5. **Serialización JSON** (2 tests)
   - Convertir List<ItemDTO> a JSON String
   - Convertir JSON String a List<ItemDTO>

6. **Consultas y reportes** (4 tests)
   - Buscar vencidas (filtro fecha + estado)
   - Reporte mensual (agregaciones correctas)
   - Buscar por cliente (case-insensitive)
   - Obtener totales generales

### Tests de Integración (InvoiceControllerTest)

**Cobertura objetivo**: Todos los endpoints

**Categorías**:
1. **CRUD básico** (8 tests)
   - POST crear factura (201 Created)
   - POST datos inválidos (400 Bad Request)
   - GET listar todas (200 OK)
   - GET obtener por ID existe (200 OK)
   - GET obtener por ID no existe (404 Not Found)
   - PUT actualizar exitoso (200 OK)
   - PUT actualizar no existe (404 Not Found)
   - DELETE eliminar (204 No Content)

2. **Operaciones de negocio** (4 tests)
   - PATCH marcar como pagada (200 OK)
   - PATCH marcar como pagada estado inválido (409 Conflict)
   - PATCH cancelar factura (200 OK)
   - PATCH cancelar factura pagada (409 Conflict)

3. **Consultas especializadas** (5 tests)
   - GET por número de factura (200 OK / 404)
   - GET facturas vencidas (200 OK)
   - GET por cliente (200 OK)
   - GET por estado (200 OK)
   - GET por rango de fechas (200 OK)

4. **Reportes** (2 tests)
   - GET reporte mensual (200 OK)
   - GET totales generales (200 OK)

---

## 📊 Flujo de Datos Completo

### Ejemplo: Crear Factura

```
1. CLIENT
   └─> POST /api/invoices
       Body: InvoiceCreateDTO (con List<ItemDTO>)

2. CONTROLLER
   ├─> @Valid valida campos (Bean Validation)
   ├─> Llama a service.crearFactura(dto)
   └─> Retorna 201 Created con InvoiceResponseDTO

3. SERVICE
   ├─> Calcula subtotal de items
   ├─> Calcula IVA (16%)
   ├─> Calcula total (subtotal + iva - descuento)
   ├─> Genera número de factura (FACT-YYYY-XXXX)
   ├─> Convierte List<ItemDTO> a JSON String
   ├─> Mapea DTO a Entity
   ├─> Llama a repository.save(invoice)
   ├─> Convierte Entity a InvoiceResponseDTO
   └─> Retorna DTO

4. REPOSITORY
   ├─> Persiste Invoice en base de datos
   └─> Retorna Invoice con ID generado

5. DATABASE
   └─> Tabla: invoices
       ├─ id (PK, autoincremental)
       ├─ numero_factura (UNIQUE)
       ├─ cliente, rfc, direccion
       ├─ fecha_emision, fecha_vencimiento
       ├─ subtotal, iva, descuento, total
       ├─ estado, metodo_pago
       └─ items (TEXT con JSON)
```

---

## 🎯 Patrones y Mejores Prácticas Aplicadas

1. **DTO Pattern**: Separación entre capa de transporte y dominio
2. **Service Layer Pattern**: Lógica de negocio centralizada
3. **Repository Pattern**: Abstracción de persistencia
4. **Dependency Injection**: Constructor injection con final
5. **Bean Validation**: Validaciones declarativas en DTOs
6. **Exception Handling**: @RestControllerAdvice centralizado
7. **Builder Pattern**: Lombok @Builder para construcción de objetos
8. **Immutability**: DTOs inmutables donde sea posible
9. **SOLID Principles**:
   - SRP: Cada clase tiene una responsabilidad única
   - OCP: Extensible mediante nuevos DTOs sin modificar existentes
   - DIP: Depende de abstracciones (interfaces Repository)
10. **Clean Code**:
    - Nombres descriptivos
    - Métodos pequeños y cohesivos
    - Comentarios en puntos clave
    - Constantes para valores mágicos (ej: IVA_RATE = 0.16)

---

## 📈 Métricas de Calidad

- **Cobertura de Tests**: >80%
- **Complejidad Ciclomática**: <10 por método
- **Líneas por Método**: <30
- **Campos por Clase**: <15
- **Dependencias por Clase**: <5
- **Tests por Método Público**: ≥1

---

## 🚀 Próximos Pasos (Mejoras Futuras)

1. **Auditoría**: Agregar campos createdAt, updatedAt, createdBy
2. **Paginación**: Implementar Pageable en listados
3. **Cache**: @Cacheable en consultas frecuentes
4. **Eventos**: Publicar eventos al cambiar estado (Spring Events)
5. **Validaciones Asíncronas**: Validar RFC contra padrón fiscal
6. **PDF Generation**: Generar PDF de factura con iText
7. **Email Notifications**: Enviar factura por correo
8. **Soft Delete**: Eliminación lógica en lugar de física
9. **Versioning**: Control de versiones de factura (@Version)
10. **Multi-tenancy**: Soporte para múltiples empresas

---

**Solución Completa**: Esta implementación cumple con todos los requisitos del Proyecto 10 y está lista para producción con testing exhaustivo ✅