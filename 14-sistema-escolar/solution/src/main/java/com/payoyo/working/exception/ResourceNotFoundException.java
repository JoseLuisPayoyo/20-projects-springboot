package com.payoyo.working.exception;

/**
 * 404 — Recurso no encontrado.
 * RuntimeException porque no queremos forzar try-catch
 * en cada llamada; el GlobalExceptionHandler la captura.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}