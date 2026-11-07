package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ProgramacionEquipamiento;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoReservaRecurso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramacionEquipamientoRepository extends JpaRepository<ProgramacionEquipamiento, UUID> {

    List<ProgramacionEquipamiento> findByProgramacionId(UUID programacionId);

    List<ProgramacionEquipamiento> findByEquipamientoId(UUID equipamientoId);

    Optional<ProgramacionEquipamiento> findByProgramacionIdAndEquipamientoId(UUID programacionId, UUID equipamientoId);

    long countByEstadoReserva(EstadoReservaRecurso estado);
}
