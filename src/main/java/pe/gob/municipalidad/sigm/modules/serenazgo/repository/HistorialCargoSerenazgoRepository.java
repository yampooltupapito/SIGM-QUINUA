package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.HistorialCargoSerenazgo;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialCargoSerenazgoRepository extends JpaRepository<HistorialCargoSerenazgo, UUID> {
    List<HistorialCargoSerenazgo> findByPersonalIdOrderByFechaInicioDescCreatedAtDesc(UUID personalId);
    long countByPersonalId(UUID personalId);
}
