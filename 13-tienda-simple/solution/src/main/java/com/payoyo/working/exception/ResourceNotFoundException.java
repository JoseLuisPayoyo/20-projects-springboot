package com.payoyo.working.exception;

/**
 * Lanzada cuando no se encuentra un recurso (Product, Order, etc.)
 * Mapeada a HTTP 404 Not Found en GlobalExceptionHandler
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
}