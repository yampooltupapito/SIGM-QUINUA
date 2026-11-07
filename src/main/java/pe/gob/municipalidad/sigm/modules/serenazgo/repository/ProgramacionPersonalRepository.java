package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ProgramacionPersonal;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAsignacionPersonal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramacionPersonalRepository extends JpaRepository<ProgramacionPersonal, UUID> {

    List<ProgramacionPersonal> findByProgramacionId(UUID programacionId);

    List<ProgramacionPersonal> findByPersonalId(UUID personalId);

    Optional<ProgramacionPersonal> findByProgramacionIdAndPersonalId(UUID programacionId, UUID personalId);

    long countByEstadoAsignacion(EstadoAsignacionPersonal estado);
}
