package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ParticipanteIntervencionSerenazgo;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipanteIntervencionSerenazgoRepository extends JpaRepository<ParticipanteIntervencionSerenazgo, UUID> {
    List<ParticipanteIntervencionSerenazgo> findByIntervencionId(UUID intervencionId);
    List<ParticipanteIntervencionSerenazgo> findByPersonalId(UUID personalId);
    boolean existsByIntervencionIdAndPersonalId(UUID intervencionId, UUID personalId);
}
