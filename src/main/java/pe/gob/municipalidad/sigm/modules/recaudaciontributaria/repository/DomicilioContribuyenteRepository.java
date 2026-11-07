package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DomicilioContribuyente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DomicilioContribuyenteRepository extends JpaRepository<DomicilioContribuyente, UUID> {

    List<DomicilioContribuyente> findByContribuyenteIdOrderByCreatedAtDesc(UUID contribuyenteId);

    Optional<DomicilioContribuyente> findByContribuyenteIdAndEsFiscalTrue(UUID contribuyenteId);

    List<DomicilioContribuyente> findByContribuyenteIdAndEstado(UUID contribuyenteId, String estado);
}
