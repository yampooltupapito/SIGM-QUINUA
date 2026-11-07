package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.PatrullajeVisitaPunto;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoVisitaPunto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatrullajeVisitaPuntoRepository extends JpaRepository<PatrullajeVisitaPunto, UUID> {

    List<PatrullajeVisitaPunto> findByPatrullajeIdOrderByOrdenVisitaAsc(UUID patrullajeId);

    Optional<PatrullajeVisitaPunto> findByPatrullajeIdAndPuntoControlId(UUID patrullajeId, UUID puntoControlId);

    long countByPatrullajeIdAndEstadoVisita(UUID patrullajeId, EstadoVisitaPunto estadoVisita);

    long countByPatrullajeId(UUID patrullajeId);
}
