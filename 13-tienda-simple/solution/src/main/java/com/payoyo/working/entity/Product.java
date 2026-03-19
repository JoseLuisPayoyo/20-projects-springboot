package com.payoyo.working.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2) // Hasta 99,999,999.99
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relación con OrderItem (no se usa típicamente en lógica, solo para navegación)
    // @JsonIgnore evita serialización circular al devolver Product
    @OneToMany(mappedBy = "product")
    @ToString.Exclude // Evita loops infinitos en toString()
    @EqualsAndHashCode.Exclude // Evita loops en equals/hashCode
    @JsonIgnore // ⭐ NO serializa orderItems al devolver Product
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    // Hook de JPA para setear createdAt automáticamente antes de persistir
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}