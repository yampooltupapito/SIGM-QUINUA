package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ObligacionMercado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ObligacionMercadoRepository extends JpaRepository<ObligacionMercado, UUID> {

    @Query(value = "SELECT nextval('mercados_obligaciones_seq')", nativeQuery = true)
    Long obtenerSiguienteCorrelativo();

    Optional<ObligacionMercado> findByCodigoObligacion(String codigoObligacion);

    @Query("SELECT o FROM ObligacionMercado o " +
           "JOIN FETCH o.comerciante c " +
           "JOIN FETCH o.concepto con " +
           "LEFT JOIN FETCH o.puesto p " +
           "LEFT JOIN FETCH p.mercado m " +
           "LEFT JOIN FETCH o.asignacion a " +
           "WHERE o.id = :id")
    Optional<ObligacionMercado> findByIdConRelaciones(@Param("id") UUID id);

    List<ObligacionMercado> findByComercianteIdOrderByFechaVencimientoAsc(UUID comercianteId);

    Page<ObligacionMercado> findByComercianteId(UUID comercianteId, Pageable pageable);

    long countByComercianteIdAndEstadoIn(UUID comercianteId, java.util.Collection<pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoObligacion> estados);

    List<ObligacionMercado> findByPuestoIdOrderByFechaVencimientoAsc(UUID puestoId);

    @Query(value = "SELECT o.* FROM mercados_obligaciones o " +
                   "WHERE o.comerciante_id = CAST(:comercianteId AS uuid) " +
                   "AND o.estado IN ('PENDIENTE', 'PARCIALMENTE_PAGADA', 'VENCIDA') " +
                   "ORDER BY o.fecha_vencimiento ASC NULLS LAST, o.created_at ASC",
           nativeQuery = true)
    List<ObligacionMercado> buscarExigiblesPorComerciante(@Param("comercianteId") UUID comercianteId);

    @Query(value = "SELECT COALESCE(SUM(o.saldo_pendiente), 0.00) FROM mercados_obligaciones o " +
                   "WHERE o.comerciante_id = CAST(:comercianteId AS uuid) " +
                   "AND o.estado IN ('PENDIENTE', 'PARCIALMENTE_PAGADA', 'VENCIDA')",
           nativeQuery = true)
    BigDecimal sumarDeudaTotalComerciante(@Param("comercianteId") UUID comercianteId);

    @Query(value = "SELECT COALESCE(SUM(o.saldo_pendiente), 0.00) FROM mercados_obligaciones o " +
                   "WHERE o.puesto_id = CAST(:puestoId AS uuid) " +
                   "AND o.estado IN ('PENDIENTE', 'PARCIALMENTE_PAGADA', 'VENCIDA')",
           nativeQuery = true)
    BigDecimal sumarDeudaTotalPuesto(@Param("puestoId") UUID puestoId);

    @Query(value = "SELECT COUNT(*) > 0 FROM mercados_obligaciones o " +
                   "WHERE o.comerciante_id = CAST(:comercianteId AS uuid) " +
                   "AND (CAST(:puestoId AS uuid) IS NULL OR o.puesto_id = CAST(:puestoId AS uuid)) " +
                   "AND o.concepto_id = CAST(:conceptoId AS uuid) " +
                   "AND o.periodo_inicio = CAST(:periodoInicio AS date) " +
                   "AND o.periodo_fin = CAST(:periodoFin AS date) " +
                   "AND o.estado NOT IN ('ANULADA')",
           nativeQuery = true)
    boolean existsDuplicada(@Param("comercianteId") UUID comercianteId,
                            @Param("puestoId") UUID puestoId,
                            @Param("conceptoId") UUID conceptoId,
                            @Param("periodoInicio") LocalDate periodoInicio,
                            @Param("periodoFin") LocalDate periodoFin);

    @Query(value = "SELECT o.* FROM mercados_obligaciones o " +
                   "JOIN mercados_comerciantes com ON com.id = o.comerciante_id " +
                   "LEFT JOIN personas per ON per.id = com.persona_id " +
                   "LEFT JOIN personas_juridicas pj ON pj.id = com.persona_juridica_id " +
                   "JOIN mercados_conceptos_cobro con ON con.id = o.concepto_id " +
                   "LEFT JOIN mercados_puestos pue ON pue.id = o.puesto_id " +
                   "WHERE (CAST(:mercadoId AS uuid) IS NULL OR pue.mercado_id = CAST(:mercadoId AS uuid)) " +
                   "AND (CAST(:comercianteId AS uuid) IS NULL OR o.comerciante_id = CAST(:comercianteId AS uuid)) " +
                   "AND (CAST(:puestoId AS uuid) IS NULL OR o.puesto_id = CAST(:puestoId AS uuid)) " +
                   "AND (CAST(:conceptoId AS uuid) IS NULL OR o.concepto_id = CAST(:conceptoId AS uuid)) " +
                   "AND (:estado IS NULL OR o.estado = :estado) " +
                   "AND (CAST(:fechaDesde AS date) IS NULL OR o.fecha_emision >= CAST(:fechaDesde AS date)) " +
                   "AND (CAST(:fechaHasta AS date) IS NULL OR o.fecha_emision <= CAST(:fechaHasta AS date)) " +
                   "AND (:q IS NULL OR LOWER(o.codigo_obligacion) LIKE LOWER(CONCAT('%', :q, '%')) " +
                   "     OR LOWER(per.nombres) LIKE LOWER(CONCAT('%', :q, '%')) " +
                   "     OR LOWER(per.apellido_paterno) LIKE LOWER(CONCAT('%', :q, '%')) " +
                   "     OR LOWER(pj.razon_social) LIKE LOWER(CONCAT('%', :q, '%')) " +
                   "     OR per.numero_documento LIKE CONCAT('%', :q, '%') " +
                   "     OR pj.ruc LIKE CONCAT('%', :q, '%')) " +
                   "ORDER BY o.fecha_emision DESC, o.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM mercados_obligaciones o " +
                        "JOIN mercados_comerciantes com ON com.id = o.comerciante_id " +
                        "LEFT JOIN personas per ON per.id = com.persona_id " +
                        "LEFT JOIN personas_juridicas pj ON pj.id = com.persona_juridica_id " +
                        "JOIN mercados_conceptos_cobro con ON con.id = o.concepto_id " +
                        "LEFT JOIN mercados_puestos pue ON pue.id = o.puesto_id " +
                        "WHERE (CAST(:mercadoId AS uuid) IS NULL OR pue.mercado_id = CAST(:mercadoId AS uuid)) " +
                        "AND (CAST(:comercianteId AS uuid) IS NULL OR o.comerciante_id = CAST(:comercianteId AS uuid)) " +
                        "AND (CAST(:puestoId AS uuid) IS NULL OR o.puesto_id = CAST(:puestoId AS uuid)) " +
                        "AND (CAST(:conceptoId AS uuid) IS NULL OR o.concepto_id = CAST(:conceptoId AS uuid)) " +
                        "AND (:estado IS NULL OR o.estado = :estado) " +
                        "AND (CAST(:fechaDesde AS date) IS NULL OR o.fecha_emision >= CAST(:fechaDesde AS date)) " +
                        "AND (CAST(:fechaHasta AS date) IS NULL OR o.fecha_emision <= CAST(:fechaHasta AS date)) " +
                        "AND (:q IS NULL OR LOWER(o.codigo_obligacion) LIKE LOWER(CONCAT('%', :q, '%')) " +
                        "     OR LOWER(per.nombres) LIKE LOWER(CONCAT('%', :q, '%')) " +
                        "     OR LOWER(per.apellido_paterno) LIKE LOWER(CONCAT('%', :q, '%')) " +
                        "     OR LOWER(pj.razon_social) LIKE LOWER(CONCAT('%', :q, '%')) " +
                        "     OR per.numero_documento LIKE CONCAT('%', :q, '%') " +
                        "     OR pj.ruc LIKE CONCAT('%', :q, '%'))",
           nativeQuery = true)
    Page<ObligacionMercado> buscarPaginado(@Param("mercadoId") UUID mercadoId,
                                           @Param("comercianteId") UUID comercianteId,
                                           @Param("puestoId") UUID puestoId,
                                           @Param("conceptoId") UUID conceptoId,
                                           @Param("estado") String estado,
                                           @Param("fechaDesde") LocalDate fechaDesde,
                                           @Param("fechaHasta") LocalDate fechaHasta,
                                           @Param("q") String q,
                                           Pageable pageable);

    @Query(value = "SELECT o.* FROM mercados_obligaciones o " +
                   "WHERE o.estado IN ('PENDIENTE', 'PARCIALMENTE_PAGADA') " +
                   "AND o.fecha_vencimiento < CAST(:fechaActual AS date)",
           nativeQuery = true)
    List<ObligacionMercado> buscarVencidas(@Param("fechaActual") LocalDate fechaActual);

    @Query(value = "SELECT o.* FROM mercados_obligaciones o " +
                   "WHERE o.estado IN ('PENDIENTE', 'PARCIALMENTE_PAGADA') " +
                   "AND o.fecha_vencimiento >= CAST(:hoy AS date) " +
                   "AND o.fecha_vencimiento <= CAST(:limite AS date)",
           nativeQuery = true)
    List<ObligacionMercado> buscarProximasAVencer(@Param("hoy") LocalDate hoy,
                                                  @Param("limite") LocalDate limite);
}
