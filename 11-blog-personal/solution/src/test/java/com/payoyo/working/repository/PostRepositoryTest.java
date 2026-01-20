package com.payoyo.working.repository;

import com.payoyo.working.models.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para PostRepository usando PostgreSQL
 * 
 * @SpringBootTest: carga el contexto completo de Spring
 * @Transactional: cada test se ejecuta en una transacción que hace rollback al final
 * - Los datos NO se guardan permanentemente en PostgreSQL
 * - Cada test parte de BD limpia
 */
@SpringBootTest
@Transactional
@DisplayName("Tests para PostRepository")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    private Post post1;
    private Post post2;
    private Post post3;

    @BeforeEach
    void setUp() {
        // Limpiar datos previos
        postRepository.deleteAll();

        // Crear posts de prueba
        post1 = new Post();
        post1.setTitle("Spring Boot Fundamentals");
        post1.setContent("Este es un post sobre fundamentos de Spring Boot con más de 10 caracteres");
        post1.setAuthor("Jose Luis");

        post2 = new Post();
        post2.setTitle("JPA Relationships Guide");
        post2.setContent("Guía completa sobre relaciones JPA OneToMany y ManyToOne con ejemplos");
        post2.setAuthor("Jose Luis");

        post3 = new Post();
        post3.setTitle("React Hooks Tutorial");
        post3.setContent("Tutorial completo sobre React Hooks incluyendo useState y useEffect");
        post3.setAuthor("Maria Garcia");
    }

    @Test
    @DisplayName("Guardar post - debe generar ID automáticamente")
    void testSavePost_ShouldGenerateId() {
        // Act
        Post savedPost = postRepository.save(post1);

        // Assert
        assertThat(savedPost).isNotNull();
        assertThat(savedPost.getId()).isNotNull();
        assertThat(savedPost.getTitle()).isEqualTo("Spring Boot Fundamentals");
        assertThat(savedPost.getAuthor()).isEqualTo("Jose Luis");
        assertThat(savedPost.getCreatedAt()).isNotNull();
        assertThat(savedPost.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Buscar post por ID - debe retornar el post correcto")
    void testFindById_ShouldReturnPost() {
        // Arrange
        Post savedPost = postRepository.save(post1);

        // Act
        Optional<Post> foundPost = postRepository.findById(savedPost.getId());

        // Assert
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getTitle()).isEqualTo("Spring Boot Fundamentals");
    }

    @Test
    @DisplayName("Buscar post inexistente - debe retornar Optional vacío")
    void testFindById_NonExistent_ShouldReturnEmpty() {
        // Act
        Optional<Post> foundPost = postRepository.findById(999L);

        // Assert
        assertThat(foundPost).isEmpty();
    }

    @Test
    @DisplayName("Listar todos los posts - debe retornar lista completa")
    void testFindAll_ShouldReturnAllPosts() {
        // Arrange
        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);

        // Act
        List<Post> posts = postRepository.findAll();

        // Assert
        assertThat(posts).hasSize(3);
        assertThat(posts).extracting(Post::getAuthor)
                .containsExactlyInAnyOrder("Jose Luis", "Jose Luis", "Maria Garcia");
    }

    @Test
    @DisplayName("Buscar por autor - debe retornar solo posts del autor")
    void testFindByAuthor_ShouldReturnPostsByAuthor() {
        // Arrange
        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);

        // Act
        List<Post> josePosts = postRepository.findByAuthor("Jose Luis");

        // Assert
        assertThat(josePosts).hasSize(2);
        assertThat(josePosts).allMatch(post -> post.getAuthor().equals("Jose Luis"));
    }

    @Test
    @DisplayName("Buscar por título (contiene) - debe encontrar posts con keyword")
    void testFindByTitleContaining_ShouldReturnMatchingPosts() {
        // Arrange
        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);

        // Act
        List<Post> springPosts = postRepository.findByTitleContainingIgnoreCase("spring");

        // Assert
        assertThat(springPosts).hasSize(1);
        assertThat(springPosts.get(0).getTitle()).contains("Spring");
    }

    @Test
    @DisplayName("Buscar por título (case insensitive) - debe ignorar mayúsculas/minúsculas")
    void testFindByTitleContaining_CaseInsensitive() {
        // Arrange
        postRepository.save(post1);

        // Act
        List<Post> results1 = postRepository.findByTitleContainingIgnoreCase("SPRING");
        List<Post> results2 = postRepository.findByTitleContainingIgnoreCase("spring");
        List<Post> results3 = postRepository.findByTitleContainingIgnoreCase("SpRiNg");

        // Assert
        assertThat(results1).hasSize(1);
        assertThat(results2).hasSize(1);
        assertThat(results3).hasSize(1);
    }

    @Test
    @DisplayName("Eliminar post - debe desaparecer de la BD")
    void testDeletePost_ShouldRemoveFromDatabase() {
        // Arrange
        Post savedPost = postRepository.save(post1);
        Long postId = savedPost.getId();

        // Act
        postRepository.deleteById(postId);

        // Assert
        Optional<Post> deletedPost = postRepository.findById(postId);
        assertThat(deletedPost).isEmpty();
    }

    @Test
    @DisplayName("Contar posts - debe retornar número correcto")
    void testCountPosts_ShouldReturnCorrectCount() {
        // Arrange
        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);

        // Act
        long count = postRepository.count();

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Buscar posts sin comentarios - debe usar @Query correctamente")
    void testFindPostsWithoutComments_ShouldReturnPostsWithNoComments() {
        // Arrange
        postRepository.save(post1);
        postRepository.save(post2);

        // Act
        List<Post> postsWithoutComments = postRepository.findPostsWithoutComments();

        // Assert
        assertThat(postsWithoutComments).hasSize(2);
    }

    @Test
    @DisplayName("Verificar existencia por ID - debe retornar true si existe")
    void testExistsById_ShouldReturnTrueWhenExists() {
        // Arrange
        Post savedPost = postRepository.save(post1);

        // Act
        boolean exists = postRepository.existsById(savedPost.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Verificar existencia por ID - debe retornar false si no existe")
    void testExistsById_ShouldReturnFalseWhenNotExists() {
        // Act
        boolean exists = postRepository.existsById(999L);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Buscar posts por autor ordenados por fecha")
    void testFindByAuthorOrderByCreatedAtDesc_ShouldReturnOrderedPosts() throws InterruptedException {
        // Arrange
        Post firstPost = postRepository.save(post1);
        Thread.sleep(10);
        Post secondPost = postRepository.save(post2);

        // Act
        List<Post> orderedPosts = postRepository.findByAuthorOrderByCreatedAtDesc("Jose Luis");

        // Assert
        assertThat(orderedPosts).hasSize(2);
        assertThat(orderedPosts.get(0).getId()).isEqualTo(secondPost.getId());
        assertThat(orderedPosts.get(1).getId()).isEqualTo(firstPost.getId());
    }

    @Test
    @DisplayName("Buscar posts creados después de una fecha")
    void testFindByCreatedAtAfter_ShouldReturnPostsAfterDate() {
        // Arrange
        postRepository.save(post1);
        LocalDateTime cutoffDate = LocalDateTime.now().minusMinutes(1);

        // Act
        List<Post> recentPosts = postRepository.findByCreatedAtAfter(cutoffDate);

        // Assert
        assertThat(recentPosts).isNotEmpty();
        assertThat(recentPosts).allMatch(post -> post.getCreatedAt().isAfter(cutoffDate));
    }
}