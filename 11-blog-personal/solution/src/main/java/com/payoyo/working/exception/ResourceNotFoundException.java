package com.payoyo.working.exception;

/**
 * Excepción personalizada para recursos no encontrados
 * Se lanza cuando no existe una entidad solicitada (Post, Comment)
 * 
 * Extiende RuntimeException (unchecked exception):
 * - No requiere try-catch obligatorio
 * - Spring la captura automáticamente en @ControllerAdvice
 * - Permite código más limpio en Services
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado
     * Ejemplo: "Post no encontrado con ID: 5"
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa raíz
     * Útil para encadenar excepciones (exception chaining)
     * Ejemplo: capturar DataAccessException y relanzar como ResourceNotFoundException
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor con formato de mensaje
     * Permite crear mensajes dinámicos más fácilmente
     * Ejemplo: new ResourceNotFoundException("Post", "id", 5)
     *          → "Post no encontrado con id: 5"
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s no encontrado con %s: %s", resourceName, fieldName, fieldValue));
    }
}