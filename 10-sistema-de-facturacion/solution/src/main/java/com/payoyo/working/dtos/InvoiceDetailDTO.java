package com.payoyo.working.dtos;

import com.payoyo.working.model.enums.EstadoFactura;
import com.payoyo.working.model.enums.MetodoPago;
import com.payoyo.working.model.enums.TipoIva;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de detalle completo de factura.
 * 
 * Incluye todos los campos de la factura, incluyendo:
 * - Items deserializados desde JSON a List<ItemDTO>
 * - Información completa (dirección, concepto, notas)
 * - Campos calculados adicionales (diasVencimiento, estadoVencimiento)
 * 
 * Usado en:
 * - GET /api/invoices/{id} (obtener detalle completo)
 * - GET /api/invoices/numero/{numero} (buscar por número)
 * 
 * Este DTO es más "pesado" que InvoiceResponseDTO porque incluye
 * los items parseados, por lo que se usa solo cuando se necesita
 * el detalle completo, no en listados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDetailDTO {

    /**
     * ID único de la factura.
     */
    private Long id;

    /**
     * Número único de factura (formato: FACT-YYYY-XXXX).
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
     * Dirección fiscal completa del cliente.
     */
    private String direccion;

    /**
     * Fecha de emisión de la factura.
     */
    private LocalDate fechaEmision;

    /**
     * Fecha límite de pago.
     */
    private LocalDate fechaVencimiento;

    /**
     * Descripción general del servicio o producto facturado.
     */
    private String concepto;

    /**
     * Tipo de IVA aplicado (GENERAL 21%, REDUCIDO 10%, SUPERREDUCIDO 4%, EXENTO 0%).
     */
    private TipoIva tipoIva;

    /**
     * Subtotal antes de IVA.
     */
    private BigDecimal subtotal;

    /**
     * Importe del IVA aplicado.
     */
    private BigDecimal iva;

    /**
     * Descuento aplicado a la factura.
     */
    private BigDecimal descuento;

    /**
     * Total a pagar (subtotal + IVA - descuento).
     */
    private BigDecimal total;

    /**
     * Estado actual de la factura.
     */
    private EstadoFactura estado;

    /**
     * Método de pago acordado o utilizado.
     */
    private MetodoPago metodoPago;

    /**
     * Notas adicionales sobre la factura.
     */
    private String notas;

    /**
     * Lista de items (productos/servicios) de la factura.
     * 
     * Deserializada desde el campo JSON String de la entidad.
     * Cada item incluye: descripción, cantidad, precioUnitario, importe.
     */
    private List<ItemDTO> items;

    /**
     * Días restantes hasta el vencimiento (o días de retraso si es negativo).
     * 
     * Calculado en Service como: DAYS_BETWEEN(hoy, fechaVencimiento)
     * - Positivo: días que faltan para vencimiento
     * - Negativo: días de retraso
     * - 0: vence hoy
     */
    private Integer diasVencimiento;

    /**
     * Estado del vencimiento en texto descriptivo.
     * 
     * Calculado en Service según diasVencimiento:
     * - "Al corriente" (si diasVencimiento > 7)
     * - "Por vencer" (si 0 <= diasVencimiento <= 7)
     * - "Vencida" (si diasVencimiento < 0)
     */
    private String estadoVencimiento;
}