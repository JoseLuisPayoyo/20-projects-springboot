package com.payoyo.working.exception;

/**
 * Lanzada cuando se intenta hacer una operación inválida sobre un pedido
 * Ejemplos: cancelar un pedido SHIPPED, cambiar estado de un pedido CANCELLED
 * Mapeada a HTTP 400 Bad Request en GlobalExceptionHandler
 */
public class InvalidOrderStateException extends RuntimeException {
    
    public InvalidOrderStateException(String message) {
        super(message);
    }
}