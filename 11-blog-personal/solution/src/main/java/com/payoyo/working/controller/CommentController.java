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
 * Controller REST para gestionar Comments
 * 
 * IMPORTANTE: Este controller usa DOS estrategias de routing:
 * 1. Endpoints anidados: /api/posts/{postId}/comments (relación jerárquica)
 * 2. Endpoints directos: /api/comments/{id} (acceso individual)
 * 
 * @RequestMapping("/api"): base común, luego cada método define su ruta completa
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    // Inyectamos la interfaz ICommentService
    private final ICommentService commentService;

    /**
     * POST /api/posts/{postId}/comments - Crear comentario en un post
     * 
     * Endpoint ANIDADO: refleja la relación jerárquica Post → Comments
     * - El comentario pertenece a un post específico
     * - RESTful: la URL muestra la relación de recursos
     * 
     * @PathVariable postId: ID del post donde se crea el comentario
     * @RequestBody comment: datos del comentario (content, author)
     * 
     * Retorna 201 CREATED con el comentario creado (incluye ID y post asociado)
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Comment> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody Comment comment) {
        Comment createdComment = commentService.createComment(postId, comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    /**
     * GET /api/posts/{postId}/comments - Listar comentarios de un post
     * 
     * Endpoint ANIDADO: obtiene todos los comentarios de un post específico
     * Los comentarios se retornan ordenados por fecha descendente (más recientes primero)
     * 
     * Si el post no existe, lanza ResourceNotFoundException → 404 JSON
     * Si el post existe pero no tiene comentarios, retorna lista vacía []
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * GET /api/comments/{id} - Obtener comentario por ID
     * 
     * Endpoint DIRECTO (no anidado): acceso individual a un comentario
     * Útil cuando ya conoces el ID del comentario y no necesitas el contexto del post
     * 
     * Ejemplo de uso: editar un comentario desde una lista general
     */
    @GetMapping("/comments/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable Long id) {
        Comment comment = commentService.getCommentById(id);
        return ResponseEntity.ok(comment);
    }

    /**
     * PUT /api/comments/{id} - Actualizar comentario
     * 
     * Endpoint DIRECTO: actualiza content y author del comentario
     * NO permite cambiar el post asociado (relación inmutable)
     * 
     * @Valid valida que content y author cumplan las restricciones
     */
    @PutMapping("/comments/{id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody Comment commentDetails) {
        Comment updatedComment = commentService.updateComment(id, commentDetails);
        return ResponseEntity.ok(updatedComment);
    }

    /**
     * DELETE /api/comments/{id} - Eliminar comentario
     * 
     * Endpoint DIRECTO: elimina el comentario individual
     * El post asociado permanece intacto (no hay cascade desde Comment hacia Post)
     * 
     * Retorna 204 NO CONTENT (estándar REST para DELETE exitoso)
     */
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/comments/author/{author} - Buscar comentarios por autor
     * 
     * Útil para ver "historial de comentarios de un usuario"
     * Retorna todos los comentarios de ese autor en cualquier post
     */
    @GetMapping("/comments/author/{author}")
    public ResponseEntity<List<Comment>> getCommentsByAuthor(@PathVariable String author) {
        List<Comment> comments = commentService.getCommentsByAuthor(author);
        return ResponseEntity.ok(comments);
    }

    /**
     * GET /api/posts/{postId}/comments/count - Contar comentarios de un post
     * 
     * Endpoint especializado: retorna solo el número (Long), no la lista completa
     * Más eficiente que cargar todos los comentarios solo para contarlos
     * 
     * Ejemplo respuesta: 15 (número de comentarios)
     */
    @GetMapping("/posts/{postId}/comments/count")
    public ResponseEntity<Long> countCommentsByPostId(@PathVariable Long postId) {
        Long count = commentService.countCommentsByPostId(postId);
        return ResponseEntity.ok(count);
    }
}
