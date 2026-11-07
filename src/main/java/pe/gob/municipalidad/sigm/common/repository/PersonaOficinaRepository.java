package pe.gob.municipalidad.sigm.common.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaOficinaRepository extends JpaRepository<PersonaOficina, Long> {

    @EntityGraph(attributePaths = {"oficina", "cargo"})
    List<PersonaOficina> findByPersonaId(Long personaId);

    @EntityGraph(attributePaths = {"oficina", "cargo"})
    List<PersonaOficina> findByOficinaId(Long oficinaId);

    @EntityGraph(attributePaths = {"oficina", "cargo"})
    Optional<PersonaOficina> findByPersonaIdAndPrincipalTrue(Long personaId);

    @EntityGraph(attributePaths = {"oficina", "cargo"})
    List<PersonaOficina> findByPersonaIdAndEstado(Long personaId, String estado);

    @EntityGraph(attributePaths = {"oficina", "cargo"})
    List<PersonaOficina> findByOficinaIdAndEstado(Long oficinaId, String estado);
}
