package com.payoyo.working.dtos;

import com.payoyo.working.model.enums.MetodoPago;
import com.payoyo.working.model.enums.TipoIva;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para crear o actualizar facturas.
 * 
 * Contiene únicamente los campos que el usuario debe proporcionar.
 * Los campos calculados (subtotal, iva, total, numeroFactura) se generan
 * automáticamente en el Service.
 * 
 * Usado en:
 * - POST /api/invoices (crear nueva factura)
 * - PUT /api/invoices/{id} (actualizar factura existente)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceCreateDTO {

    /**
     * Nombre del cliente o razón social.
     */
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 3, max = 200, message = "El nombre del cliente debe tener entre 3 y 200 caracteres")
    private String cliente;

    /**
     * NIF/CIF del cliente (validación española).
     * 
     * Formatos válidos:
     * - NIF: 12345678A (8 dígitos + letra)
     * - NIE: X1234567A (X/Y/Z + 7 dígitos + letra)
     * - CIF: A12345678 (letra + 7 dígitos + control)
     */
    @NotBlank(message = "El NIF/CIF es obligatorio")
    @Pattern(
        regexp = "^[XYZ]?[0-9]{7,8}[A-Z]$|^[A-W][0-9]{7}[0-9A-J]$",
        message = "NIF/CIF inválido. Formatos: 12345678A (NIF), X1234567A (NIE), A12345678 (CIF)"
    )
    private String nifCif;

    /**
     * Dirección fiscal completa del cliente.
     */
    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 500, message = "La dirección no puede superar 500 caracteres")
    private String direccion;

    /**
     * Fecha de emisión de la factura.
     * No puede ser fecha futura (validación adicional en Service).
     */
    @NotNull(message = "La fecha de emisión es obligatoria")
    @PastOrPresent(message = "La fecha de emisión no puede ser futura")
    private LocalDate fechaEmision;

    /**
     * Fecha límite de pago.
     * Debe ser igual o posterior a fechaEmision (validado con @AssertTrue).
     */
    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;

    /**
     * Descripción general del servicio o producto facturado.
     */
    @NotBlank(message = "El concepto es obligatorio")
    private String concepto;

    /**
     * Tipo de IVA aplicado a toda la factura.
     * - GENERAL: 21%
     * - REDUCIDO: 10%
     * - SUPERREDUCIDO: 4%
     * - EXENTO: 0%
     */
    @NotNull(message = "El tipo de IVA es obligatorio")
    private TipoIva tipoIva;

    /**
     * Descuento aplicado a la factura (opcional).
     * Por defecto 0 si no se especifica.
     * Debe ser >= 0 y <= subtotal (validado en Service).
     */
    @NotNull(message = "El descuento es obligatorio")
    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
    private BigDecimal descuento = BigDecimal.ZERO;

    /**
     * Método de pago acordado.
     */
    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    /**
     * Notas adicionales sobre la factura (opcional).
     * Puede contener información sobre condiciones de pago, referencias bancarias, etc.
     */
    private String notas;

    /**
     * Lista de productos/servicios de la factura.
     * Cada item contiene: descripción, cantidad, precioUnitario.
     * 
     * La validación @Valid asegura que cada ItemDTO dentro de la lista
     * también pase sus propias validaciones.
     * 
     * Mínimo 1 item requerido.
     */
    @NotNull(message = "La lista de items es obligatoria")
    @Size(min = 1, message = "Debe haber al menos un item en la factura")
    @Valid  // Valida cada ItemDTO de la lista
    private List<ItemDTO> items;

    /**
     * Validación personalizada: la fecha de vencimiento debe ser igual o posterior
     * a la fecha de emisión.
     * 
     * @return true si las fechas son válidas, false en caso contrario
     */
    @AssertTrue(message = "La fecha de vencimiento debe ser igual o posterior a la fecha de emisión")
    public boolean isFechaVencimientoValida() {
        if (fechaEmision == null || fechaVencimiento == null) {
            return true; // Si son null, lo manejan las validaciones @NotNull
        }
        return !fechaVencimiento.isBefore(fechaEmision);
    }
}