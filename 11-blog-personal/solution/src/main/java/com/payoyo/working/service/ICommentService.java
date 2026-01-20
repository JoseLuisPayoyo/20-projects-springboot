package com.payoyo.working.service;

import com.payoyo.working.models.*;

import java.util.List;

/**
 * Interfaz que define el contrato de servicios para Comment
 */
public interface ICommentService {

    /**
     * Crea un nuevo comentario en un post específico
     * @param postId ID del post donde se crea el comentario
     * @param comment Comentario a crear (sin ID ni post asignado)
     * @return Comentario creado con ID generado y post asociado
     * @throws com.blogpersonal.exception.ResourceNotFoundException si el post no existe
     */
    Comment createComment(Long postId, Comment comment);

    /**
     * Obtiene todos los comentarios de un post
     * @param postId ID del post
     * @return Lista de comentarios del post (ordenados por fecha desc)
     * @throws com.blogpersonal.exception.ResourceNotFoundException si el post no existe
     */
    List<Comment> getCommentsByPostId(Long postId);

    /**
     * Obtiene un comentario por su ID
     * @param id ID del comentario
     * @return Comentario encontrado
     * @throws com.blogpersonal.exception.ResourceNotFoundException si no existe
     */
    Comment getCommentById(Long id);

    /**
     * Actualiza un comentario existente
     * @param id ID del comentario a actualizar
     * @param commentDetails Nuevos datos del comentario
     * @return Comentario actualizado
     * @throws com.blogpersonal.exception.ResourceNotFoundException si no existe
     */
    Comment updateComment(Long id, Comment commentDetails);

    /**
     * Elimina un comentario
     * El post asociado permanece intacto
     * @param id ID del comentario a eliminar
     * @throws com.blogpersonal.exception.ResourceNotFoundException si no existe
     */
    void deleteComment(Long id);

    /**
     * Busca comentarios por autor
     * @param author Nombre del autor
     * @return Lista de comentarios del autor
     */
    List<Comment> getCommentsByAuthor(String author);

    /**
     * Cuenta el número de comentarios de un post
     * @param postId ID del post
     * @return Número de comentarios
     */
    Long countCommentsByPostId(Long postId);
}
