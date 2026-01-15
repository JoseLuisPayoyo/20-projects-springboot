package com.payoyo.working.controller;

import com.payoyo.working.dtos.*;
import com.payoyo.working.model.enums.EstadoFactura;
import com.payoyo.working.model.enums.MetodoPago;
import com.payoyo.working.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de facturas.
 * 
 * Expone todos los endpoints de la API de facturación:
 * - CRUD básico
 * - Búsquedas y filtros especializados
 * - Operaciones de negocio (pagar, cancelar)
 * - Reportes y estadísticas
 * 
 * Base URL: /api/invoices
 * 
 * Anotaciones:
 * - @RestController: Combina @Controller + @ResponseBody (respuestas automáticas en JSON)
 * - @RequestMapping: Define la ruta base del controlador
 * - @RequiredArgsConstructor: Inyección de dependencias por constructor (Lombok)
 */
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    // Inyección de dependencias inmutable
    private final InvoiceService service;

    // ========== CRUD BÁSICO ==========

    /**
     * Crea una nueva factura.
     * 
     * POST /api/invoices
     * 
     * @param dto Datos de la factura (validados con @Valid)
     * @return ResponseEntity con factura creada y status 201 Created
     */
    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> crearFactura(@Valid @RequestBody InvoiceCreateDTO dto) {
        InvoiceResponseDTO factura = service.crearFactura(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(factura);
    }

    /**
     * Obtiene todas las facturas en formato resumido.
     * 
     * GET /api/invoices
     * 
     * @return ResponseEntity con lista de facturas y status 200 OK
     */
    @GetMapping
    public ResponseEntity<List<InvoiceSummaryDTO>> obtenerTodas() {
        List<InvoiceSummaryDTO> facturas = service.obtenerTodas();
        return ResponseEntity.ok(facturas);
    }

    /**
     * Obtiene el detalle completo de una factura por ID.
     * 
     * GET /api/invoices/{id}
     * 
     * @param id ID de la factura
     * @return ResponseEntity con detalle completo y status 200 OK
     * @throws com.payoyo.working.exception.InvoiceNotFoundException si no existe (→ 404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDetailDTO> obtenerPorId(@PathVariable Long id) {
        InvoiceDetailDTO factura = service.obtenerPorId(id);
        return ResponseEntity.ok(factura);
    }

    /**
     * Actualiza una factura existente.
     * 
     * PUT /api/invoices/{id}
     * 
     * Solo permite actualizar facturas en estado PENDIENTE.
     * 
     * @param id ID de la factura a actualizar
     * @param dto Nuevos datos de la factura (validados con @Valid)
     * @return ResponseEntity con factura actualizada y status 200 OK
     * @throws com.payoyo.working.exception.InvoiceNotFoundException si no existe (→ 404)
     * @throws com.payoyo.working.exception.InvalidInvoiceStateException si estado != PENDIENTE (→ 409)
     */
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> actualizarFactura(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceCreateDTO dto) {
        
        InvoiceResponseDTO factura = service.actualizarFactura(id, dto);
        return ResponseEntity.ok(factura);
    }

    /**
     * Elimina una factura por ID.
     * 
     * DELETE /api/invoices/{id}
     * 
     * @param id ID de la factura a eliminar
     * @return ResponseEntity sin contenido y status 204 No Content
     * @throws com.payoyo.working.exception.InvoiceNotFoundException si no existe (→ 404)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFactura(@PathVariable Long id) {
        service.eliminarFactura(id);
        return ResponseEntity.noContent().build();
    }

    // ========== BÚSQUEDAS Y CONSULTAS ==========

    /**
     * Busca una factura por su número único.
     * 
     * GET /api/invoices/numero/{numero}
     * 
     * @param numero Número de factura (formato: FACT-YYYY-XXXX)
     * @return ResponseEntity con detalle completo y status 200 OK
     * @throws com.payoyo.working.exception.InvoiceNotFoundException si no existe (→ 404)
     */
    @GetMapping("/numero/{numero}")
    public ResponseEntity<InvoiceDetailDTO> buscarPorNumero(@PathVariable String numero) {
        InvoiceDetailDTO factura = service.buscarPorNumero(numero);
        return ResponseEntity.ok(factura);
    }

    /**
     * Obtiene todas las facturas vencidas.
     * 
     * GET /api/invoices/vencidas
     * 
     * Una factura está vencida si estado = PENDIENTE y fechaVencimiento < hoy.
     * 
     * @return ResponseEntity con lista de facturas vencidas y status 200 OK
     */
    @GetMapping("/vencidas")
    public ResponseEntity<List<InvoiceSummaryDTO>> obtenerVencidas() {
        List<InvoiceSummaryDTO> facturas = service.obtenerVencidas();
        return ResponseEntity.ok(facturas);
    }

    /**
     * Busca facturas por nombre de cliente (búsqueda parcial case-insensitive).
     * 
     * GET /api/invoices/cliente/{cliente}
     * 
     * Ejemplo: /api/invoices/cliente/Tech
     * Encontrará: "Tech Solutions", "FinTech Corp", "TECH INNOVATORS"
     * 
     * @param cliente Texto a buscar en el nombre del cliente
     * @return ResponseEntity con lista de facturas y status 200 OK
     */
    @GetMapping("/cliente/{cliente}")
    public ResponseEntity<List<InvoiceSummaryDTO>> buscarPorCliente(@PathVariable String cliente) {
        List<InvoiceSummaryDTO> facturas = service.buscarPorCliente(cliente);
        return ResponseEntity.ok(facturas);
    }

    /**
     * Filtra facturas por estado.
     * 
     * GET /api/invoices/estado/{estado}
     * 
     * Valores válidos: PENDIENTE, PAGADA, CANCELADA, VENCIDA
     * 
     * @param estado Estado de la factura
     * @return ResponseEntity con lista de facturas y status 200 OK
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<InvoiceSummaryDTO>> buscarPorEstado(@PathVariable EstadoFactura estado) {
        List<InvoiceSummaryDTO> facturas = service.buscarPorEstado(estado);
        return ResponseEntity.ok(facturas);
    }

    /**
     * Busca facturas emitidas en un rango de fechas (inclusive).
     * 
     * GET /api/invoices/fecha-rango?inicio=2024-01-01&fin=2024-01-31
     * 
     * @param inicio Fecha de inicio del rango (formato: yyyy-MM-dd)
     * @param fin Fecha de fin del rango (formato: yyyy-MM-dd)
     * @return ResponseEntity con lista de facturas y status 200 OK
     */
    @GetMapping("/fecha-rango")
    public ResponseEntity<List<InvoiceSummaryDTO>> buscarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        
        List<InvoiceSummaryDTO> facturas = service.buscarPorRangoFechas(inicio, fin);
        return ResponseEntity.ok(facturas);
    }

    /**
     * Filtra facturas por método de pago.
     * 
     * GET /api/invoices/metodo-pago/{metodo}
     * 
     * Valores válidos: EFECTIVO, TRANSFERENCIA, TARJETA, DOMICILIACION, CHEQUE, PAGARE
     * 
     * @param metodo Método de pago
     * @return ResponseEntity con lista de facturas y status 200 OK
     */
    @GetMapping("/metodo-pago/{metodo}")
    public ResponseEntity<List<InvoiceSummaryDTO>> buscarPorMetodoPago(@PathVariable MetodoPago metodo) {
        List<InvoiceSummaryDTO> facturas = service.buscarPorMetodoPago(metodo);
        return ResponseEntity.ok(facturas);
    }

    // ========== OPERACIONES DE NEGOCIO ==========

    /**
     * Marca una factura como pagada.
     * 
     * PATCH /api/invoices/{id}/pay
     * 
     * Validaciones:
     * - Estado actual debe ser PENDIENTE o VENCIDA
     * - No se puede pagar una factura CANCELADA
     * - No se puede pagar una factura ya PAGADA
     * 
     * @param id ID de la factura
     * @param dto Datos del pago (metodoPago, fechaPago, notas)
     * @return ResponseEntity con factura actualizada y status 200 OK
     * @throws com.payoyo.working.exception.InvoiceNotFoundException si no existe (→ 404)
     * @throws com.payoyo.working.exception.InvalidInvoiceStateException si estado no permite pago (→ 409)
     */
    @PatchMapping("/{id}/pay")
    public ResponseEntity<InvoiceResponseDTO> marcarComoPagada(
            @PathVariable Long id,
            @Valid @RequestBody InvoicePaymentDTO dto) {
        
        InvoiceResponseDTO factura = service.marcarComoPagada(id, dto);
        return ResponseEntity.ok(factura);
    }

    /**
     * Cancela una factura.
     * 
     * PATCH /api/invoices/{id}/cancel
     * 
     * Validaciones:
     * - Solo se pueden cancelar facturas PENDIENTES o VENCIDAS
     * - No se puede cancelar una factura PAGADA
     * 
     * @param id ID de la factura
     * @return ResponseEntity con factura cancelada y status 200 OK
     * @throws com.payoyo.working.exception.InvoiceNotFoundException si no existe (→ 404)
     * @throws com.payoyo.working.exception.InvalidInvoiceStateException si estado = PAGADA (→ 409)
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<InvoiceResponseDTO> cancelarFactura(@PathVariable Long id) {
        InvoiceResponseDTO factura = service.cancelarFactura(id);
        return ResponseEntity.ok(factura);
    }

    // ========== REPORTES Y ESTADÍSTICAS ==========

    /**
     * Genera un reporte financiero mensual.
     * 
     * GET /api/invoices/reporte/mensual/{anio}/{mes}
     * 
     * Ejemplo: GET /api/invoices/reporte/mensual/2024/1
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
     * @return ResponseEntity con reporte mensual y status 200 OK
     */
    @GetMapping("/reporte/mensual/{anio}/{mes}")
    public ResponseEntity<InvoiceReportDTO> reporteMensual(
            @PathVariable int anio,
            @PathVariable int mes) {
        
        InvoiceReportDTO reporte = service.reporteMensual(anio, mes);
        return ResponseEntity.ok(reporte);
    }

    /**
     * Obtiene totales generales de todas las facturas.
     * 
     * GET /api/invoices/totales
     * 
     * Retorna un mapa con:
     * - totalFacturas: cantidad total
     * - totalPendientes, totalPagadas, totalCanceladas, totalVencidas: cantidades por estado
     * - montoTotal: suma de todas las facturas
     * - montoCobrado: suma de facturas pagadas
     * - montoPendiente: suma de facturas pendientes + vencidas
     * 
     * @return ResponseEntity con mapa de estadísticas y status 200 OK
     */
    @GetMapping("/totales")
    public ResponseEntity<Map<String, Object>> obtenerTotales() {
        Map<String, Object> totales = service.obtenerTotales();
        return ResponseEntity.ok(totales);
    }
}