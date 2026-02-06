package com.payoyo.working.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título no puede estar vacío")
    @Size(min = 2, max = 200, message = "El título debe tener entre 2 y 200 caracteres")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "El autor no puede estar vacío")
    @Size(min = 2, max = 100, message = "El autor debe tener entre 2 y 100 caracteres")
    @Column(nullable = false)
    private String author;

    // unique = true → constraint a nivel de BD para garantizar unicidad
    @NotBlank(message = "El ISBN no puede estar vacío")
    @Pattern(regexp = "^978-\\d{1,5}-\\d{1,7}-\\d{1,7}-\\d$",
            message = "Formato de ISBN no válido (ej: 978-0-06-088328-7)")
    @Column(unique = true, nullable = false)
    private String isbn;

    @NotBlank(message = "El género no puede estar vacío")
    @Column(nullable = false)
    private String genre;

    @NotNull(message = "El año de publicación es obligatorio")
    @Min(value = 1000, message = "El año debe ser al menos 1000")
    private Integer publishedYear;

    // Todo libro nuevo está disponible por defecto
    @Column(nullable = false)
    @Builder.Default
    private Boolean available = true;

    // updatable = false → no se modifica tras la creación
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // mappedBy = "book" → Loan es el dueño de esta relación (tiene la FK book_id)
    // @JsonIgnore → evita loop infinito: Book → loans → Loan → book → Book...
    @OneToMany(mappedBy = "book")
    @JsonIgnore
    @Builder.Default
    private List<Loan> loans = new ArrayList<>();

    // @PrePersist → callback JPA que se ejecuta antes del INSERT
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.available == null) {
            this.available = true;
        }
    }
}