package pe.gob.municipalidad.sigm.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.model.PersonaJuridica;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonaJuridicaRepository extends JpaRepository<PersonaJuridica, UUID> {

    Optional<PersonaJuridica> findByRuc(String ruc);

    boolean existsByRuc(String ruc);

    boolean existsByRucAndIdNot(String ruc, UUID id);

    Page<PersonaJuridica> findByRazonSocialContainingIgnoreCaseOrRucContaining(
            String razonSocial, String ruc, Pageable pageable
    );
}
