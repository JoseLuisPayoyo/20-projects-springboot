package com.payoyo.working.exceptions;

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
 * Manejador global de excepciones para la API REST.
 * 
 * Centraliza el manejo de errores en toda la aplicación, proporcionando
 * respuestas HTTP consistentes y bien estructuradas para diferentes tipos
 * de excepciones.
 * 
 * Anotaciones:
 * - @RestControllerAdvice: Combina @ControllerAdvice + @ResponseBody
 *   Permite capturar excepciones de todos los controladores y
 *   retornar respuestas JSON automáticamente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja InvoiceNotFoundException.
     * Retorna HTTP 404 Not Found cuando no se encuentra una factura.
     * 
     * @param ex Excepción de factura no encontrada
     * @param request Request HTTP para obtener el path
     * @return ResponseEntity con ErrorResponse y status 404
     */
    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceNotFound(
            InvoiceNotFoundException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja InvalidInvoiceStateException.
     * Retorna HTTP 409 Conflict cuando se intenta una operación
     * no permitida por el estado actual de la factura.
     * 
     * Ejemplos:
     * - Pagar una factura ya pagada
     * - Cancelar una factura pagada
     * - Actualizar una factura cancelada
     * 
     * @param ex Excepción de estado inválido
     * @param request Request HTTP para obtener el path
     * @return ResponseEntity con ErrorResponse y status 409
     */
    @ExceptionHandler(InvalidInvoiceStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInvoiceState(
            InvalidInvoiceStateException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja MethodArgumentNotValidException.
     * Retorna HTTP 400 Bad Request cuando fallan las validaciones
     * de Bean Validation (@NotNull, @NotBlank, @Pattern, etc.).
     * 
     * Extrae todos los errores de validación y los mapea por campo,
     * proporcionando feedback detallado sobre qué campos son inválidos.
     * 
     * @param ex Excepción de validación
     * @param request Request HTTP para obtener el path
     * @return ResponseEntity con ErrorResponse incluyendo mapa de errores por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        // Extraer todos los errores de validación de campos
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Error de validación en los datos enviados",
            request.getRequestURI(),
            validationErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja IllegalArgumentException.
     * Retorna HTTP 400 Bad Request para argumentos inválidos
     * lanzados programáticamente en el código.
     * 
     * Ejemplo: Descuento mayor que subtotal, fechas incoherentes
     * validadas en Service, etc.
     * 
     * @param ex Excepción de argumento ilegal
     * @param request Request HTTP para obtener el path
     * @return ResponseEntity con ErrorResponse y status 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja cualquier otra excepción no capturada específicamente.
     * Retorna HTTP 500 Internal Server Error.
     * 
     * Este es el handler de último recurso para errores inesperados.
     * En producción, estos errores deberían ser loggeados para investigación.
     * 
     * @param ex Excepción genérica
     * @param request Request HTTP para obtener el path
     * @return ResponseEntity con ErrorResponse y status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        // En producción, aquí se debería loggear el error completo
        // logger.error("Error inesperado: ", ex);
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "Ocurrió un error interno en el servidor. Por favor, contacte al administrador.",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}