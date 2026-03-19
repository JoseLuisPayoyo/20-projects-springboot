package com.payoyo.working.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.payoyo.working.entity.enums.OrderStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber; // Formato: ORD-YYYYMMDD-XXXX

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Column(nullable = false)
    private String customerName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false, updatable = false)
    private LocalDateTime orderDate;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING) // Guarda como texto ("PENDING") no como número
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    // Relación bidireccional con OrderItem
    // cascade = ALL: al guardar Order se guardan items automáticamente
    // orphanRemoval = true: si se elimina un item de la lista, se borra de BD
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Inicializa lista en el builder
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonManagedReference // ⭐ Serializa los items desde Order
    private List<OrderItem> items = new ArrayList<>();

    // Hook de JPA ejecutado antes de persistir
    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING;
        }
        if (total == null) {
            total = BigDecimal.ZERO;
        }
    }

    // Helper method para mantener sincronización bidireccional
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this); // Sincroniza el lado inverso
    }

    // Helper method para eliminar item manteniendo sincronización
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}