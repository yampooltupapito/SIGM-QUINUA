package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ObservacionSupervisionSerenazgo;

import java.util.List;
import java.util.UUID;

@Repository
public interface ObservacionSupervisionSerenazgoRepository extends JpaRepository<ObservacionSupervisionSerenazgo, UUID> {
    List<ObservacionSupervisionSerenazgo> findBySeguimientoIdOrderByFechaDesc(UUID seguimientoId);
    long countBySeguimientoId(UUID seguimientoId);
}
