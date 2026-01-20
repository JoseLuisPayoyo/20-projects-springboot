# Colección Postman - Blog Personal API

## 📋 Configuración Inicial

### Variables de Entorno
```
base_url = http://localhost:8080
```

### Headers Globales
```
Content-Type: application/json
Accept: application/json
```

---

## 📂 POSTS - Gestión de Publicaciones

### 1️⃣ Crear Post
**POST** `{{base_url}}/api/posts`

**Body (JSON):**
```json
{
    "title": "Mi primer post sobre Spring Boot",
    "content": "Este es el contenido de mi primer post donde explico conceptos básicos de Spring Boot y JPA. Aquí profundizo en las relaciones OneToMany y ManyToOne.",
    "author": "Jose Luis"
}
```

**Expected Response:** `201 CREATED`
```json
{
    "id": 1,
    "title": "Mi primer post sobre Spring Boot",
    "content": "Este es el contenido de mi primer post donde explico conceptos básicos de Spring Boot y JPA. Aquí profundizo en las relaciones OneToMany y ManyToOne.",
    "author": "Jose Luis",
    "createdAt": "2025-01-20T10:30:45.123",
    "updatedAt": "2025-01-20T10:30:45.123",
    "comments": []
}
```

**Tests (Postman):**
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Post has ID", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.exist;
    pm.environment.set("post_id", jsonData.id);
});
```

---

### 2️⃣ Crear Post - Título Corto (Validación)
**POST** `{{base_url}}/api/posts`

**Body (JSON):**
```json
{
    "title": "Short",
    "content": "Contenido válido con más de diez caracteres",
    "author": "Jose Luis"
}
```

**Expected Response:** `400 BAD REQUEST`
```json
{
    "timestamp": "2025-01-20T10:32:15.456",
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "path": "/api/posts",
    "validationErrors": {
        "title": "El título debe tener al menos 5 caracteres"
    }
}
```

---

### 3️⃣ Crear Post - Sin Autor (Validación)
**POST** `{{base_url}}/api/posts`

**Body (JSON):**
```json
{
    "title": "Post sin autor para prueba de validación",
    "content": "Este post no tiene autor y debe fallar la validación",
    "author": ""
}
```

**Expected Response:** `400 BAD REQUEST`
```json
{
    "timestamp": "2025-01-20T10:33:20.789",
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "path": "/api/posts",
    "validationErrors": {
        "author": "El autor no puede estar vacío"
    }
}
```

---

### 4️⃣ Listar Todos los Posts
**GET** `{{base_url}}/api/posts`

**Expected Response:** `200 OK`
```json
[
    {
        "id": 1,
        "title": "Mi primer post sobre Spring Boot",
        "content": "Este es el contenido de mi primer post...",
        "author": "Jose Luis",
        "createdAt": "2025-01-20T10:30:45.123",
        "updatedAt": "2025-01-20T10:30:45.123",
        "comments": []
    },
    {
        "id": 2,
        "title": "Introducción a JPA y Relaciones",
        "content": "En este post exploramos las relaciones bidireccionales...",
        "author": "María García",
        "createdAt": "2025-01-20T10:35:10.456",
        "updatedAt": "2025-01-20T10:35:10.456",
        "comments": []
    }
]
```

---

### 5️⃣ Obtener Post por ID
**GET** `{{base_url}}/api/posts/{{post_id}}`

**Expected Response:** `200 OK`
```json
{
    "id": 1,
    "title": "Mi primer post sobre Spring Boot",
    "content": "Este es el contenido de mi primer post...",
    "author": "Jose Luis",
    "createdAt": "2025-01-20T10:30:45.123",
    "updatedAt": "2025-01-20T10:30:45.123",
    "comments": [
        {
            "id": 1,
            "content": "Excelente explicación sobre Spring Boot",
            "author": "María García",
            "createdAt": "2025-01-20T10:40:30.789"
        }
    ]
}
```

---

### 6️⃣ Obtener Post Inexistente
**GET** `{{base_url}}/api/posts/999`

**Expected Response:** `404 NOT FOUND`
```json
{
    "timestamp": "2025-01-20T10:45:15.123",
    "status": 404,
    "error": "Not Found",
    "message": "Post no encontrado con id: 999",
    "path": "/api/posts/999"
}
```

---

### 7️⃣ Actualizar Post
**PUT** `{{base_url}}/api/posts/{{post_id}}`

**Body (JSON):**
```json
{
    "title": "Mi primer post sobre Spring Boot - Actualizado",
    "content": "Contenido actualizado con información adicional sobre relaciones JPA y cascade operations. Ahora incluye ejemplos prácticos de OneToMany y ManyToOne.",
    "author": "Jose Luis"
}
```

**Expected Response:** `200 OK`
```json
{
    "id": 1,
    "title": "Mi primer post sobre Spring Boot - Actualizado",
    "content": "Contenido actualizado con información adicional sobre relaciones JPA...",
    "author": "Jose Luis",
    "createdAt": "2025-01-20T10:30:45.123",
    "updatedAt": "2025-01-20T10:50:22.456",
    "comments": [
        {
            "id": 1,
            "content": "Excelente explicación sobre Spring Boot",
            "author": "María García",
            "createdAt": "2025-01-20T10:40:30.789"
        }
    ]
}
```

**Tests (Postman):**
```javascript
pm.test("UpdatedAt is different from CreatedAt", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.updatedAt).to.not.equal(jsonData.createdAt);
});
```

---

### 8️⃣ Buscar Posts por Autor
**GET** `{{base_url}}/api/posts/author/Jose Luis`

**Expected Response:** `200 OK`
```json
[
    {
        "id": 1,
        "title": "Mi primer post sobre Spring Boot - Actualizado",
        "content": "Contenido actualizado...",
        "author": "Jose Luis",
        "createdAt": "2025-01-20T10:30:45.123",
        "updatedAt": "2025-01-20T10:50:22.456",
        "comments": [...]
    }
]
```

---

### 9️⃣ Buscar Posts por Palabra Clave
**GET** `{{base_url}}/api/posts/search?keyword=Spring`

**Expected Response:** `200 OK`
```json
[
    {
        "id": 1,
        "title": "Mi primer post sobre Spring Boot - Actualizado",
        "content": "Contenido actualizado...",
        "author": "Jose Luis",
        "createdAt": "2025-01-20T10:30:45.123",
        "updatedAt": "2025-01-20T10:50:22.456",
        "comments": [...]
    }
]
```

---

### 🔟 Obtener Posts Populares (con mínimo de comentarios)
**GET** `{{base_url}}/api/posts/popular?minComments=2`

**Expected Response:** `200 OK`
```json
[
    {
        "id": 1,
        "title": "Mi primer post sobre Spring Boot - Actualizado",
        "content": "Contenido actualizado...",
        "author": "Jose Luis",
        "createdAt": "2025-01-20T10:30:45.123",
        "updatedAt": "2025-01-20T10:50:22.456",
        "comments": [
            {
                "id": 1,
                "content": "Excelente explicación sobre Spring Boot",
                "author": "María García",
                "createdAt": "2025-01-20T10:40:30.789"
            },
            {
                "id": 2,
                "content": "Me ha ayudado mucho este post",
                "author": "Pedro Martínez",
                "createdAt": "2025-01-20T10:42:15.123"
            }
        ]
    }
]
```

---

### 1️⃣1️⃣ Eliminar Post (y comentarios en cascada)
**DELETE** `{{base_url}}/api/posts/{{post_id}}`

**Expected Response:** `204 NO CONTENT`
```
(Sin body - respuesta vacía)
```

**Tests (Postman):**
```javascript
pm.test("Status code is 204", function () {
    pm.response.to.have.status(204);
});
```

---

## 💬 COMMENTS - Gestión de Comentarios

### 1️⃣ Crear Comentario en un Post
**POST** `{{base_url}}/api/posts/{{post_id}}/comments`

**Body (JSON):**
```json
{
    "content": "Excelente explicación sobre Spring Boot y las relaciones JPA",
    "author": "María García"
}
```

**Expected Response:** `201 CREATED`
```json
{
    "id": 1,
    "content": "Excelente explicación sobre Spring Boot y las relaciones JPA",
    "author": "María García",
    "createdAt": "2025-01-20T10:40:30.789"
}
```

**Tests (Postman):**
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Comment has ID", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.exist;
    pm.environment.set("comment_id", jsonData.id);
});
```

---

### 2️⃣ Crear Comentario - Contenido Corto (Validación)
**POST** `{{base_url}}/api/posts/{{post_id}}/comments`

**Body (JSON):**
```json
{
    "content": "Ok",
    "author": "María García"
}
```

**Expected Response:** `400 BAD REQUEST`
```json
{
    "timestamp": "2025-01-20T10:42:00.123",
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "path": "/api/posts/1/comments",
    "validationErrors": {
        "content": "El comentario debe tener al menos 3 caracteres"
    }
}
```

---

### 3️⃣ Crear Comentario en Post Inexistente
**POST** `{{base_url}}/api/posts/999/comments`

**Body (JSON):**
```json
{
    "content": "Este comentario no se puede crear porque el post no existe",
    "author": "María García"
}
```

**Expected Response:** `404 NOT FOUND`
```json
{
    "timestamp": "2025-01-20T10:43:30.456",
    "status": 404,
    "error": "Not Found",
    "message": "Post no encontrado con id: 999",
    "path": "/api/posts/999/comments"
}
```

---

### 4️⃣ Listar Comentarios de un Post
**GET** `{{base_url}}/api/posts/{{post_id}}/comments`

**Expected Response:** `200 OK`
```json
[
    {
        "id": 3,
        "content": "Muy útil la información sobre cascade operations",
        "author": "Ana López",
        "createdAt": "2025-01-20T10:45:20.789"
    },
    {
        "id": 2,
        "content": "Me ha ayudado mucho este post",
        "author": "Pedro Martínez",
        "createdAt": "2025-01-20T10:42:15.123"
    },
    {
        "id": 1,
        "content": "Excelente explicación sobre Spring Boot y las relaciones JPA",
        "author": "María García",
        "createdAt": "2025-01-20T10:40:30.789"
    }
]
```

**Nota:** Los comentarios están ordenados por `createdAt DESC` (más recientes primero)

---

### 5️⃣ Obtener Comentario por ID
**GET** `{{base_url}}/api/comments/{{comment_id}}`

**Expected Response:** `200 OK`
```json
{
    "id": 1,
    "content": "Excelente explicación sobre Spring Boot y las relaciones JPA",
    "author": "María García",
    "createdAt": "2025-01-20T10:40:30.789"
}
```

**Nota:** El campo `post` NO aparece debido a `@JsonIgnore` (prevención de loop infinito)

---

### 6️⃣ Obtener Comentario Inexistente
**GET** `{{base_url}}/api/comments/999`

**Expected Response:** `404 NOT FOUND`
```json
{
    "timestamp": "2025-01-20T10:48:00.123",
    "status": 404,
    "error": "Not Found",
    "message": "Comment no encontrado con id: 999",
    "path": "/api/comments/999"
}
```

---

### 7️⃣ Actualizar Comentario
**PUT** `{{base_url}}/api/comments/{{comment_id}}`

**Body (JSON):**
```json
{
    "content": "Excelente y muy detallada explicación sobre Spring Boot, JPA y las relaciones bidireccionales",
    "author": "María García"
}
```

**Expected Response:** `200 OK`
```json
{
    "id": 1,
    "content": "Excelente y muy detallada explicación sobre Spring Boot, JPA y las relaciones bidireccionales",
    "author": "María García",
    "createdAt": "2025-01-20T10:40:30.789"
}
```

---

### 8️⃣ Buscar Comentarios por Autor
**GET** `{{base_url}}/api/comments/author/María García`

**Expected Response:** `200 OK`
```json
[
    {
        "id": 1,
        "content": "Excelente y muy detallada explicación sobre Spring Boot...",
        "author": "María García",
        "createdAt": "2025-01-20T10:40:30.789"
    },
    {
        "id": 4,
        "content": "Otro comentario de María en un post diferente",
        "author": "María García",
        "createdAt": "2025-01-20T11:15:45.123"
    }
]
```

---

### 9️⃣ Contar Comentarios de un Post
**GET** `{{base_url}}/api/posts/{{post_id}}/comments/count`

**Expected Response:** `200 OK`
```json
5
```

**Nota:** Retorna directamente el número (Long), no un objeto JSON

---

### 🔟 Eliminar Comentario (Post permanece intacto)
**DELETE** `{{base_url}}/api/comments/{{comment_id}}`

**Expected Response:** `204 NO CONTENT`
```
(Sin body - respuesta vacía)
```

**Tests (Postman):**
```javascript
pm.test("Status code is 204", function () {
    pm.response.to.have.status(204);
});
```

---

## 🧪 ESCENARIOS DE PRUEBA COMPLETOS

### Escenario 1: Flujo Completo CRUD Posts + Comments
```
1. POST /api/posts
   → Crear post "Tutorial Spring Boot"
   → Guardar ID en variable {{post_id}}

2. POST /api/posts/{{post_id}}/comments
   → Crear comentario "Excelente tutorial"
   → Guardar ID en variable {{comment_id_1}}

3. POST /api/posts/{{post_id}}/comments
   → Crear comentario "Muy útil"
   → Guardar ID en variable {{comment_id_2}}

4. POST /api/posts/{{post_id}}/comments
   → Crear comentario "Gracias por compartir"
   → Guardar ID en variable {{comment_id_3}}

5. GET /api/posts/{{post_id}}
   → Verificar que el post tiene 3 comentarios

6. GET /api/posts/{{post_id}}/comments
   → Verificar lista de 3 comentarios (ordenados desc)

7. PUT /api/comments/{{comment_id_1}}
   → Actualizar primer comentario

8. DELETE /api/comments/{{comment_id_2}}
   → Eliminar segundo comentario

9. GET /api/posts/{{post_id}}/comments/count
   → Verificar que ahora hay 2 comentarios

10. GET /api/posts/{{post_id}}
    → Confirmar que el post sigue existiendo con 2 comentarios
```

---

### Escenario 2: Cascade Delete (Post → Comments)
```
1. POST /api/posts
   → Crear post "Post con muchos comentarios"
   → Guardar {{post_id}}

2. POST /api/posts/{{post_id}}/comments (x5)
   → Crear 5 comentarios diferentes
   → Guardar IDs: {{comment_1}} ... {{comment_5}}

3. GET /api/posts/{{post_id}}/comments/count
   → Verificar 5 comentarios

4. DELETE /api/posts/{{post_id}}
   → Eliminar el post (204 NO CONTENT)

5. GET /api/comments/{{comment_1}}
   → Verificar que el comentario YA NO EXISTE (404 NOT FOUND)

6. GET /api/comments/{{comment_2}}
   → Verificar que el comentario YA NO EXISTE (404 NOT FOUND)

✅ RESULTADO: Cascade funcionó correctamente
```

---

### Escenario 3: Validaciones Bean Validation
```
1. POST /api/posts
   Body: { "title": "Ab", "content": "...", "author": "..." }
   → Esperado: 400 BAD REQUEST (title < 5 caracteres)

2. POST /api/posts
   Body: { "title": "...", "content": "Short", "author": "..." }
   → Esperado: 400 BAD REQUEST (content < 10 caracteres)

3. POST /api/posts
   Body: { "title": "...", "content": "...", "author": "" }
   → Esperado: 400 BAD REQUEST (author vacío)

4. POST /api/posts
   Body: { "title": "", "content": "", "author": "" }
   → Esperado: 400 BAD REQUEST (múltiples errores de validación)

5. POST /api/posts/1/comments
   Body: { "content": "Ok", "author": "..." }
   → Esperado: 400 BAD REQUEST (content < 3 caracteres)

✅ RESULTADO: Todas las validaciones funcionan correctamente
```

---

### Escenario 4: Búsquedas y Filtros
```
1. Crear 5 posts diferentes:
   - POST 1: "Spring Boot Básico" (autor: Jose Luis)
   - POST 2: "JPA Avanzado" (autor: Jose Luis)
   - POST 3: "React Hooks" (autor: María García)
   - POST 4: "Docker Tips" (autor: Pedro Martínez)
   - POST 5: "Spring Security" (autor: Jose Luis)

2. GET /api/posts/author/Jose Luis
   → Esperado: 3 posts (1, 2, 5)

3. GET /api/posts/search?keyword=Spring
   → Esperado: 2 posts (1, 5)

4. Crear 5 comentarios en POST 1
5. Crear 3 comentarios en POST 2
6. Crear 1 comentario en POST 3

7. GET /api/posts/popular?minComments=3
   → Esperado: 2 posts (POST 1 con 5 comments, POST 2 con 3 comments)

8. GET /api/posts/popular?minComments=5
   → Esperado: 1 post (POST 1 con 5 comments)

✅ RESULTADO: Búsquedas y filtros funcionan correctamente
```

---

### Escenario 5: Manejo de Errores 404
```
1. GET /api/posts/999
   → Esperado: 404 NOT FOUND con mensaje "Post no encontrado con id: 999"

2. GET /api/comments/888
   → Esperado: 404 NOT FOUND con mensaje "Comment no encontrado con id: 888"

3. PUT /api/posts/777
   → Esperado: 404 NOT FOUND

4. DELETE /api/posts/666
   → Esperado: 404 NOT FOUND

5. POST /api/posts/555/comments
   → Esperado: 404 NOT FOUND (post no existe)

6. GET /api/posts/444/comments
   → Esperado: 404 NOT FOUND (post no existe)

✅ RESULTADO: Manejo de errores 404 consistente en toda la API
```

---

## 📊 Datos de Prueba Completos

### Posts de Ejemplo
```json
// Post 1
{
    "title": "Introducción a Spring Boot",
    "content": "Spring Boot es un framework que facilita el desarrollo de aplicaciones Java empresariales mediante configuración automática y convención sobre configuración.",
    "author": "Jose Luis"
}

// Post 2
{
    "title": "Relaciones JPA OneToMany y ManyToOne",
    "content": "Las relaciones bidireccionales en JPA permiten navegar entre entidades en ambas direcciones. Es fundamental configurar correctamente mappedBy, cascade y orphanRemoval.",
    "author": "Jose Luis"
}

// Post 3
{
    "title": "Best Practices en APIs RESTful",
    "content": "Diseñar APIs RESTful requiere seguir convenciones como usar códigos HTTP apropiados, nombres de recursos en plural y estructura consistente de respuestas.",
    "author": "María García"
}

// Post 4
{
    "title": "Docker para Desarrolladores Java",
    "content": "Docker permite empaquetar aplicaciones Java con todas sus dependencias en contenedores portables, facilitando despliegues consistentes en diferentes entornos.",
    "author": "Pedro Martínez"
}

// Post 5
{
    "title": "Spring Security Fundamentals",
    "content": "Spring Security proporciona autenticación y autorización robusta para aplicaciones Spring Boot, incluyendo protección contra vulnerabilidades comunes como CSRF y XSS.",
    "author": "Ana López"
}
```

### Comentarios de Ejemplo
```json
// Comentario 1
{
    "content": "Excelente introducción, muy clara y concisa",
    "author": "María García"
}

// Comentario 2
{
    "content": "Me ha ayudado mucho a entender los conceptos básicos",
    "author": "Pedro Martínez"
}

// Comentario 3
{
    "content": "¿Podrías profundizar más en la configuración de cascade?",
    "author": "Ana López"
}

// Comentario 4
{
    "content": "Muy útil la explicación sobre orphanRemoval",
    "author": "Carlos Ruiz"
}

// Comentario 5
{
    "content": "Aplicaré estos conceptos en mi proyecto actual",
    "author": "Laura Sánchez"
}
```

---

## ✅ Checklist de Pruebas

### Posts
- [ ] Crear post válido → 201 CREATED
- [ ] Crear post con título corto → 400 BAD REQUEST
- [ ] Crear post sin autor → 400 BAD REQUEST
- [ ] Listar todos los posts → 200 OK
- [ ] Obtener post por ID → 200 OK
- [ ] Obtener post inexistente → 404 NOT FOUND
- [ ] Actualizar post → 200 OK (updatedAt cambia)
- [ ] Buscar por autor → 200 OK
- [ ] Buscar por keyword → 200 OK
- [ ] Posts populares (minComments) → 200 OK
- [ ] Eliminar post → 204 NO CONTENT

### Comments
- [ ] Crear comentario válido → 201 CREATED
- [ ] Crear comentario con contenido corto → 400 BAD REQUEST
- [ ] Crear comentario en post inexistente → 404 NOT FOUND
- [ ] Listar comentarios de post → 200 OK (ordenados desc)
- [ ] Obtener comentario por ID → 200 OK (sin campo post)
- [ ] Obtener comentario inexistente → 404 NOT FOUND
- [ ] Actualizar comentario → 200 OK
- [ ] Buscar por autor → 200 OK
- [ ] Contar comentarios de post → 200 OK (retorna número)
- [ ] Eliminar comentario → 204 NO CONTENT (post intacto)

### Relaciones y Cascade
- [ ] Crear post con comentarios → comments aparecen en GET post
- [ ] Eliminar post → comentarios se eliminan en cascada (404)
- [ ] Eliminar comentario → post permanece intacto
- [ ] Post no muestra FK post_id en comentarios (@JsonIgnore)

### Validaciones
- [ ] Todas las validaciones @NotBlank funcionan
- [ ] Todas las validaciones @Size funcionan
- [ ] Response 400 incluye detalles de errores
- [ ] Múltiples errores aparecen en validationErrors

### Errores
- [ ] 404 con mensaje claro y path
- [ ] 400 con validationErrors detallados
- [ ] 500 si ocurre error inesperado (catch-all)
- [ ] Estructura ErrorResponse consistente

---

## 🎯 Orden Recomendado de Ejecución

1. **Setup Inicial** → Crear 2-3 posts
2. **Comentarios** → Añadir comentarios a los posts
3. **Lectura** → Listar posts, obtener por ID, ver comentarios
4. **Búsquedas** → Filtrar por autor, keyword, posts populares
5. **Actualizaciones** → Modificar posts y comentarios
6. **Validaciones** → Probar casos de error 400
7. **Errores 404** → Intentar acceder a recursos inexistentes
8. **Cascade Delete** → Eliminar post y verificar comentarios eliminados
9. **Delete Individual** → Eliminar comentario y verificar post intacto

---

## 📝 Notas Finales

### Variables de Postman a Configurar
```
post_id → Se guarda al crear un post
comment_id → Se guarda al crear un comentario
```

### Acceso a H2 Console (Verificación Manual)
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:blogdb
User: sa
Password: (vacío)
```

### Queries SQL Útiles para Verificar BD
```sql
-- Ver todos los posts
SELECT * FROM posts;

-- Ver todos los comentarios
SELECT * FROM comments;

-- Ver comentarios con su post
SELECT c.id, c.content, c.author, c.post_id, p.title 
FROM comments c 
LEFT JOIN posts p ON c.post_id = p.id;

-- Contar comentarios por post
SELECT p.id, p.title, COUNT(c.id) as num_comments
FROM posts p
LEFT JOIN comments c ON p.id = c.post_id
GROUP BY p.id, p.title;
```

---

**✅ Colección completa lista para importar y ejecutar**

🚀 **¡A probar el proyecto Jose Luis!**