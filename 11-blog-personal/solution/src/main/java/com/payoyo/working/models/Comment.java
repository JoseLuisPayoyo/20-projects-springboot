package com.payoyo.working.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El contenido del comentario no puede estar vacío")
    @Size(min = 3, message = "El comentario debe tener al menos 3 caracteres")
    @Column(columnDefinition = "TEXT") // Permite comentarios largos
    private String content;

    @NotBlank(message = "El autor no puede estar vacío")
    @Size(min = 3, message = "El autor debe tener al menos 3 caracteres")
    private String author;

    private LocalDateTime createdAt;

    /**
     * Relación ManyToOne con Post (muchos comentarios pertenecen a un post)
     * - Este es el lado PROPIETARIO de la relación (contiene la FK post_id)
     * - FetchType.LAZY: el Post se carga solo cuando se accede explícitamente (optimización)
     * - @JoinColumn: especifica el nombre de la columna FK en la tabla comments
     * - @JsonIgnore: CRÍTICO - evita loop infinito al serializar (Post → Comments → Post → ...)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id") // Columna FK en la tabla comments
    @JsonIgnore // Rompe el ciclo de serialización JSON
    private Post post;

    /**
     * Callback ejecutado antes de persistir el comentario por primera vez
     * Establece automáticamente la fecha de creación
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}