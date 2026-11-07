package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.HistorialSupervisionSerenazgo;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialSupervisionSerenazgoRepository extends JpaRepository<HistorialSupervisionSerenazgo, UUID> {
    List<HistorialSupervisionSerenazgo> findBySeguimientoIdOrderByFechaDesc(UUID seguimientoId);
    List<HistorialSupervisionSerenazgo> findTop20ByOrderByFechaDesc();
}
