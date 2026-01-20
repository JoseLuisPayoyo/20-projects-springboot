package com.payoyo.working.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para estructurar las respuestas de error
 * Proporciona información consistente al cliente sobre errores
 * 
 * Estructura JSON retornada:
 * {
 *   "timestamp": "2025-01-20T10:30:45",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Post no encontrado con ID: 5",
 *   "path": "/api/posts/5"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Momento exacto en que ocurrió el error
     * Útil para debugging y logs
     */
    private LocalDateTime timestamp;

    /**
     * Código de estado HTTP (404, 400, 500, etc.)
     */
    private int status;

    /**
     * Nombre del error HTTP (Not Found, Bad Request, etc.)
     */
    private String error;

    /**
     * Mensaje descriptivo del error
     * Debe ser claro para el cliente/desarrollador
     */
    private String message;

    /**
     * Ruta del endpoint que generó el error
     * Ayuda a identificar qué endpoint falló
     */
    private String path;

    /**
     * Constructor simplificado sin path
     * Útil cuando el path no es relevante
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
