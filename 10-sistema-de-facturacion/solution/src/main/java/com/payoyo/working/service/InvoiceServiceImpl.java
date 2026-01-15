package com.payoyo.working.service;

import com.payoyo.working.dtos.*;
import com.payoyo.working.exceptions.InvoiceNotFoundException;
import com.payoyo.working.exceptions.InvalidInvoiceStateException;
import com.payoyo.working.model.Invoice;
import com.payoyo.working.model.enums.EstadoFactura;
import com.payoyo.working.model.enums.MetodoPago;
import com.payoyo.working.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de facturas.
 * 
 * Contiene toda la lógica de negocio:
 * - Cálculos automáticos (subtotal, IVA, total)
 * - Generación de números de factura
 * - Conversiones entre DTOs y Entity
 * - Serialización/deserialización manual de items (sin Jackson)
 * - Validaciones de negocio
 * - Operaciones especializadas y reportes
 * 
 * Anotaciones:
 * - @Service: Marca como componente de servicio Spring
 * - @RequiredArgsConstructor (Lombok): Genera constructor con dependencias final
 * - @Transactional: Gestiona transacciones automáticamente en métodos que modifican datos
 */
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    // Inyección de dependencias por constructor (immutable)
    private final InvoiceRepository repository;

    // ========== OPERACIONES CRUD ==========

    /**
     * Crea una nueva factura con todos los cálculos automáticos.
     */
    @Override
    @Transactional
    public InvoiceResponseDTO crearFactura(InvoiceCreateDTO dto) {
        // 1. Validar fechas coherentes
        validarFechas(dto.getFechaEmision(), dto.getFechaVencimiento());
        
        // 2. Calcular subtotal de los items
        BigDecimal subtotal = calcularSubtotal(dto.getItems());
        
        // 3. Validar descuento
        validarDescuento(dto.getDescuento(), subtotal);
        
        // 4. Calcular IVA según tipo
        BigDecimal iva = calcularIva(subtotal, dto.getTipoIva());
        
        // 5. Calcular total
        BigDecimal total = calcularTotal(subtotal, iva, dto.getDescuento());
        
        // 6. Generar número de factura
        String numeroFactura = generarNumeroFactura();
        
        // 7. Serializar items a JSON
        String itemsJson = convertirItemsAJson(dto.getItems());
        
        // 8. Crear entidad Invoice
        Invoice invoice = new Invoice();
        invoice.setNumeroFactura(numeroFactura);
        invoice.setCliente(dto.getCliente());
        invoice.setNifCif(dto.getNifCif());
        invoice.setDireccion(dto.getDireccion());
        invoice.setFechaEmision(dto.getFechaEmision());
        invoice.setFechaVencimiento(dto.getFechaVencimiento());
        invoice.setConcepto(dto.getConcepto());
        invoice.setTipoIva(dto.getTipoIva());
        invoice.setSubtotal(subtotal);
        invoice.setIva(iva);
        invoice.setDescuento(dto.getDescuento());
        invoice.setTotal(total);
        invoice.setEstado(EstadoFactura.PENDIENTE); // Estado inicial
        invoice.setMetodoPago(dto.getMetodoPago());
        invoice.setNotas(dto.getNotas());
        invoice.setItems(itemsJson);
        
        // 9. Guardar en base de datos
        Invoice invoiceGuardada = repository.save(invoice);
        
        // 10. Convertir a DTO de respuesta
        return convertirAResponseDTO(invoiceGuardada);
    }

    /**
     * Obtiene todas las facturas en formato resumido.
     */
    @Override
    @Transactional(readOnly = true)
    public List<InvoiceSummaryDTO> obtenerTodas() {
        return repository.findAll().stream()
                .map(this::convertirASummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el detalle completo de una factura.
     */
    @Override
    @Transactional(readOnly = true)
    public InvoiceDetailDTO obtenerPorId(Long id) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        
        return convertirADetailDTO(invoice);
    }

    /**
     * Actualiza una factura existente.
     * Solo permite actualizar facturas en estado PENDIENTE.
     */
    @Override
    @Transactional
    public InvoiceResponseDTO actualizarFactura(Long id, InvoiceCreateDTO dto) {
        // 1. Buscar factura existente
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        
        // 2. Validar que se puede actualizar (solo PENDIENTE)
        if (invoice.getEstado() != EstadoFactura.PENDIENTE) {
            throw new InvalidInvoiceStateException(
                "No se puede actualizar una factura en estado " + invoice.getEstado() + 
                ". Solo se pueden actualizar facturas PENDIENTES."
            );
        }
        
        // 3. Validar fechas
        validarFechas(dto.getFechaEmision(), dto.getFechaVencimiento());
        
        // 4. Recalcular todos los valores
        BigDecimal subtotal = calcularSubtotal(dto.getItems());
        validarDescuento(dto.getDescuento(), subtotal);
        BigDecimal iva = calcularIva(subtotal, dto.getTipoIva());
        BigDecimal total = calcularTotal(subtotal, iva, dto.getDescuento());
        String itemsJson = convertirItemsAJson(dto.getItems());
        
        // 5. Actualizar campos (mantener numeroFactura y estado)
        invoice.setCliente(dto.getCliente());
        invoice.setNifCif(dto.getNifCif());
        invoice.setDireccion(dto.getDireccion());
        invoice.setFechaEmision(dto.getFechaEmision());
        invoice.setFechaVencimiento(dto.getFechaVencimiento());
        invoice.setConcepto(dto.getConcepto());
        invoice.setTipoIva(dto.getTipoIva());
        invoice.setSubtotal(subtotal);
        invoice.setIva(iva);
        invoice.setDescuento(dto.getDescuento());
        invoice.setTotal(total);
        invoice.setMetodoPago(dto.getMetodoPago());
        invoice.setNotas(dto.getNotas());
        invoice.setItems(itemsJson);
        
        // 6. Guardar cambios
        Invoice invoiceActualizada = repository.save(invoice);
        
        return convertirAResponseDTO(invoiceActualizada);
    }

    /**
     * Elimina una factura.
     */
    @Override
    @Transactional
    public void eliminarFactura(Long id) {
        if (!repository.existsById(id)) {
            throw new InvoiceNotFoundException(id);
        }
        repository.deleteById(id);
    }

    // ========== BÚSQUEDAS Y CONSULTAS ==========

    @Override
    @Transactional(readOnly = true)
    public InvoiceDetailDTO buscarPorNumero(String numeroFactura) {
        Invoice invoice = repository.findByNumeroFactura(numeroFactura)
                .orElseThrow(() -> new InvoiceNotFoundException(numeroFactura));
        
        return convertirADetailDTO(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceSummaryDTO> obtenerVencidas() {
        return repository.findVencidas(LocalDate.now()).stream()
                .map(this::convertirASummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceSummaryDTO> buscarPorCliente(String cliente) {
        return repository.findByClienteContainingIgnoreCase(cliente).stream()
                .map(this::convertirASummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceSummaryDTO> buscarPorEstado(EstadoFactura estado) {
        return repository.findByEstado(estado).stream()
                .map(this::convertirASummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceSummaryDTO> buscarPorMetodoPago(MetodoPago metodoPago) {
        return repository.findByMetodoPago(metodoPago).stream()
                .map(this::convertirASummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceSummaryDTO> buscarPorRangoFechas(LocalDate inicio, LocalDate fin) {
        return repository.findByFechaEmisionBetween(inicio, fin).stream()
                .map(this::convertirASummaryDTO)
                .collect(Collectors.toList());
    }

    // ========== OPERACIONES DE NEGOCIO ==========

    /**
     * Marca una factura como pagada.
     */
    @Override
    @Transactional
    public InvoiceResponseDTO marcarComoPagada(Long id, InvoicePaymentDTO dto) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        
        // Validar estado actual
        if (invoice.getEstado() == EstadoFactura.CANCELADA) {
            throw new InvalidInvoiceStateException(
                "No se puede marcar como pagada una factura cancelada"
            );
        }
        
        if (invoice.getEstado() == EstadoFactura.PAGADA) {
            throw new InvalidInvoiceStateException(
                "La factura ya está marcada como pagada"
            );
        }
        
        // Actualizar estado a PAGADA
        invoice.setEstado(EstadoFactura.PAGADA);
        
        // Actualizar método de pago si se especifica uno diferente
        if (dto.getMetodoPago() != null) {
            invoice.setMetodoPago(dto.getMetodoPago());
        }
        
        // Añadir notas del pago a las notas existentes
        if (dto.getNotas() != null && !dto.getNotas().isBlank()) {
            String notasActuales = invoice.getNotas() != null ? invoice.getNotas() : "";
            String notasPago = "\n\n--- Pago registrado el " + dto.getFechaPago() + " ---\n" + dto.getNotas();
            invoice.setNotas(notasActuales + notasPago);
        }
        
        Invoice invoiceActualizada = repository.save(invoice);
        return convertirAResponseDTO(invoiceActualizada);
    }

    /**
     * Cancela una factura.
     */
    @Override
    @Transactional
    public InvoiceResponseDTO cancelarFactura(Long id) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        
        // Validar que no esté pagada
        if (invoice.getEstado() == EstadoFactura.PAGADA) {
            throw new InvalidInvoiceStateException(
                "No se puede cancelar una factura que ya ha sido pagada"
            );
        }
        
        if (invoice.getEstado() == EstadoFactura.CANCELADA) {
            throw new InvalidInvoiceStateException(
                "La factura ya está cancelada"
            );
        }
        
        // Cambiar estado a CANCELADA
        invoice.setEstado(EstadoFactura.CANCELADA);
        
        Invoice invoiceActualizada = repository.save(invoice);
        return convertirAResponseDTO(invoiceActualizada);
    }

    // ========== REPORTES Y ESTADÍSTICAS ==========

    /**
     * Genera reporte financiero mensual.
     */
    @Override
    @Transactional(readOnly = true)
    public InvoiceReportDTO reporteMensual(int anio, int mes) {
        // Obtener estadísticas básicas del mes
        Object[] estadisticas = repository.getEstadisticasMensuales(anio, mes);
        
        Long cantidadTotal = estadisticas[0] != null ? (Long) estadisticas[0] : 0L;
        BigDecimal totalFacturado = estadisticas[1] != null ? (BigDecimal) estadisticas[1] : BigDecimal.ZERO;
        
        // Obtener cantidades por estado
        Long cantidadPagadas = repository.countByEstadoAndMonth(EstadoFactura.PAGADA, anio, mes);
        Long cantidadPendientes = repository.countByEstadoAndMonth(EstadoFactura.PENDIENTE, anio, mes);
        Long cantidadVencidas = repository.countByEstadoAndMonth(EstadoFactura.VENCIDA, anio, mes);
        
        // Obtener totales por estado
        BigDecimal totalCobrado = repository.sumTotalByEstadoAndMonth(EstadoFactura.PAGADA, anio, mes);
        BigDecimal totalPendienteMes = repository.sumTotalByEstadoAndMonth(EstadoFactura.PENDIENTE, anio, mes);
        BigDecimal totalVencidoMes = repository.sumTotalByEstadoAndMonth(EstadoFactura.VENCIDA, anio, mes);
        BigDecimal totalPendiente = totalPendienteMes.add(totalVencidoMes);
        
        // Calcular promedio de ticket
        BigDecimal promedioTicket = cantidadTotal > 0 
            ? totalFacturado.divide(BigDecimal.valueOf(cantidadTotal), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        // Construir DTO de reporte
        InvoiceReportDTO reporte = new InvoiceReportDTO();
        reporte.setPeriodo(String.format("%d-%02d", anio, mes));
        reporte.setTotalFacturado(totalFacturado);
        reporte.setTotalCobrado(totalCobrado);
        reporte.setTotalPendiente(totalPendiente);
        reporte.setCantidadFacturas(cantidadTotal.intValue());
        reporte.setCantidadPagadas(cantidadPagadas.intValue());
        reporte.setCantidadPendientes(cantidadPendientes.intValue());
        reporte.setCantidadVencidas(cantidadVencidas.intValue());
        reporte.setPromedioTicket(promedioTicket);
        
        return reporte;
    }

    /**
     * Obtiene totales generales de todas las facturas.
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerTotales() {
        Map<String, Object> totales = new HashMap<>();
        
        // Cantidades por estado
        totales.put("totalFacturas", repository.countTotal());
        totales.put("totalPendientes", repository.countByEstado(EstadoFactura.PENDIENTE));
        totales.put("totalPagadas", repository.countByEstado(EstadoFactura.PAGADA));
        totales.put("totalCanceladas", repository.countByEstado(EstadoFactura.CANCELADA));
        totales.put("totalVencidas", repository.countByEstado(EstadoFactura.VENCIDA));
        
        // Montos por estado
        totales.put("montoTotal", repository.sumTotalGeneral());
        totales.put("montoCobrado", repository.sumTotalByEstado(EstadoFactura.PAGADA));
        
        BigDecimal montoPendiente = repository.sumTotalByEstado(EstadoFactura.PENDIENTE);
        BigDecimal montoVencido = repository.sumTotalByEstado(EstadoFactura.VENCIDA);
        totales.put("montoPendiente", montoPendiente.add(montoVencido));
        
        return totales;
    }

    // ========== MÉTODOS HELPER PRIVADOS ==========

    /**
     * Genera número de factura único con formato FACT-YYYY-XXXX.
     * El número se incrementa automáticamente por año.
     */
    private String generarNumeroFactura() {
        int anioActual = LocalDate.now().getYear();
        String prefijo = "FACT-" + anioActual;
        
        // Buscar última factura del año
        Optional<Invoice> ultimaFactura = repository
                .findTopByNumeroFacturaStartingWithOrderByNumeroFacturaDesc(prefijo);
        
        int siguienteNumero = 1;
        if (ultimaFactura.isPresent()) {
            // Extraer número de FACT-2024-0015 → 15
            String ultimoNumero = ultimaFactura.get().getNumeroFactura();
            String[] partes = ultimoNumero.split("-");
            siguienteNumero = Integer.parseInt(partes[2]) + 1;
        }
        
        // Formato: FACT-2024-0001
        return String.format("FACT-%d-%04d", anioActual, siguienteNumero);
    }

    /**
     * Calcula el subtotal sumando todos los items.
     * Formula: suma de (cantidad × precioUnitario) de cada item
     */
    private BigDecimal calcularSubtotal(List<ItemDTO> items) {
        return items.stream()
                .map(item -> item.getPrecioUnitario()
                        .multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el IVA según el tipo especificado.
     * 
     * @param subtotal Base imponible
     * @param tipoIva Tipo de IVA (GENERAL 21%, REDUCIDO 10%, SUPERREDUCIDO 4%, EXENTO 0%)
     * @return Importe del IVA calculado
     */
    private BigDecimal calcularIva(BigDecimal subtotal, com.payoyo.working.model.enums.TipoIva tipoIva) {
        double porcentajeDecimal = tipoIva.getPorcentajeDecimal();
        return subtotal.multiply(BigDecimal.valueOf(porcentajeDecimal))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el total a pagar.
     * Formula: subtotal + IVA - descuento
     */
    private BigDecimal calcularTotal(BigDecimal subtotal, BigDecimal iva, BigDecimal descuento) {
        return subtotal.add(iva).subtract(descuento)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Valida que las fechas sean coherentes.
     * 
     * @throws IllegalArgumentException si fechaVencimiento < fechaEmision
     */
    private void validarFechas(LocalDate fechaEmision, LocalDate fechaVencimiento) {
        if (fechaVencimiento.isBefore(fechaEmision)) {
            throw new IllegalArgumentException(
                "La fecha de vencimiento no puede ser anterior a la fecha de emisión"
            );
        }
    }

    /**
     * Valida que el descuento no supere el subtotal.
     * 
     * @throws IllegalArgumentException si descuento > subtotal
     */
    private void validarDescuento(BigDecimal descuento, BigDecimal subtotal) {
        if (descuento.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException(
                "El descuento (" + descuento + "€) no puede ser mayor que el subtotal (" + subtotal + "€)"
            );
        }
    }

    /**
     * Convierte una lista de ItemDTO a String JSON manualmente.
     * 
     * Formato JSON generado:
     * [
     *   {"descripcion":"...", "cantidad":10, "precioUnitario":50.00, "importe":500.00},
     *   {"descripcion":"...", "cantidad":20, "precioUnitario":30.00, "importe":600.00}
     * ]
     * 
     * @param items Lista de items a convertir
     * @return String JSON con los items
     */
    private String convertirItemsAJson(List<ItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder("[");
        
        for (int i = 0; i < items.size(); i++) {
            ItemDTO item = items.get(i);
            
            // Calcular importe si no está presente
            BigDecimal importe = item.getImporte();
            if (importe == null) {
                importe = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
            }
            
            json.append("{");
            json.append("\"descripcion\":\"").append(escaparJson(item.getDescripcion())).append("\",");
            json.append("\"cantidad\":").append(item.getCantidad()).append(",");
            json.append("\"precioUnitario\":").append(item.getPrecioUnitario()).append(",");
            json.append("\"importe\":").append(importe);
            json.append("}");
            
            // Añadir coma si no es el último elemento
            if (i < items.size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]");
        return json.toString();
    }

    /**
     * Convierte un String JSON a lista de ItemDTO manualmente.
     * 
     * Parsea JSON en formato:
     * [
     *   {"descripcion":"...", "cantidad":10, "precioUnitario":50.00, "importe":500.00}
     * ]
     * 
     * @param json String JSON con los items
     * @return Lista de ItemDTO parseados
     * @throws RuntimeException si hay error en el parseo
     */
    private List<ItemDTO> convertirJsonAItems(String json) {
        List<ItemDTO> items = new ArrayList<>();
        
        if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
            return items;
        }
        
        try {
            // Eliminar corchetes externos y espacios
            String contenido = json.trim().substring(1, json.trim().length() - 1).trim();
            
            if (contenido.isEmpty()) {
                return items;
            }
            
            // Separar objetos JSON (dividir por },{ )
            List<String> objetosJson = separarObjetosJson(contenido);
            
            // Parsear cada objeto
            for (String objetoJson : objetosJson) {
                ItemDTO item = parsearItemJson(objetoJson);
                items.add(item);
            }
            
            return items;
            
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear items desde JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Escapa caracteres especiales para JSON (comillas dobles, barras, etc.).
     * 
     * @param texto Texto a escapar
     * @return Texto con caracteres especiales escapados
     */
    private String escaparJson(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    /**
     * Separa objetos JSON de un array.
     * Divide por },{ teniendo cuidado con las comillas.
     * 
     * @param contenido Contenido del array sin corchetes externos
     * @return Lista de strings, cada uno representa un objeto JSON
     */
    private List<String> separarObjetosJson(String contenido) {
        List<String> objetos = new ArrayList<>();
        StringBuilder objetoActual = new StringBuilder();
        boolean dentroComillas = false;
        int nivelLlaves = 0;
        
        for (int i = 0; i < contenido.length(); i++) {
            char c = contenido.charAt(i);
            
            // Detectar si estamos dentro de comillas
            if (c == '"' && (i == 0 || contenido.charAt(i - 1) != '\\')) {
                dentroComillas = !dentroComillas;
            }
            
            // Contar nivel de llaves (solo si no estamos en comillas)
            if (!dentroComillas) {
                if (c == '{') {
                    nivelLlaves++;
                } else if (c == '}') {
                    nivelLlaves--;
                }
            }
            
            objetoActual.append(c);
            
            // Si llegamos al final de un objeto (nivel de llaves = 0 después de cerrar)
            if (!dentroComillas && nivelLlaves == 0 && c == '}') {
                objetos.add(objetoActual.toString().trim());
                objetoActual = new StringBuilder();
                
                // Saltar la coma si existe
                if (i + 1 < contenido.length() && contenido.charAt(i + 1) == ',') {
                    i++;
                }
            }
        }
        
        return objetos;
    }

    /**
     * Parsea un objeto JSON a ItemDTO.
     * 
     * Formato esperado: {"descripcion":"...", "cantidad":10, "precioUnitario":50.00, "importe":500.00}
     * 
     * @param objetoJson String con el objeto JSON (con llaves)
     * @return ItemDTO parseado
     */
    private ItemDTO parsearItemJson(String objetoJson) {
        ItemDTO item = new ItemDTO();
        
        // Eliminar llaves externas
        String contenido = objetoJson.trim();
        if (contenido.startsWith("{")) {
            contenido = contenido.substring(1);
        }
        if (contenido.endsWith("}")) {
            contenido = contenido.substring(0, contenido.length() - 1);
        }
        
        // Separar pares clave:valor
        Map<String, String> campos = extraerCamposJson(contenido);
        
        // Mapear a ItemDTO
        if (campos.containsKey("descripcion")) {
            item.setDescripcion(desescaparJson(campos.get("descripcion")));
        }
        
        if (campos.containsKey("cantidad")) {
            item.setCantidad(Integer.parseInt(campos.get("cantidad")));
        }
        
        if (campos.containsKey("precioUnitario")) {
            item.setPrecioUnitario(new BigDecimal(campos.get("precioUnitario")));
        }
        
        if (campos.containsKey("importe")) {
            item.setImporte(new BigDecimal(campos.get("importe")));
        }
        
        return item;
    }

    /**
     * Extrae pares clave:valor de un contenido JSON.
     * 
     * @param contenido Contenido del objeto sin llaves
     * @return Mapa con los campos (clave -> valor)
     */
    private Map<String, String> extraerCamposJson(String contenido) {
        Map<String, String> campos = new HashMap<>();
        StringBuilder claveActual = new StringBuilder();
        StringBuilder valorActual = new StringBuilder();
        boolean leyendoClave = true;
        boolean dentroComillas = false;
        
        for (int i = 0; i < contenido.length(); i++) {
            char c = contenido.charAt(i);
            
            // Detectar comillas (inicio/fin de string)
            if (c == '"' && (i == 0 || contenido.charAt(i - 1) != '\\')) {
                dentroComillas = !dentroComillas;
                continue; // No incluir las comillas en clave/valor
            }
            
            // Detectar dos puntos (separador clave:valor)
            if (c == ':' && !dentroComillas) {
                leyendoClave = false;
                continue;
            }
            
            // Detectar coma (separador entre pares)
            if (c == ',' && !dentroComillas) {
                // Guardar el par clave:valor
                String clave = claveActual.toString().trim();
                String valor = valorActual.toString().trim();
                if (!clave.isEmpty()) {
                    campos.put(clave, valor);
                }
                
                // Resetear para el siguiente par
                claveActual = new StringBuilder();
                valorActual = new StringBuilder();
                leyendoClave = true;
                continue;
            }
            
            // Añadir carácter a clave o valor
            if (leyendoClave) {
                claveActual.append(c);
            } else {
                valorActual.append(c);
            }
        }
        
        // Guardar el último par
        String clave = claveActual.toString().trim();
        String valor = valorActual.toString().trim();
        if (!clave.isEmpty()) {
            campos.put(clave, valor);
        }
        
        return campos;
    }

    /**
     * Desescapa caracteres especiales de JSON.
     * 
     * @param texto Texto con caracteres escapados
     * @return Texto con caracteres reales
     */
    private String desescaparJson(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t");
    }

    /**
     * Calcula los días para el vencimiento.
     * 
     * @return Positivo: días que faltan, Negativo: días de retraso, 0: vence hoy
     */
    private Integer calcularDiasVencimiento(LocalDate fechaVencimiento) {
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    /**
     * Calcula el estado descriptivo del vencimiento.
     * 
     * @param diasVencimiento Días para vencimiento
     * @return "Al corriente", "Por vencer" o "Vencida"
     */
    private String calcularEstadoVencimiento(Integer diasVencimiento) {
        if (diasVencimiento < 0) {
            return "Vencida";
        } else if (diasVencimiento <= 7) {
            return "Por vencer";
        } else {
            return "Al corriente";
        }
    }

    // ========== CONVERSIONES DTO ↔ ENTITY ==========

    /**
     * Convierte Invoice a InvoiceResponseDTO.
     */
    private InvoiceResponseDTO convertirAResponseDTO(Invoice invoice) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setId(invoice.getId());
        dto.setNumeroFactura(invoice.getNumeroFactura());
        dto.setCliente(invoice.getCliente());
        dto.setNifCif(invoice.getNifCif());
        dto.setFechaEmision(invoice.getFechaEmision());
        dto.setFechaVencimiento(invoice.getFechaVencimiento());
        dto.setTipoIva(invoice.getTipoIva());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setIva(invoice.getIva());
        dto.setDescuento(invoice.getDescuento());
        dto.setTotal(invoice.getTotal());
        dto.setEstado(invoice.getEstado());
        dto.setMetodoPago(invoice.getMetodoPago());
        return dto;
    }

    /**
     * Convierte Invoice a InvoiceDetailDTO (con items parseados).
     */
    private InvoiceDetailDTO convertirADetailDTO(Invoice invoice) {
        InvoiceDetailDTO dto = new InvoiceDetailDTO();
        dto.setId(invoice.getId());
        dto.setNumeroFactura(invoice.getNumeroFactura());
        dto.setCliente(invoice.getCliente());
        dto.setNifCif(invoice.getNifCif());
        dto.setDireccion(invoice.getDireccion());
        dto.setFechaEmision(invoice.getFechaEmision());
        dto.setFechaVencimiento(invoice.getFechaVencimiento());
        dto.setConcepto(invoice.getConcepto());
        dto.setTipoIva(invoice.getTipoIva());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setIva(invoice.getIva());
        dto.setDescuento(invoice.getDescuento());
        dto.setTotal(invoice.getTotal());
        dto.setEstado(invoice.getEstado());
        dto.setMetodoPago(invoice.getMetodoPago());
        dto.setNotas(invoice.getNotas());
        
        // Parsear items desde JSON
        dto.setItems(convertirJsonAItems(invoice.getItems()));
        
        // Calcular campos adicionales
        Integer diasVencimiento = calcularDiasVencimiento(invoice.getFechaVencimiento());
        dto.setDiasVencimiento(diasVencimiento);
        dto.setEstadoVencimiento(calcularEstadoVencimiento(diasVencimiento));
        
        return dto;
    }

    /**
     * Convierte Invoice a InvoiceSummaryDTO (resumido).
     */
    private InvoiceSummaryDTO convertirASummaryDTO(Invoice invoice) {
        InvoiceSummaryDTO dto = new InvoiceSummaryDTO();
        dto.setId(invoice.getId());
        dto.setNumeroFactura(invoice.getNumeroFactura());
        dto.setCliente(invoice.getCliente());
        dto.setFechaEmision(invoice.getFechaEmision());
        dto.setTotal(invoice.getTotal());
        dto.setEstado(invoice.getEstado());
        dto.setDiasParaVencimiento(calcularDiasVencimiento(invoice.getFechaVencimiento()));
        return dto;
    }
}