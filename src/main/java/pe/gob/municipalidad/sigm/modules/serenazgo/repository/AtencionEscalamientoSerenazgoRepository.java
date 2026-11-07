package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.AtencionEscalamientoSerenazgo;

import java.util.List;
import java.util.UUID;

@Repository
public interface AtencionEscalamientoSerenazgoRepository extends JpaRepository<AtencionEscalamientoSerenazgo, UUID> {

    List<AtencionEscalamientoSerenazgo> findByAtencionIdOrderByFechaEscalamientoDesc(UUID atencionId);
}
