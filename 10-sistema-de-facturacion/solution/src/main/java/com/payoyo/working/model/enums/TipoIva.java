package com.payoyo.working.model.enums;

/**
 * Tipos de IVA aplicables en España según la Agencia Tributaria (AEAT).
 * 
 * Los porcentajes corresponden a la normativa vigente del IVA español.
 * El tipo aplicable depende del producto o servicio facturado.
 */
public enum TipoIva {
    
    /**
     * IVA General - 21%
     * Aplicable a la mayoría de productos y servicios.
     * Es el tipo por defecto cuando no aplica ninguna reducción.
     */
    GENERAL(21),
    
    /**
     * IVA Reducido - 10%
     * Aplicable a: alimentación, transporte de viajeros, hostelería,
     * servicios culturales, vivienda, etc.
     */
    REDUCIDO(10),
    
    /**
     * IVA Superreducido - 4%
     * Aplicable a: productos de primera necesidad (pan, leche, huevos, frutas, verduras),
     * libros, periódicos, medicamentos, vivienda de protección oficial, etc.
     */
    SUPERREDUCIDO(4),
    
    /**
     * Exento de IVA - 0%
     * Operaciones exentas: servicios médicos, educación, seguros,
     * operaciones financieras, exportaciones, etc.
     */
    EXENTO(0);
    
    private final int porcentaje;
    
    TipoIva(int porcentaje) {
        this.porcentaje = porcentaje;
    }
    
    /**
     * Obtiene el porcentaje numérico del tipo de IVA.
     * @return Porcentaje como entero (21, 10, 4, 0)
     */
    public int getPorcentaje() {
        return porcentaje;
    }
    
    /**
     * Obtiene el porcentaje como decimal para cálculos (0.21, 0.10, 0.04, 0.00).
     * @return Porcentaje como decimal
     */
    public double getPorcentajeDecimal() {
        return porcentaje / 100.0;
    }
}