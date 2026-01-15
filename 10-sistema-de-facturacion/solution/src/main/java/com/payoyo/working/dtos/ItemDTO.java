package com.payoyo.working.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO que representa un producto o servicio dentro de una factura.
 * 
 * Este DTO se utiliza:
 * - Anidado en InvoiceCreateDTO (Request - lista de items al crear factura)
 * - Anidado en InvoiceDetailDTO (Response - items parseados desde JSON)
 * 
 * El campo 'importe' es calculado automáticamente en el Service
 * como: cantidad × precioUnitario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {

    /**
     * Descripción del producto o servicio.
     * Ejemplo: "Desarrollo frontend React", "Consultoría técnica", "Licencia software"
     */
    @NotBlank(message = "La descripción del item es obligatoria")
    private String descripcion;

    /**
     * Cantidad de unidades del producto/servicio.
     * Debe ser un número positivo entero.
     * Ejemplo: 10 horas, 5 licencias, 3 unidades
     */
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    /**
     * Precio unitario del producto/servicio (sin IVA).
     * Debe ser mayor a 0.
     * Precisión: 2 decimales.
     * Ejemplo: 50.00€/hora, 120.50€/unidad
     */
    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor a 0")
    private BigDecimal precioUnitario;

    /**
     * Importe total del item (cantidad × precioUnitario).
     * 
     * En Request (InvoiceCreateDTO): Campo opcional, se calcula automáticamente
     * En Response (InvoiceDetailDTO): Campo calculado y poblado por el Service
     * 
     * Fórmula: importe = cantidad × precioUnitario
     * Precisión: 2 decimales.
     */
    private BigDecimal importe;
}