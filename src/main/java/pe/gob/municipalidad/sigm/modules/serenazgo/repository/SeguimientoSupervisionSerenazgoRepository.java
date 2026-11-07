package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.SeguimientoSupervisionSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSeguimientoSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadAdministrativaSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRegistroSupervision;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeguimientoSupervisionSerenazgoRepository extends JpaRepository<SeguimientoSupervisionSerenazgo, UUID>, JpaSpecificationExecutor<SeguimientoSupervisionSerenazgo> {

    @Query(value = "SELECT nextval('seq_serenazgo_supervision_seguimientos')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<SeguimientoSupervisionSerenazgo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    long countByEstado(EstadoSeguimientoSupervision estado);

    long countByEstadoIn(Collection<EstadoSeguimientoSupervision> estados);

    long countByPrioridadAdministrativa(PrioridadAdministrativaSupervision prioridad);

    List<SeguimientoSupervisionSerenazgo> findByRegistroOrigenId(UUID registroOrigenId);

    List<SeguimientoSupervisionSerenazgo> findTop10ByOrderByFechaCreacionDesc();

    @Query("SELECT s.estado, COUNT(s) FROM SeguimientoSupervisionSerenazgo s GROUP BY s.estado")
    List<Object[]> contarPorEstado();

    @Query("SELECT s.prioridadAdministrativa, COUNT(s) FROM SeguimientoSupervisionSerenazgo s GROUP BY s.prioridadAdministrativa")
    List<Object[]> contarPorPrioridad();

    @Query("SELECT s.tipoRegistroOrigen, COUNT(s) FROM SeguimientoSupervisionSerenazgo s GROUP BY s.tipoRegistroOrigen")
    List<Object[]> contarPorTipoRegistroOrigen();

    @Query("SELECT COUNT(s) FROM SeguimientoSupervisionSerenazgo s " +
           "WHERE s.estado IN ('ABIERTO', 'EN_REVISION', 'OBSERVADO', 'EN_SUBSANACION') " +
           "AND s.fechaCompromiso IS NOT NULL AND s.fechaCompromiso < :ahora")
    long contarVencidos(@Param("ahora") OffsetDateTime ahora);
}
