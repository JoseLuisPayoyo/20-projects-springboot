package com.payoyo.working.dtos;

import com.payoyo.working.model.enums.MetodoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para registrar el pago de una factura.
 * 
 * Contiene únicamente los campos necesarios para la operación
 * de marcar una factura como PAGADA.
 * 
 * Permite:
 * - Actualizar el método de pago real si difiere del acordado inicialmente
 * - Registrar la fecha en que se recibió el pago
 * - Añadir notas sobre el pago (referencia bancaria, comprobante, etc.)
 * 
 * Usado en:
 * - PATCH /api/invoices/{id}/pay
 * 
 * Validaciones de negocio adicionales en Service:
 * - El estado actual debe ser PENDIENTE o VENCIDA
 * - No se puede pagar una factura CANCELADA o ya PAGADA
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePaymentDTO {

    /**
     * Método de pago real utilizado.
     * 
     * Si es null o coincide con el método original, no se actualiza.
     * Si es diferente, actualiza el método de pago de la factura.
     * 
     * Ejemplo: La factura se emitió con método TRANSFERENCIA,
     * pero finalmente el cliente pagó con TARJETA.
     */
    private MetodoPago metodoPago;

    /**
     * Fecha en que se recibió el pago.
     * 
     * No puede ser fecha futura.
     * Útil para auditoría y análisis de tiempos de cobro.
     */
    @NotNull(message = "La fecha de pago es obligatoria")
    @PastOrPresent(message = "La fecha de pago no puede ser futura")
    private LocalDate fechaPago;

    /**
     * Notas sobre el pago (opcional).
     * 
     * Puede contener:
     * - Referencia bancaria
     * - Número de comprobante
     * - Número de autorización (tarjetas)
     * - Cualquier observación relevante sobre el cobro
     * 
     * Estas notas se añaden a las notas existentes de la factura.
     */
    private String notas;
}