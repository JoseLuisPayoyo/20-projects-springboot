# Proyecto 14: Sistema Escolar

## 📋 Enunciado

Desarrollar un **sistema de gestión escolar** que permita administrar estudiantes, cursos y matrículas.
El sistema modela la relación **ManyToMany entre Student y Course** a través de una entidad intermedia
**Enrollment** que almacena información adicional (calificación, fecha de inscripción).

Este patrón —descomponer una ManyToMany en dos OneToMany con entidad intermedia— es el enfoque
profesional cuando la relación necesita atributos propios.

---

## 🎯 Objetivos de Aprendizaje

- Modelar una relación **ManyToMany descompuesta** con entidad intermedia rica en atributos
- Implementar **clave compuesta** con `@EmbeddedId` / `@Embeddable` o ID propio
- Gestionar **cascadas y ciclo de vida** de entidades relacionadas
- Construir **consultas derivadas y JPQL** sobre relaciones complejas
- Manejar **validaciones de negocio** (no duplicar matrícula, límites de plazas, etc.)
- Aplicar **manejo global de excepciones** con `@RestControllerAdvice`

---

## 🏗️ Arquitectura de Entidades

```
┌──────────┐        ┌──────────────┐        ┌──────────┐
│ Student  │ 1    * │  Enrollment  │ *    1  │  Course  │
│          │────────│              │────────│          │
│ id       │        │ id           │        │ id       │
│ firstName│        │ student (FK) │        │ name     │
│ lastName │        │ course (FK)  │        │ category │
│ email    │        │ enrolledAt   │        │ maxStudents│
│ birthDate│        │ grade        │        │ active   │
│          │        │ status       │        │          │
└──────────┘        └──────────────┘        └──────────┘
```

---

## 📦 Entidades y Campos

### Student (Estudiante)
| Campo      | Tipo          | Restricciones                          |
|------------|---------------|----------------------------------------|
| id         | Long          | PK, Auto-generado                      |
| firstName  | String        | No nulo, 2-50 caracteres               |
| lastName   | String        | No nulo, 2-50 caracteres               |
| email      | String        | No nulo, único, formato email válido   |
| birthDate  | LocalDate     | No nulo, debe ser fecha pasada         |
| enrollments| List\<Enrollment\> | OneToMany, mappedBy student        |

### Course (Curso)
| Campo       | Tipo          | Restricciones                         |
|-------------|---------------|---------------------------------------|
| id          | Long          | PK, Auto-generado                     |
| name        | String        | No nulo, 3-100 caracteres             |
| category    | String        | No nulo (ej: "Matemáticas", "Ciencias")|
| maxStudents | Integer       | No nulo, mínimo 1                     |
| active      | Boolean       | No nulo, default true                 |
| enrollments | List\<Enrollment\> | OneToMany, mappedBy course        |

### Enrollment (Matrícula)
| Campo      | Tipo              | Restricciones                        |
|------------|-------------------|--------------------------------------|
| id         | Long              | PK, Auto-generado                    |
| student    | Student (FK)      | ManyToOne, no nulo                   |
| course     | Course (FK)       | ManyToOne, no nulo                   |
| enrolledAt | LocalDateTime     | No nulo, auto-asignado al crear      |
| grade      | Double            | Nullable, rango 0.0 - 10.0          |
| status     | EnrollmentStatus  | Enum: ACTIVE, COMPLETED, DROPPED     |

> **Restricción única**: Un estudiante NO puede matricularse dos veces en el mismo curso
> (constraint unique sobre student_id + course_id).

---

## 🔗 Relaciones JPA

| Relación                     | Tipo       | Dirección     | Detalles                          |
|------------------------------|------------|---------------|-----------------------------------|
| Student → Enrollment         | OneToMany  | Bidireccional | mappedBy="student"                |
| Course → Enrollment          | OneToMany  | Bidireccional | mappedBy="course"                 |
| Enrollment → Student         | ManyToOne  | Owning side   | @JoinColumn(name="student_id")    |
| Enrollment → Course          | ManyToOne  | Owning side   | @JoinColumn(name="course_id")     |

---

## 🌐 Endpoints Requeridos

### StudentController (`/api/students`)
| Método | Ruta                              | Descripción                            |
|--------|-----------------------------------|----------------------------------------|
| POST   | `/api/students`                   | Crear estudiante                       |
| GET    | `/api/students`                   | Listar todos los estudiantes           |
| GET    | `/api/students/{id}`              | Obtener estudiante por ID              |
| PUT    | `/api/students/{id}`              | Actualizar estudiante                  |
| DELETE | `/api/students/{id}`              | Eliminar estudiante                    |
| GET    | `/api/students/{id}/courses`      | Listar cursos de un estudiante         |

### CourseController (`/api/courses`)
| Método | Ruta                              | Descripción                            |
|--------|-----------------------------------|----------------------------------------|
| POST   | `/api/courses`                    | Crear curso                            |
| GET    | `/api/courses`                    | Listar todos los cursos                |
| GET    | `/api/courses/{id}`               | Obtener curso por ID                   |
| PUT    | `/api/courses/{id}`               | Actualizar curso                       |
| DELETE | `/api/courses/{id}`               | Eliminar curso                         |
| GET    | `/api/courses/{id}/students`      | Listar estudiantes de un curso         |
| GET    | `/api/courses/category/{category}`| Listar cursos por categoría            |

### EnrollmentController (`/api/enrollments`)
| Método | Ruta                                        | Descripción                          |
|--------|---------------------------------------------|--------------------------------------|
| POST   | `/api/enrollments`                          | Crear matrícula (body: studentId, courseId) |
| GET    | `/api/enrollments`                          | Listar todas las matrículas          |
| GET    | `/api/enrollments/{id}`                     | Obtener matrícula por ID             |
| PUT    | `/api/enrollments/{id}/grade`               | Asignar/actualizar calificación      |
| PUT    | `/api/enrollments/{id}/status`              | Cambiar estado de matrícula          |
| DELETE | `/api/enrollments/{id}`                     | Eliminar matrícula                   |
| GET    | `/api/enrollments/student/{studentId}`      | Matrículas de un estudiante          |
| GET    | `/api/enrollments/course/{courseId}`         | Matrículas de un curso               |
| GET    | `/api/enrollments/course/{courseId}/average` | Nota media de un curso               |

---

## 🏛️ Validaciones de Negocio

1. **Matrícula duplicada**: No se puede matricular al mismo estudiante en el mismo curso dos veces
2. **Curso lleno**: No se puede matricular si el curso ha alcanzado `maxStudents`
3. **Curso inactivo**: No se puede matricular en un curso con `active = false`
4. **Calificación válida**: La nota debe estar entre 0.0 y 10.0
5. **Estado coherente**: Solo se puede poner COMPLETED si tiene calificación asignada
6. **Eliminación segura**: No se puede eliminar un Student o Course que tenga matrículas activas

---

## ⚠️ Manejo de Excepciones

Implementar `@RestControllerAdvice` con las siguientes excepciones personalizadas:
- `ResourceNotFoundException` — Recurso no encontrado (404)
- `DuplicateEnrollmentException` — Matrícula duplicada (409 Conflict)
- `CourseFullException` — Curso sin plazas disponibles (409 Conflict)
- `BusinessRuleException` — Violación de regla de negocio genérica (400 Bad Request)

---

## 🛠️ Stack Tecnológico

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Web** (REST)
- **Spring Data JPA** (Hibernate)
- **H2 Database** (en memoria, para desarrollo)
- **Bean Validation** (jakarta.validation)
- **Lombok** (opcional, para reducir boilerplate)

---

## 📁 Estructura de Paquetes

```
com.school
├── controller
│   ├── StudentController.java
│   ├── CourseController.java
│   └── EnrollmentController.java
├── entity
│   ├── Student.java
│   ├── Course.java
│   ├── Enrollment.java
│   └── EnrollmentStatus.java
├── repository
│   ├── StudentRepository.java
│   ├── CourseRepository.java
│   └── EnrollmentRepository.java
├── service
│   ├── StudentService.java
│   ├── CourseService.java
│   └── EnrollmentService.java
├── exception
│   ├── ResourceNotFoundException.java
│   ├── DuplicateEnrollmentException.java
│   ├── CourseFullException.java
│   ├── BusinessRuleException.java
│   └── GlobalExceptionHandler.java
└── SchoolApplication.java
```

---

## 📊 Consultas Especiales (Repository)

- Buscar matrículas por estudiante: `findByStudentId(Long studentId)`
- Buscar matrículas por curso: `findByCourseId(Long courseId)`
- Verificar matrícula existente: `existsByStudentIdAndCourseId(Long studentId, Long courseId)`
- Contar matrículas activas de un curso: `countByCourseIdAndStatus(Long courseId, EnrollmentStatus status)`
- Cursos por categoría: `findByCategory(String category)`
- Nota media de un curso (JPQL): `SELECT AVG(e.grade) FROM Enrollment e WHERE e.course.id = :courseId AND e.grade IS NOT NULL`