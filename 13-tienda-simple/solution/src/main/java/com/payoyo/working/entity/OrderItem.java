package com.payoyo.working.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación ManyToOne con Order
    // LAZY: no carga Order hasta que se acceda explícitamente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude // Evita loops infinitos
    @EqualsAndHashCode.Exclude
    @JsonBackReference // ⭐ Evita serializar Order desde OrderItem (previene loop)
    private Order order;

    // Relación ManyToOne con Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer quantity;

    // Precio capturado al momento de la compra (histórico)
    // Permite cambiar precio del producto sin afectar pedidos existentes
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;

    // Subtotal calculado: quantity * priceAtPurchase
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Calcula el subtotal: quantity * priceAtPurchase
     * Debe llamarse desde Service antes de guardar
     */
    public void calculateSubtotal() {
        if (priceAtPurchase != null && quantity != null) {
            this.subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        }
    }
}