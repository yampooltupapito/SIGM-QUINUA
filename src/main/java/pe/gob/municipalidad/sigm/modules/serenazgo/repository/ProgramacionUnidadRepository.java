package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ProgramacionUnidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoReservaRecurso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramacionUnidadRepository extends JpaRepository<ProgramacionUnidad, UUID> {

    List<ProgramacionUnidad> findByProgramacionId(UUID programacionId);

    List<ProgramacionUnidad> findByUnidadId(UUID unidadId);

    Optional<ProgramacionUnidad> findByProgramacionIdAndUnidadId(UUID programacionId, UUID unidadId);

    long countByEstadoReserva(EstadoReservaRecurso estado);
}
