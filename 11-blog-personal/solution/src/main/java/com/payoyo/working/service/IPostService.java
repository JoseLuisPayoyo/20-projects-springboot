package com.payoyo.working.service;

import com.payoyo.working.models.Post;

import java.util.List;

/**
 * Interfaz que define el contrato de servicios para Post
 * 
 * Ventajas de usar interfaces:
 * - Separación de contrato e implementación (SOLID - Dependency Inversion)
 * - Facilita testing (mockear la interfaz con Mockito)
 * - Permite múltiples implementaciones (PostServiceImpl, PostServiceV2Impl, etc.)
 * - Mejor documentación (el contrato está claro)
 */
public interface IPostService {

    /**
     * Crea un nuevo post
     * @param post Post a crear (sin ID)
     * @return Post creado con ID generado
     */
    Post createPost(Post post);

    /**
     * Obtiene todos los posts
     * @return Lista de todos los posts (con sus comentarios si están cargados)
     */
    List<Post> getAllPosts();

    /**
     * Obtiene un post por su ID
     * @param id ID del post
     * @return Post encontrado
     * @throws com.blogpersonal.exception.ResourceNotFoundException si no existe
     */
    Post getPostById(Long id);

    /**
     * Actualiza un post existente
     * @param id ID del post a actualizar
     * @param postDetails Nuevos datos del post
     * @return Post actualizado
     * @throws com.blogpersonal.exception.ResourceNotFoundException si no existe
     */
    Post updatePost(Long id, Post postDetails);

    /**
     * Elimina un post (y sus comentarios en cascada)
     * @param id ID del post a eliminar
     * @throws com.blogpersonal.exception.ResourceNotFoundException si no existe
     */
    void deletePost(Long id);

    /**
     * Busca posts por autor
     * @param author Nombre del autor
     * @return Lista de posts del autor
     */
    List<Post> getPostsByAuthor(String author);

    /**
     * Busca posts que contengan una palabra clave en el título
     * @param keyword Palabra clave a buscar
     * @return Lista de posts que coinciden
     */
    List<Post> searchPostsByTitle(String keyword);

    /**
     * Obtiene posts con al menos N comentarios
     * @param minComments Número mínimo de comentarios
     * @return Lista de posts populares
     */
    List<Post> getPostsWithMinComments(int minComments);
}
