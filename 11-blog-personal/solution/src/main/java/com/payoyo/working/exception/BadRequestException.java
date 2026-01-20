package com.payoyo.working.exception;


/**
 * Excepción personalizada para peticiones inválidas
 * Se lanza cuando:
 * - Los datos enviados no son válidos
 * - Se viola una regla de negocio
 * - Operación no permitida por el estado actual
 * 
 * Ejemplos de uso:
 * - Intentar crear comentario sin contenido
 * - Intentar asociar comentario a post inexistente
 * - Datos duplicados cuando no se permiten
 */
public class BadRequestException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado
     * Ejemplo: "No se puede crear un comentario sin contenido"
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa raíz
     * Útil para envolver excepciones de validación
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
