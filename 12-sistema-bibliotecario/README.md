# 📚 Proyecto 12: Sistema de Biblioteca

## 📋 Enunciado

Desarrollar una **API REST** para gestionar una biblioteca, implementando un sistema de préstamos de libros con **dos relaciones OneToMany independientes** que convergen en una entidad central (`Loan`). El sistema debe controlar la disponibilidad de libros y limitar el número de préstamos activos por socio.

---

## 🎯 Objetivos de Aprendizaje

- Implementar **dos relaciones OneToMany/ManyToOne** independientes que convergen en una misma entidad
- Gestionar **lógica de negocio compleja** con validaciones cruzadas entre entidades
- Controlar el **estado de disponibilidad** de un recurso mediante operaciones de préstamo/devolución
- Aplicar **@Transactional** en operaciones que modifican múltiples entidades
- Manejar **consultas derivadas** y **@Query** para búsquedas específicas de negocio

---

## 🏗️ Arquitectura del Sistema

### Entidades y Relaciones

```
┌──────────┐       ┌──────────┐       ┌──────────┐
│  Member   │       │   Loan   │       │   Book   │
│──────────│       │──────────│       │──────────│
│ id       │ 1───* │ id       │ *───1 │ id       │
│ name     │       │ loanDate │       │ title    │
│ email    │       │ dueDate  │       │ author   │
│ phone    │       │ returnDt │       │ isbn     │
│ memberDt │       │ status   │       │ genre    │
│ active   │       │ member   │       │ pubYear  │
│ loans[]  │       │ book     │       │ available│
└──────────┘       └──────────┘       │ loans[]  │
                                      └──────────┘
```

### Relaciones JPA

| Relación | Tipo | Descripción |
|----------|------|-------------|
| `Member` → `Loan` | `@OneToMany` / `@ManyToOne` | Un socio puede tener muchos préstamos |
| `Book` → `Loan` | `@OneToMany` / `@ManyToOne` | Un libro puede tener muchos préstamos (historial) |

> **Diferencia con Proyecto 11**: Aquí hay **dos padres independientes** (`Member` y `Book`) que apuntan a un mismo hijo (`Loan`). En el Proyecto 11, solo había un padre (`Post`) con un hijo (`Comment`).

---

## 📦 Entidades

### Book (Libro)

| Campo | Tipo | Validaciones | Descripción |
|-------|------|-------------|-------------|
| `id` | `Long` | Auto-generado | Identificador único |
| `title` | `String` | `@NotBlank`, `@Size(min=2, max=200)` | Título del libro |
| `author` | `String` | `@NotBlank`, `@Size(min=2, max=100)` | Autor del libro |
| `isbn` | `String` | `@NotBlank`, `@Pattern(regexp)` | ISBN único (formato: XXX-X-XX-XXXXXX-X) |
| `genre` | `String` | `@NotBlank` | Género literario |
| `publishedYear` | `Integer` | `@Min(1000)`, `@Max(currentYear)` | Año de publicación |
| `available` | `Boolean` | Default `true` | Disponibilidad para préstamo |
| `createdAt` | `LocalDateTime` | Auto-generado | Fecha de registro |
| `loans` | `List<Loan>` | `@OneToMany(mappedBy)` | Historial de préstamos |

### Member (Socio)

| Campo | Tipo | Validaciones | Descripción |
|-------|------|-------------|-------------|
| `id` | `Long` | Auto-generado | Identificador único |
| `name` | `String` | `@NotBlank`, `@Size(min=2, max=100)` | Nombre completo |
| `email` | `String` | `@NotBlank`, `@Email`, `unique` | Correo electrónico |
| `phone` | `String` | `@Pattern(regexp)` | Teléfono (opcional) |
| `membershipDate` | `LocalDate` | Auto-generado | Fecha de alta |
| `active` | `Boolean` | Default `true` | Estado del socio |
| `loans` | `List<Loan>` | `@OneToMany(mappedBy)` | Préstamos del socio |

### Loan (Préstamo)

| Campo | Tipo | Validaciones | Descripción |
|-------|------|-------------|-------------|
| `id` | `Long` | Auto-generado | Identificador único |
| `loanDate` | `LocalDate` | Auto-generado | Fecha del préstamo |
| `dueDate` | `LocalDate` | Auto-calculado (+14 días) | Fecha límite de devolución |
| `returnDate` | `LocalDate` | `null` hasta devolución | Fecha de devolución real |
| `status` | `LoanStatus` | `@Enumerated` | Estado: `ACTIVE`, `RETURNED`, `OVERDUE` |
| `member` | `Member` | `@ManyToOne`, `@JoinColumn` | Socio que realiza el préstamo |
| `book` | `Book` | `@ManyToOne`, `@JoinColumn` | Libro prestado |

### LoanStatus (Enum)

```java
public enum LoanStatus {
    ACTIVE,     // Préstamo en curso
    RETURNED,   // Libro devuelto
    OVERDUE     // Plazo vencido (no devuelto a tiempo)
}
```

---

## 🔧 Reglas de Negocio

### Préstamos

| Regla | Descripción | Error |
|-------|-------------|-------|
| **Libro disponible** | Solo se puede prestar un libro con `available = true` | `409 Conflict` |
| **Socio activo** | Solo socios con `active = true` pueden pedir préstamos | `409 Conflict` |
| **Límite de préstamos** | Máximo **5 préstamos activos** por socio | `409 Conflict` |
| **Préstamo vigente** | Al crear préstamo → `book.available = false` | Automático |
| **Devolución** | Al devolver → `book.available = true`, `status = RETURNED` | Automático |
| **Solo activos** | Solo se pueden devolver préstamos con `status = ACTIVE` | `409 Conflict` |

### Libros

| Regla | Descripción | Error |
|-------|-------------|-------|
| **ISBN único** | No pueden existir dos libros con el mismo ISBN | `409 Conflict` |
| **No eliminar con préstamos activos** | No se puede eliminar un libro que tiene préstamos activos | `409 Conflict` |

### Socios

| Regla | Descripción | Error |
|-------|-------------|-------|
| **Email único** | No pueden existir dos socios con el mismo email | `409 Conflict` |
| **No eliminar con préstamos activos** | No se puede eliminar un socio con préstamos activos | `409 Conflict` |
| **Desactivar socio** | Al desactivar, no se eliminan sus préstamos activos (solo se bloquean nuevos) | Automático |

---

## 🌐 Endpoints

### Books (`/api/books`)

| Método | Endpoint | Descripción | Response |
|--------|----------|-------------|----------|
| `POST` | `/api/books` | Crear libro | `201 Created` |
| `GET` | `/api/books` | Listar todos los libros | `200 OK` |
| `GET` | `/api/books/{id}` | Obtener libro por ID | `200 OK` / `404` |
| `GET` | `/api/books/isbn/{isbn}` | Buscar libro por ISBN | `200 OK` / `404` |
| `GET` | `/api/books/available` | Listar libros disponibles | `200 OK` |
| `GET` | `/api/books/author/{author}` | Buscar por autor | `200 OK` |
| `GET` | `/api/books/genre/{genre}` | Buscar por género | `200 OK` |
| `PUT` | `/api/books/{id}` | Actualizar libro | `200 OK` / `404` |
| `DELETE` | `/api/books/{id}` | Eliminar libro | `204 No Content` / `409` |

### Members (`/api/members`)

| Método | Endpoint | Descripción | Response |
|--------|----------|-------------|----------|
| `POST` | `/api/members` | Registrar socio | `201 Created` |
| `GET` | `/api/members` | Listar todos los socios | `200 OK` |
| `GET` | `/api/members/{id}` | Obtener socio por ID | `200 OK` / `404` |
| `GET` | `/api/members/email/{email}` | Buscar socio por email | `200 OK` / `404` |
| `GET` | `/api/members/{id}/loans` | Obtener préstamos de un socio | `200 OK` / `404` |
| `PUT` | `/api/members/{id}` | Actualizar socio | `200 OK` / `404` |
| `PATCH` | `/api/members/{id}/deactivate` | Desactivar socio | `200 OK` / `404` |
| `DELETE` | `/api/members/{id}` | Eliminar socio | `204 No Content` / `409` |

### Loans (`/api/loans`)

| Método | Endpoint | Descripción | Response |
|--------|----------|-------------|----------|
| `POST` | `/api/loans` | Crear préstamo (body: `bookId`, `memberId`) | `201 Created` / `409` |
| `GET` | `/api/loans` | Listar todos los préstamos | `200 OK` |
| `GET` | `/api/loans/{id}` | Obtener préstamo por ID | `200 OK` / `404` |
| `GET` | `/api/loans/active` | Listar préstamos activos | `200 OK` |
| `GET` | `/api/loans/overdue` | Listar préstamos vencidos | `200 OK` |
| `GET` | `/api/loans/book/{bookId}` | Historial de préstamos de un libro | `200 OK` |
| `PATCH` | `/api/loans/{id}/return` | Devolver libro | `200 OK` / `409` |

---

## ⚙️ Requisitos Técnicos

### Dependencias (`pom.xml`)

```xml
<dependencies>
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
</dependencies>
```

### Configuración (`application.properties`)

```properties
# Nombre de la aplicación
spring.application.name=library-system

# H2 Database
spring.datasource.url=jdbc:h2:mem:librarydb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Formato de fechas en JSON
spring.jackson.serialization.write-dates-as-timestamps=false
```

### Estructura de Paquetes

```
com.library
├── controller/
│   ├── BookController.java
│   ├── MemberController.java
│   └── LoanController.java
├── entity/
│   ├── Book.java
│   ├── Member.java
│   ├── Loan.java
│   └── LoanStatus.java
├── repository/
│   ├── BookRepository.java
│   ├── MemberRepository.java
│   └── LoanRepository.java
├── service/
│   ├── BookService.java
│   ├── MemberService.java
│   └── LoanService.java
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── BusinessRuleException.java
│   └── GlobalExceptionHandler.java
└── LibraryApplication.java
```

---

## 🧪 Flujo de Pruebas Recomendado

1. **Crear libros** → Verificar ISBN único
2. **Crear socios** → Verificar email único
3. **Crear préstamo** → Verificar que el libro pasa a `available = false`
4. **Intentar prestar mismo libro** → Debe dar `409 Conflict`
5. **Crear 5 préstamos para un socio** → Verificar límite
6. **Devolver libro** → Verificar que vuelve a `available = true`
7. **Intentar devolver préstamo ya devuelto** → Debe dar `409 Conflict`
8. **Desactivar socio** → Intentar crear préstamo → `409 Conflict`
9. **Eliminar libro con préstamo activo** → `409 Conflict`
10. **Consultar préstamos vencidos** → Filtrado por fecha

---

## 📂 Estructura del Proyecto

```
proyecto-12-biblioteca/
├── README.md              ← Este archivo
├── working/
│   ├── README.md          ← Guía de inicio + Endpoints + Postman
│   └── postman/
│       └── Proyecto12-Biblioteca.postman_collection.json
└── solution/
    └── README.md          ← Documentación técnica de la solución
```