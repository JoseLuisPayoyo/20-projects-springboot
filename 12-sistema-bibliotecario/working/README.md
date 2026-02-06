# 🔨 Proyecto 12: Sistema de Biblioteca - Working

## 🚀 Guía de Inicio Rápido

### 1. Crear el proyecto

Ir a [Spring Initializr](https://start.spring.io/) con:

| Configuración | Valor |
|---------------|-------|
| **Project** | Maven |
| **Language** | Java |
| **Spring Boot** | 3.2.x+ |
| **Group** | `com.library` |
| **Artifact** | `library-system` |
| **Packaging** | Jar |
| **Java** | 17 |

### 2. Dependencias

- Spring Web
- Spring Data JPA
- H2 Database
- Validation
- Lombok

### 3. Configurar `application.properties`

```properties
spring.application.name=library-system
spring.datasource.url=jdbc:h2:mem:librarydb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.jackson.serialization.write-dates-as-timestamps=false
```

### 4. Orden de implementación recomendado

```
1. LoanStatus (Enum)
2. Book (Entity)
3. Member (Entity)
4. Loan (Entity)
5. BookRepository
6. MemberRepository
7. LoanRepository
8. ResourceNotFoundException + BusinessRuleException
9. GlobalExceptionHandler
10. BookService
11. MemberService
12. LoanService
13. BookController
14. MemberController
15. LoanController
```

---

## 📬 Colección Postman

📎 **Descargar**: [Proyecto12-Biblioteca.postman_collection.json](postman/Proyecto12-Biblioteca.postman_collection.json)

### Variables de Entorno

```
base_url = http://localhost:8080
```

---

## 📂 BOOKS - Gestión de Libros

### 1️⃣ Crear Libro

**POST** `{{base_url}}/api/books`

```json
{
    "title": "Cien años de soledad",
    "author": "Gabriel García Márquez",
    "isbn": "978-0-06-088328-7",
    "genre": "Realismo mágico",
    "publishedYear": 1967
}
```

**Response:** `201 CREATED`
```json
{
    "id": 1,
    "title": "Cien años de soledad",
    "author": "Gabriel García Márquez",
    "isbn": "978-0-06-088328-7",
    "genre": "Realismo mágico",
    "publishedYear": 1967,
    "available": true,
    "createdAt": "2025-01-20T10:30:00",
    "loans": []
}
```

### 2️⃣ Crear Libro - ISBN Duplicado (Validación)

**POST** `{{base_url}}/api/books`

```json
{
    "title": "Otro libro",
    "author": "Otro autor",
    "isbn": "978-0-06-088328-7",
    "genre": "Ficción",
    "publishedYear": 2020
}
```

**Response:** `409 CONFLICT`
```json
{
    "timestamp": "2025-01-20T10:31:00",
    "status": 409,
    "error": "Conflict",
    "message": "Ya existe un libro con ISBN: 978-0-06-088328-7"
}
```

### 3️⃣ Crear Libro - Datos Inválidos (Validación)

**POST** `{{base_url}}/api/books`

```json
{
    "title": "",
    "author": "",
    "isbn": "isbn-invalido",
    "genre": "Ficción",
    "publishedYear": 500
}
```

**Response:** `400 BAD REQUEST`
```json
{
    "timestamp": "2025-01-20T10:32:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "validationErrors": {
        "title": "El título no puede estar vacío",
        "author": "El autor no puede estar vacío",
        "isbn": "Formato de ISBN no válido",
        "publishedYear": "El año debe ser al menos 1000"
    }
}
```

### 4️⃣ Crear Segundo Libro

**POST** `{{base_url}}/api/books`

```json
{
    "title": "Don Quijote de la Mancha",
    "author": "Miguel de Cervantes",
    "isbn": "978-84-376-0494-7",
    "genre": "Novela",
    "publishedYear": 1605
}
```

**Response:** `201 CREATED`

### 5️⃣ Crear Tercer Libro

**POST** `{{base_url}}/api/books`

```json
{
    "title": "La sombra del viento",
    "author": "Carlos Ruiz Zafón",
    "isbn": "978-84-08-04171-3",
    "genre": "Misterio",
    "publishedYear": 2001
}
```

**Response:** `201 CREATED`

### 6️⃣ Listar Todos los Libros

**GET** `{{base_url}}/api/books`

**Response:** `200 OK` → Array con todos los libros

### 7️⃣ Obtener Libro por ID

**GET** `{{base_url}}/api/books/1`

**Response:** `200 OK` → Libro con id=1 y su historial de préstamos

### 8️⃣ Buscar Libro por ISBN

**GET** `{{base_url}}/api/books/isbn/978-0-06-088328-7`

**Response:** `200 OK`

### 9️⃣ Listar Libros Disponibles

**GET** `{{base_url}}/api/books/available`

**Response:** `200 OK` → Solo libros con `available = true`

### 🔟 Buscar por Autor

**GET** `{{base_url}}/api/books/author/Gabriel`

**Response:** `200 OK` → Libros cuyo autor contiene "Gabriel"

### 1️⃣1️⃣ Buscar por Género

**GET** `{{base_url}}/api/books/genre/Novela`

**Response:** `200 OK`

### 1️⃣2️⃣ Actualizar Libro

**PUT** `{{base_url}}/api/books/1`

```json
{
    "title": "Cien años de soledad - Edición Especial",
    "author": "Gabriel García Márquez",
    "isbn": "978-0-06-088328-7",
    "genre": "Realismo mágico",
    "publishedYear": 1967
}
```

**Response:** `200 OK`

### 1️⃣3️⃣ Obtener Libro Inexistente

**GET** `{{base_url}}/api/books/999`

**Response:** `404 NOT FOUND`
```json
{
    "timestamp": "2025-01-20T10:40:00",
    "status": 404,
    "error": "Not Found",
    "message": "Libro no encontrado con id: 999"
}
```

### 1️⃣4️⃣ Eliminar Libro (sin préstamos activos)

**DELETE** `{{base_url}}/api/books/3`

**Response:** `204 NO CONTENT`

---

## 👤 MEMBERS - Gestión de Socios

### 1️⃣ Crear Socio

**POST** `{{base_url}}/api/members`

```json
{
    "name": "Jose Luis Martínez",
    "email": "joseluis@biblioteca.com",
    "phone": "+34 612 345 678"
}
```

**Response:** `201 CREATED`
```json
{
    "id": 1,
    "name": "Jose Luis Martínez",
    "email": "joseluis@biblioteca.com",
    "phone": "+34 612 345 678",
    "membershipDate": "2025-01-20",
    "active": true,
    "loans": []
}
```

### 2️⃣ Crear Socio - Email Duplicado (Validación)

**POST** `{{base_url}}/api/members`

```json
{
    "name": "Otro Usuario",
    "email": "joseluis@biblioteca.com",
    "phone": "+34 600 000 000"
}
```

**Response:** `409 CONFLICT`
```json
{
    "timestamp": "2025-01-20T10:45:00",
    "status": 409,
    "error": "Conflict",
    "message": "Ya existe un socio con email: joseluis@biblioteca.com"
}
```

### 3️⃣ Crear Segundo Socio

**POST** `{{base_url}}/api/members`

```json
{
    "name": "María García López",
    "email": "maria@biblioteca.com",
    "phone": "+34 698 765 432"
}
```

**Response:** `201 CREATED`

### 4️⃣ Listar Todos los Socios

**GET** `{{base_url}}/api/members`

**Response:** `200 OK`

### 5️⃣ Obtener Socio por ID

**GET** `{{base_url}}/api/members/1`

**Response:** `200 OK` → Socio con sus préstamos

### 6️⃣ Buscar Socio por Email

**GET** `{{base_url}}/api/members/email/joseluis@biblioteca.com`

**Response:** `200 OK`

### 7️⃣ Actualizar Socio

**PUT** `{{base_url}}/api/members/1`

```json
{
    "name": "Jose Luis Martínez Ruiz",
    "email": "joseluis@biblioteca.com",
    "phone": "+34 612 345 999"
}
```

**Response:** `200 OK`

### 8️⃣ Desactivar Socio

**PATCH** `{{base_url}}/api/members/2/deactivate`

**Response:** `200 OK`
```json
{
    "id": 2,
    "name": "María García López",
    "email": "maria@biblioteca.com",
    "active": false,
    "loans": []
}
```

### 9️⃣ Obtener Préstamos de un Socio

**GET** `{{base_url}}/api/members/1/loans`

**Response:** `200 OK` → Lista de préstamos del socio

---

## 📖 LOANS - Gestión de Préstamos

### 1️⃣ Crear Préstamo

**POST** `{{base_url}}/api/loans`

```json
{
    "bookId": 1,
    "memberId": 1
}
```

**Response:** `201 CREATED`
```json
{
    "id": 1,
    "loanDate": "2025-01-20",
    "dueDate": "2025-02-03",
    "returnDate": null,
    "status": "ACTIVE",
    "book": {
        "id": 1,
        "title": "Cien años de soledad",
        "isbn": "978-0-06-088328-7"
    },
    "member": {
        "id": 1,
        "name": "Jose Luis Martínez"
    }
}
```

> ⚠️ **Verificar**: El libro con id=1 ahora debe tener `available = false`

### 2️⃣ Crear Préstamo - Libro No Disponible (Validación)

**POST** `{{base_url}}/api/loans`

```json
{
    "bookId": 1,
    "memberId": 1
}
```

**Response:** `409 CONFLICT`
```json
{
    "timestamp": "2025-01-20T11:01:00",
    "status": 409,
    "error": "Conflict",
    "message": "El libro 'Cien años de soledad' no está disponible"
}
```

### 3️⃣ Crear Préstamo - Socio Inactivo (Validación)

**POST** `{{base_url}}/api/loans`

```json
{
    "bookId": 2,
    "memberId": 2
}
```

**Response:** `409 CONFLICT`
```json
{
    "timestamp": "2025-01-20T11:02:00",
    "status": 409,
    "error": "Conflict",
    "message": "El socio 'María García López' no está activo"
}
```

> ⚠️ **Nota**: María fue desactivada en el paso anterior de Members

### 4️⃣ Crear Segundo Préstamo (mismo socio, otro libro)

**POST** `{{base_url}}/api/loans`

```json
{
    "bookId": 2,
    "memberId": 1
}
```

**Response:** `201 CREATED`

### 5️⃣ Listar Todos los Préstamos

**GET** `{{base_url}}/api/loans`

**Response:** `200 OK`

### 6️⃣ Obtener Préstamo por ID

**GET** `{{base_url}}/api/loans/1`

**Response:** `200 OK`

### 7️⃣ Listar Préstamos Activos

**GET** `{{base_url}}/api/loans/active`

**Response:** `200 OK` → Solo préstamos con `status = ACTIVE`

### 8️⃣ Listar Préstamos Vencidos

**GET** `{{base_url}}/api/loans/overdue`

**Response:** `200 OK` → Préstamos con `dueDate` pasada y `status ≠ RETURNED`

### 9️⃣ Historial de Préstamos de un Libro

**GET** `{{base_url}}/api/loans/book/1`

**Response:** `200 OK` → Todos los préstamos (activos y devueltos) del libro

### 🔟 Devolver Libro

**PATCH** `{{base_url}}/api/loans/1/return`

**Response:** `200 OK`
```json
{
    "id": 1,
    "loanDate": "2025-01-20",
    "dueDate": "2025-02-03",
    "returnDate": "2025-01-20",
    "status": "RETURNED",
    "book": {
        "id": 1,
        "title": "Cien años de soledad",
        "isbn": "978-0-06-088328-7"
    },
    "member": {
        "id": 1,
        "name": "Jose Luis Martínez"
    }
}
```

> ⚠️ **Verificar**: El libro con id=1 ahora debe tener `available = true` de nuevo

### 1️⃣1️⃣ Devolver Libro Ya Devuelto (Validación)

**PATCH** `{{base_url}}/api/loans/1/return`

**Response:** `409 CONFLICT`
```json
{
    "timestamp": "2025-01-20T11:10:00",
    "status": 409,
    "error": "Conflict",
    "message": "Este préstamo ya fue devuelto"
}
```

### 1️⃣2️⃣ Eliminar Libro con Préstamo Activo (Validación)

**DELETE** `{{base_url}}/api/books/2`

**Response:** `409 CONFLICT`
```json
{
    "timestamp": "2025-01-20T11:11:00",
    "status": 409,
    "error": "Conflict",
    "message": "No se puede eliminar el libro 'Don Quijote de la Mancha' porque tiene préstamos activos"
}
```

---

## 🧪 Flujo de Pruebas Completo

### Orden Recomendado

1. ✅ Crear 3 libros
2. ✅ Verificar ISBN único
3. ✅ Crear 2 socios
4. ✅ Verificar email único
5. ✅ Crear préstamo → libro pasa a no disponible
6. ✅ Intentar prestar libro no disponible → 409
7. ✅ Desactivar socio → intentar prestar → 409
8. ✅ Crear múltiples préstamos para un socio
9. ✅ Devolver libro → libro vuelve a disponible
10. ✅ Intentar devolver préstamo ya devuelto → 409
11. ✅ Intentar eliminar libro con préstamo activo → 409
12. ✅ Consultar préstamos activos y vencidos
13. ✅ Consultar historial de préstamos de un libro

### Verificaciones en H2 Console

URL: `http://localhost:8080/h2-console`

```sql
-- Ver todos los libros y su disponibilidad
SELECT id, title, isbn, available FROM book;

-- Ver préstamos activos con detalle
SELECT l.id, b.title, m.name, l.loan_date, l.due_date, l.status
FROM loan l
JOIN book b ON l.book_id = b.id
JOIN member m ON l.member_id = m.id
WHERE l.status = 'ACTIVE';

-- Contar préstamos activos por socio
SELECT m.name, COUNT(l.id) as active_loans
FROM member m
LEFT JOIN loan l ON m.id = l.member_id AND l.status = 'ACTIVE'
GROUP BY m.name;
```