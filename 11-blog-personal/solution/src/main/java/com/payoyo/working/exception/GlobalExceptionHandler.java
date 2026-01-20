package com.payoyo.working.exception;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación
 * 
 * @RestControllerAdvice:
 * - Intercepta excepciones de todos los @RestController
 * - Centraliza el manejo de errores (no repetir try-catch en cada Controller)
 * - Retorna automáticamente respuestas JSON estructuradas
 * 
 * Ventajas:
 * - Código más limpio en Controllers (no manejan errores)
 * - Respuestas de error consistentes en toda la API
 * - Fácil mantenimiento (un solo lugar para cambiar formato de errores)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja ResourceNotFoundException (entidades no encontradas)
     * Retorna: 404 NOT FOUND
     * 
     * Casos de uso:
     * - GET /api/posts/999 (post no existe)
     * - GET /api/comments/888 (comment no existe)
     * - PUT /api/posts/777 (intentar actualizar post inexistente)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja BadRequestException (peticiones inválidas)
     * Retorna: 400 BAD REQUEST
     * 
     * Casos de uso:
     * - Intentar crear comentario en post inexistente
     * - Operación no permitida por reglas de negocio
     * - Datos inválidos (más allá de validaciones Bean Validation)
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja MethodArgumentNotValidException (validaciones Bean Validation)
     * Retorna: 400 BAD REQUEST con detalles de todos los campos inválidos
     * 
     * Se lanza cuando @Valid falla en Controllers:
     * - @NotBlank, @Size, @Min, @Max, etc.
     * 
     * Retorna un Map con todos los errores de validación:
     * {
     *   "timestamp": "2025-01-20T10:30:45",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "Errores de validación",
     *   "path": "/api/posts",
     *   "validationErrors": {
     *     "title": "El título debe tener al menos 5 caracteres",
     *     "content": "El contenido no puede estar vacío",
     *     "author": "El autor debe tener al menos 3 caracteres"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Extraer todos los errores de validación
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        // Construir respuesta con información del error y validaciones
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorResponse.put("message", "Errores de validación");
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("validationErrors", validationErrors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja cualquier otra excepción no controlada
     * Retorna: 500 INTERNAL SERVER ERROR
     * 
     * Actúa como red de seguridad (catch-all):
     * - Errores inesperados (NullPointerException, etc.)
     * - Problemas de base de datos no previstos
     * - Cualquier RuntimeException no manejada específicamente
     * 
     * IMPORTANTE: En producción, NO revelar detalles técnicos al cliente
     * (mensaje genérico, pero loguear el error completo internamente)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        // En producción: mensaje genérico para el cliente
        // En desarrollo: mensaje detallado para debugging
        String message = "Ha ocurrido un error interno en el servidor";
        
        // TODO: Añadir logging aquí (log.error("Error inesperado", ex))
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}