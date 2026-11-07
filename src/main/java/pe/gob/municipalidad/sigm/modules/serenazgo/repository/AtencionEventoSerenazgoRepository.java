package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.AtencionEventoSerenazgo;

import java.util.List;
import java.util.UUID;

@Repository
public interface AtencionEventoSerenazgoRepository extends JpaRepository<AtencionEventoSerenazgo, UUID> {

    List<AtencionEventoSerenazgo> findByAtencionIdOrderByFechaEventoAsc(UUID atencionId);

    List<AtencionEventoSerenazgo> findByAtencionIdAndVisibleCiudadanoTrueOrderByFechaEventoAsc(UUID atencionId);
}
