package com.payoyo.working.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.payoyo.working.model.enums.EstadoFactura;
import com.payoyo.working.model.enums.MetodoPago;
import com.payoyo.working.model.enums.TipoIva;

/**
 * Entidad que representa una factura según normativa española.
 * 
 * Características principales:
 * - Número de factura único generado automáticamente (formato: FACT-YYYY-XXXX)
 * - Cálculos automáticos de subtotal, IVA (según tipo) y total en el Service
 * - Soporte para múltiples tipos de IVA español (21%, 10%, 4%, exento)
 * - Items almacenados como JSON String para flexibilidad
 * - Validaciones fiscales españolas (NIF/CIF, fechas, montos)
 * - Estados del ciclo de vida con transiciones controladas
 */
@Entity
@Table(name = "invoices",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_numero_factura",
           columnNames = "numero_factura"
       ))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número único de factura generado automáticamente.
     * Formato: FACT-YYYY-XXXX (ej: FACT-2024-0001)
     * Se incrementa por año automáticamente en el Service.
     */
    @Column(name = "numero_factura", nullable = false, unique = true, length = 20)
    @NotBlank(message = "El número de factura es obligatorio")
    private String numeroFactura;

    /**
     * Nombre del cliente o razón social de la empresa.
     */
    @Column(nullable = false)
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 3, max = 200, message = "El nombre del cliente debe tener entre 3 y 200 caracteres")
    private String cliente;

    /**
     * NIF/CIF (Número de Identificación Fiscal / Código de Identificación Fiscal) español.
     * 
     * Formatos válidos:
     * - NIF (DNI): 8 dígitos + letra (12345678A)
     * - NIE: Letra + 7 dígitos + letra (X1234567A)
     * - CIF: Letra + 7 dígitos + carácter de control (A12345678, B12345678)
     * 
     * Pattern regex: Acepta los tres formatos principales de identificación fiscal española.
     */
    @Column(name = "nif_cif", nullable = false, length = 10)
    @NotBlank(message = "El NIF/CIF es obligatorio")
    @Pattern(
        regexp = "^[XYZ]?[0-9]{7,8}[A-Z]$|^[A-W][0-9]{7}[0-9A-J]$",
        message = "NIF/CIF inválido. Formatos válidos: 12345678A (NIF), X1234567A (NIE), A12345678 (CIF)"
    )
    private String nifCif;

    /**
     * Dirección fiscal completa del cliente.
     */
    @Column(nullable = false, length = 500)
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    /**
     * Fecha de emisión de la factura.
     * No puede ser fecha futura (validado en Service).
     */
    @Column(name = "fecha_emision", nullable = false)
    @NotNull(message = "La fecha de emisión es obligatoria")
    @PastOrPresent(message = "La fecha de emisión no puede ser futura")
    private LocalDate fechaEmision;

    /**
     * Fecha límite de pago.
     * Debe ser igual o posterior a la fecha de emisión (validado en Service).
     * Si la fecha se supera y el estado es PENDIENTE, el sistema lo cambia a VENCIDA.
     */
    @Column(name = "fecha_vencimiento", nullable = false)
    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Future(message = "La fecha de vencimiento debe ser futura")
    private LocalDate fechaVencimiento;

    /**
     * Descripción general del servicio o producto facturado.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "El concepto es obligatorio")
    private String concepto;

    /**
     * Subtotal antes de impuestos.
     * Calculado automáticamente en Service: suma de (cantidad × precioUnitario) de todos los items.
     * Precisión: 2 decimales.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.01", message = "El subtotal debe ser mayor a 0")
    private BigDecimal subtotal;

    /**
     * Tipo de IVA aplicado a la factura.
     * Determina el porcentaje de IVA a aplicar:
     * - GENERAL: 21%
     * - REDUCIDO: 10%
     * - SUPERREDUCIDO: 4%
     * - EXENTO: 0%
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_iva", nullable = false, length = 20)
    @NotNull(message = "El tipo de IVA es obligatorio")
    private TipoIva tipoIva;

    /**
     * Impuesto al Valor Agregado (IVA).
     * Calculado automáticamente en Service: subtotal × tipoIva.getPorcentajeDecimal()
     * Ejemplos:
     * - GENERAL (21%): subtotal × 0.21
     * - REDUCIDO (10%): subtotal × 0.10
     * - SUPERREDUCIDO (4%): subtotal × 0.04
     * - EXENTO (0%): subtotal × 0.00
     * Precisión: 2 decimales.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull(message = "El IVA es obligatorio")
    @DecimalMin(value = "0.00", message = "El IVA no puede ser negativo")
    private BigDecimal iva;

    /**
     * Descuento aplicado a la factura.
     * Opcional, por defecto 0.
     * Debe ser menor o igual al subtotal (validado en Service).
     * Precisión: 2 decimales.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull(message = "El descuento es obligatorio")
    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
    private BigDecimal descuento;

    /**
     * Total a pagar (con IVA, menos descuento).
     * Calculado automáticamente en Service: subtotal + iva - descuento.
     * Precisión: 2 decimales.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull(message = "El total es obligatorio")
    @DecimalMin(value = "0.01", message = "El total debe ser mayor a 0")
    private BigDecimal total;

    /**
     * Estado actual de la factura en su ciclo de vida.
     * Almacenado como String para mejor legibilidad en BD.
     * 
     * Estados y transiciones:
     * - PENDIENTE: Estado inicial, permite todas las operaciones
     * - PAGADA: Estado final, no permite modificaciones
     * - CANCELADA: Estado final, no permite modificaciones
     * - VENCIDA: Asignado automáticamente si fechaVencimiento < hoy
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "El estado es obligatorio")
    private EstadoFactura estado;

    /**
     * Método de pago acordado o utilizado.
     * Almacenado como String para mejor legibilidad en BD.
     * Puede actualizarse al registrar el pago real.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    /**
     * Notas adicionales sobre la factura.
     * Opcional, puede contener información sobre condiciones de pago,
     * referencias bancarias, o cualquier observación relevante.
     */
    @Column(columnDefinition = "TEXT")
    private String notas;

    /**
     * Detalle de productos/servicios en formato JSON.
     * 
     * Almacenado como String para:
     * 1. Evitar tabla relacionada (proyecto enfocado en DTOs, no relaciones)
     * 2. Flexibilidad en estructura de items sin migraciones
     * 3. Atomicidad: items siempre se modifican junto con la factura
     * 
     * Estructura JSON esperada:
     * [
     *   {
     *     "descripcion": "Producto/Servicio",
     *     "cantidad": 10,
     *     "precioUnitario": 150.00,
     *     "importe": 1500.00
     *   }
     * ]
     * 
     * columnDefinition = "TEXT" permite JSONs grandes (>255 chars).
     * La serialización/deserialización se maneja en el Service con Jackson.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Los items son obligatorios")
    private String items;
}