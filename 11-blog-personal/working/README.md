# Blog Personal - Proyecto Working

## 🚀 Guía de Inicio Rápido

### 1. Configuración Inicial
Este proyecto utiliza **H2 Database** en memoria. La configuración ya está lista en `application.properties`.

### 2. Estructura del Proyecto
```
src/main/java/com/blogpersonal/
├── entity/
│   ├── Post.java       # Entidad principal
│   └── Comment.java    # Entidad dependiente
├── repository/
│   ├── PostRepository.java
│   └── CommentRepository.java
├── service/
│   ├── PostService.java
│   └── CommentService.java
└── controller/
    ├── PostController.java
    └── CommentController.java
```

### 3. Orden de Desarrollo
1. **Entidades**: Post → Comment (configurar relación)
2. **Repositories**: Interfaces JPA
3. **Services**: Lógica de negocio
4. **Controllers**: Endpoints REST

## 📡 Endpoints Esperados

### Posts Management

#### 1. Crear Post
```http
POST http://localhost:8080/api/posts
Content-Type: application/json

{
    "title": "Mi primer post sobre Spring Boot",
    "content": "Este es el contenido de mi primer post donde explico conceptos básicos de Spring Boot y JPA.",
    "author": "Jose Luis"
}
```

#### 2. Listar Todos los Posts
```http
GET http://localhost:8080/api/posts
```

#### 3. Obtener Post por ID
```http
GET http://localhost:8080/api/posts/1
```

#### 4. Actualizar Post
```http
PUT http://localhost:8080/api/posts/1
Content-Type: application/json

{
    "title": "Mi primer post sobre Spring Boot - Actualizado",
    "content": "Contenido actualizado con más información sobre relaciones JPA.",
    "author": "Jose Luis"
}
```

#### 5. Eliminar Post
```http
DELETE http://localhost:8080/api/posts/1
```

### Comments Management

#### 1. Crear Comentario en un Post
```http
POST http://localhost:8080/api/posts/1/comments
Content-Type: application/json

{
    "content": "Excelente explicación sobre Spring Boot",
    "author": "María García"
}
```

#### 2. Listar Comentarios de un Post
```http
GET http://localhost:8080/api/posts/1/comments
```

#### 3. Obtener Comentario por ID
```http
GET http://localhost:8080/api/comments/1
```

#### 4. Actualizar Comentario
```http
PUT http://localhost:8080/api/comments/1
Content-Type: application/json

{
    "content": "Excelente y muy detallada explicación sobre Spring Boot y JPA",
    "author": "María García"
}
```

#### 5. Eliminar Comentario
```http
DELETE http://localhost:8080/api/comments/1
```

## 🧪 Colección Postman

### Importar Colección
1. Descarga: [blog-personal.postman_collection.json](./blog-personal.postman_collection.json)
2. Importa en Postman: File → Import
3. Ejecuta los requests en orden

### Escenarios de Prueba

#### Escenario 1: Flujo Completo
```
1. Crear Post → guardar {post_id}
2. Crear 3 comentarios en ese post
3. Listar comentarios del post
4. Obtener post por ID (ver comentarios)
5. Actualizar un comentario
6. Eliminar un comentario
7. Verificar que el post sigue con 2 comentarios
```

#### Escenario 2: Cascade Delete
```
1. Crear Post → guardar {post_id}
2. Crear 5 comentarios en ese post
3. Eliminar el post
4. Verificar que los comentarios también se eliminaron
```

#### Escenario 3: Validaciones
```
1. Intentar crear post sin título → Error 400
2. Intentar crear comentario con content vacío → Error 400
3. Intentar crear comentario en post inexistente → Error 404
```

## 🎯 Puntos Clave de Implementación

### Relación Bidireccional
```java
// Post.java
@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Comment> comments = new ArrayList<>();

// Comment.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "post_id")
@JsonIgnore  // Prevenir loop infinito
private Post post;
```

### Cascade en Acción
- Crear comentarios: Asociar al post existente
- Eliminar post: Comentarios se eliminan automáticamente
- Eliminar comentario: Post permanece intacto

### Prevención de Loops JSON
- `@JsonIgnore` en Comment.post evita serialización infinita
- Al consultar Post, se incluyen sus comments
- Al consultar Comment, NO se incluye el post completo

## ⚠️ Errores Comunes a Evitar

1. **Loop Infinito JSON**: Olvidar @JsonIgnore
2. **Comentarios Huérfanos**: No configurar orphanRemoval = true
3. **Lazy Loading**: Acceder a comments fuera de transacción
4. **Cascade Incorrecto**: Operaciones no propagadas correctamente

## 📝 Notas de Desarrollo
- Los timestamps (createdAt, updatedAt) se gestionan con @PrePersist/@PreUpdate
- La relación es bidireccional: ambas entidades conocen la relación
- El lado ManyToOne (Comment) es el propietario de la relación
- El campo `post_id` se crea automáticamente en la tabla comments