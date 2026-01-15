package com.payoyo.working.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Clase para estructurar las respuestas de error de la API.
 * 
 * Proporciona una estructura consistente para todos los errores,
 * facilitando el manejo en el cliente (frontend, móvil, etc.).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Timestamp del momento en que ocurrió el error.
     */
    private LocalDateTime timestamp;

    /**
     * Código de estado HTTP (400, 404, 409, 500, etc.).
     */
    private int status;

    /**
     * Nombre del error HTTP (Bad Request, Not Found, Conflict, etc.).
     */
    private String error;

    /**
     * Mensaje descriptivo del error.
     * Puede ser un mensaje simple o una descripción detallada.
     */
    private String message;

    /**
     * Path del endpoint donde ocurrió el error.
     * Ejemplo: "/api/invoices/123"
     */
    private String path;

    /**
     * Detalles adicionales del error (opcional).
     * 
     * Usado principalmente para errores de validación, donde
     * se mapean los campos con sus mensajes de error específicos.
     * 
     * Ejemplo:
     * {
     *   "cliente": "El nombre del cliente es obligatorio",
     *   "nifCif": "NIF/CIF inválido"
     * }
     */
    private Map<String, String> validationErrors;

    /**
     * Constructor simplificado para errores sin validaciones detalladas.
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}