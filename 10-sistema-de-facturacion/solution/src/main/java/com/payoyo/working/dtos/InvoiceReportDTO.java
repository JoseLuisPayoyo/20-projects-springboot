package com.payoyo.working.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para reportes financieros mensuales.
 * 
 * Contiene estadísticas agregadas de un periodo específico (mes/año).
 * NO mapea directamente a una entidad, sino que se construye en el Service
 * mediante agregaciones de múltiples facturas.
 * 
 * Usado en:
 * - GET /api/invoices/reporte/mensual/{anio}/{mes}
 * 
 * Proporciona una vista ejecutiva del estado financiero del periodo,
 * útil para dashboards, análisis de flujo de caja y toma de decisiones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceReportDTO {

    /**
     * Periodo del reporte en formato YYYY-MM.
     * Ejemplo: "2024-01" para enero de 2024
     */
    private String periodo;

    /**
     * Total facturado en el mes (suma de todos los totales).
     * Incluye facturas de todos los estados.
     */
    private BigDecimal totalFacturado;

    /**
     * Total cobrado en el mes (suma de facturas PAGADAS).
     * Representa el dinero realmente ingresado.
     */
    private BigDecimal totalCobrado;

    /**
     * Total pendiente de cobro (suma de facturas PENDIENTES + VENCIDAS).
     * Representa el dinero por recibir.
     */
    private BigDecimal totalPendiente;

    /**
     * Cantidad total de facturas emitidas en el mes.
     */
    private Integer cantidadFacturas;

    /**
     * Cantidad de facturas pagadas en el mes.
     */
    private Integer cantidadPagadas;

    /**
     * Cantidad de facturas pendientes de pago.
     */
    private Integer cantidadPendientes;

    /**
     * Cantidad de facturas vencidas (fechaVencimiento superada).
     */
    private Integer cantidadVencidas;

    /**
     * Ticket promedio (totalFacturado / cantidadFacturas).
     * 
     * Calculado en Service como:
     * - Si cantidadFacturas > 0: totalFacturado / cantidadFacturas
     * - Si cantidadFacturas = 0: 0
     * 
     * Útil para analizar el valor medio de las transacciones.
     */
    private BigDecimal promedioTicket;
}