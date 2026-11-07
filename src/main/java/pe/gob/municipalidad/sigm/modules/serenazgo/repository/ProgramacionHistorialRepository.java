package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ProgramacionHistorial;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProgramacionHistorialRepository extends JpaRepository<ProgramacionHistorial, UUID> {

    List<ProgramacionHistorial> findByProgramacionIdOrderByCreatedAtDesc(UUID programacionId);
}
