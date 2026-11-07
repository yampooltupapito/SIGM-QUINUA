package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.EvidenciaNotificacionTributaria;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvidenciaNotificacionTributariaRepository extends JpaRepository<EvidenciaNotificacionTributaria, UUID> {

    List<EvidenciaNotificacionTributaria> findByNotificacionIdOrderByCreatedAtDesc(UUID notificacionId);

    long countByNotificacionId(UUID notificacionId);
}

