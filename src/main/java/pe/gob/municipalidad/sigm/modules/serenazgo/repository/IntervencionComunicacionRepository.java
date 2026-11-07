package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.IntervencionComunicacionSerenazgo;

import java.util.List;
import java.util.UUID;

@Repository
public interface IntervencionComunicacionRepository extends JpaRepository<IntervencionComunicacionSerenazgo, UUID> {

    List<IntervencionComunicacionSerenazgo> findByIntervencionIdOrderByFechaComunicacionAsc(UUID intervencionId);
}
