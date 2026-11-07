package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.IncidenciaHistorial;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidenciaHistorialRepository extends JpaRepository<IncidenciaHistorial, UUID> {
    List<IncidenciaHistorial> findByIncidenciaIdOrderByCreatedAtDesc(UUID incidenciaId);
    List<IncidenciaHistorial> findByIncidenciaIdOrderByCreatedAtAsc(UUID incidenciaId);
}
