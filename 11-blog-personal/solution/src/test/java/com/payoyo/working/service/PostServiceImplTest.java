package com.payoyo.working.service;

import com.payoyo.working.models.Post;
import com.payoyo.working.exception.ResourceNotFoundException;
import com.payoyo.working.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests para PostServiceImpl usando Mockito
 * 
 * @ExtendWith(MockitoExtension.class): integración de Mockito con JUnit 5
 * 
 * @Mock: crea un mock (objeto simulado) del repository
 * - NO ejecuta código real del repository
 * - Permite controlar qué retorna cada método
 * - Útil para aislar la lógica del Service
 * 
 * @InjectMocks: crea una instancia de PostServiceImpl
 * - Inyecta automáticamente los mocks (@Mock) en el Service
 * - Equivale a: new PostServiceImpl(mockPostRepository)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para PostServiceImpl")
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository; // Mock del repository

    @InjectMocks
    private PostServiceImpl postService; // Service con mock inyectado

    private Post post1;
    private Post post2;

    @BeforeEach
    void setUp() {
        // Crear posts de prueba
        post1 = new Post();
        post1.setId(1L);
        post1.setTitle("Spring Boot Testing");
        post1.setContent("Contenido sobre testing en Spring Boot con JUnit y Mockito");
        post1.setAuthor("Jose Luis");
        post1.setComments(new ArrayList<>());

        post2 = new Post();
        post2.setId(2L);
        post2.setTitle("JPA Relationships");
        post2.setContent("Tutorial sobre relaciones JPA OneToMany y ManyToOne bidireccionales");
        post2.setAuthor("Maria Garcia");
        post2.setComments(new ArrayList<>());
    }

    /**
     * Test básico de Mockito: simular comportamiento del repository
     * 
     * when(...).thenReturn(...): configura el mock
     * - Cuando se llame a postRepository.save(any(Post.class))
     * - Retorna post1
     * 
     * verify(...): verifica que se llamó al método
     * - Asegura que el service interactuó correctamente con el repository
     */
    @Test
    @DisplayName("Crear post - debe llamar a repository.save()")
    void testCreatePost_ShouldCallRepositorySave() {
        // Arrange: configurar el mock
        when(postRepository.save(any(Post.class))).thenReturn(post1);

        // Act: llamar al service
        Post createdPost = postService.createPost(post1);

        // Assert: verificar resultado y llamadas
        assertThat(createdPost).isNotNull();
        assertThat(createdPost.getTitle()).isEqualTo("Spring Boot Testing");
        
        // Verificar que se llamó a save() exactamente 1 vez
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Obtener todos los posts - debe retornar lista del repository")
    void testGetAllPosts_ShouldReturnListFromRepository() {
        // Arrange: configurar mock para retornar lista
        List<Post> mockPosts = Arrays.asList(post1, post2);
        when(postRepository.findAll()).thenReturn(mockPosts);

        // Act
        List<Post> result = postService.getAllPosts();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(post1, post2);
        verify(postRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Obtener post por ID existente - debe retornar el post")
    void testGetPostById_Existing_ShouldReturnPost() {
        // Arrange: simular que el post existe
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));

        // Act
        Post foundPost = postService.getPostById(1L);

        // Assert
        assertThat(foundPost).isNotNull();
        assertThat(foundPost.getId()).isEqualTo(1L);
        assertThat(foundPost.getTitle()).isEqualTo("Spring Boot Testing");
        verify(postRepository, times(1)).findById(1L);
    }

    /**
     * Test de excepciones: verificar que se lanza ResourceNotFoundException
     * 
     * assertThatThrownBy: verifica que se lanza una excepción específica
     * - Ejecuta el código dentro del lambda
     * - Captura la excepción lanzada
     * - Permite verificar tipo y mensaje
     */
    @Test
    @DisplayName("Obtener post por ID inexistente - debe lanzar ResourceNotFoundException")
    void testGetPostById_NonExistent_ShouldThrowException() {
        // Arrange: simular que el post NO existe
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert: verificar que lanza excepción
        assertThatThrownBy(() -> postService.getPostById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post no encontrado con id: 999");

        verify(postRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Actualizar post - debe modificar campos y llamar a save()")
    void testUpdatePost_ShouldUpdateFieldsAndSave() {
        // Arrange: simular que el post existe
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        
        // Crear datos actualizados
        Post updatedData = new Post();
        updatedData.setTitle("Spring Boot Testing - Updated");
        updatedData.setContent("Contenido actualizado con más información sobre Mockito");
        updatedData.setAuthor("Jose Luis");

        // Configurar save() para retornar el post actualizado
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            return savedPost;
        });

        // Act
        Post result = postService.updatePost(1L, updatedData);

        // Assert
        assertThat(result.getTitle()).isEqualTo("Spring Boot Testing - Updated");
        assertThat(result.getContent()).contains("Mockito");
        
        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Actualizar post inexistente - debe lanzar excepción")
    void testUpdatePost_NonExistent_ShouldThrowException() {
        // Arrange: simular que el post NO existe
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        Post updatedData = new Post();
        updatedData.setTitle("Updated Title");
        updatedData.setContent("Updated content with more than ten characters");
        updatedData.setAuthor("Jose Luis");

        // Act & Assert
        assertThatThrownBy(() -> postService.updatePost(999L, updatedData))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository, times(1)).findById(999L);
        verify(postRepository, never()).save(any(Post.class)); // NO debe llamar a save
    }

    @Test
    @DisplayName("Eliminar post - debe llamar a delete()")
    void testDeletePost_ShouldCallRepositoryDelete() {
        // Arrange: simular que el post existe
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        doNothing().when(postRepository).delete(any(Post.class));

        // Act
        postService.deletePost(1L);

        // Assert: verificar llamadas
        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).delete(post1);
    }

    @Test
    @DisplayName("Eliminar post inexistente - debe lanzar excepción")
    void testDeletePost_NonExistent_ShouldThrowException() {
        // Arrange: simular que el post NO existe
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> postService.deletePost(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository, times(1)).findById(999L);
        verify(postRepository, never()).delete(any(Post.class)); // NO debe llamar a delete
    }

    @Test
    @DisplayName("Buscar posts por autor - debe delegar al repository")
    void testGetPostsByAuthor_ShouldDelegateToRepository() {
        // Arrange
        List<Post> josePosts = Arrays.asList(post1);
        when(postRepository.findByAuthor("Jose Luis")).thenReturn(josePosts);

        // Act
        List<Post> result = postService.getPostsByAuthor("Jose Luis");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthor()).isEqualTo("Jose Luis");
        verify(postRepository, times(1)).findByAuthor("Jose Luis");
    }

    @Test
    @DisplayName("Buscar posts por título - debe delegar al repository")
    void testSearchPostsByTitle_ShouldDelegateToRepository() {
        // Arrange
        List<Post> springPosts = Arrays.asList(post1);
        when(postRepository.findByTitleContainingIgnoreCase("Spring")).thenReturn(springPosts);

        // Act
        List<Post> result = postService.searchPostsByTitle("Spring");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("Spring");
        verify(postRepository, times(1)).findByTitleContainingIgnoreCase("Spring");
    }

    @Test
    @DisplayName("Obtener posts populares - debe usar @Query del repository")
    void testGetPostsWithMinComments_ShouldUseRepositoryQuery() {
        // Arrange
        List<Post> popularPosts = Arrays.asList(post1);
        when(postRepository.findPostsWithMinComments(3)).thenReturn(popularPosts);

        // Act
        List<Post> result = postService.getPostsWithMinComments(3);

        // Assert
        assertThat(result).hasSize(1);
        verify(postRepository, times(1)).findPostsWithMinComments(3);
    }

    /**
     * Test de comportamiento: verificar que NO se llama a métodos innecesarios
     */
    @Test
    @DisplayName("Crear post - NO debe llamar a findById()")
    void testCreatePost_ShouldNotCallFindById() {
        // Arrange
        when(postRepository.save(any(Post.class))).thenReturn(post1);

        // Act
        postService.createPost(post1);

        // Assert: verificar que NO se llamó a findById
        verify(postRepository, never()).findById(anyLong());
        verify(postRepository, times(1)).save(any(Post.class));
    }
}