# Proyecto 11: Blog Personal - Sistema de Posts y Comentarios

## 🎯 Objetivo del Proyecto
Desarrollar un sistema de blog personal que gestione posts y sus comentarios, implementando tu primera relación JPA bidireccional OneToMany/ManyToOne. Este proyecto introduce conceptos fundamentales de relaciones entre entidades y manejo de serialización JSON.

## 📋 Descripción
Sistema REST API para gestionar un blog personal con publicaciones (posts) y sus comentarios. Cada post puede tener múltiples comentarios asociados, y cada comentario pertenece a un único post.

## 🎓 Conceptos de Aprendizaje
### Relaciones JPA
- **@OneToMany / @ManyToOne**: Relación bidireccional
- **mappedBy**: Definición del lado propietario de la relación
- **CascadeType**: Propagación de operaciones (PERSIST, REMOVE, ALL)
- **OrphanRemoval**: Eliminación automática de entidades huérfanas
- **FetchType**: LAZY vs EAGER loading

### Serialización JSON
- **@JsonIgnore**: Prevenir loops infinitos en relaciones bidireccionales
- **@JsonManagedReference / @JsonBackReference**: Alternativa a @JsonIgnore

### Arquitectura
- Estructura en capas (Controller → Service → Repository)
- Operaciones CRUD con entidades relacionadas
- Manejo de relaciones en operaciones de persistencia

## 🏗️ Estructura de Entidades

### Post
```
- id (Long, PK)
- title (String, NOT NULL)
- content (String, NOT NULL)
- author (String, NOT NULL)
- createdAt (LocalDateTime)
- updatedAt (LocalDateTime)
- comments (List<Comment>, OneToMany)
```

### Comment
```
- id (Long, PK)
- content (String, NOT NULL)
- author (String, NOT NULL)
- createdAt (LocalDateTime)
- post (Post, ManyToOne)
```

## 🔗 Relación
**Post ← (1:N) → Comment**
- Un Post tiene muchos Comments
- Un Comment pertenece a un solo Post
- Relación bidireccional (ambas entidades conocen la relación)
- Cascade: Operaciones en Post afectan a Comments
- OrphanRemoval: Comentarios sin Post se eliminan automáticamente

## ⚙️ Requisitos Técnicos

### Dependencias
```xml
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- H2 Database
- Lombok
- Spring Boot Starter Validation
```

### Configuración (application.properties)
```properties
spring.application.name=blog-personal
spring.datasource.url=jdbc:h2:mem:blogdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

## 📡 Endpoints REST

### Posts
- `POST /api/posts` - Crear post
- `GET /api/posts` - Listar todos los posts
- `GET /api/posts/{id}` - Obtener post por ID
- `PUT /api/posts/{id}` - Actualizar post
- `DELETE /api/posts/{id}` - Eliminar post (y sus comentarios en cascada)

### Comments
- `POST /api/posts/{postId}/comments` - Crear comentario en un post
- `GET /api/posts/{postId}/comments` - Listar comentarios de un post
- `GET /api/comments/{id}` - Obtener comentario por ID
- `PUT /api/comments/{id}` - Actualizar comentario
- `DELETE /api/comments/{id}` - Eliminar comentario

## 🎯 Funcionalidades Clave

### Gestión de Posts
- Crear posts con título, contenido y autor
- Actualizar información del post
- Eliminar post (elimina automáticamente sus comentarios)
- Consultar posts con/sin sus comentarios

### Gestión de Comentarios
- Añadir comentarios a un post existente
- Actualizar contenido de comentarios
- Eliminar comentarios individuales
- Consultar comentarios por post

### Validaciones
- Title: no vacío, longitud mínima 5 caracteres
- Content (Post): no vacío, longitud mínima 10 caracteres
- Author: no vacío, longitud mínima 3 caracteres
- Content (Comment): no vacío, longitud mínima 3 caracteres
- Post existente al crear comentario

## 🔍 Casos de Uso

### Caso 1: Crear Post con Comentarios
```
1. Crear un post nuevo
2. Añadir comentarios al post
3. Consultar el post con todos sus comentarios
```

### Caso 2: Eliminar Post
```
1. Crear post con varios comentarios
2. Eliminar el post
3. Verificar que los comentarios también se eliminaron (cascade)
```

### Caso 3: Eliminar Comentario Individual
```
1. Crear post con comentarios
2. Eliminar un comentario específico
3. Verificar que el post sigue existiendo con los demás comentarios
```

## ⚠️ Consideraciones Importantes

### Prevención de Loops Infinitos
- Usar `@JsonIgnore` en el lado ManyToOne (Comment.post)
- Alternativamente: `@JsonManagedReference` y `@JsonBackReference`

### Cascade Configuration
- `CascadeType.ALL` en Post.comments para propagar operaciones
- `orphanRemoval = true` para eliminar comentarios huérfanos

### Fetch Strategy
- `FetchType.LAZY` recomendado para evitar cargas innecesarias
- Considerar FetchType.EAGER solo si siempre necesitas los comentarios

## 🚀 Flujo de Desarrollo
1. **Entidades**: Post, Comment (con relaciones JPA)
2. **Repositories**: PostRepository, CommentRepository
3. **Services**: PostService, CommentService (lógica de negocio)
4. **Controllers**: PostController, CommentController (endpoints REST)
5. **Pruebas**: Postman collection para validar operaciones

## 📚 Stack Tecnológico
- Java 17+
- Spring Boot 3.x
- Spring Data JPA
- H2 Database
- Lombok
- Maven

## 🎓 Aprendizajes Esperados
- Implementar relaciones JPA OneToMany/ManyToOne
- Configurar cascade y orphanRemoval
- Prevenir loops infinitos en serialización JSON
- Gestionar entidades relacionadas en operaciones CRUD
- Entender el lado propietario de relaciones bidireccionales