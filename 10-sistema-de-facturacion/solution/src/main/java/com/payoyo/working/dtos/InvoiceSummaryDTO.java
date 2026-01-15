package com.payoyo.working.dtos;

import com.payoyo.working.model.enums.EstadoFactura;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO resumido de factura para listados.
 * 
 * Contiene solo los campos esenciales para mostrar en listados,
 * reduciendo el tamaño del payload en respuestas con múltiples facturas.
 * 
 * NO incluye:
 * - Items (pesados al deserializar)
 * - Campos detallados (dirección, concepto, notas)
 * - Información redundante
 * 
 * Usado en:
 * - GET /api/invoices (listar todas)
 * - GET /api/invoices/vencidas
 * - GET /api/invoices/cliente/{cliente}
 * - GET /api/invoices/estado/{estado}
 * - GET /api/invoices/fecha-rango
 * - GET /api/invoices/metodo-pago/{metodo}
 * 
 * Para detalle completo, el cliente debe llamar a GET /api/invoices/{id}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceSummaryDTO {

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
     * Fecha de emisión de la factura.
     */
    private LocalDate fechaEmision;

    /**
     * Total a pagar.
     */
    private BigDecimal total;

    /**
     * Estado actual de la factura.
     */
    private EstadoFactura estado;

    /**
     * Días restantes para el vencimiento (o días de retraso si es negativo).
     * 
     * Calculado en Service:
     * - Positivo: días que faltan
     * - Negativo: días de retraso
     * - 0: vence hoy
     * 
     * Útil para ordenar por urgencia en el frontend.
     */
    private Integer diasParaVencimiento;
}