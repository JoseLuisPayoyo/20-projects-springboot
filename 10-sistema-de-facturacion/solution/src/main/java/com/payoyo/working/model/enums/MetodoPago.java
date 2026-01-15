package com.payoyo.working.model.enums;

/**
 * Métodos de pago aceptados para facturas.
 * 
 * Estos valores corresponden a las formas de pago más comunes
 * en el sistema fiscal español (AEAT).
 * 
 * El método de pago puede ser especificado al crear la factura
 * y puede actualizarse al momento de registrar el pago real.
 */
public enum MetodoPago {
    
    /**
     * Pago en efectivo.
     * Generalmente para montos pequeños o transacciones inmediatas.
     * Limitado a 1.000€ para pagos entre empresarios según normativa española.
     */
    EFECTIVO,
    
    /**
     * Transferencia bancaria SEPA.
     * Método más común para pagos B2B (empresa a empresa) en España.
     */
    TRANSFERENCIA,
    
    /**
     * Pago con tarjeta de crédito o débito.
     * Común en servicios en línea o puntos de venta.
     */
    TARJETA,
    
    /**
     * Domiciliación bancaria.
     * Cobro directo en cuenta del cliente mediante autorización SEPA.
     */
    DOMICILIACION,
    
    /**
     * Pago mediante cheque bancario.
     * Menos común pero aún utilizado en algunas transacciones comerciales.
     */
    CHEQUE,
    
    /**
     * Pago mediante pagaré.
     * Documento de compromiso de pago a fecha determinada.
     */
    PAGARE
}