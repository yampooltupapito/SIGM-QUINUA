package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.AlertaCiudadanaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAlertaCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertaCiudadanaSeguridadRepository extends JpaRepository<AlertaCiudadanaSeguridad, UUID>, JpaSpecificationExecutor<AlertaCiudadanaSeguridad> {

    @Query(value = "SELECT nextval('seq_serenazgo_alerta')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<AlertaCiudadanaSeguridad> findByCodigo(String codigo);

    List<AlertaCiudadanaSeguridad> findByEstado(EstadoAlertaCiudadana estado);

    Page<AlertaCiudadanaSeguridad> findByEstado(EstadoAlertaCiudadana estado, Pageable pageable);

    List<AlertaCiudadanaSeguridad> findByReportanteUsuarioIdOrderByFechaRecepcionDesc(Long reportanteUsuarioId);

    Page<AlertaCiudadanaSeguridad> findByReportanteUsuarioIdOrderByFechaRecepcionDesc(Long reportanteUsuarioId, Pageable pageable);

    List<AlertaCiudadanaSeguridad> findByIncidenciaId(UUID incidenciaId);

    long countByEstado(EstadoAlertaCiudadana estado);

    long countByPrioridad(PrioridadIncidenciaSeguridad prioridad);

    long countByFechaRecepcionBetween(OffsetDateTime desde, OffsetDateTime hasta);
}
