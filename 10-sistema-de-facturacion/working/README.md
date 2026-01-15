# Proyecto 10 - Sistema de Facturación (Working)

## 🎯 Guía de Inicio

Bienvenido al **Proyecto 10** del curso de Spring Boot. En este proyecto implementarás un sistema completo de facturación con múltiples DTOs especializados, cálculos automáticos de impuestos y **tu primera suite completa de tests**.

## 📋 Checklist de Desarrollo

### Fase 1: Estructuras Base
- [ ] `EstadoFactura.java` (enum: PENDIENTE, PAGADA, CANCELADA, VENCIDA)
- [ ] `MetodoPago.java` (enum: EFECTIVO, TRANSFERENCIA, TARJETA, CHEQUE)
- [ ] `Invoice.java` (entidad con todas las validaciones)

### Fase 2: DTOs
- [ ] `ItemDTO.java` (DTO anidado)
- [ ] `InvoiceCreateDTO.java` (con List<ItemDTO>)
- [ ] `InvoiceResponseDTO.java` (respuesta estándar)
- [ ] `InvoiceDetailDTO.java` (vista completa con items parseados)
- [ ] `InvoiceSummaryDTO.java` (para listados)
- [ ] `InvoicePaymentDTO.java` (para operación de pago)
- [ ] `InvoiceReportDTO.java` (para reportes mensuales)

### Fase 3: Persistencia
- [ ] `InvoiceRepository.java` (con queries personalizadas)

### Fase 4: Excepciones
- [ ] `InvoiceNotFoundException.java`
- [ ] `InvalidInvoiceStateException.java`
- [ ] `GlobalExceptionHandler.java`

### Fase 5: Lógica de Negocio
- [ ] `InvoiceService.java` (cálculos + conversiones + validaciones)

### Fase 6: API REST
- [ ] `InvoiceController.java` (todos los endpoints)

### Fase 7: Testing (¡Primera vez!)
- [ ] `InvoiceServiceTest.java` (tests unitarios con Mockito)
- [ ] `InvoiceControllerTest.java` (tests de integración con MockMvc)

## 🌐 Endpoints Esperados

### CRUD Básico
| Método | Endpoint | Body | Respuesta | Descripción |
|--------|----------|------|-----------|-------------|
| POST | `/api/invoices` | InvoiceCreateDTO | InvoiceResponseDTO | Crear factura |
| GET | `/api/invoices` | - | List<InvoiceSummaryDTO> | Listar todas |
| GET | `/api/invoices/{id}` | - | InvoiceDetailDTO | Obtener detalle |
| PUT | `/api/invoices/{id}` | InvoiceCreateDTO | InvoiceResponseDTO | Actualizar |
| DELETE | `/api/invoices/{id}` | - | 204 No Content | Eliminar |

### Consultas Especializadas
| Método | Endpoint | Params | Respuesta | Descripción |
|--------|----------|--------|-----------|-------------|
| GET | `/api/invoices/numero/{numero}` | - | InvoiceDetailDTO | Por número de factura |
| GET | `/api/invoices/vencidas` | - | List<InvoiceSummaryDTO> | Facturas vencidas |
| GET | `/api/invoices/cliente/{cliente}` | - | List<InvoiceSummaryDTO> | Por cliente |
| GET | `/api/invoices/estado/{estado}` | - | List<InvoiceSummaryDTO> | Por estado |
| GET | `/api/invoices/fecha-rango` | inicio, fin | List<InvoiceSummaryDTO> | Por rango fechas |
| GET | `/api/invoices/metodo-pago/{metodo}` | - | List<InvoiceSummaryDTO> | Por método pago |

### Operaciones de Negocio
| Método | Endpoint | Body | Respuesta | Descripción |
|--------|----------|------|-----------|-------------|
| PATCH | `/api/invoices/{id}/pay` | InvoicePaymentDTO | InvoiceResponseDTO | Marcar como pagada |
| PATCH | `/api/invoices/{id}/cancel` | - | InvoiceResponseDTO | Cancelar factura |

### Reportes
| Método | Endpoint | Params | Respuesta | Descripción |
|--------|----------|--------|-----------|-------------|
| GET | `/api/invoices/reporte/mensual/{anio}/{mes}` | - | InvoiceReportDTO | Reporte mensual |
| GET | `/api/invoices/totales` | - | Map<String, Object> | Totales generales |

## 📝 Estructura de DTOs

### InvoiceCreateDTO (Request - Crear/Actualizar)
```json
{
  "cliente": "Acme Corporation S.A. de C.V.",
  "rfc": "ACM950103ABC",
  "direccion": "Av. Revolución 1234, Col. Centro, CDMX",
  "fechaEmision": "2024-01-15",
  "fechaVencimiento": "2024-02-15",
  "concepto": "Servicios de consultoría tecnológica",
  "descuento": 500.00,
  "metodoPago": "TRANSFERENCIA",
  "notas": "Pago a 30 días",
  "items": [
    {
      "descripcion": "Desarrollo de API REST",
      "cantidad": 40,
      "precioUnitario": 800.00
    },
    {
      "descripcion": "Consultoría arquitectura",
      "cantidad": 20,
      "precioUnitario": 1200.00
    }
  ]
}
```

**Cálculos automáticos**:
- subtotal = (40 × 800) + (20 × 1200) = 32,000 + 24,000 = 56,000.00
- iva = 56,000 × 0.16 = 8,960.00
- total = 56,000 + 8,960 - 500 = 64,460.00

### InvoiceResponseDTO (Response - CRUD básico)
```json
{
  "id": 1,
  "numeroFactura": "FACT-2024-0001",
  "cliente": "Acme Corporation S.A. de C.V.",
  "rfc": "ACM950103ABC",
  "fechaEmision": "2024-01-15",
  "fechaVencimiento": "2024-02-15",
  "subtotal": 56000.00,
  "iva": 8960.00,
  "descuento": 500.00,
  "total": 64460.00,
  "estado": "PENDIENTE",
  "metodoPago": "TRANSFERENCIA"
}
```

### InvoiceDetailDTO (Response - Detalle completo)
```json
{
  "id": 1,
  "numeroFactura": "FACT-2024-0001",
  "cliente": "Acme Corporation S.A. de C.V.",
  "rfc": "ACM950103ABC",
  "direccion": "Av. Revolución 1234, Col. Centro, CDMX",
  "fechaEmision": "2024-01-15",
  "fechaVencimiento": "2024-02-15",
  "concepto": "Servicios de consultoría tecnológica",
  "subtotal": 56000.00,
  "iva": 8960.00,
  "descuento": 500.00,
  "total": 64460.00,
  "estado": "PENDIENTE",
  "metodoPago": "TRANSFERENCIA",
  "notas": "Pago a 30 días",
  "items": [
    {
      "descripcion": "Desarrollo de API REST",
      "cantidad": 40,
      "precioUnitario": 800.00,
      "importe": 32000.00
    },
    {
      "descripcion": "Consultoría arquitectura",
      "cantidad": 20,
      "precioUnitario": 1200.00,
      "importe": 24000.00
    }
  ],
  "diasVencimiento": 31,
  "estadoVencimiento": "Al corriente"
}
```

### InvoiceSummaryDTO (Response - Listados)
```json
{
  "id": 1,
  "numeroFactura": "FACT-2024-0001",
  "cliente": "Acme Corporation S.A. de C.V.",
  "fechaEmision": "2024-01-15",
  "total": 64460.00,
  "estado": "PENDIENTE",
  "diasParaVencimiento": 31
}
```

### InvoicePaymentDTO (Request - Marcar como pagada)
```json
{
  "metodoPago": "TRANSFERENCIA",
  "fechaPago": "2024-01-20",
  "notas": "Pagado mediante transferencia bancaria. Ref: TRF123456"
}
```

### InvoiceReportDTO (Response - Reporte mensual)
```json
{
  "periodo": "2024-01",
  "totalFacturado": 325000.00,
  "totalCobrado": 180000.00,
  "totalPendiente": 145000.00,
  "cantidadFacturas": 15,
  "cantidadPagadas": 8,
  "cantidadPendientes": 6,
  "cantidadVencidas": 1,
  "promedioTicket": 21666.67
}
```

## 🔗 Colección Postman

Descarga la colección completa: [postman/Proyecto10-Facturacion.postman_collection.json](postman/Proyecto10-Facturacion.postman_collection.json)

### Ejemplos de Requests

#### 1. Crear Factura (POST /api/invoices)
**Body**: Ver InvoiceCreateDTO arriba  
**Status esperado**: 201 Created  
**Response**: InvoiceResponseDTO

#### 2. Obtener Todas (GET /api/invoices)
**Status esperado**: 200 OK  
**Response**: Array de InvoiceSummaryDTO

#### 3. Obtener Detalle (GET /api/invoices/1)
**Status esperado**: 200 OK  
**Response**: InvoiceDetailDTO con items parseados

#### 4. Actualizar (PUT /api/invoices/1)
**Body**: InvoiceCreateDTO (misma estructura que crear)  
**Status esperado**: 200 OK  
**Response**: InvoiceResponseDTO

**Validación**: No debe permitir actualizar si estado = PAGADA o CANCELADA

#### 5. Marcar como Pagada (PATCH /api/invoices/1/pay)
**Body**:
```json
{
  "metodoPago": "TARJETA",
  "fechaPago": "2024-01-18",
  "notas": "Pago con tarjeta terminación 4567"
}
```
**Status esperado**: 200 OK  
**Response**: InvoiceResponseDTO con estado = PAGADA

#### 6. Cancelar Factura (PATCH /api/invoices/1/cancel)
**Status esperado**: 200 OK  
**Response**: InvoiceResponseDTO con estado = CANCELADA

**Validación**: No debe permitir cancelar si estado = PAGADA

#### 7. Buscar Vencidas (GET /api/invoices/vencidas)
**Status esperado**: 200 OK  
**Response**: Array de InvoiceSummaryDTO donde estado = VENCIDA

#### 8. Reporte Mensual (GET /api/invoices/reporte/mensual/2024/1)
**Status esperado**: 200 OK  
**Response**: InvoiceReportDTO con estadísticas del mes

#### 9. Totales Generales (GET /api/invoices/totales)
**Status esperado**: 200 OK  
**Response**:
```json
{
  "totalFacturas": 15,
  "totalPendientes": 6,
  "totalPagadas": 8,
  "totalCanceladas": 0,
  "totalVencidas": 1,
  "montoTotal": 325000.00,
  "montoCobrado": 180000.00,
  "montoPendiente": 145000.00
}
```

## ⚠️ Casos de Error Esperados

### 400 Bad Request
- RFC inválido (no cumple patrón mexicano)
- fechaEmision futura
- fechaVencimiento anterior a fechaEmision
- descuento negativo o mayor que subtotal
- items vacío
- cantidad o precioUnitario <= 0

### 404 Not Found
- ID de factura inexistente
- Número de factura inexistente

### 409 Conflict
- Intentar actualizar factura PAGADA o CANCELADA
- Intentar cancelar factura PAGADA
- Intentar pagar factura CANCELADA
- Número de factura duplicado (si se permite especificar manualmente)

## 🧪 Testing - Guía de Implementación

### InvoiceServiceTest.java

**Setup**:
```java
@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
    @Mock
    private InvoiceRepository repository;
    
    @InjectMocks
    private InvoiceService service;
}
```

**Tests recomendados** (15-20 tests):
1. `crearFactura_CalculosCorrectos()` - Verificar subtotal, IVA, total
2. `crearFactura_GeneraNumeroAutomatico()` - Verificar formato FACT-YYYY-XXXX
3. `crearFactura_RfcInvalido_LanzaExcepcion()` - Validación RFC
4. `crearFactura_FechaEmisionFutura_LanzaExcepcion()`
5. `crearFactura_FechaVencimientoAnterior_LanzaExcepcion()`
6. `crearFactura_DescuentoNegativo_LanzaExcepcion()`
7. `crearFactura_ItemsSinCantidad_LanzaExcepcion()`
8. `marcarComoPagada_EstadoPendiente_Exitoso()`
9. `marcarComoPagada_EstadoCancelada_LanzaExcepcion()`
10. `cancelarFactura_EstadoPendiente_Exitoso()`
11. `cancelarFactura_EstadoPagada_LanzaExcepcion()`
12. `buscarVencidas_RetornaFacturasVencidas()`
13. `reporteMensual_CalculaEstadisticasCorrectas()`
14. `convertirItemsAJson_YViceversa()` - Test serialización
15. `actualizarFactura_EstadoPagada_LanzaExcepcion()`

### InvoiceControllerTest.java

**Setup**:
```java
@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private InvoiceService service;
}
```

**Tests recomendados** (12-15 tests):
1. `crearFactura_DatosValidos_Retorna201()` - POST exitoso
2. `crearFactura_DatosInvalidos_Retorna400()` - Validaciones
3. `obtenerTodas_RetornaListaFacturas_200()`
4. `obtenerPorId_Existe_Retorna200()`
5. `obtenerPorId_NoExiste_Retorna404()`
6. `actualizarFactura_Exitoso_Retorna200()`
7. `actualizarFactura_NoExiste_Retorna404()`
8. `eliminarFactura_Exitoso_Retorna204()`
9. `marcarComoPagada_Exitoso_Retorna200()`
10. `marcarComoPagada_EstadoInvalido_Retorna409()`
11. `cancelarFactura_Exitoso_Retorna200()`
12. `obtenerVencidas_RetornaListado_200()`
13. `reporteMensual_RetornaReporte_200()`

## 📦 Dependencias Maven

```xml
<dependencies>
    <!-- Spring Boot Starters -->
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
    
    <!-- Database -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## 🎓 Conceptos Clave a Practicar

1. **DTOs Especializados**: Cada operación tiene su DTO optimizado
2. **DTOs Anidados**: ItemDTO dentro de InvoiceCreateDTO
3. **JSON Embebido**: Serializar/deserializar List<ItemDTO> a String
4. **Cálculos Automáticos**: Lógica de negocio para subtotal, IVA, total
5. **Validaciones Personalizadas**: RFC, fechas, montos
6. **Testing con Mockito**: Mock del repository en tests unitarios
7. **Testing con MockMvc**: Tests de integración de endpoints REST
8. **Manejo de Estados**: Validaciones de transiciones de estado
9. **Queries Personalizadas**: Consultas JPA especializadas
10. **Manejo de Excepciones**: Custom exceptions y GlobalExceptionHandler

---

¡Comienza con los **Enums** y luego la **Entidad**! Iré revisando cada clase que desarrolles 🚀