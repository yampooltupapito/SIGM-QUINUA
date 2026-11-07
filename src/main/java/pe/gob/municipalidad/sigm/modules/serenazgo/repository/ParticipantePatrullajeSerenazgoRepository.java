package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ParticipantePatrullajeSerenazgo;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipantePatrullajeSerenazgoRepository extends JpaRepository<ParticipantePatrullajeSerenazgo, UUID> {
    List<ParticipantePatrullajeSerenazgo> findByPatrullajeId(UUID patrullajeId);
    List<ParticipantePatrullajeSerenazgo> findByPersonalId(UUID personalId);
    boolean existsByPatrullajeIdAndPersonalId(UUID patrullajeId, UUID personalId);
}
