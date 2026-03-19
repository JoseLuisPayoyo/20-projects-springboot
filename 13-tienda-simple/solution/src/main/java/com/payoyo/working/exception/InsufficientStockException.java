package com.payoyo.working.exception;

/**
 * Lanzada cuando se intenta crear un pedido sin stock suficiente
 * Mapeada a HTTP 400 Bad Request en GlobalExceptionHandler
 */
public class InsufficientStockException extends RuntimeException {
    
    public InsufficientStockException(String message) {
        super(message);
    }
}