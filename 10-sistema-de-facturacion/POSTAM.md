# 🧪 Guía de Pruebas con Postman - Sistema de Facturación

## 📋 Índice
1. [Configuración Inicial](#configuración-inicial)
2. [CRUD Básico](#crud-básico)
3. [Búsquedas y Consultas](#búsquedas-y-consultas)
4. [Operaciones de Negocio](#operaciones-de-negocio)
5. [Reportes y Estadísticas](#reportes-y-estadísticas)
6. [Casos de Prueba de Validación](#casos-de-prueba-de-validación)

---

## ⚙️ Configuración Inicial

### 1. Arrancar la Aplicación
```bash
mvn spring-boot:run
```

**Verificar que está corriendo**:
- URL: http://localhost:8080
- Consola H2: http://localhost:8080/h2-console

### 2. Configurar Postman
- **Base URL**: `http://localhost:8080`
- **Headers**: 
  - `Content-Type: application/json`
  - `Accept: application/json`

---

## 1️⃣ CRUD Básico

### 1.1. Crear Factura

**Endpoint**: `POST http://localhost:8080/api/invoices`

**Headers**:
```
Content-Type: application/json
```

**Body (JSON)**:
```json
{
  "cliente": "Tech Solutions España S.L.",
  "nifCif": "B12345678",
  "direccion": "Calle Gran Vía 28, 28013 Madrid",
  "fechaEmision": "2024-01-15",
  "fechaVencimiento": "2026-02-15",
  "concepto": "Desarrollo de aplicación web corporativa",
  "tipoIva": "GENERAL",
  "descuento": 500.00,
  "metodoPago": "TRANSFERENCIA",
  "notas": "Transferencia SEPA. IBAN: ES91 2100 0418 4502 0005 1332",
  "items": [
    {
      "descripcion": "Desarrollo frontend con React",
      "cantidad": 40,
      "precioUnitario": 50.00
    },
    {
      "descripcion": "Desarrollo backend con Spring Boot",
      "cantidad": 60,
      "precioUnitario": 55.00
    },
    {
      "descripcion": "Testing y QA",
      "cantidad": 20,
      "precioUnitario": 45.00
    }
  ]
}
```

**Cálculos esperados**:
- Subtotal: (40×50) + (60×55) + (20×45) = 2,000 + 3,300 + 900 = **5,200.00€**
- IVA (21%): 5,200 × 0.21 = **1,092.00€**
- Total: 5,200 + 1,092 - 500 = **5,792.00€**

**Response (201 Created)**:
```json
{
  "id": 1,
  "numeroFactura": "FACT-2024-0001",
  "cliente": "Tech Solutions España S.L.",
  "nifCif": "B12345678",
  "fechaEmision": "2024-01-15",
  "fechaVencimiento": "2026-02-15",
  "tipoIva": "GENERAL",
  "subtotal": 5200.00,
  "iva": 1092.00,
  "descuento": 500.00,
  "total": 5792.00,
  "estado": "PENDIENTE",
  "metodoPago": "TRANSFERENCIA"
}
```

---

### 1.2. Crear Factura con IVA Reducido

**Endpoint**: `POST http://localhost:8080/api/invoices`

**Body (JSON)**:
```json
{
  "cliente": "Restaurante El Buen Sabor S.L.",
  "nifCif": "B87654321",
  "direccion": "Plaza Mayor 5, 28012 Madrid",
  "fechaEmision": "2024-01-16",
  "fechaVencimiento": "2026-02-16",
  "concepto": "Servicios de consultoría hostelera",
  "tipoIva": "REDUCIDO",
  "descuento": 0.00,
  "metodoPago": "TARJETA",
  "items": [
    {
      "descripcion": "Consultoría de gestión de restaurante",
      "cantidad": 10,
      "precioUnitario": 80.00
    }
  ]
}
```

**Cálculos esperados**:
- Subtotal: 10 × 80 = **800.00€**
- IVA (10%): 800 × 0.10 = **80.00€**
- Total: 800 + 80 = **880.00€**

---

### 1.3. Crear Factura Exenta de IVA

**Endpoint**: `POST http://localhost:8080/api/invoices`

**Body (JSON)**:
```json
{
  "cliente": "Clínica Dental Sonrisa S.L.",
  "nifCif": "B11223344",
  "direccion": "Calle Serrano 100, 28006 Madrid",
  "fechaEmision": "2024-01-17",
  "fechaVencimiento": "2026-02-17",
  "concepto": "Servicios médicos odontológicos",
  "tipoIva": "EXENTO",
  "descuento": 0.00,
  "metodoPago": "EFECTIVO",
  "items": [
    {
      "descripcion": "Tratamiento dental",
      "cantidad": 1,
      "precioUnitario": 500.00
    }
  ]
}
```

**Cálculos esperados**:
- Subtotal: **500.00€**
- IVA (0%): **0.00€**
- Total: **500.00€**

---

### 1.4. Obtener Todas las Facturas

**Endpoint**: `GET http://localhost:8080/api/invoices`

**Response (200 OK)**:
```json
[
  {
    "id": 1,
    "numeroFactura": "FACT-2024-0001",
    "cliente": "Tech Solutions España S.L.",
    "fechaEmision": "2024-01-15",
    "total": 5792.00,
    "estado": "PENDIENTE",
    "diasParaVencimiento": 31
  },
  {
    "id": 2,
    "numeroFactura": "FACT-2024-0002",
    "cliente": "Restaurante El Buen Sabor S.L.",
    "fechaEmision": "2024-01-16",
    "total": 880.00,
    "estado": "PENDIENTE",
    "diasParaVencimiento": 32
  }
]
```

---

### 1.5. Obtener Detalle de Factura por ID

**Endpoint**: `GET http://localhost:8080/api/invoices/1`

**Response (200 OK)**:
```json
{
  "id": 1,
  "numeroFactura": "FACT-2024-0001",
  "cliente": "Tech Solutions España S.L.",
  "nifCif": "B12345678",
  "direccion": "Calle Gran Vía 28, 28013 Madrid",
  "fechaEmision": "2024-01-15",
  "fechaVencimiento": "2024-02-15",
  "concepto": "Desarrollo de aplicación web corporativa",
  "tipoIva": "GENERAL",
  "subtotal": 5200.00,
  "iva": 1092.00,
  "descuento": 500.00,
  "total": 5792.00,
  "estado": "PENDIENTE",
  "metodoPago": "TRANSFERENCIA",
  "notas": "Transferencia SEPA. IBAN: ES91 2100 0418 4502 0005 1332",
  "items": [
    {
      "descripcion": "Desarrollo frontend con React",
      "cantidad": 40,
      "precioUnitario": 50.00,
      "importe": 2000.00
    },
    {
      "descripcion": "Desarrollo backend con Spring Boot",
      "cantidad": 60,
      "precioUnitario": 55.00,
      "importe": 3300.00
    },
    {
      "descripcion": "Testing y QA",
      "cantidad": 20,
      "precioUnitario": 45.00,
      "importe": 900.00
    }
  ],
  "diasVencimiento": 31,
  "estadoVencimiento": "Al corriente"
}
```

---

### 1.6. Actualizar Factura

**Endpoint**: `PUT http://localhost:8080/api/invoices/1`

**Body (JSON)** - Ejemplo: Añadir horas de soporte:
```json
{
  "cliente": "Tech Solutions España S.L.",
  "nifCif": "B12345678",
  "direccion": "Calle Gran Vía 28, 28013 Madrid",
  "fechaEmision": "2024-01-15",
  "fechaVencimiento": "2024-02-28",
  "concepto": "Desarrollo de aplicación web corporativa + Soporte",
  "tipoIva": "GENERAL",
  "descuento": 800.00,
  "metodoPago": "TRANSFERENCIA",
  "notas": "Plazo extendido. Incluye 3 meses de soporte.",
  "items": [
    {
      "descripcion": "Desarrollo frontend con React",
      "cantidad": 40,
      "precioUnitario": 50.00
    },
    {
      "descripcion": "Desarrollo backend con Spring Boot",
      "cantidad": 60,
      "precioUnitario": 55.00
    },
    {
      "descripcion": "Testing y QA",
      "cantidad": 20,
      "precioUnitario": 45.00
    },
    {
      "descripcion": "Soporte técnico (3 meses)",
      "cantidad": 30,
      "precioUnitario": 40.00
    }
  ]
}
```

**Nuevos cálculos**:
- Subtotal: 5,200 + (30×40) = 5,200 + 1,200 = **6,400.00€**
- IVA (21%): 6,400 × 0.21 = **1,344.00€**
- Total: 6,400 + 1,344 - 800 = **6,944.00€**

**Response (200 OK)**:
```json
{
  "id": 1,
  "numeroFactura": "FACT-2024-0001",
  "cliente": "Tech Solutions España S.L.",
  "nifCif": "B12345678",
  "fechaEmision": "2024-01-15",
  "fechaVencimiento": "2024-02-28",
  "tipoIva": "GENERAL",
  "subtotal": 6400.00,
  "iva": 1344.00,
  "descuento": 800.00,
  "total": 6944.00,
  "estado": "PENDIENTE",
  "metodoPago": "TRANSFERENCIA"
}
```

---

### 1.7. Eliminar Factura

**Endpoint**: `DELETE http://localhost:8080/api/invoices/3`

**Response (204 No Content)**:
```
(Sin contenido)
```

---

## 2️⃣ Búsquedas y Consultas

### 2.1. Buscar por Número de Factura

**Endpoint**: `GET http://localhost:8080/api/invoices/numero/FACT-2026-0001`

**Response (200 OK)**:
```json
{
  "id": 1,
  "numeroFactura": "FACT-2026-0001",
  "cliente": "Tech Solutions España S.L.",
  "nifCif": "B12345678",
  "direccion": "Calle Gran Vía 28, 28013 Madrid",
  "fechaEmision": "2024-01-15",
  "fechaVencimiento": "2024-02-28",
  "concepto": "Desarrollo de aplicación web corporativa + Soporte",
  "tipoIva": "GENERAL",
  "subtotal": 6400.00,
  "iva": 1344.00,
  "descuento": 800.00,
  "total": 6944.00,
  "estado": "PENDIENTE",
  "metodoPago": "TRANSFERENCIA",
  "notas": "Plazo extendido. Incluye 3 meses de soporte.",
  "items": [...],
  "diasVencimiento": 44,
  "estadoVencimiento": "Al corriente"
}
```

---

### 2.2. Obtener Facturas Vencidas

**Endpoint**: `GET http://localhost:8080/api/invoices/vencidas`

**Response (200 OK)**:
```json
[
  {
    "id": 5,
    "numeroFactura": "FACT-2024-0005",
    "cliente": "Cliente Moroso S.L.",
    "fechaEmision": "2023-12-01",
    "total": 1500.00,
    "estado": "VENCIDA",
    "diasParaVencimiento": -15
  }
]
```

---

### 2.3. Buscar por Cliente

**Endpoint**: `GET http://localhost:8080/api/invoices/cliente/Tech`

**Búsqueda case-insensitive y parcial**: Encuentra "Tech Solutions", "FinTech", "TECH INNOVATORS"

**Response (200 OK)**:
```json
[
  {
    "id": 1,
    "numeroFactura": "FACT-2024-0001",
    "cliente": "Tech Solutions España S.L.",
    "fechaEmision": "2024-01-15",
    "total": 6944.00,
    "estado": "PENDIENTE",
    "diasParaVencimiento": 44
  }
]
```

---

### 2.4. Filtrar por Estado

**Endpoint**: `GET http://localhost:8080/api/invoices/estado/PENDIENTE`

**Estados válidos**: `PENDIENTE`, `PAGADA`, `CANCELADA`, `VENCIDA`

**Response (200 OK)**:
```json
[
  {
    "id": 1,
    "numeroFactura": "FACT-2024-0001",
    "cliente": "Tech Solutions España S.L.",
    "fechaEmision": "2024-01-15",
    "total": 6944.00,
    "estado": "PENDIENTE",
    "diasParaVencimiento": 44
  },
  {
    "id": 2,
    "numeroFactura": "FACT-2024-0002",
    "cliente": "Restaurante El Buen Sabor S.L.",
    "fechaEmision": "2024-01-16",
    "total": 880.00,
    "estado": "PENDIENTE",
    "diasParaVencimiento": 32
  }
]
```

---

### 2.5. Filtrar por Rango de Fechas

**Endpoint**: `GET http://localhost:8080/api/invoices/fecha-rango?inicio=2024-01-01&fin=2024-01-31`

**Parámetros**:
- `inicio`: Fecha inicio (formato: yyyy-MM-dd)
- `fin`: Fecha fin (formato: yyyy-MM-dd)

**Response (200 OK)**:
```json
[
  {
    "id": 1,
    "numeroFactura": "FACT-2024-0001",
    "cliente": "Tech Solutions España S.L.",
    "fechaEmision": "2024-01-15",
    "total": 6944.00,
    "estado": "PENDIENTE",
    "diasParaVencimiento": 44
  },
  {
    "id": 2,
    "numeroFactura": "FACT-2024-0002",
    "cliente": "Restaurante El Buen Sabor S.L.",
    "fechaEmision": "2024-01-16",
    "total": 880.00,
    "estado": "PENDIENTE",
    "diasParaVencimiento": 32
  }
]
```

---

### 2.6. Filtrar por Método de Pago

**Endpoint**: `GET http://localhost:8080/api/invoices/metodo-pago/TRANSFERENCIA`

**Métodos válidos**: `EFECTIVO`, `TRANSFERENCIA`, `TARJETA`, `DOMICILIACION`, `CHEQUE`, `PAGARE`

**Response (200 OK)**:
```json
[
  {
    "id": 1,
    "numeroFactura": "FACT-2024-0001",
    "cliente": "Tech Solutions España S.L.",
    "fechaEmision": "2024-01-15",
    "total": 6944.00,
    "estado": "PENDIENTE",
    "diasParaVencimiento": 44
  }
]
```

---

## 3️⃣ Operaciones de Negocio

### 3.1. Marcar Factura como Pagada

**Endpoint**: `PATCH http://localhost:8080/api/invoices/1/pay`

**Body (JSON)**:
```json
{
  "metodoPago": "TRANSFERENCIA",
  "fechaPago": "2024-01-20",
  "notas": "Pagado mediante transferencia bancaria. Referencia: TRF-20240120-001"
}
```

**Response (200 OK)**:
```json
{
  "id": 1,
  "numeroFactura": "FACT-2024-0001",
  "cliente": "Tech Solutions España S.L.",
  "nifCif": "B12345678",
  "fechaEmision": "2024-01-15",
  "fechaVencimiento": "2024-02-28",
  "tipoIva": "GENERAL",
  "subtotal": 6400.00,
  "iva": 1344.00,
  "descuento": 800.00,
  "total": 6944.00,
  "estado": "PAGADA",
  "metodoPago": "TRANSFERENCIA"
}
```

---

### 3.2. Marcar como Pagada (con cambio de método)

**Endpoint**: `PATCH http://localhost:8080/api/invoices/2/pay`

**Body (JSON)** - Cliente acordó TRANSFERENCIA pero pagó con TARJETA:
```json
{
  "metodoPago": "TARJETA",
  "fechaPago": "2024-01-18",
  "notas": "Pago con tarjeta terminación 4567. Autorización: AUTH-789456"
}
```

**Response (200 OK)**:
```json
{
  "id": 2,
  "numeroFactura": "FACT-2024-0002",
  "cliente": "Restaurante El Buen Sabor S.L.",
  "nifCif": "B87654321",
  "fechaEmision": "2024-01-16",
  "fechaVencimiento": "2024-02-16",
  "tipoIva": "REDUCIDO",
  "subtotal": 800.00,
  "iva": 80.00,
  "descuento": 0.00,
  "total": 880.00,
  "estado": "PAGADA",
  "metodoPago": "TARJETA"
}
```

---

### 3.3. Cancelar Factura

**Endpoint**: `PATCH http://localhost:8080/api/invoices/4/cancel`

**Response (200 OK)**:
```json
{
  "id": 4,
  "numeroFactura": "FACT-2024-0004",
  "cliente": "Cliente X S.L.",
  "nifCif": "B99887766",
  "fechaEmision": "2024-01-18",
  "fechaVencimiento": "2024-02-18",
  "tipoIva": "GENERAL",
  "subtotal": 1000.00,
  "iva": 210.00,
  "descuento": 0.00,
  "total": 1210.00,
  "estado": "CANCELADA",
  "metodoPago": "CHEQUE"
}
```

---

## 4️⃣ Reportes y Estadísticas

### 4.1. Reporte Mensual

**Endpoint**: `GET http://localhost:8080/api/invoices/reporte/mensual/2024/1`

**Formato**: `/reporte/mensual/{año}/{mes}`

**Response (200 OK)**:
```json
{
  "periodo": "2024-01",
  "totalFacturado": 9614.00,
  "totalCobrado": 7824.00,
  "totalPendiente": 1790.00,
  "cantidadFacturas": 4,
  "cantidadPagadas": 2,
  "cantidadPendientes": 1,
  "cantidadVencidas": 0,
  "promedioTicket": 2403.50
}
```

---

### 4.2. Totales Generales

**Endpoint**: `GET http://localhost:8080/api/invoices/totales`

**Response (200 OK)**:
```json
{
  "totalFacturas": 4,
  "totalPendientes": 1,
  "totalPagadas": 2,
  "totalCanceladas": 1,
  "totalVencidas": 0,
  "montoTotal": 9614.00,
  "montoCobrado": 7824.00,
  "montoPendiente": 1790.00
}
```

---

## 5️⃣ Casos de Prueba de Validación

### 5.1. Error 400 - NIF/CIF Inválido

**Endpoint**: `POST http://localhost:8080/api/invoices`

**Body (JSON)** - NIF/CIF mal formateado:
```json
{
  "cliente": "Test Company",
  "nifCif": "INVALIDO123",
  "direccion": "Calle Test 123",
  "fechaEmision": "2024-01-15",
  "fechaVencimiento": "2024-02-15",
  "concepto": "Test",
  "tipoIva": "GENERAL",
  "descuento": 0,
  "metodoPago": "EFECTIVO",
  "items": [
    {
      "descripcion": "Item test",
      "cantidad": 1,
      "precioUnitario": 100.00
    }
  ]
}
```

**Response (400 Bad Request)**:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los datos enviados",
  "path": "/api/invoices",
  "validationErrors": {
    "nifCif": "NIF/CIF inválido. Formatos: 12345678A (NIF), X1234567A (NIE), A12345678 (CIF)"
  }
}
```

---

### 5.2. Error 400 - Fecha Vencimiento Anterior a Emisión

**Endpoint**: `POST http://localhost:8080/api/invoices`

**Body (JSON)**:
```json
{
  "cliente": "Test Company",
  "nifCif": "B12345678",
  "direccion": "Calle Test 123",
  "fechaEmision": "2024-02-15",
  "fechaVencimiento": "2024-01-15",
  "concepto": "Test",
  "tipoIva": "GENERAL",
  "descuento": 0,
  "metodoPago": "EFECTIVO",
  "items": [
    {
      "descripcion": "Item test",
      "cantidad": 1,
      "precioUnitario": 100.00
    }
  ]
}
```

**Response (400 Bad Request)**:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los datos enviados",
  "path": "/api/invoices",
  "validationErrors": {
    "fechaVencimientoValida": "La fecha de vencimiento debe ser igual o posterior a la fecha de emisión"
  }
}
```

---

### 5.3. Error 400 - Lista de Items Vacía

**Endpoint**: `POST http://localhost:8080/api/invoices`

**Body (JSON)**:
```json
{
  "cliente": "Test Company",
  "nifCif": "B12345678",
  "direccion": "Calle Test 123",
  "fechaEmision": "2024-01-15",
  "fechaVencimiento": "2024-02-15",
  "concepto": "Test",
  "tipoIva": "GENERAL",
  "descuento": 0,
  "metodoPago": "EFECTIVO",
  "items": []
}
```

**Response (400 Bad Request)**:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los datos enviados",
  "path": "/api/invoices",
  "validationErrors": {
    "items": "Debe haber al menos un item en la factura"
  }
}
```

---

### 5.4. Error 404 - Factura No Encontrada

**Endpoint**: `GET http://localhost:8080/api/invoices/999`

**Response (404 Not Found)**:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "No se encontró la factura con ID: 999",
  "path": "/api/invoices/999"
}
```

---

### 5.5. Error 409 - Intentar Actualizar Factura Pagada

**Endpoint**: `PUT http://localhost:8080/api/invoices/1`

**Condición**: La factura ID=1 ya está en estado PAGADA

**Body (JSON)**: Cualquier actualización

**Response (409 Conflict)**:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "No se puede actualizar una factura en estado PAGADA. Solo se pueden actualizar facturas PENDIENTES.",
  "path": "/api/invoices/1"
}
```

---

### 5.6. Error 409 - Intentar Pagar Factura Cancelada

**Endpoint**: `PATCH http://localhost:8080/api/invoices/4/pay`

**Condición**: La factura ID=4 está en estado CANCELADA

**Body (JSON)**:
```json
{
  "metodoPago": "TRANSFERENCIA",
  "fechaPago": "2024-01-20",
  "notas": "Intento de pago"
}
```

**Response (409 Conflict)**:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "No se puede marcar como pagada una factura cancelada",
  "path": "/api/invoices/4/pay"
}
```

---

### 5.7. Error 409 - Intentar Cancelar Factura Pagada

**Endpoint**: `PATCH http://localhost:8080/api/invoices/1/cancel`

**Condición**: La factura ID=1 está en estado PAGADA

**Response (409 Conflict)**:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "No se puede cancelar una factura que ya ha sido pagada",
  "path": "/api/invoices/1/cancel"
}
```

---

## 📊 Resumen de Códigos HTTP

| Código | Descripción | Cuándo se usa |
|--------|-------------|---------------|
| **200 OK** | Éxito | GET, PUT, PATCH exitosos |
| **201 Created** | Recurso creado | POST crear factura exitoso |
| **204 No Content** | Sin contenido | DELETE exitoso |
| **400 Bad Request** | Datos inválidos | Validaciones fallidas |
| **404 Not Found** | No encontrado | ID o número de factura inexistente |
| **409 Conflict** | Operación no permitida | Intentar operación con estado incorrecto |

---

## 🎯 Secuencia de Prueba Recomendada

1. **Crear 3-4 facturas** con diferentes tipos de IVA (GENERAL, REDUCIDO, EXENTO)
2. **Listar todas** las facturas (GET /api/invoices)
3. **Obtener detalle** de una factura específica (GET /api/invoices/1)
4. **Actualizar** una factura en estado PENDIENTE
5. **Marcar como pagada** una factura
6. **Cancelar** otra factura
7. **Buscar por cliente** (GET /api/invoices/cliente/Tech)
8. **Filtrar por estado** PAGADA (GET /api/invoices/estado/PAGADA)
9. **Generar reporte mensual** (GET /api/invoices/reporte/mensual/2024/1)
10. **Obtener totales generales** (GET /api/invoices/totales)
11. **Probar validaciones** (NIF inválido, fechas incorrectas, etc.)
12. **Probar errores de estado** (actualizar factura pagada, pagar factura cancelada)

---

## 💡 Tips para Postman

### Variables de Entorno
Crea variables para reutilizar valores:
```
base_url = http://localhost:8080
invoice_id = 1
```

### Tests Automáticos
Añade scripts en la pestaña "Tests" de Postman:
```javascript
// Verificar status code
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

// Verificar que se generó el número de factura
pm.test("Número de factura generado", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.numeroFactura).to.match(/^FACT-\d{4}-\d{4}$/);
});

// Guardar ID para usar en siguiente request
pm.environment.set("invoice_id", pm.response.json().id);
```

---

## ✅ Checklist de Pruebas

- [ ] Crear factura con IVA GENERAL (21%)
- [ ] Crear factura con IVA REDUCIDO (10%)
- [ ] Crear factura con IVA SUPERREDUCIDO (4%)
- [ ] Crear factura EXENTA (0%)
- [ ] Listar todas las facturas
- [ ] Obtener detalle por ID
- [ ] Obtener por número de factura
- [ ] Actualizar factura PENDIENTE
- [ ] Marcar como PAGADA
- [ ] Cancelar factura
- [ ] Buscar por cliente
- [ ] Filtrar por estado
- [ ] Filtrar por rango de fechas
- [ ] Filtrar por método de pago
- [ ] Obtener facturas vencidas
- [ ] Reporte mensual
- [ ] Totales generales
- [ ] Validar NIF/CIF inválido (400)
- [ ] Validar fechas incoherentes (400)
- [ ] Validar items vacíos (400)
- [ ] Factura no encontrada (404)
- [ ] Actualizar factura pagada (409)
- [ ] Pagar factura cancelada (409)
- [ ] Cancelar factura pagada (409)

---

**¡Listo para probar!** 🚀

Comienza creando 3-4 facturas con diferentes configuraciones y luego prueba todas las operaciones disponibles.