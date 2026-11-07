package pe.gob.municipalidad.sigm.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.model.PersonaJuridicaRepresentante;

import java.util.List;
import java.util.UUID;

@Repository
public interface PersonaJuridicaRepresentanteRepository extends JpaRepository<PersonaJuridicaRepresentante, Long> {

    List<PersonaJuridicaRepresentante> findByPersonaJuridicaId(UUID personaJuridicaId);

    List<PersonaJuridicaRepresentante> findByPersonaJuridicaIdAndEstado(UUID personaJuridicaId, String estado);

    boolean existsByPersonaJuridicaIdAndPersonaId(UUID personaJuridicaId, Long personaId);
}
