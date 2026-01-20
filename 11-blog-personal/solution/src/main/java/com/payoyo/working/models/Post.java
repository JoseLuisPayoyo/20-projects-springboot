package com.payoyo.working.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título no puede estar vacío")
    @Size(min = 5, message = "El título debe tener al menos 5 caracteres")
    private String title;

    @NotBlank(message = "El contenido no puede estar vacío")
    @Size(min = 10, message = "El contenido debe tener al menos 10 caracteres")
    @Column(columnDefinition = "TEXT") // Permite contenido largo
    private String content;

    @NotBlank(message = "El autor no puede estar vacío")
    @Size(min = 3, message = "El autor debe tener al menos 3 caracteres")
    private String author;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Relación OneToMany con Comment (un post tiene muchos comentarios)
     * - mappedBy: indica que Comment.post es el dueño de la relación (tiene la FK)
     * - cascade: todas las operaciones en Post se propagan a Comments
     * - orphanRemoval: si un comment pierde su referencia al post, se elimina automáticamente
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>(); // Inicialización para evitar NullPointerException

    /**
     * Callback ejecutado antes de persistir la entidad por primera vez
     * Establece automáticamente las fechas de creación y actualización
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Callback ejecutado antes de actualizar la entidad
     * Actualiza automáticamente la fecha de modificación
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Método helper para añadir un comentario al post
     * Mantiene sincronizados ambos lados de la relación bidireccional
     */
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this); // Establece la relación inversa
    }

    /**
     * Método helper para eliminar un comentario del post
     * Mantiene sincronizados ambos lados de la relación bidireccional
     */
    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null); // Rompe la relación inversa
    }
}