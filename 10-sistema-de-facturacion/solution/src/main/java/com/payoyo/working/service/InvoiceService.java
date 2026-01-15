package com.payoyo.working.service;

import com.payoyo.working.dtos.*;
import com.payoyo.working.model.enums.EstadoFactura;
import com.payoyo.working.model.enums.MetodoPago;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interfaz del servicio de gestión de facturas.
 * 
 * Define el contrato para todas las operaciones de negocio relacionadas
 * con facturas, incluyendo CRUD, operaciones especializadas, cálculos
 * automáticos y generación de reportes.
 */
public interface InvoiceService {

    // ========== OPERACIONES CRUD ==========

    /**
     * Crea una nueva factura con cálculos automáticos.
     * 
     * Operaciones realizadas:
     * - Generar número de factura único (FACT-YYYY-XXXX)
     * - Calcular subtotal (suma de items)
     * - Calcular IVA según tipoIva
     * - Calcular total (subtotal + IVA - descuento)
     * - Serializar items a JSON
     * - Establecer estado inicial: PENDIENTE
     * - Validar fechas coherentes
     * - Validar descuento <= subtotal
     * 
     * @param dto Datos de la factura a crear
     * @return DTO con la factura creada y campos calculados
     * @throws IllegalArgumentException si fechaVencimiento < fechaEmision o descuento > subtotal
     */
    InvoiceResponseDTO crearFactura(InvoiceCreateDTO dto);

    /**
     * Obtiene todas las facturas en formato resumido.
     * 
     * @return Lista de facturas en formato InvoiceSummaryDTO
     */
    List<InvoiceSummaryDTO> obtenerTodas();

    /**
     * Obtiene el detalle completo de una factura por ID.
     * 
     * Incluye items deserializados desde JSON y campos calculados
     * adicionales (diasVencimiento, estadoVencimiento).
     * 
     * @param id ID de la factura
     * @return DTO con detalle completo de la factura
     * @throws com.payoyo.working.exception.InvoiceNotFoundException si no existe
     */
    InvoiceDetailDTO obtenerPorId(Long id);

    /**
     * Actualiza una factura existente.
     * 
     * Validaciones:
     * - Solo se pueden actualizar facturas en estado PENDIENTE
     * - No se pueden actualizar facturas PAGADAS o CANCELADAS
     * - Recalcula todos los campos (subtotal, IVA, total)
     * - Reserializa items a JSON
     * 
     * @param id ID de la factura a actualizar
     * @param dto Nuevos datos de la factura
     * @return DTO con la factura actualizada
     * @throws com.payoyo.working.exception.InvoiceNotFoundException si no existe
     * @throws com.payoyo.working.exception.InvalidInvoiceStateException si estado != PENDIENTE
     */
    InvoiceResponseDTO actualizarFactura(Long id, InvoiceCreateDTO dto);

    /**
     * Elimina una factura por ID.
     * 
     * Puede ser eliminación física o lógica según implementación.
     * 
     * @param id ID de la factura a eliminar
     * @throws com.payoyo.working.exception.InvoiceNotFoundException si no existe
     */
    void eliminarFactura(Long id);

    // ========== BÚSQUEDAS Y CONSULTAS ==========

    /**
     * Busca una factura por su número único.
     * 
     * @param numeroFactura Número de factura (formato: FACT-YYYY-XXXX)
     * @return DTO con detalle completo de la factura
     * @throws com.payoyo.working.exception.InvoiceNotFoundException si no existe
     */
    InvoiceDetailDTO buscarPorNumero(String numeroFactura);

    /**
     * Obtiene todas las facturas vencidas.
     * 
     * Una factura está vencida si:
     * - Estado = PENDIENTE
     * - fechaVencimiento < hoy
     * 
     * @return Lista de facturas vencidas en formato resumido
     */
    List<InvoiceSummaryDTO> obtenerVencidas();

    /**
     * Busca facturas por nombre de cliente (búsqueda parcial case-insensitive).
     * 
     * @param cliente Texto a buscar en el nombre del cliente
     * @return Lista de facturas que coinciden
     */
    List<InvoiceSummaryDTO> buscarPorCliente(String cliente);

    /**
     * Filtra facturas por estado.
     * 
     * @param estado Estado de la factura (PENDIENTE, PAGADA, CANCELADA, VENCIDA)
     * @return Lista de facturas con el estado especificado
     */
    List<InvoiceSummaryDTO> buscarPorEstado(EstadoFactura estado);

    /**
     * Filtra facturas por método de pago.
     * 
     * @param metodoPago Método de pago
     * @return Lista de facturas con el método de pago especificado
     */
    List<InvoiceSummaryDTO> buscarPorMetodoPago(MetodoPago metodoPago);

    /**
     * Busca facturas emitidas en un rango de fechas (inclusive).
     * 
     * @param inicio Fecha de inicio del rango
     * @param fin Fecha de fin del rango
     * @return Lista de facturas emitidas entre las fechas especificadas
     */
    List<InvoiceSummaryDTO> buscarPorRangoFechas(LocalDate inicio, LocalDate fin);

    // ========== OPERACIONES DE NEGOCIO ==========

    /**
     * Marca una factura como pagada.
     * 
     * Operaciones:
     * - Cambiar estado a PAGADA
     * - Actualizar metodoPago si se especifica uno diferente
     * - Añadir notas del pago a las notas existentes
     * 
     * Validaciones:
     * - Estado actual debe ser PENDIENTE o VENCIDA
     * - No se puede pagar una factura CANCELADA
     * - No se puede pagar una factura ya PAGADA
     * 
     * @param id ID de la factura
     * @param dto Datos del pago (metodoPago, fechaPago, notas)
     * @return DTO con la factura actualizada
     * @throws com.payoyo.working.exception.InvoiceNotFoundException si no existe
     * @throws com.payoyo.working.exception.InvalidInvoiceStateException si estado no permite pago
     */
    InvoiceResponseDTO marcarComoPagada(Long id, InvoicePaymentDTO dto);

    /**
     * Cancela una factura.
     * 
     * Validaciones:
     * - Solo se pueden cancelar facturas PENDIENTES o VENCIDAS
     * - No se puede cancelar una factura PAGADA
     * 
     * @param id ID de la factura
     * @return DTO con la factura cancelada
     * @throws com.payoyo.working.exception.InvoiceNotFoundException si no existe
     * @throws com.payoyo.working.exception.InvalidInvoiceStateException si estado = PAGADA
     */
    InvoiceResponseDTO cancelarFactura(Long id);

    // ========== REPORTES Y ESTADÍSTICAS ==========

    /**
     * Genera un reporte financiero mensual.
     * 
     * Calcula:
     * - Total facturado en el mes
     * - Total cobrado (facturas PAGADAS)
     * - Total pendiente (facturas PENDIENTES + VENCIDAS)
     * - Cantidades por estado
     * - Promedio de ticket
     * 
     * @param anio Año del reporte
     * @param mes Mes del reporte (1-12)
     * @return DTO con estadísticas del mes
     */
    InvoiceReportDTO reporteMensual(int anio, int mes);

    /**
     * Obtiene totales generales de todas las facturas.
     * 
     * Retorna un mapa con:
     * - totalFacturas: cantidad total
     * - totalPendientes, totalPagadas, totalCanceladas, totalVencidas: cantidades por estado
     * - montoTotal: suma de todas las facturas
     * - montoCobrado: suma de facturas pagadas
     * - montoPendiente: suma de facturas pendientes + vencidas
     * 
     * @return Mapa con estadísticas generales
     */
    Map<String, Object> obtenerTotales();
}