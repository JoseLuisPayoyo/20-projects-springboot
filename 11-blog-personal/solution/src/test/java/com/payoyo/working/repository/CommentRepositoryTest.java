package com.payoyo.working.repository;

import com.payoyo.working.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para CommentRepository usando PostgreSQL
 */
@SpringBootTest
@Transactional
@DisplayName("Tests para CommentRepository")
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    private Post post1;
    private Post post2;
    private Comment comment1;
    private Comment comment2;
    private Comment comment3;

    @BeforeEach
    void setUp() {
        // Limpiar BD
        commentRepository.deleteAll();
        postRepository.deleteAll();

        // Crear posts
        post1 = new Post();
        post1.setTitle("Spring Boot Testing Guide");
        post1.setContent("Guía completa sobre testing en Spring Boot con JUnit y Mockito");
        post1.setAuthor("Jose Luis");
        post1 = postRepository.save(post1);

        post2 = new Post();
        post2.setTitle("JPA Relationships");
        post2.setContent("Tutorial sobre relaciones JPA bidireccionales OneToMany ManyToOne");
        post2.setAuthor("Maria Garcia");
        post2 = postRepository.save(post2);

        // Crear comentarios
        comment1 = new Comment();
        comment1.setContent("Excelente explicación sobre testing");
        comment1.setAuthor("Maria Garcia");
        comment1.setPost(post1);

        comment2 = new Comment();
        comment2.setContent("Me ha ayudado mucho este tutorial");
        comment2.setAuthor("Pedro Martinez");
        comment2.setPost(post1);

        comment3 = new Comment();
        comment3.setContent("Muy útil la información sobre JPA");
        comment3.setAuthor("Ana Lopez");
        comment3.setPost(post2);
    }

    @Test
    @DisplayName("Guardar comentario - debe generar ID y establecer relación")
    void testSaveComment_ShouldGenerateIdAndSetRelation() {
        // Act
        Comment savedComment = commentRepository.save(comment1);

        // Assert
        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getContent()).isEqualTo("Excelente explicación sobre testing");
        assertThat(savedComment.getPost()).isNotNull();
        assertThat(savedComment.getPost().getId()).isEqualTo(post1.getId());
        assertThat(savedComment.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Buscar comentario por ID - debe retornar el comentario correcto")
    void testFindById_ShouldReturnComment() {
        // Arrange
        Comment savedComment = commentRepository.save(comment1);

        // Act
        Optional<Comment> foundComment = commentRepository.findById(savedComment.getId());

        // Assert
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getContent()).isEqualTo("Excelente explicación sobre testing");
    }

    @Test
    @DisplayName("Buscar comentarios por postId - debe retornar solo comentarios de ese post")
    void testFindByPostId_ShouldReturnCommentsOfPost() {
        // Arrange
        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);

        // Act
        List<Comment> post1Comments = commentRepository.findByPostId(post1.getId());

        // Assert
        assertThat(post1Comments).hasSize(2);
        assertThat(post1Comments).allMatch(c -> c.getPost().getId().equals(post1.getId()));
    }

    @Test
    @DisplayName("Buscar comentarios por postId ordenados - debe retornar en orden descendente")
    void testFindByPostIdOrderByCreatedAtDesc_ShouldReturnOrderedComments() throws InterruptedException {
        // Arrange
        Comment saved1 = commentRepository.save(comment1);
        Thread.sleep(10);
        Comment saved2 = commentRepository.save(comment2);

        // Act
        List<Comment> orderedComments = commentRepository.findByPostIdOrderByCreatedAtDesc(post1.getId());

        // Assert
        assertThat(orderedComments).hasSize(2);
        assertThat(orderedComments.get(0).getId()).isEqualTo(saved2.getId());
        assertThat(orderedComments.get(1).getId()).isEqualTo(saved1.getId());
    }

    @Test
    @DisplayName("Buscar comentarios por autor - debe retornar todos los comentarios del autor")
    void testFindByAuthor_ShouldReturnCommentsByAuthor() {
        // Arrange
        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);

        // Act
        List<Comment> mariaComments = commentRepository.findByAuthor("Maria Garcia");

        // Assert
        assertThat(mariaComments).hasSize(1);
        assertThat(mariaComments.get(0).getAuthor()).isEqualTo("Maria Garcia");
    }

    @Test
    @DisplayName("Contar comentarios por postId - debe usar @Query correctamente")
    void testCountCommentsByPostId_ShouldReturnCorrectCount() {
        // Arrange
        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);

        // Act
        Long count = commentRepository.countCommentsByPostId(post1.getId());

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Contar comentarios de post sin comentarios - debe retornar 0")
    void testCountCommentsByPostId_EmptyPost_ShouldReturnZero() {
        // Arrange
        Post emptyPost = new Post();
        emptyPost.setTitle("Post sin comentarios");
        emptyPost.setContent("Este post no tiene ningún comentario todavía");
        emptyPost.setAuthor("Jose Luis");
        emptyPost = postRepository.save(emptyPost);

        // Act
        Long count = commentRepository.countCommentsByPostId(emptyPost.getId());

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Buscar comentarios con keyword - debe usar @Query con LIKE")
    void testSearchByContentKeyword_ShouldFindMatchingComments() {
        // Arrange
        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);

        // Act
        List<Comment> results = commentRepository.searchByContentKeyword("testing");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getContent()).contains("testing");
    }

    @Test
    @DisplayName("Actualizar comentario - debe mantener la relación con el post")
    void testUpdateComment_ShouldKeepPostRelation() {
        // Arrange
        Comment savedComment = commentRepository.save(comment1);
        Long originalPostId = savedComment.getPost().getId();

        // Act
        savedComment.setContent("Contenido actualizado del comentario");
        Comment updatedComment = commentRepository.save(savedComment);

        // Assert
        assertThat(updatedComment.getContent()).isEqualTo("Contenido actualizado del comentario");
        assertThat(updatedComment.getPost().getId()).isEqualTo(originalPostId);
    }

    @Test
    @DisplayName("Eliminar comentario - no debe afectar al post")
    void testDeleteComment_ShouldNotAffectPost() {
        // Arrange
        Comment savedComment = commentRepository.save(comment1);
        Long commentId = savedComment.getId();
        Long postId = savedComment.getPost().getId();

        // Act
        commentRepository.deleteById(commentId);

        // Assert
        Optional<Comment> deletedComment = commentRepository.findById(commentId);
        Optional<Post> post = postRepository.findById(postId);

        assertThat(deletedComment).isEmpty();
        assertThat(post).isPresent();
    }

    @Test
    @DisplayName("Listar todos los comentarios - debe retornar lista completa")
    void testFindAll_ShouldReturnAllComments() {
        // Arrange
        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);

        // Act
        List<Comment> allComments = commentRepository.findAll();

        // Assert
        assertThat(allComments).hasSize(3);
    }

    @Test
    @DisplayName("Buscar comentarios de autor en post específico - combina dos filtros")
    void testFindByAuthorAndPostId_ShouldReturnFilteredComments() {
        // Arrange
        Comment mariaInPost2 = new Comment();
        mariaInPost2.setContent("Comentario de Maria en post2");
        mariaInPost2.setAuthor("Maria Garcia");
        mariaInPost2.setPost(post2);

        commentRepository.save(comment1);
        commentRepository.save(mariaInPost2);

        // Act
        List<Comment> results = commentRepository.findByAuthorAndPostId("Maria Garcia", post1.getId());

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getContent()).isEqualTo("Excelente explicación sobre testing");
    }

    @Test
    @DisplayName("Buscar comentarios recientes de un post - debe usar @Query correctamente")
    void testFindRecentCommentsByPostId_ShouldReturnOrderedComments() throws InterruptedException {
        // Arrange
        Comment first = commentRepository.save(comment1);
        Thread.sleep(10);
        Comment second = commentRepository.save(comment2);

        // Act
        List<Comment> recentComments = commentRepository.findRecentCommentsByPostId(post1.getId());

        // Assert
        assertThat(recentComments).hasSize(2);
        assertThat(recentComments.get(0).getId()).isEqualTo(second.getId());
    }

    @Test
    @DisplayName("Buscar comentarios creados después de una fecha")
    void testFindByCreatedAtAfter_ShouldReturnCommentsAfterDate() {
        // Arrange
        Comment savedComment = commentRepository.save(comment1);
        var cutoffDate = savedComment.getCreatedAt().minusMinutes(1);

        // Act
        List<Comment> recentComments = commentRepository.findByCreatedAtAfter(cutoffDate);

        // Assert
        assertThat(recentComments).isNotEmpty();
        assertThat(recentComments).allMatch(c -> c.getCreatedAt().isAfter(cutoffDate));
    }

    @Test
    @DisplayName("Buscar comentarios con JOIN FETCH - debe cargar post")
    void testFindByAuthorWithPost_ShouldLoadPostEagerly() {
        // Arrange
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        // Act
        List<Comment> comments = commentRepository.findByAuthorWithPost("Maria Garcia");

        // Assert
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getPost()).isNotNull();
        assertThat(comments.get(0).getPost().getTitle()).isNotNull();
    }
}