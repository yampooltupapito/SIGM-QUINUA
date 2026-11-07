package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.HistorialEstadoPersonalSerenazgo;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialEstadoPersonalSerenazgoRepository extends JpaRepository<HistorialEstadoPersonalSerenazgo, UUID> {
    List<HistorialEstadoPersonalSerenazgo> findByPersonalIdOrderByFechaEfectivaDescCreatedAtDesc(UUID personalId);
    long countByPersonalId(UUID personalId);
}
