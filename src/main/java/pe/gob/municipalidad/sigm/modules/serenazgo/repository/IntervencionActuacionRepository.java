package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.IntervencionActuacionSerenazgo;

import java.util.List;
import java.util.UUID;

@Repository
public interface IntervencionActuacionRepository extends JpaRepository<IntervencionActuacionSerenazgo, UUID> {

    List<IntervencionActuacionSerenazgo> findByIntervencionIdOrderByFechaHoraAsc(UUID intervencionId);
}
