package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.IntentoNotificacionTributaria;

import java.util.List;
import java.util.UUID;

@Repository
public interface IntentoNotificacionTributariaRepository extends JpaRepository<IntentoNotificacionTributaria, UUID> {

    List<IntentoNotificacionTributaria> findByNotificacionIdOrderByFechaHoraIntentoDesc(UUID notificacionId);

    long countByNotificacionId(UUID notificacionId);
}

