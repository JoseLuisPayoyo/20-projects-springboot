# Proyecto 14: Sistema Escolar — Documentación de la Solución

## 🏗️ Decisiones Arquitectónicas

### 1. ManyToMany descompuesta vs @ManyToMany directa

Se optó por **descomponer la relación ManyToMany** en dos OneToMany con la entidad intermedia `Enrollment`.
Esto es preferible a `@ManyToMany` nativa cuando:
- La relación tiene **atributos propios** (grade, enrolledAt, status)
- Se necesita **consultar la tabla intermedia** directamente
- Se requiere **control fino** sobre el ciclo de vida de la relación

La alternativa `@ManyToMany` solo genera una join table sin atributos, útil solo para relaciones puras.

### 2. ID autoincremental vs Clave compuesta en Enrollment

Se eligió un **ID autoincremental propio** (`Long id`) en lugar de `@EmbeddedId` con clave compuesta
(studentId + courseId). Razones:
- Simplifica las URLs REST: `/api/enrollments/1` vs `/api/enrollments/student/1/course/2`
- Facilita las operaciones CRUD estándar
- La **unicidad student+course** se garantiza con `@UniqueConstraint` a nivel de tabla

### 3. Serialización JSON y prevención de recursión infinita

La relación bidireccional Student ↔ Enrollment ↔ Course causa recursión infinita en Jackson.
Se resuelve con `@JsonIgnore` en el lado "back" de la relación:
- `Student.enrollments` → `@JsonIgnore` (se accede via endpoint dedicado)
- `Course.enrollments` → `@JsonIgnore` (se accede via endpoint dedicado)
- `Enrollment.student` y `Enrollment.course` → se serializan normalmente

> **Nota**: En proyectos con DTOs (Proyecto 16+), este problema desaparece porque
> los DTOs controlan exactamente qué se serializa.

### 4. Validaciones de negocio en Service, no en Controller

Toda la lógica de validación (matrícula duplicada, curso lleno, curso inactivo, nota válida,
estado coherente) reside en `EnrollmentService`. El controller solo delega y gestiona HTTP status.

---

## 📋 Capas y Responsabilidades

### Entity Layer
- **Student**: Entidad con validaciones Bean Validation. `enrollments` con `@JsonIgnore` y fetch LAZY.
- **Course**: Similar a Student. Campo `active` con default `true` via `@PrePersist` o `@Column(columnDefinition)`.
- **Enrollment**: Owning side de ambas relaciones. `enrolledAt` auto-asignado. `status` como enum.
- **EnrollmentStatus**: Enum con valores ACTIVE, COMPLETED, DROPPED.

### Repository Layer
- **StudentRepository**: Queries derivadas estándar.
- **CourseRepository**: `findByCategory()` para filtrar por categoría.
- **EnrollmentRepository**: Queries clave:
  - `existsByStudentIdAndCourseId()` — verificar duplicado
  - `countByCourseIdAndStatus()` — contar plazas ocupadas
  - `findByStudentId()` / `findByCourseId()` — listar matrículas
  - `@Query` con JPQL para nota media: `AVG(e.grade)`
  - `existsByStudentIdAndStatus()` / `existsByCourseIdAndStatus()` — verificar antes de eliminar

### Service Layer
- **StudentService**: CRUD + verificación antes de eliminar (no borrar si tiene matrículas activas).
- **CourseService**: CRUD + endpoint de cursos por categoría + verificación antes de eliminar.
- **EnrollmentService**: La más compleja. Orquesta:
  1. Verificar que student y course existen
  2. Verificar que el curso está activo
  3. Verificar que no hay matrícula duplicada
  4. Verificar que hay plazas disponibles
  5. Crear enrollment con estado ACTIVE y fecha actual
  6. Validar nota en rango al actualizar
  7. Validar coherencia estado-nota al cambiar status

### Controller Layer
- Cada controller delega completamente al service correspondiente.
- Usa `ResponseEntity` con status codes apropiados (201 para create, 204 para delete).
- Los endpoints de relación (`/students/{id}/courses`, `/courses/{id}/students`) están en sus
  respectivos controllers pero delegan al service adecuado.

### Exception Layer
- **ResourceNotFoundException** → 404
- **DuplicateEnrollmentException** → 409 Conflict
- **CourseFullException** → 409 Conflict
- **BusinessRuleException** → 400 Bad Request
- **GlobalExceptionHandler**: `@RestControllerAdvice` con `@ExceptionHandler` para cada tipo.
  También maneja `MethodArgumentNotValidException` para errores de Bean Validation.

---

## 🔍 Queries JPQL Destacadas

### Nota media de un curso
```java
@Query("SELECT AVG(e.grade) FROM Enrollment e WHERE e.course.id = :courseId AND e.grade IS NOT NULL")
Double findAverageGradeByCourseId(@Param("courseId") Long courseId);
```
Se filtra `grade IS NOT NULL` para excluir matrículas sin calificar del cálculo.

### Contar matrículas activas (para verificar plazas)
```java
long countByCourseIdAndStatus(Long courseId, EnrollmentStatus status);
```
Se usa `count` (no `findAll`) para eficiencia — solo necesitamos el número, no las entidades.

---

## ⚡ Patrones Aplicados

| Patrón                     | Dónde                     | Por qué                                    |
|----------------------------|---------------------------|---------------------------------------------|
| Repository Pattern         | Spring Data JPA           | Abstracción de acceso a datos               |
| Service Layer              | *Service classes          | Encapsulación de lógica de negocio          |
| Global Exception Handler   | GlobalExceptionHandler    | Manejo centralizado y consistente de errores|
| Builder / Constructor      | Enrollment creation       | Inicialización controlada de enrolledAt     |
| Enum Strategy              | EnrollmentStatus          | Estados tipados y seguros                   |
| Unique Constraint          | Enrollment table          | Integridad a nivel BD + validación en código|

---

## 🧩 Flujo de Matriculación (Secuencia)

```
Client → POST /api/enrollments {studentId: 1, courseId: 2}
  │
  ├── EnrollmentController.create()
  │     └── EnrollmentService.createEnrollment(studentId, courseId)
  │           ├── studentRepository.findById(1) → ✅ Student encontrado
  │           ├── courseRepository.findById(2)  → ✅ Course encontrado
  │           ├── course.isActive()?            → ✅ Curso activo
  │           ├── enrollmentRepo.existsByStudentIdAndCourseId(1, 2) → ✅ No duplicado
  │           ├── enrollmentRepo.countByCourseIdAndStatus(2, ACTIVE) → ✅ Hay plazas
  │           └── enrollmentRepo.save(new Enrollment(...))
  │
  └── Response: 201 Created + Enrollment JSON
```