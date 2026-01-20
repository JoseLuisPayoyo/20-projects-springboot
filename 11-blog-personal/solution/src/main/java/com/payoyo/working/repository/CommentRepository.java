package com.payoyo.working.repository;

import com.payoyo.working.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para la entidad Comment
 * JpaRepository<Comment, Long> proporciona CRUD completo:
 * - save(), findById(), findAll(), deleteById(), etc.
 * - Comment: tipo de entidad
 * - Long: tipo de la clave primaria
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Query method: encuentra todos los comentarios de un post específico
     * Spring Data JPA navega automáticamente por la relación Comment → Post
     * SQL generado: SELECT * FROM comments WHERE post_id = ?
     * 
     * Este es el método MÁS USADO en este repository (listar comentarios de un post)
     */
    List<Comment> findByPostId(Long postId);

    /**
     * Query method: busca comentarios por autor
     * Útil para ver "todos los comentarios de un usuario"
     */
    List<Comment> findByAuthor(String author);

    /**
     * Query method: comentarios de un post ordenados por fecha descendente
     * Combina filtro por post + ordenamiento (comentarios más recientes primero)
     * Útil para mostrar "últimos comentarios" en la UI
     */
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);

    /**
     * Query method: comentarios creados después de una fecha
     * Útil para obtener "comentarios recientes" o "actividad desde X fecha"
     */
    List<Comment> findByCreatedAtAfter(LocalDateTime date);

    /**
     * @Query personalizada: comentarios recientes de un post (top N)
     * ORDER BY + LIMIT sería nativo, pero en JPQL usamos la anotación @Query
     * Para limitar resultados, mejor usar Pageable en el Service
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsByPostId(@Param("postId") Long postId);

    /**
     * @Query personalizada: cuenta comentarios de un post específico
     * Alternativa a usar SIZE() en PostRepository
     * Útil para mostrar "número de comentarios" sin cargar todos
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    Long countCommentsByPostId(@Param("postId") Long postId);

    /**
     * @Query personalizada: encuentra comentarios que contienen una palabra clave
     * Búsqueda parcial en el contenido (case insensitive)
     * Útil para "buscar comentarios sobre un tema"
     */
    @Query("SELECT c FROM Comment c WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Comment> searchByContentKeyword(@Param("keyword") String keyword);

    /**
     * @Query con JOIN: obtiene comentarios con información del post cargada
     * JOIN FETCH evita N+1 queries al acceder a comment.getPost()
     * Útil cuando necesitas mostrar comentarios CON datos del post asociado
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.post WHERE c.author = :author")
    List<Comment> findByAuthorWithPost(@Param("author") String author);

    /**
     * Query method compuesto: comentarios de un autor en un post específico
     * Combina dos condiciones: author AND postId
     * Útil para ver "comentarios de este usuario en este post"
     */
    List<Comment> findByAuthorAndPostId(String author, Long postId);

    /**
     * @Query nativa: top 5 autores con más comentarios
     * GROUP BY + COUNT + ORDER BY + LIMIT
     * Ejemplo de query analítica (estadísticas)
     */
    @Query(value = "SELECT author, COUNT(*) as total FROM comments " +
                   "GROUP BY author ORDER BY total DESC LIMIT 5", 
           nativeQuery = true)
    List<Object[]> findTopCommentAuthors();
}