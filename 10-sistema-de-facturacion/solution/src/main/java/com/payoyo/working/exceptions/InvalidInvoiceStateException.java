package com.payoyo.working.exceptions;

/**
 * Excepción lanzada cuando se intenta realizar una operación
 * no permitida debido al estado actual de la factura.
 * 
 * Ejemplos de escenarios:
 * - Intentar pagar una factura ya PAGADA
 * - Intentar pagar una factura CANCELADA
 * - Intentar cancelar una factura ya PAGADA
 * - Intentar actualizar una factura PAGADA o CANCELADA
 * - Intentar aplicar un descuento mayor al subtotal
 * 
 * Esta excepción será capturada por el GlobalExceptionHandler
 * y convertida en una respuesta HTTP 409 Conflict.
 */
public class InvalidInvoiceStateException extends RuntimeException {

    /**
     * Constructor con mensaje descriptivo del conflicto.
     * 
     * @param mensaje Descripción del problema con el estado de la factura
     */
    public InvalidInvoiceStateException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa.
     * 
     * @param mensaje Descripción del problema
     * @param causa Excepción que causó este error
     */
    public InvalidInvoiceStateException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}