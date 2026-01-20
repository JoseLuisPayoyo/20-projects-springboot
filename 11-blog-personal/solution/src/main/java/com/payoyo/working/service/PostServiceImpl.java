package com.payoyo.working.service;

import com.payoyo.working.models.*;
import com.payoyo.working.exception.*;
import com.payoyo.working.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de IPostService
 * Contiene la lógica de negocio para gestionar Posts
 * 
 * @Service: marca esta clase como componente de servicio de Spring
 * @RequiredArgsConstructor: inyección de dependencias por constructor (Lombok)
 * @Transactional: gestión automática de transacciones en métodos de escritura
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements IPostService {

    // Inyección de dependencias por constructor (inmutable, mejor práctica)
    private final PostRepository postRepository;

    /**
     * Crea un nuevo post
     * No requiere validaciones adicionales porque @Valid en Controller ya las hace
     */
    @Override
    @Transactional // Asegura que la operación se ejecuta en una transacción
    public Post createPost(Post post) {
        // JPA genera el ID automáticamente (@GeneratedValue)
        // @PrePersist establece createdAt y updatedAt
        return postRepository.save(post);
    }

    /**
     * Obtiene todos los posts
     * Los comentarios se cargan según la estrategia LAZY/EAGER configurada
     */
    @Override
    @Transactional(readOnly = true) // Optimización para operaciones de solo lectura
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * Obtiene un post por ID
     * orElseThrow: lanza excepción personalizada si no existe
     * GlobalExceptionHandler convierte la excepción en respuesta 404 JSON
     */
    @Override
    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
    }

    /**
     * Actualiza un post existente
     * 
     * Pasos:
     * 1. Verificar que el post existe (getPostById lanza excepción si no)
     * 2. Actualizar solo los campos editables (title, content, author)
     * 3. Los comentarios NO se modifican (son gestionados por CommentService)
     * 4. @PreUpdate actualiza automáticamente updatedAt
     */
    @Override
    @Transactional
    public Post updatePost(Long id, Post postDetails) {
        Post post = getPostById(id); // Reutiliza el método (DRY principle)

        // Actualizar campos editables
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());
        post.setAuthor(postDetails.getAuthor());
        // Los comentarios NO se modifican aquí
        
        return postRepository.save(post);
    }

    /**
     * Elimina un post
     * 
     * Gracias a cascade = CascadeType.ALL y orphanRemoval = true:
     * - Todos los comentarios asociados se eliminan automáticamente
     * - No necesitamos eliminar comentarios manualmente
     */
    @Override
    @Transactional
    public void deletePost(Long id) {
        Post post = getPostById(id); // Verifica existencia primero
        postRepository.delete(post);
        // Cascade DELETE: todos los comments con post_id = id se eliminan
    }

    /**
     * Busca posts por autor
     * Query method: findByAuthor generado automáticamente por Spring Data JPA
     */
    @Override
    @Transactional(readOnly = true)
    public List<Post> getPostsByAuthor(String author) {
        return postRepository.findByAuthor(author);
    }

    /**
     * Búsqueda parcial en el título (case insensitive)
     * Útil para implementar una barra de búsqueda
     */
    @Override
    @Transactional(readOnly = true)
    public List<Post> searchPostsByTitle(String keyword) {
        return postRepository.findByTitleContainingIgnoreCase(keyword);
    }

    /**
     * Obtiene posts "populares" (con muchos comentarios)
     * Usa @Query personalizada con SIZE(p.comments)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Post> getPostsWithMinComments(int minComments) {
        return postRepository.findPostsWithMinComments(minComments);
    }
}
