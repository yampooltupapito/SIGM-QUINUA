package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.TarifaMercado;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TarifaMercadoRepository extends JpaRepository<TarifaMercado, UUID> {

    List<TarifaMercado> findByConceptoIdAndEstado(UUID conceptoId, String estado);

    @Query(value = "SELECT t.* FROM mercados_tarifas t " +
                   "WHERE t.concepto_id = CAST(:conceptoId AS uuid) " +
                   "AND (t.mercado_id IS NULL OR t.mercado_id = CAST(:mercadoId AS uuid)) " +
                   "AND (t.tipo_puesto_id IS NULL OR t.tipo_puesto_id = :tipoPuestoId) " +
                   "AND t.estado = 'ACTIVA' " +
                   "AND t.fecha_inicio_vigencia <= CAST(:fecha AS date) " +
                   "AND (t.fecha_fin_vigencia IS NULL OR t.fecha_fin_vigencia >= CAST(:fecha AS date)) " +
                   "ORDER BY t.mercado_id NULLS LAST, t.tipo_puesto_id NULLS LAST, t.version_tarifa DESC LIMIT 1",
           nativeQuery = true)
    Optional<TarifaMercado> buscarTarifaAplicable(@Param("conceptoId") UUID conceptoId,
                                                @Param("mercadoId") UUID mercadoId,
                                                @Param("tipoPuestoId") Long tipoPuestoId,
                                                @Param("fecha") LocalDate fecha);

    @Query(value = "SELECT t.* FROM mercados_tarifas t " +
                   "JOIN mercados_conceptos_cobro c ON c.id = t.concepto_id " +
                   "WHERE (CAST(:conceptoId AS uuid) IS NULL OR t.concepto_id = CAST(:conceptoId AS uuid)) " +
                   "AND (CAST(:mercadoId AS uuid) IS NULL OR t.mercado_id = CAST(:mercadoId AS uuid)) " +
                   "AND (:estado IS NULL OR t.estado = :estado) " +
                   "ORDER BY c.nombre ASC, t.version_tarifa DESC",
           countQuery = "SELECT COUNT(*) FROM mercados_tarifas t " +
                        "WHERE (CAST(:conceptoId AS uuid) IS NULL OR t.concepto_id = CAST(:conceptoId AS uuid)) " +
                        "AND (CAST(:mercadoId AS uuid) IS NULL OR t.mercado_id = CAST(:mercadoId AS uuid)) " +
                        "AND (:estado IS NULL OR t.estado = :estado)",
           nativeQuery = true)
    Page<TarifaMercado> buscarPaginado(@Param("conceptoId") UUID conceptoId,
                                       @Param("mercadoId") UUID mercadoId,
                                       @Param("estado") String estado,
                                       Pageable pageable);
}
