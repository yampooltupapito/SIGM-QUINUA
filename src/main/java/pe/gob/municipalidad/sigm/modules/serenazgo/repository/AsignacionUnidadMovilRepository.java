package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.AsignacionUnidadMovil;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAsignacionRecurso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AsignacionUnidadMovilRepository extends JpaRepository<AsignacionUnidadMovil, UUID>, JpaSpecificationExecutor<AsignacionUnidadMovil> {

    List<AsignacionUnidadMovil> findByUnidadIdOrderByFechaInicioDesc(UUID unidadId);

    Optional<AsignacionUnidadMovil> findFirstByUnidadIdAndEstadoAsignacion(UUID unidadId, EstadoAsignacionRecurso estadoAsignacion);

    boolean existsByUnidadIdAndEstadoAsignacion(UUID unidadId, EstadoAsignacionRecurso estadoAsignacion);

    List<AsignacionUnidadMovil> findByPersonalIdOrderByFechaInicioDesc(UUID personalId);

    long countByEstadoAsignacion(EstadoAsignacionRecurso estadoAsignacion);
}
