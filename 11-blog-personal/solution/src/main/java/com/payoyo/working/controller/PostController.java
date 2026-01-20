package com.payoyo.working.controller;

import com.payoyo.working.models.*;
import com.payoyo.working.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestionar Posts
 * 
 * @RestController: combinación de @Controller + @ResponseBody
 * - Todos los métodos retornan datos (JSON), no vistas (HTML)
 * - Spring convierte automáticamente los objetos a JSON (Jackson)
 * 
 * @RequestMapping("/api/posts"): prefijo base para todos los endpoints
 * - Todos los métodos de este controller empiezan con /api/posts
 * 
 * @RequiredArgsConstructor: inyección de dependencias por constructor (Lombok)
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    // Inyectamos la INTERFAZ, no la implementación (Dependency Inversion Principle)
    // Spring automáticamente inyecta PostServiceImpl (la única implementación)
    private final IPostService postService;

    /**
     * POST /api/posts - Crear nuevo post
     * 
     * @Valid: activa las validaciones Bean Validation de Post
     * - @NotBlank, @Size, etc. se validan automáticamente
     * - Si falla, lanza MethodArgumentNotValidException (capturada por GlobalExceptionHandler)
     * 
     * @RequestBody: convierte JSON del request body a objeto Post
     * 
     * ResponseEntity<Post>: permite control explícito del código HTTP
     * - 201 CREATED: recurso creado exitosamente (estándar REST)
     * - Incluye el post creado en el body de la respuesta
     */
    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody Post post) {
        Post createdPost = postService.createPost(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    /**
     * GET /api/posts - Listar todos los posts
     * 
     * ResponseEntity.ok(): atajo para ResponseEntity con status 200 OK
     * - Equivalente a: ResponseEntity.status(HttpStatus.OK).body(...)
     */
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    /**
     * GET /api/posts/{id} - Obtener post por ID
     * 
     * @PathVariable: extrae {id} de la URL y lo pasa como parámetro
     * Ejemplo: GET /api/posts/5 → id = 5
     * 
     * Si el post no existe, postService.getPostById() lanza ResourceNotFoundException
     * GlobalExceptionHandler la captura y retorna 404 JSON automáticamente
     */
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    /**
     * PUT /api/posts/{id} - Actualizar post existente
     * 
     * Combina @PathVariable (ID del post) y @RequestBody (nuevos datos)
     * @Valid valida los nuevos datos antes de actualizar
     * 
     * Retorna 200 OK con el post actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody Post postDetails) {
        Post updatedPost = postService.updatePost(id, postDetails);
        return ResponseEntity.ok(updatedPost);
    }

    /**
     * DELETE /api/posts/{id} - Eliminar post (y sus comentarios en cascada)
     * 
     * ResponseEntity<Void>: no retorna contenido en el body
     * - 204 NO CONTENT: operación exitosa pero sin body (estándar REST para DELETE)
     * - .noContent().build(): construye ResponseEntity vacío con status 204
     * 
     * IMPORTANTE: Gracias a cascade = CascadeType.ALL en Post.comments,
     * todos los comentarios asociados se eliminan automáticamente
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/posts/author/{author} - Buscar posts por autor
     * 
     * @PathVariable con nombre diferente al parámetro de método
     * Endpoint alternativo: podría ser @RequestParam: GET /api/posts?author=Jose
     * 
     * Retorna lista vacía [] si no hay posts del autor (no error 404)
     */
    @GetMapping("/author/{author}")
    public ResponseEntity<List<Post>> getPostsByAuthor(@PathVariable String author) {
        List<Post> posts = postService.getPostsByAuthor(author);
        return ResponseEntity.ok(posts);
    }

    /**
     * GET /api/posts/search?keyword=spring - Búsqueda por palabra clave en título
     * 
     * @RequestParam: extrae parámetros de query string (?keyword=...)
     * Diferencia con @PathVariable:
     * - @PathVariable: /posts/search/spring (parte de la ruta)
     * - @RequestParam: /posts/search?keyword=spring (parámetro opcional)
     * 
     * Ejemplo: GET /api/posts/search?keyword=Spring Boot
     * → busca posts con "Spring Boot" en el título (case insensitive)
     */
    @GetMapping("/search")
    public ResponseEntity<List<Post>> searchPostsByTitle(@RequestParam String keyword) {
        List<Post> posts = postService.searchPostsByTitle(keyword);
        return ResponseEntity.ok(posts);
    }

    /**
     * GET /api/posts/popular?minComments=5 - Posts con mínimo N comentarios
     * 
     * @RequestParam con defaultValue: si no se envía, usa valor por defecto
     * Ejemplo:
     * - GET /api/posts/popular → minComments = 3 (valor por defecto)
     * - GET /api/posts/popular?minComments=10 → minComments = 10
     * 
     * Útil para obtener "posts populares" o "posts con actividad"
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Post>> getPopularPosts(
            @RequestParam(defaultValue = "3") int minComments) {
        List<Post> posts = postService.getPostsWithMinComments(minComments);
        return ResponseEntity.ok(posts);
    }
}