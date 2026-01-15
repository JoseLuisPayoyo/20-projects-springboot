package com.payoyo.working.model.enums;

/**
 * Estados del ciclo de vida de una factura.
 * 
 * Transiciones permitidas:
 * - PENDIENTE → PAGADA (cuando se recibe el pago)
 * - PENDIENTE → CANCELADA (cuando se cancela manualmente)
 * - PENDIENTE → VENCIDA (asignado automáticamente si fechaVencimiento < hoy)
 * - VENCIDA → PAGADA (se permite pagar facturas vencidas)
 * - VENCIDA → CANCELADA (se permite cancelar facturas vencidas)
 * 
 * Estados finales (no permiten cambios):
 * - PAGADA: Una vez pagada, no se puede modificar ni cancelar
 * - CANCELADA: Una vez cancelada, no se puede modificar ni pagar
 */
public enum EstadoFactura {
    
    /**
     * Estado inicial al crear la factura.
     * Permite todas las operaciones: actualizar, pagar, cancelar.
     */
    PENDIENTE,
    
    /**
     * Factura pagada por el cliente.
     * Estado final - no permite modificaciones posteriores.
     */
    PAGADA,
    
    /**
     * Factura cancelada manualmente.
     * Estado final - no permite modificaciones posteriores.
     */
    CANCELADA,
    
    /**
     * Factura con fecha de vencimiento superada y aún no pagada.
     * Asignado automáticamente por el sistema.
     * Aún permite operaciones de pago o cancelación.
     */
    VENCIDA
}