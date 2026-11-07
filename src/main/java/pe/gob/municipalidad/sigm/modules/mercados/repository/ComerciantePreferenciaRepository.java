package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ComerciantePreferencia;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComerciantePreferenciaRepository extends JpaRepository<ComerciantePreferencia, UUID> {

    Optional<ComerciantePreferencia> findByComercianteId(UUID comercianteId);

    boolean existsByComercianteId(UUID comercianteId);
}
