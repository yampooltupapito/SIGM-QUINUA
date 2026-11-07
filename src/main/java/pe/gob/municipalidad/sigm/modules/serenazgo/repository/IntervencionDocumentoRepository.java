package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.IntervencionDocumentoSerenazgo;

import java.util.List;
import java.util.UUID;

@Repository
public interface IntervencionDocumentoRepository extends JpaRepository<IntervencionDocumentoSerenazgo, UUID> {

    List<IntervencionDocumentoSerenazgo> findByIntervencionIdOrderByCreatedAtAsc(UUID intervencionId);

    List<IntervencionDocumentoSerenazgo> findByIntervencionIdAndVisibleCiudadanoTrue(UUID intervencionId);
}
