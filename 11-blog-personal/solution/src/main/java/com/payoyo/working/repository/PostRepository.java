package com.payoyo.working.repository;

import com.payoyo.working.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para la entidad Post
 * JpaRepository<Post, Long> proporciona CRUD completo:
 * - save(), findById(), findAll(), deleteById(), etc.
 * - Post: tipo de entidad
 * - Long: tipo de la clave primaria
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Query method: encuentra posts por autor
     * Spring Data JPA genera automáticamente la query:
     * SELECT * FROM posts WHERE author = ?
     */
    List<Post> findByAuthor(String author);

    /**
     * Query method: búsqueda parcial en el título (case insensitive)
     * Útil para búsquedas tipo "buscar posts que contengan 'Spring'"
     * SQL generado: SELECT * FROM posts WHERE LOWER(title) LIKE LOWER('%keyword%')
     */
    List<Post> findByTitleContainingIgnoreCase(String keyword);

    /**
     * Query method: busca posts creados después de una fecha
     * Útil para filtrar "posts recientes" o "posts desde X fecha"
     */
    List<Post> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Query method: busca posts por autor ordenados por fecha descendente
     * Combina filtro + ordenamiento (posts más recientes primero)
     */
    List<Post> findByAuthorOrderByCreatedAtDesc(String author);

    /**
     * @Query personalizada: encuentra posts con al menos N comentarios
     * SIZE(p.comments) cuenta los elementos de la colección comments
     * Útil para encontrar "posts populares" o "posts con actividad"
     */
    @Query("SELECT p FROM Post p WHERE SIZE(p.comments) >= :minComments")
    List<Post> findPostsWithMinComments(@Param("minComments") int minComments);

    /**
     * @Query personalizada: encuentra posts sin comentarios (huérfanos de interacción)
     * Útil para identificar contenido que necesita promoción
     */
    @Query("SELECT p FROM Post p WHERE SIZE(p.comments) = 0")
    List<Post> findPostsWithoutComments();

    /**
     * @Query con JOIN FETCH: carga posts con sus comentarios de forma eficiente
     * JOIN FETCH evita el problema N+1 (múltiples queries)
     * LAZY loading + JOIN FETCH = una sola query que trae todo
     * Útil cuando sabes que SIEMPRE necesitarás los comentarios
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.comments WHERE p.id = :id")
    Post findByIdWithComments(@Param("id") Long id);

    /**
     * @Query nativa: búsqueda avanzada en título Y contenido
     * nativeQuery = true permite usar SQL nativo en vez de JPQL
     * Útil para búsquedas full-text o funciones específicas de la BD
     */
    @Query(value = "SELECT * FROM posts WHERE LOWER(title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                   "OR LOWER(content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))", 
           nativeQuery = true)
    List<Post> searchInTitleOrContent(@Param("searchTerm") String searchTerm);
}