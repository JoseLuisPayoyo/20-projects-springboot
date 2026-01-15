package com.payoyo.working.exceptions;

/**
 * Excepción lanzada cuando no se encuentra una factura.
 * 
 * Se utiliza en escenarios donde se busca una factura por:
 * - ID (Long)
 * - Número de factura (String)
 * 
 * Esta excepción será capturada por el GlobalExceptionHandler
 * y convertida en una respuesta HTTP 404 Not Found.
 */
public class InvoiceNotFoundException extends RuntimeException {

    /**
     * Constructor para búsqueda por ID.
     * 
     * @param id ID de la factura no encontrada
     */
    public InvoiceNotFoundException(Long id) {
        super("No se encontró la factura con ID: " + id);
    }

    /**
     * Constructor para búsqueda por número de factura.
     * 
     * @param numeroFactura Número de factura no encontrado
     */
    public InvoiceNotFoundException(String numeroFactura) {
        super("No se encontró la factura con número: " + numeroFactura);
    }

    /**
     * Constructor genérico con mensaje personalizado.
     * 
     * @param mensaje Mensaje de error personalizado
     */
    public InvoiceNotFoundException(String mensaje, boolean custom) {
        super(mensaje);
    }
}