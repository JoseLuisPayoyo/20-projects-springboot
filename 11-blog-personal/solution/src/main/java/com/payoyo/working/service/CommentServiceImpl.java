package com.payoyo.working.service;

import com.payoyo.working.models.*;
import com.payoyo.working.exception.*;
import com.payoyo.working.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de ICommentService
 * Gestiona la lógica de negocio de comentarios y su relación con posts
 * 
 * IMPORTANTE: Este Service necesita tanto CommentRepository como PostRepository
 * porque los comentarios dependen de la existencia de posts
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {

    // Inyección de ambos repositories (relación entre entidades)
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    /**
     * Crea un comentario en un post específico
     * 
     * Flujo:
     * 1. Verificar que el post existe
     * 2. Asociar el comentario al post (establecer la relación ManyToOne)
     * 3. Guardar el comentario (JPA inserta post_id automáticamente)
     * 
     * NOTA: No usamos post.addComment() aquí porque solo queremos guardar el comment,
     * no modificar la colección del post (evita cargar toda la lista de comments)
     */
    @Override
    @Transactional
    public Comment createComment(Long postId, Comment comment) {
        // Verificar que el post existe (lanza ResourceNotFoundException si no)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        // Establecer la relación: este comment pertenece a este post
        comment.setPost(post);

        // Guardar el comentario
        // @PrePersist establece createdAt automáticamente
        // JPA inserta post_id en la tabla comments
        return commentRepository.save(comment);
    }

    /**
     * Obtiene todos los comentarios de un post (ordenados por fecha desc)
     * 
     * Primero verifica que el post existe (buena práctica)
     * Luego busca los comentarios usando el query method del repository
     */
    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostId(Long postId) {
        // Verificar que el post existe antes de buscar sus comentarios
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        // Buscar comentarios ordenados por fecha descendente (más recientes primero)
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    /**
     * Obtiene un comentario por ID
     */
    @Override
    @Transactional(readOnly = true)
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
    }

    /**
     * Actualiza un comentario existente
     * 
     * Solo permite modificar content y author
     * NO permite cambiar el post asociado (la relación es inmutable una vez creada)
     */
    @Override
    @Transactional
    public Comment updateComment(Long id, Comment commentDetails) {
        Comment comment = getCommentById(id); // Verifica existencia

        // Actualizar solo campos editables
        comment.setContent(commentDetails.getContent());
        comment.setAuthor(commentDetails.getAuthor());
        // NO se modifica comment.post (relación inmutable)

        return commentRepository.save(comment);
    }

    /**
     * Elimina un comentario
     * 
     * El post asociado permanece intacto (no hay cascade desde Comment hacia Post)
     * Solo se elimina el registro en la tabla comments
     */
    @Override
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = getCommentById(id); // Verifica existencia
        commentRepository.delete(comment);
        // El post permanece sin cambios
    }

    /**
     * Busca todos los comentarios de un autor específico
     * Útil para ver "historial de comentarios de un usuario"
     */
    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByAuthor(String author) {
        return commentRepository.findByAuthor(author);
    }

    /**
     * Cuenta comentarios de un post sin cargar todos los objetos
     * Eficiente: ejecuta COUNT(*) en vez de cargar toda la lista
     */
    @Override
    @Transactional(readOnly = true)
    public Long countCommentsByPostId(Long postId) {
        return commentRepository.countCommentsByPostId(postId);
    }
}
