# Blog Personal - Solución Completa

## 🎯 Implementación de Relaciones JPA

### Análisis de la Relación OneToMany/ManyToOne

#### Características de la Relación
- **Tipo**: Bidireccional OneToMany/ManyToOne
- **Lado Propietario**: Comment (ManyToOne) - contiene la FK
- **Lado Inverso**: Post (OneToMany) - usa mappedBy
- **Cascade**: CascadeType.ALL desde Post hacia Comments
- **OrphanRemoval**: true - elimina comentarios sin post

#### Diseño de Base de Datos
```sql
-- Tabla POSTS
CREATE TABLE posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Tabla COMMENTS
CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    created_at TIMESTAMP,
    post_id BIGINT,  -- FK hacia posts
    FOREIGN KEY (post_id) REFERENCES posts(id)
);
```

## 📦 Entidades Implementadas

### Post.java
```java
@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El título no puede estar vacío")
    @Size(min = 5, message = "El título debe tener al menos 5 caracteres")
    private String title;
    
    @NotBlank(message = "El contenido no puede estar vacío")
    @Size(min = 10, message = "El contenido debe tener al menos 10 caracteres")
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @NotBlank(message = "El autor no puede estar vacío")
    @Size(min = 3, message = "El autor debe tener al menos 3 caracteres")
    private String author;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relación OneToMany con Comment
    // mappedBy: indica que Comment.post es el propietario de la relación
    // cascade: operaciones en Post se propagan a Comments
    // orphanRemoval: elimina comments que pierden la referencia al post
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Métodos helper para mantener sincronizada la relación bidireccional
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }
    
    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }
}
```

**Decisiones Técnicas**:
- **@OneToMany(mappedBy = "post")**: Post no es dueño de la relación, solo referencia
- **CascadeType.ALL**: Todas las operaciones (PERSIST, MERGE, REMOVE, etc.) se propagan
- **orphanRemoval = true**: Si un comment pierde su post, se elimina automáticamente
- **List<Comment> = new ArrayList<>()**: Evita NullPointerException, inicialización segura
- **Métodos helper**: addComment/removeComment mantienen ambos lados sincronizados

---

### Comment.java
```java
@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El contenido del comentario no puede estar vacío")
    @Size(min = 3, message = "El comentario debe tener al menos 3 caracteres")
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @NotBlank(message = "El autor no puede estar vacío")
    @Size(min = 3, message = "El autor debe tener al menos 3 caracteres")
    private String author;
    
    private LocalDateTime createdAt;
    
    // Relación ManyToOne con Post
    // Este es el lado propietario de la relación (contiene la FK)
    // FetchType.LAZY: no carga el Post automáticamente, solo cuando se accede
    // @JsonIgnore: evita loop infinito al serializar Comment → Post → Comments → ...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")  // nombre de la columna FK en la tabla comments
    @JsonIgnore
    private Post post;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

**Decisiones Técnicas**:
- **@ManyToOne**: Lado propietario, contiene la FK real en BD
- **FetchType.LAZY**: Optimización, carga el Post solo si se accede (evita N+1 queries)
- **@JoinColumn(name = "post_id")**: Especifica el nombre de la columna FK
- **@JsonIgnore**: CRÍTICO para evitar loops infinitos en serialización JSON
- **No updatedAt**: Los comentarios no se actualizan, solo se crean/eliminan

---

## 🔧 Repositories

### PostRepository.java
```java
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Query methods derivados del nombre
    List<Post> findByAuthor(String author);
    
    List<Post> findByTitleContainingIgnoreCase(String keyword);
    
    // @Query personalizada: encontrar posts con al menos N comentarios
    @Query("SELECT p FROM Post p WHERE SIZE(p.comments) >= :minComments")
    List<Post> findPostsWithMinComments(@Param("minComments") int minComments);
}
```

### CommentRepository.java
```java
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Encontrar comentarios por ID de post
    // Útil para listar comentarios de un post específico
    List<Comment> findByPostId(Long postId);
    
    List<Comment> findByAuthor(String author);
    
    // @Query para encontrar comentarios recientes de un post
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsByPostId(@Param("postId") Long postId);
}
```

---

## 💼 Services

### PostService.java
```java
@Service
@RequiredArgsConstructor  // Inyección por constructor con Lombok
public class PostService {
    
    private final PostRepository postRepository;
    
    // Crear nuevo post (sin comentarios iniciales)
    public Post createPost(Post post) {
        return postRepository.save(post);
    }
    
    // Listar todos los posts
    // Los comentarios se cargan debido a la relación OneToMany
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }
    
    // Obtener post por ID
    // orElseThrow: lanza excepción si no existe
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado con ID: " + id));
    }
    
    // Actualizar post existente
    // Solo actualiza title, content, author - los comentarios permanecen
    public Post updatePost(Long id, Post postDetails) {
        Post post = getPostById(id);
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());
        post.setAuthor(postDetails.getAuthor());
        return postRepository.save(post);
    }
    
    // Eliminar post
    // Gracias a cascade = ALL y orphanRemoval = true,
    // todos los comentarios asociados se eliminan automáticamente
    public void deletePost(Long id) {
        Post post = getPostById(id);
        postRepository.delete(post);
    }
}
```

---

### CommentService.java
```java
@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    
    // Crear comentario en un post específico
    // 1. Verificar que el post existe
    // 2. Asociar el comentario al post
    // 3. Guardar el comentario
    public Comment createComment(Long postId, Comment comment) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado con ID: " + postId));
        
        // Establecer la relación: comment pertenece a este post
        comment.setPost(post);
        
        return commentRepository.save(comment);
    }
    
    // Listar comentarios de un post específico
    public List<Comment> getCommentsByPostId(Long postId) {
        // Verificar que el post existe antes de buscar comentarios
        postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado con ID: " + postId));
        
        return commentRepository.findByPostId(postId);
    }
    
    // Obtener comentario por ID
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado con ID: " + id));
    }
    
    // Actualizar comentario
    // Solo permite cambiar content y author, no el post asociado
    public Comment updateComment(Long id, Comment commentDetails) {
        Comment comment = getCommentById(id);
        comment.setContent(commentDetails.getContent());
        comment.setAuthor(commentDetails.getAuthor());
        return commentRepository.save(comment);
    }
    
    // Eliminar comentario individual
    // El post permanece intacto
    public void deleteComment(Long id) {
        Comment comment = getCommentById(id);
        commentRepository.delete(comment);
    }
}
```

**Decisiones Técnicas en Services**:
- **@RequiredArgsConstructor**: Inyección de dependencias por constructor (mejor práctica)
- **Validación de existencia**: Todos los métodos verifican que la entidad existe
- **Manejo de relaciones**: createComment establece explícitamente comment.setPost(post)
- **Transacciones implícitas**: @Service marca los métodos como @Transactional por defecto

---

## 🎮 Controllers

### PostController.java
```java
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Validated  // Habilita validaciones Bean Validation
public class PostController {
    
    private final PostService postService;
    
    // POST /api/posts - Crear nuevo post
    // @Valid activa las validaciones en Post (@NotBlank, @Size, etc.)
    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody Post post) {
        Post createdPost = postService.createPost(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }
    
    // GET /api/posts - Listar todos los posts
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }
    
    // GET /api/posts/{id} - Obtener post por ID
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }
    
    // PUT /api/posts/{id} - Actualizar post
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody Post post) {
        return ResponseEntity.ok(postService.updatePost(id, post));
    }
    
    // DELETE /api/posts/{id} - Eliminar post
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

### CommentController.java
```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class CommentController {
    
    private final CommentService commentService;
    
    // POST /api/posts/{postId}/comments - Crear comentario en un post
    // Endpoint anidado: refleja la relación jerárquica Post → Comments
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Comment> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody Comment comment) {
        Comment createdComment = commentService.createComment(postId, comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }
    
    // GET /api/posts/{postId}/comments - Listar comentarios de un post
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }
    
    // GET /api/comments/{id} - Obtener comentario por ID
    // Endpoint directo (no anidado) para acceso individual
    @GetMapping("/comments/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }
    
    // PUT /api/comments/{id} - Actualizar comentario
    @PutMapping("/comments/{id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody Comment comment) {
        return ResponseEntity.ok(commentService.updateComment(id, comment));
    }
    
    // DELETE /api/comments/{id} - Eliminar comentario
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Decisiones de Diseño API**:
- **Endpoints Anidados**: `/posts/{postId}/comments` refleja la relación jerárquica
- **Endpoints Directos**: `/comments/{id}` para operaciones individuales
- **ResponseEntity**: Control explícito de códigos HTTP (201 Created, 204 No Content)
- **@Valid**: Activa validaciones Bean Validation automáticamente

---

## 🔍 Conceptos Clave Implementados

### 1. Relación Bidireccional
```java
// Post conoce a Comment
@OneToMany(mappedBy = "post")
private List<Comment> comments;

// Comment conoce a Post
@ManyToOne
@JsonIgnore  // CRÍTICO: evita loop infinito
private Post post;
```

### 2. Cascade Operations
```java
Post post = new Post("Título", "Contenido", "Autor");
Comment c1 = new Comment("Comentario 1", "User1");
Comment c2 = new Comment("Comentario 2", "User2");

post.addComment(c1);  // Método helper
post.addComment(c2);

postRepository.save(post);  // Guarda post Y comments (cascade)
```

### 3. Orphan Removal
```java
Post post = postRepository.findById(1L).get();
Comment comment = post.getComments().get(0);

post.removeComment(comment);  // Método helper
postRepository.save(post);  // comment se elimina automáticamente
```

### 4. Lazy Loading
```java
// Comment se carga sin el Post (LAZY)
Comment comment = commentRepository.findById(1L).get();
// comment.post está como proxy hasta que se accede

// Acceso dentro de transacción
String postTitle = comment.getPost().getTitle();  // Carga el Post ahora
```

---

## ⚠️ Problemas Comunes y Soluciones

### Loop Infinito JSON
**Problema**: Post → Comments → Post → Comments → ...
```java
// ❌ SIN @JsonIgnore
@ManyToOne
private Post post;  // Loop infinito al serializar

// ✅ CON @JsonIgnore
@ManyToOne
@JsonIgnore
private Post post;  // Rompe el loop
```

### Lazy Loading Exception
**Problema**: Acceder a `comment.getPost()` fuera de transacción
```java
// ❌ Fuera de transacción
Comment comment = commentRepository.findById(1L).get();
// ... transacción cerrada ...
String title = comment.getPost().getTitle();  // LazyInitializationException

// ✅ Dentro de transacción o FetchType.EAGER
@ManyToOne(fetch = FetchType.EAGER)  // Solo si SIEMPRE necesitas el post
private Post post;
```

### Comentarios Huérfanos
**Problema**: Comentarios sin post permanecen en BD
```java
// ❌ Sin orphanRemoval
@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
private List<Comment> comments;

// ✅ Con orphanRemoval
@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Comment> comments;
```

---

## 📊 Diagramas

### Diagrama de Clases
```
┌─────────────────┐          ┌──────────────────┐
│      Post       │          │     Comment      │
├─────────────────┤          ├──────────────────┤
│ - id            │◄────────┤│ - id             │
│ - title         │ 1     * ││ - content        │
│ - content       │          ││ - author         │
│ - author        │          ││ - createdAt      │
│ - createdAt     │          ││ + post           │
│ - updatedAt     │          │└──────────────────┘
│ + comments      │
└─────────────────┘
```

### Flujo de Cascade Delete
```
1. DELETE Post (id=1)
   └─> 2. Cascade a Comments
       ├─> DELETE Comment (id=1, post_id=1)
       ├─> DELETE Comment (id=2, post_id=1)
       └─> DELETE Comment (id=3, post_id=1)
```

---

## 🎯 Checklist de Implementación

- [x] Entidades Post y Comment con relación bidireccional
- [x] @OneToMany en Post con cascade y orphanRemoval
- [x] @ManyToOne en Comment con @JsonIgnore
- [x] Validaciones Bean Validation (@NotBlank, @Size)
- [x] Repositories con query methods
- [x] Services con lógica de relaciones
- [x] Controllers con endpoints RESTful
- [x] Métodos helper (addComment, removeComment)
- [x] Timestamps automáticos (@PrePersist, @PreUpdate)
- [x] Manejo de errores (orElseThrow)

---

## 🚀 Mejoras Futuras (Proyectos 16-20)
- [ ] DTOs para separar modelo de BD y API
- [ ] Testing unitario (Mockito) y de integración
- [ ] Paginación y ordenamiento de posts/comments
- [ ] Búsqueda avanzada con especificaciones JPA
- [ ] Auditoría con @CreatedBy, @LastModifiedBy