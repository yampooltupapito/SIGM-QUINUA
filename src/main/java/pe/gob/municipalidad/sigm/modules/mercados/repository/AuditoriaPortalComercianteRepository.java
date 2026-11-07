package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AuditoriaPortalComerciante;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditoriaPortalComercianteRepository extends JpaRepository<AuditoriaPortalComerciante, UUID> {

    List<AuditoriaPortalComerciante> findByComercianteIdOrderByCreatedAtDesc(UUID comercianteId);

    Page<AuditoriaPortalComerciante> findByComercianteId(UUID comercianteId, Pageable pageable);
}
