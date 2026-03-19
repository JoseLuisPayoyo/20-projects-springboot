package com.payoyo.working.entity.enums;

/**
 * Estados posibles de un pedido
 * 
 * Transiciones válidas:
 * PENDING → CONFIRMED, CANCELLED
 * CONFIRMED → SHIPPED, CANCELLED
 * SHIPPED → DELIVERED
 * DELIVERED → (estado final)
 * CANCELLED → (estado final)
 */
public enum OrderStatus {
    PENDING,      // Pedido creado, pendiente de confirmación
    CONFIRMED,    // Pedido confirmado por el cliente
    SHIPPED,      // Pedido enviado
    DELIVERED,    // Pedido entregado
    CANCELLED     // Pedido cancelado
}