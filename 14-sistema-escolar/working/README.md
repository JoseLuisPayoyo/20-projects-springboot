# Proyecto 14: Sistema Escolar — Guía de Desarrollo

## 🚀 Inicio Rápido

### 1. Crear el proyecto
Genera un proyecto en [Spring Initializr](https://start.spring.io/) con:
- **Group**: `com.school`
- **Artifact**: `school-system`
- **Dependencies**: Spring Web, Spring Data JPA, H2 Database, Validation, Lombok (opcional)

### 2. Configurar `application.properties`
```properties
# Datasource H2
spring.datasource.url=jdbc:h2:mem:schooldb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Consola H2 (acceder en http://localhost:8080/h2-console)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Formato de fechas en JSON
spring.jackson.serialization.write-dates-as-timestamps=false
```

### 3. Orden de desarrollo recomendado
1. `EnrollmentStatus` (enum)
2. `Student` (entity)
3. `Course` (entity)
4. `Enrollment` (entity — depende de Student y Course)
5. `StudentRepository`
6. `CourseRepository`
7. `EnrollmentRepository` (con queries custom)
8. Excepciones (`ResourceNotFoundException`, `DuplicateEnrollmentException`, `CourseFullException`, `BusinessRuleException`)
9. `GlobalExceptionHandler`
10. `StudentService`
11. `CourseService`
12. `EnrollmentService` (la más compleja, con validaciones de negocio)
13. `StudentController`
14. `CourseController`
15. `EnrollmentController`

---

## 📡 Endpoints y Ejemplos

### Students

**POST /api/students** — Crear estudiante
```json
{
  "firstName": "Ana",
  "lastName": "García",
  "email": "ana.garcia@email.com",
  "birthDate": "2000-03-15"
}
```
Respuesta `201 Created`:
```json
{
  "id": 1,
  "firstName": "Ana",
  "lastName": "García",
  "email": "ana.garcia@email.com",
  "birthDate": "2000-03-15"
}
```

**GET /api/students** — Listar todos
Respuesta `200 OK`:
```json
[
  {
    "id": 1,
    "firstName": "Ana",
    "lastName": "García",
    "email": "ana.garcia@email.com",
    "birthDate": "2000-03-15"
  }
]
```

**GET /api/students/{id}** — Obtener por ID
Respuesta `200 OK` o `404 Not Found`

**PUT /api/students/{id}** — Actualizar
```json
{
  "firstName": "Ana María",
  "lastName": "García López",
  "email": "anamaria@email.com",
  "birthDate": "2000-03-15"
}
```

**DELETE /api/students/{id}** — Eliminar (falla si tiene matrículas activas)

**GET /api/students/{id}/courses** — Cursos del estudiante
Respuesta `200 OK`:
```json
[
  {
    "id": 1,
    "name": "Álgebra Lineal",
    "category": "Matemáticas",
    "maxStudents": 30,
    "active": true
  }
]
```

---

### Courses

**POST /api/courses** — Crear curso
```json
{
  "name": "Álgebra Lineal",
  "category": "Matemáticas",
  "maxStudents": 30
}
```
Respuesta `201 Created`:
```json
{
  "id": 1,
  "name": "Álgebra Lineal",
  "category": "Matemáticas",
  "maxStudents": 30,
  "active": true
}
```

**GET /api/courses** — Listar todos

**GET /api/courses/{id}** — Obtener por ID

**PUT /api/courses/{id}** — Actualizar
```json
{
  "name": "Álgebra Lineal Avanzada",
  "category": "Matemáticas",
  "maxStudents": 25,
  "active": true
}
```

**DELETE /api/courses/{id}** — Eliminar (falla si tiene matrículas)

**GET /api/courses/{id}/students** — Estudiantes del curso

**GET /api/courses/category/{category}** — Cursos por categoría
Ejemplo: `GET /api/courses/category/Matemáticas`

---

### Enrollments

**POST /api/enrollments** — Crear matrícula
```json
{
  "studentId": 1,
  "courseId": 1
}
```
Respuesta `201 Created`:
```json
{
  "id": 1,
  "studentId": 1,
  "studentName": "Ana García",
  "courseId": 1,
  "courseName": "Álgebra Lineal",
  "enrolledAt": "2026-03-23T10:30:00",
  "grade": null,
  "status": "ACTIVE"
}
```
Posibles errores:
- `409`: Matrícula duplicada o curso lleno
- `400`: Curso inactivo
- `404`: Estudiante o curso no encontrado

**PUT /api/enrollments/{id}/grade** — Asignar nota
```json
{
  "grade": 8.5
}
```

**PUT /api/enrollments/{id}/status** — Cambiar estado
```json
{
  "status": "COMPLETED"
}
```
> ⚠️ Solo se puede poner COMPLETED si ya tiene calificación

**GET /api/enrollments/student/{studentId}** — Matrículas de un estudiante

**GET /api/enrollments/course/{courseId}** — Matrículas de un curso

**GET /api/enrollments/course/{courseId}/average** — Nota media
Respuesta `200 OK`:
```json
{
  "courseId": 1,
  "courseName": "Álgebra Lineal",
  "averageGrade": 7.25,
  "totalGraded": 4
}
```

---

## 🧪 Colección Postman

Importa el archivo `School_System.postman_collection.json` incluido en este directorio
para probar todos los endpoints.

---

## 💡 Pistas Clave

- **Unique Constraint** en Enrollment: usa `@Table(uniqueConstraints = ...)` sobre la combinación student + course
- **@JsonIgnore / @JsonManagedReference / @JsonBackReference**: imprescindible para evitar recursión infinita en la serialización JSON
- La nota media se calcula mejor con una **query JPQL con AVG()**
- El campo `enrolledAt` se auto-asigna con `@PrePersist` o en el constructor
- Cuidado con el **fetch lazy**: las colecciones `enrollments` en Student y Course deben ser LAZY (default en OneToMany)