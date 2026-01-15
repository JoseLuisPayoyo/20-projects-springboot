package com.payoyo.working.dtos;

import com.payoyo.working.model.enums.EstadoFactura;
import com.payoyo.working.model.enums.MetodoPago;
import com.payoyo.working.model.enums.TipoIva;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de respuesta estándar para operaciones CRUD de facturas.
 * 
 * Incluye todos los campos calculados (subtotal, IVA, total) pero
 * NO incluye información extendida como items parseados, notas largas
 * o direcciones completas.
 * 
 * Usado en:
 * - POST /api/invoices (respuesta al crear)
 * - PUT /api/invoices/{id} (respuesta al actualizar)
 * - PATCH /api/invoices/{id}/pay (respuesta al marcar como pagada)
 * - PATCH /api/invoices/{id}/cancel (respuesta al cancelar)
 * 
 * Para detalle completo con items, usar InvoiceDetailDTO.
 * Para listados optimizados, usar InvoiceSummaryDTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponseDTO {

    /**
     * ID único de la factura.
     */
    private Long id;

    /**
     * Número único de factura (formato: FACT-YYYY-XXXX).
     * Generado automáticamente por el sistema.
     */
    private String numeroFactura;

    /**
     * Nombre del cliente o razón social.
     */
    private String cliente;

    /**
     * NIF/CIF del cliente.
     */
    private String nifCif;

    /**
     * Fecha de emisión de la factura.
     */
    private LocalDate fechaEmision;

    /**
     * Fecha límite de pago.
     */
    private LocalDate fechaVencimiento;

    /**
     * Tipo de IVA aplicado (GENERAL 21%, REDUCIDO 10%, SUPERREDUCIDO 4%, EXENTO 0%).
     */
    private TipoIva tipoIva;

    /**
     * Subtotal antes de IVA.
     * Calculado automáticamente: suma de (cantidad × precioUnitario) de todos los items.
     */
    private BigDecimal subtotal;

    /**
     * Importe del IVA aplicado.
     * Calculado automáticamente: subtotal × tipoIva.getPorcentajeDecimal()
     */
    private BigDecimal iva;

    /**
     * Descuento aplicado a la factura.
     */
    private BigDecimal descuento;

    /**
     * Total a pagar (subtotal + IVA - descuento).
     * Calculado automáticamente.
     */
    private BigDecimal total;

    /**
     * Estado actual de la factura.
     * Valores: PENDIENTE, PAGADA, CANCELADA, VENCIDA
     */
    private EstadoFactura estado;

    /**
     * Método de pago acordado o utilizado.
     */
    private MetodoPago metodoPago;
}