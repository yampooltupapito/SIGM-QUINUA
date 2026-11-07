package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PagoMercado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PagoMercadoRepository extends JpaRepository<PagoMercado, UUID> {

    @Query(value = "SELECT nextval('mercados_pagos_seq')", nativeQuery = true)
    Long obtenerSiguienteCorrelativo();

    Optional<PagoMercado> findByCodigoPago(String codigoPago);

    Optional<PagoMercado> findByCodigoSeguro(String codigoSeguro);

    @Query("SELECT p FROM PagoMercado p " +
           "JOIN FETCH p.comerciante c " +
           "LEFT JOIN FETCH p.sesionCaja s " +
           "LEFT JOIN FETCH p.aplicaciones a " +
           "LEFT JOIN FETCH a.obligacion o " +
           "WHERE p.id = :id")
    Optional<PagoMercado> findByIdConRelaciones(@Param("id") UUID id);

    List<PagoMercado> findByComercianteIdOrderByFechaPagoDesc(UUID comercianteId);

    List<PagoMercado> findByComercianteId(UUID comercianteId);

    Page<PagoMercado> findByComercianteId(UUID comercianteId, Pageable pageable);

    long countByComercianteId(UUID comercianteId);

    List<PagoMercado> findBySesionCajaIdOrderByFechaPagoAsc(UUID sesionCajaId);

    @Query(value = "SELECT p.* FROM mercados_pagos p " +
                   "JOIN mercados_comerciantes com ON com.id = p.comerciante_id " +
                   "LEFT JOIN personas per ON per.id = com.persona_id " +
                   "LEFT JOIN personas_juridicas pj ON pj.id = com.persona_juridica_id " +
                   "WHERE (CAST(:comercianteId AS uuid) IS NULL OR p.comerciante_id = CAST(:comercianteId AS uuid)) " +
                   "AND (CAST(:cajaSesionId AS uuid) IS NULL OR p.caja_sesion_id = CAST(:cajaSesionId AS uuid)) " +
                   "AND (:medioPago IS NULL OR p.medio_pago = :medioPago) " +
                   "AND (:estado IS NULL OR p.estado = :estado) " +
                   "AND (CAST(:fechaDesde AS timestamptz) IS NULL OR p.fecha_pago >= CAST(:fechaDesde AS timestamptz)) " +
                   "AND (CAST(:fechaHasta AS timestamptz) IS NULL OR p.fecha_pago <= CAST(:fechaHasta AS timestamptz)) " +
                   "AND (:q IS NULL OR LOWER(p.codigo_pago) LIKE LOWER(CONCAT('%', :q, '%')) " +
                   "     OR LOWER(COALESCE(per.nombres, '')) LIKE LOWER(CONCAT('%', :q, '%')) " +
                   "     OR LOWER(COALESCE(per.apellido_paterno, '')) LIKE LOWER(CONCAT('%', :q, '%')) " +
                   "     OR COALESCE(per.numero_documento, '') LIKE CONCAT('%', :q, '%') " +
                   "     OR LOWER(COALESCE(pj.razon_social, '')) LIKE LOWER(CONCAT('%', :q, '%')) " +
                   "     OR COALESCE(pj.ruc, '') LIKE CONCAT('%', :q, '%') " +
                   "     OR LOWER(COALESCE(p.referencia_operacion, '')) LIKE LOWER(CONCAT('%', :q, '%'))) " +
                   "ORDER BY p.fecha_pago DESC",
           countQuery = "SELECT COUNT(*) FROM mercados_pagos p " +
                        "JOIN mercados_comerciantes com ON com.id = p.comerciante_id " +
                        "LEFT JOIN personas per ON per.id = com.persona_id " +
                        "LEFT JOIN personas_juridicas pj ON pj.id = com.persona_juridica_id " +
                        "WHERE (CAST(:comercianteId AS uuid) IS NULL OR p.comerciante_id = CAST(:comercianteId AS uuid)) " +
                        "AND (CAST(:cajaSesionId AS uuid) IS NULL OR p.caja_sesion_id = CAST(:cajaSesionId AS uuid)) " +
                        "AND (:medioPago IS NULL OR p.medio_pago = :medioPago) " +
                        "AND (:estado IS NULL OR p.estado = :estado) " +
                        "AND (CAST(:fechaDesde AS timestamptz) IS NULL OR p.fecha_pago >= CAST(:fechaDesde AS timestamptz)) " +
                        "AND (CAST(:fechaHasta AS timestamptz) IS NULL OR p.fecha_pago <= CAST(:fechaHasta AS timestamptz)) " +
                        "AND (:q IS NULL OR LOWER(p.codigo_pago) LIKE LOWER(CONCAT('%', :q, '%')) " +
                        "     OR LOWER(COALESCE(per.nombres, '')) LIKE LOWER(CONCAT('%', :q, '%')) " +
                        "     OR LOWER(COALESCE(per.apellido_paterno, '')) LIKE LOWER(CONCAT('%', :q, '%')) " +
                        "     OR COALESCE(per.numero_documento, '') LIKE CONCAT('%', :q, '%') " +
                        "     OR LOWER(COALESCE(pj.razon_social, '')) LIKE LOWER(CONCAT('%', :q, '%')) " +
                        "     OR COALESCE(pj.ruc, '') LIKE CONCAT('%', :q, '%') " +
                        "     OR LOWER(COALESCE(p.referencia_operacion, '')) LIKE LOWER(CONCAT('%', :q, '%')))",
           nativeQuery = true)
    Page<PagoMercado> buscarPaginado(@Param("comercianteId") UUID comercianteId,
                                     @Param("cajaSesionId") UUID cajaSesionId,
                                     @Param("medioPago") String medioPago,
                                     @Param("estado") String estado,
                                     @Param("fechaDesde") OffsetDateTime fechaDesde,
                                     @Param("fechaHasta") OffsetDateTime fechaHasta,
                                     @Param("q") String q,
                                     Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(p.monto_recibido), 0.00) FROM mercados_pagos p " +
                   "WHERE p.estado IN ('CONFIRMADO', 'CONCILIADO') " +
                   "AND p.fecha_pago >= CAST(:desde AS timestamptz) " +
                   "AND p.fecha_pago <= CAST(:hasta AS timestamptz)",
           nativeQuery = true)
    BigDecimal sumarTotalRecaudadoPeriodo(@Param("desde") OffsetDateTime desde,
                                          @Param("hasta") OffsetDateTime hasta);

    @Query(value = "SELECT COALESCE(SUM(p.monto_recibido), 0.00) FROM mercados_pagos p " +
                   "WHERE p.caja_sesion_id = CAST(:cajaSesionId AS uuid) " +
                   "AND p.estado IN ('CONFIRMADO', 'CONCILIADO')",
           nativeQuery = true)
    BigDecimal sumarTotalRecaudadoSesion(@Param("cajaSesionId") UUID cajaSesionId);

    @Query(value = "SELECT p.medio_pago, COALESCE(SUM(p.monto_recibido), 0.00) " +
                   "FROM mercados_pagos p " +
                   "WHERE p.estado IN ('CONFIRMADO', 'CONCILIADO') " +
                   "AND p.fecha_pago >= CAST(:desde AS timestamptz) " +
                   "AND p.fecha_pago <= CAST(:hasta AS timestamptz) " +
                   "GROUP BY p.medio_pago",
           nativeQuery = true)
    List<Object[]> recaudacionPorMedioPago(@Param("desde") OffsetDateTime desde,
                                           @Param("hasta") OffsetDateTime hasta);
}
