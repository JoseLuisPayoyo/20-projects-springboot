package com.payoyo.working.service;

import com.payoyo.working.models.*;
import com.payoyo.working.exception.*;
import com.payoyo.working.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests para CommentServiceImpl
 * 
 * IMPORTANTE: CommentService depende de DOS repositories:
 * - CommentRepository: para operaciones CRUD de comentarios
 * - PostRepository: para verificar que el post existe antes de crear comentarios
 * 
 * Por eso necesitamos mockear AMBOS repositories
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para CommentServiceImpl")
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository; // Necesario para verificar posts

    @InjectMocks
    private CommentServiceImpl commentService;

    private Post post;
    private Comment comment1;
    private Comment comment2;

    @BeforeEach
    void setUp() {
        // Crear post
        post = new Post();
        post.setId(1L);
        post.setTitle("Spring Boot Testing");
        post.setContent("Contenido sobre testing en Spring Boot con ejemplos prácticos");
        post.setAuthor("Jose Luis");

        // Crear comentarios
        comment1 = new Comment();
        comment1.setId(1L);
        comment1.setContent("Excelente explicación sobre testing");
        comment1.setAuthor("Maria Garcia");
        comment1.setPost(post);

        comment2 = new Comment();
        comment2.setId(2L);
        comment2.setContent("Me ha ayudado mucho este tutorial");
        comment2.setAuthor("Pedro Martinez");
        comment2.setPost(post);
    }

    @Test
    @DisplayName("Crear comentario - debe verificar que el post existe y guardar")
    void testCreateComment_ShouldVerifyPostExistsAndSave() {
        // Arrange: simular que el post existe
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment1);

        Comment newComment = new Comment();
        newComment.setContent("Nuevo comentario de prueba");
        newComment.setAuthor("Ana Lopez");

        // Act
        Comment createdComment = commentService.createComment(1L, newComment);

        // Assert
        assertThat(createdComment).isNotNull();
        assertThat(createdComment.getContent()).isEqualTo("Excelente explicación sobre testing");
        
        // Verificar que se verificó la existencia del post
        verify(postRepository, times(1)).findById(1L);
        // Verificar que se guardó el comentario
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("Crear comentario en post inexistente - debe lanzar excepción")
    void testCreateComment_PostNotFound_ShouldThrowException() {
        // Arrange: simular que el post NO existe
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        Comment newComment = new Comment();
        newComment.setContent("Comentario en post inexistente");
        newComment.setAuthor("Ana Lopez");

        // Act & Assert
        assertThatThrownBy(() -> commentService.createComment(999L, newComment))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post no encontrado con id: 999");

        // Verificar que NO se intentó guardar el comentario
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Listar comentarios de un post - debe verificar post y retornar lista")
    void testGetCommentsByPostId_ShouldVerifyPostAndReturnList() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(comment2, comment1)); // Orden desc

        // Act
        List<Comment> comments = commentService.getCommentsByPostId(1L);

        // Assert
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getId()).isEqualTo(2L); // Más reciente primero
        
        verify(postRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).findByPostIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("Listar comentarios de post inexistente - debe lanzar excepción")
    void testGetCommentsByPostId_PostNotFound_ShouldThrowException() {
        // Arrange: simular que el post NO existe
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> commentService.getCommentsByPostId(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        // NO debe llamar al commentRepository
        verify(commentRepository, never()).findByPostIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    @DisplayName("Obtener comentario por ID - debe retornar el comentario")
    void testGetCommentById_Existing_ShouldReturnComment() {
        // Arrange
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment1));

        // Act
        Comment foundComment = commentService.getCommentById(1L);

        // Assert
        assertThat(foundComment).isNotNull();
        assertThat(foundComment.getId()).isEqualTo(1L);
        assertThat(foundComment.getContent()).isEqualTo("Excelente explicación sobre testing");
        verify(commentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener comentario inexistente - debe lanzar excepción")
    void testGetCommentById_NonExistent_ShouldThrowException() {
        // Arrange
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> commentService.getCommentById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment no encontrado con id: 999");
    }

    @Test
    @DisplayName("Actualizar comentario - debe modificar solo content y author")
    void testUpdateComment_ShouldUpdateOnlyContentAndAuthor() {
        // Arrange
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment1));

        Comment updatedData = new Comment();
        updatedData.setContent("Contenido actualizado del comentario");
        updatedData.setAuthor("Maria Garcia");

        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment savedComment = invocation.getArgument(0);
            return savedComment;
        });

        // Act
        Comment result = commentService.updateComment(1L, updatedData);

        // Assert: verificar que se actualizaron los campos
        assertThat(result.getContent()).isEqualTo("Contenido actualizado del comentario");
        assertThat(result.getPost()).isEqualTo(post); // Post NO cambia
        
        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("Eliminar comentario - debe llamar a delete()")
    void testDeleteComment_ShouldCallRepositoryDelete() {
        // Arrange
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment1));
        doNothing().when(commentRepository).delete(any(Comment.class));

        // Act
        commentService.deleteComment(1L);

        // Assert
        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).delete(comment1);
    }

    @Test
    @DisplayName("Buscar comentarios por autor - debe delegar al repository")
    void testGetCommentsByAuthor_ShouldDelegateToRepository() {
        // Arrange
        List<Comment> mariaComments = Arrays.asList(comment1);
        when(commentRepository.findByAuthor("Maria Garcia")).thenReturn(mariaComments);

        // Act
        List<Comment> result = commentService.getCommentsByAuthor("Maria Garcia");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthor()).isEqualTo("Maria Garcia");
        verify(commentRepository, times(1)).findByAuthor("Maria Garcia");
    }

    @Test
    @DisplayName("Contar comentarios de un post - debe usar @Query del repository")
    void testCountCommentsByPostId_ShouldUseRepositoryQuery() {
        // Arrange
        when(commentRepository.countCommentsByPostId(1L)).thenReturn(5L);

        // Act
        Long count = commentService.countCommentsByPostId(1L);

        // Assert
        assertThat(count).isEqualTo(5L);
        verify(commentRepository, times(1)).countCommentsByPostId(1L);
    }

    @Test
    @DisplayName("Crear comentario - debe establecer la relación con el post")
    void testCreateComment_ShouldSetPostRelation() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        Comment newComment = new Comment();
        newComment.setContent("Nuevo comentario sin post asignado");
        newComment.setAuthor("Ana Lopez");
        // NO tiene post asignado todavía

        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment savedComment = invocation.getArgument(0);
            // Verificar que el service estableció la relación
            assertThat(savedComment.getPost()).isNotNull();
            assertThat(savedComment.getPost().getId()).isEqualTo(1L);
            return savedComment;
        });

        // Act
        commentService.createComment(1L, newComment);

        // Assert: verificado en el thenAnswer
        verify(commentRepository, times(1)).save(any(Comment.class));
    }
}  

