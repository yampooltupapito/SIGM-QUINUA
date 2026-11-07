package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.AtencionIncidenciaSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAtencionIncidencia;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AtencionIncidenciaSerenazgoRepository extends
        JpaRepository<AtencionIncidenciaSerenazgo, UUID>,
        JpaSpecificationExecutor<AtencionIncidenciaSerenazgo> {

    Optional<AtencionIncidenciaSerenazgo> findByCodigo(String codigo);

    List<AtencionIncidenciaSerenazgo> findByIncidenciaIdOrderByFechaHoraAsignacionDesc(UUID incidenciaId);

    Optional<AtencionIncidenciaSerenazgo> findFirstByIncidenciaIdAndEstadoNotIn(
            UUID incidenciaId, Collection<EstadoAtencionIncidencia> estadosFinales);

    boolean existsByIncidenciaIdAndEstadoNotIn(
            UUID incidenciaId, Collection<EstadoAtencionIncidencia> estadosFinales);

    long countByEstado(EstadoAtencionIncidencia estado);

    long countByEstadoIn(Collection<EstadoAtencionIncidencia> estados);

    @Query(value = "SELECT nextval('seq_serenazgo_atencion')", nativeQuery = true)
    Long getNextCorrelativo();

    boolean existsByUnidadMovilIdAndEstadoNotIn(
            UUID unidadId, Collection<EstadoAtencionIncidencia> estadosFinales);

    @Query("SELECT COUNT(ap) > 0 FROM AtencionPersonalSerenazgo ap " +
           "WHERE ap.personal.id = :personalId AND ap.activo = true " +
           "AND ap.atencion.estado NOT IN :estadosFinales")
    boolean isPersonalEnAtencionActiva(
            @Param("personalId") UUID personalId,
            @Param("estadosFinales") Collection<EstadoAtencionIncidencia> estadosFinales);
}
