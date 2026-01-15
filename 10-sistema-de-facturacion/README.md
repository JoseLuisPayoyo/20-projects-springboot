# Proyecto 10 - Sistema de Facturación

## 📋 Descripción del Proyecto

Sistema completo de gestión de facturas con soporte para CFDI (Comprobante Fiscal Digital por Internet) mexicano. Este proyecto introduce **testing completo** como parte fundamental del desarrollo, además de trabajar con múltiples DTOs especializados y cálculos automáticos de impuestos.

## 🎯 Objetivos de Aprendizaje

- Implementar DTOs especializados para diferentes operaciones y vistas
- Trabajar con estructuras JSON embebidas (items como String)
- Aplicar cálculos automáticos de impuestos (IVA, subtotal, total)
- Implementar validaciones fiscales (RFC, fechas, montos)
- Desarrollar suite completa de tests unitarios y de integración
- Manejar estados de factura con lógica de negocio
- Crear reportes y consultas especializadas

## 📦 Entidad Principal

### Invoice (Factura)

```
- id: Long (autoincremental)
- numeroFactura: String (único, formato: FACT-YYYY-0001)
- cliente: String (nombre o razón social)
- rfc: String (validación fiscal mexicana)
- direccion: String
- fechaEmision: LocalDate
- fechaVencimiento: LocalDate
- concepto: String (descripción general)
- subtotal: BigDecimal (calculado)
- iva: BigDecimal (calculado - 16%)
- descuento: BigDecimal
- total: BigDecimal (calculado)
- estado: EstadoFactura (PENDIENTE, PAGADA, CANCELADA, VENCIDA)
- metodoPago: MetodoPago (EFECTIVO, TRANSFERENCIA, TARJETA, CHEQUE)
- notas: String (opcional)
- items: String (JSON con detalle de productos/servicios)
```

## 🔧 Arquitectura de DTOs

### 1. InvoiceCreateDTO
**Propósito**: Recibir datos para crear nueva factura

**Campos**:
```java
- cliente: String (required, validación @NotBlank)
- rfc: String (required, patrón RFC mexicano)
- direccion: String (required)
- fechaEmision: LocalDate (required, no puede ser futura)
- fechaVencimiento: LocalDate (required, debe ser >= fechaEmision)
- concepto: String (required)
- descuento: BigDecimal (opcional, default 0, >= 0)
- metodoPago: MetodoPago (required)
- notas: String (opcional)
- items: List<ItemDTO> (required, min 1 item)
```

**Cálculos automáticos** (en Service):
- subtotal = suma de (item.cantidad * item.precioUnitario)
- iva = subtotal * 0.16
- total = subtotal + iva - descuento
- numeroFactura = generado automáticamente (FACT-YYYY-XXXX)

### 2. ItemDTO (Anidado)
**Propósito**: Representar un producto/servicio en la factura

**Campos**:
```java
- descripcion: String (required)
- cantidad: Integer (required, > 0)
- precioUnitario: BigDecimal (required, > 0)
- importe: BigDecimal (calculado = cantidad * precioUnitario)
```

### 3. InvoiceResponseDTO
**Propósito**: Respuesta estándar en operaciones CRUD

**Campos**:
```java
- id: Long
- numeroFactura: String
- cliente: String
- rfc: String
- fechaEmision: LocalDate
- fechaVencimiento: LocalDate
- subtotal: BigDecimal
- iva: BigDecimal
- descuento: BigDecimal
- total: BigDecimal
- estado: EstadoFactura
- metodoPago: MetodoPago
```

### 4. InvoiceDetailDTO
**Propósito**: Vista completa con items parseados

**Campos**:
```java
- (todos los campos de InvoiceResponseDTO)
- direccion: String
- concepto: String
- notas: String
- items: List<ItemDTO> (parseados desde JSON)
- diasVencimiento: Integer (calculado desde fechaVencimiento)
- estadoVencimiento: String ("Al corriente", "Por vencer", "Vencida")
```

### 5. InvoiceSummaryDTO
**Propósito**: Listados rápidos y resúmenes

**Campos**:
```java
- id: Long
- numeroFactura: String
- cliente: String
- fechaEmision: LocalDate
- total: BigDecimal
- estado: EstadoFactura
- diasParaVencimiento: Integer
```

### 6. InvoicePaymentDTO
**Propósito**: Registrar pagos y cambiar estado

**Campos**:
```java
- metodoPago: MetodoPago (opcional, actualizar si difiere)
- fechaPago: LocalDate (required, para validar vencimiento)
- notas: String (opcional, agregar nota de pago)
```

### 7. InvoiceReportDTO
**Propósito**: Reportes financieros y estadísticas

**Campos**:
```java
- periodo: String (ej: "2024-01")
- totalFacturado: BigDecimal (suma de totales)
- totalCobrado: BigDecimal (suma de facturas pagadas)
- totalPendiente: BigDecimal (suma de facturas pendientes)
- cantidadFacturas: Integer
- cantidadPagadas: Integer
- cantidadPendientes: Integer
- cantidadVencidas: Integer
- promedioTicket: BigDecimal
```

## 🌐 Endpoints REST

### CRUD Básico
```
POST   /api/invoices              → Crear factura (InvoiceCreateDTO → InvoiceResponseDTO)
GET    /api/invoices              → Listar todas (→ List<InvoiceSummaryDTO>)
GET    /api/invoices/{id}         → Obtener por ID (→ InvoiceDetailDTO)
PUT    /api/invoices/{id}         → Actualizar (InvoiceCreateDTO → InvoiceResponseDTO)
DELETE /api/invoices/{id}         → Eliminar lógico/físico
```

### Operaciones Especializadas
```
GET    /api/invoices/numero/{numero}           → Buscar por número
PATCH  /api/invoices/{id}/pay                  → Marcar como pagada (InvoicePaymentDTO)
PATCH  /api/invoices/{id}/cancel               → Cancelar factura
GET    /api/invoices/vencidas                  → Listar vencidas
GET    /api/invoices/cliente/{cliente}         → Facturas por cliente
```

### Consultas y Reportes
```
GET    /api/invoices/estado/{estado}           → Filtrar por estado
GET    /api/invoices/fecha-rango               → Filtrar por rango de fechas (params: inicio, fin)
GET    /api/invoices/metodo-pago/{metodo}     → Filtrar por método de pago
GET    /api/invoices/reporte/mensual/{anio}/{mes} → Reporte mensual (→ InvoiceReportDTO)
GET    /api/invoices/totales                   → Totales generales (→ Map con estadísticas)
```

## ✅ Validaciones

### Validaciones de Campos
- **RFC**: Patrón mexicano (13 caracteres para personas morales, 12 para físicas)
- **Fechas**: fechaEmision no puede ser futura, fechaVencimiento >= fechaEmision
- **Montos**: descuento >= 0, descuento <= subtotal
- **Items**: Lista no vacía, cantidad > 0, precioUnitario > 0

### Validaciones de Negocio
- No permitir editar facturas PAGADAS o CANCELADAS
- Al marcar como pagada, validar que no esté CANCELADA
- Al cancelar, validar que no esté PAGADA
- Actualizar automáticamente estado a VENCIDA si fechaVencimiento < hoy y estado == PENDIENTE
- Número de factura único generado automáticamente

## 🧪 Testing (Proyecto 10 - Primera Suite Completa)

### Tests Unitarios (Service)
1. **Creación de factura**
   - Test cálculos automáticos (subtotal, IVA, total)
   - Test generación número de factura
   - Test parsing y serialización de items JSON

2. **Validaciones**
   - Test RFC válido/inválido
   - Test fechas coherentes
   - Test montos positivos y descuento válido

3. **Operaciones de estado**
   - Test marcar como pagada (estado válido/inválido)
   - Test cancelación (estado válido/inválido)
   - Test detección automática de vencidas

4. **Consultas**
   - Test búsqueda por número de factura
   - Test filtrado por estado
   - Test cálculo de reportes mensuales

### Tests de Integración (Controller)
1. **CRUD completo**
   - POST crear factura (201 Created)
   - GET obtener todas (200 OK)
   - GET obtener por ID (200 OK, 404 Not Found)
   - PUT actualizar (200 OK, 404 Not Found, 400 Bad Request)
   - DELETE eliminar (204 No Content, 404 Not Found)

2. **Operaciones especializadas**
   - PATCH marcar como pagada
   - PATCH cancelar factura
   - GET facturas vencidas

3. **Validaciones HTTP**
   - Test 400 Bad Request para datos inválidos
   - Test 404 Not Found para IDs inexistentes
   - Test 409 Conflict para operaciones no permitidas (ej: editar factura pagada)

## 📊 Reglas de Negocio

1. **Generación de Número de Factura**
   - Formato: FACT-YYYY-XXXX (ej: FACT-2024-0001)
   - Autoincremental por año
   - Único en el sistema

2. **Cálculos Automáticos**
   - Subtotal = Σ (cantidad × precio unitario)
   - IVA = Subtotal × 0.16 (16% fijo)
   - Total = Subtotal + IVA - Descuento

3. **Estados de Factura**
   - **PENDIENTE**: Estado inicial, puede editarse
   - **PAGADA**: No puede editarse ni cancelarse
   - **CANCELADA**: No puede modificarse
   - **VENCIDA**: Asignado automáticamente si fechaVencimiento < hoy y estado == PENDIENTE

4. **Manejo de Items**
   - Almacenados como JSON en campo String
   - Parseados a List<ItemDTO> para InvoiceDetailDTO
   - Validados al crear/actualizar factura

## 🛠️ Tecnologías

- **Spring Boot 3.x**
- **Spring Data JPA**
- **H2 Database** (desarrollo)
- **Validation API** (Bean Validation)
- **Jackson** (JSON parsing)
- **Lombok** (reducir boilerplate)
- **JUnit 5** (testing)
- **MockMvc** (tests de integración)
- **Mockito** (mocking en tests unitarios)

## 📁 Estructura del Proyecto

```
proyecto-10-facturacion/
│
├── README.md (este archivo)
│
├── working/
│   ├── README.md (guía de inicio)
│   ├── postman/
│   │   └── Proyecto10-Facturacion.postman_collection.json
│   └── src/
│       └── main/
│           ├── java/com/project10/invoicing/
│           │   ├── InvoicingApplication.java
│           │   ├── entity/
│           │   │   └── Invoice.java
│           │   ├── dto/
│           │   │   ├── InvoiceCreateDTO.java
│           │   │   ├── InvoiceResponseDTO.java
│           │   │   ├── InvoiceDetailDTO.java
│           │   │   ├── InvoiceSummaryDTO.java
│           │   │   ├── InvoicePaymentDTO.java
│           │   │   ├── InvoiceReportDTO.java
│           │   │   └── ItemDTO.java
│           │   ├── enums/
│           │   │   ├── EstadoFactura.java
│           │   │   └── MetodoPago.java
│           │   ├── repository/
│           │   │   └── InvoiceRepository.java
│           │   ├── service/
│           │   │   └── InvoiceService.java
│           │   ├── controller/
│           │   │   └── InvoiceController.java
│           │   └── exception/
│           │       ├── InvoiceNotFoundException.java
│           │       ├── InvalidInvoiceStateException.java
│           │       └── GlobalExceptionHandler.java
│           └── resources/
│               └── application.properties
│
└── solution/
    └── README.md (documentación técnica)
    └── (estructura idéntica a working con implementación completa)
```

## 🚀 Orden de Desarrollo

1. **Enums** (EstadoFactura, MetodoPago)
2. **Entity** (Invoice con validations)
3. **DTOs** (ItemDTO, InvoiceCreateDTO, InvoiceResponseDTO, InvoiceDetailDTO, InvoiceSummaryDTO, InvoicePaymentDTO, InvoiceReportDTO)
4. **Repository** (con queries personalizadas)
5. **Exceptions** (custom exceptions y GlobalExceptionHandler)
6. **Service** (lógica de negocio + cálculos + conversiones DTO)
7. **Controller** (endpoints REST)
8. **Tests Unitarios** (ServiceTest con Mockito)
9. **Tests de Integración** (ControllerTest con MockMvc)

## 💡 Conceptos Clave de este Proyecto

- **Múltiples DTOs especializados**: Cada operación tiene su DTO optimizado
- **DTOs anidados**: ItemDTO dentro de InvoiceCreateDTO
- **JSON embebido**: Items almacenados como String, parseados según necesidad
- **Cálculos automáticos**: Subtotal, IVA, total calculados en Service
- **Validaciones fiscales**: RFC mexicano, fechas coherentes
- **Estado y lógica de negocio**: Transiciones de estado validadas
- **Testing completo**: Primera suite de tests unitarios e integración
- **Reportes y agregaciones**: Cálculos estadísticos y financieros

---

**Nivel**: Intermedio (Proyecto 10/20)  
**Categoría**: DTOs + Testing  
**Duración estimada**: 8-10 horas