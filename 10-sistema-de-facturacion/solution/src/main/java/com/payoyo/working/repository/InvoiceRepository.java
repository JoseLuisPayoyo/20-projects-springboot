package com.payoyo.working.repository;

import com.payoyo.working.model.Invoice;
import com.payoyo.working.model.enums.EstadoFactura;
import com.payoyo.working.model.enums.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para gestión de facturas.
 * 
 * Proporciona operaciones CRUD básicas (heredadas de JpaRepository) y
 * queries personalizadas para búsquedas especializadas y reportes.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Busca una factura por su número único.
     * 
     * @param numeroFactura Número de factura (formato: FACT-YYYY-XXXX)
     * @return Optional con la factura si existe
     */
    Optional<Invoice> findByNumeroFactura(String numeroFactura);

    /**
     * Encuentra la última factura del año para generación de número consecutivo.
     * Utilizado por el Service para generar automáticamente el siguiente número de factura.
     * 
     * Ejemplo: Si existen FACT-2024-0001, FACT-2024-0002, FACT-2024-0003
     *          retorna FACT-2024-0003 (la última del año)
     * 
     * @param prefix Prefijo de búsqueda (ej: "FACT-2024")
     * @return Optional con la última factura del año
     */
    Optional<Invoice> findTopByNumeroFacturaStartingWithOrderByNumeroFacturaDesc(String prefix);

    /**
     * Encuentra todas las facturas vencidas.
     * Una factura está vencida si:
     * - Su estado es PENDIENTE
     * - Su fecha de vencimiento es anterior a la fecha actual
     * 
     * @param fecha Fecha de referencia (normalmente LocalDate.now())
     * @return Lista de facturas vencidas
     */
    @Query("SELECT i FROM Invoice i WHERE i.estado = 'PENDIENTE' AND i.fechaVencimiento < :fecha")
    List<Invoice> findVencidas(@Param("fecha") LocalDate fecha);

    /**
     * Busca facturas por nombre de cliente (búsqueda parcial case-insensitive).
     * 
     * Ejemplo: "Acme" encontrará "Acme Corporation", "ACME TECH", "Acme España S.L."
     * 
     * @param cliente Texto a buscar en el nombre del cliente
     * @return Lista de facturas que coinciden
     */
    List<Invoice> findByClienteContainingIgnoreCase(String cliente);

    /**
     * Filtra facturas por estado.
     * 
     * @param estado Estado de la factura (PENDIENTE, PAGADA, CANCELADA, VENCIDA)
     * @return Lista de facturas con el estado especificado
     */
    List<Invoice> findByEstado(EstadoFactura estado);

    /**
     * Filtra facturas por método de pago.
     * 
     * @param metodoPago Método de pago (EFECTIVO, TRANSFERENCIA, TARJETA, etc.)
     * @return Lista de facturas con el método de pago especificado
     */
    List<Invoice> findByMetodoPago(MetodoPago metodoPago);

    /**
     * Busca facturas emitidas en un rango de fechas (inclusive).
     * 
     * @param inicio Fecha de inicio del rango
     * @param fin Fecha de fin del rango
     * @return Lista de facturas emitidas entre las fechas especificadas
     */
    List<Invoice> findByFechaEmisionBetween(LocalDate inicio, LocalDate fin);

    /**
     * Obtiene estadísticas mensuales para reportes.
     * Retorna un array de objetos con:
     * - [0]: Cantidad total de facturas del mes (Long)
     * - [1]: Suma total facturado del mes (BigDecimal)
     * 
     * Utilizado por el Service para construir InvoiceReportDTO.
     * 
     * @param anio Año del reporte
     * @param mes Mes del reporte (1-12)
     * @return Array de objetos [cantidad, suma] o array vacío si no hay datos
     */
    @Query("SELECT COUNT(i), SUM(i.total) FROM Invoice i " +
           "WHERE YEAR(i.fechaEmision) = :anio AND MONTH(i.fechaEmision) = :mes")
    Object[] getEstadisticasMensuales(@Param("anio") int anio, @Param("mes") int mes);

    /**
     * Cuenta facturas por estado en un mes específico.
     * Utilizado para reportes mensuales detallados.
     * 
     * @param estado Estado a contar
     * @param anio Año del reporte
     * @param mes Mes del reporte (1-12)
     * @return Cantidad de facturas con ese estado en el mes
     */
    @Query("SELECT COUNT(i) FROM Invoice i " +
           "WHERE i.estado = :estado " +
           "AND YEAR(i.fechaEmision) = :anio " +
           "AND MONTH(i.fechaEmision) = :mes")
    Long countByEstadoAndMonth(@Param("estado") EstadoFactura estado, 
                                @Param("anio") int anio, 
                                @Param("mes") int mes);

    /**
     * Suma el total de facturas por estado en un mes específico.
     * Utilizado para calcular totales cobrados vs pendientes en reportes.
     * 
     * @param estado Estado a sumar
     * @param anio Año del reporte
     * @param mes Mes del reporte (1-12)
     * @return Suma total de facturas con ese estado, o 0 si no hay ninguna
     */
    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i " +
           "WHERE i.estado = :estado " +
           "AND YEAR(i.fechaEmision) = :anio " +
           "AND MONTH(i.fechaEmision) = :mes")
    java.math.BigDecimal sumTotalByEstadoAndMonth(@Param("estado") EstadoFactura estado, 
                                                    @Param("anio") int anio, 
                                                    @Param("mes") int mes);

    /**
     * Cuenta el total de facturas en el sistema.
     * Query explícita para mejor rendimiento en reportes generales.
     * 
     * @return Total de facturas
     */
    @Query("SELECT COUNT(i) FROM Invoice i")
    Long countTotal();

    /**
     * Cuenta facturas por estado (para totales generales).
     * 
     * @param estado Estado a contar
     * @return Cantidad de facturas con ese estado
     */
    Long countByEstado(EstadoFactura estado);

    /**
     * Suma el total general de todas las facturas.
     * 
     * @return Suma total de todas las facturas
     */
    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i")
    java.math.BigDecimal sumTotalGeneral();

    /**
     * Suma el total de facturas por estado específico.
     * 
     * @param estado Estado a sumar
     * @return Suma total de facturas con ese estado
     */
    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.estado = :estado")
    java.math.BigDecimal sumTotalByEstado(@Param("estado") EstadoFactura estado);
}