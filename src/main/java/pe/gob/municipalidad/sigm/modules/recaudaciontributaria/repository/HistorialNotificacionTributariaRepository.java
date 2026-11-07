package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.HistorialNotificacionTributaria;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialNotificacionTributariaRepository extends JpaRepository<HistorialNotificacionTributaria, UUID> {

    List<HistorialNotificacionTributaria> findByNotificacionIdOrderByFechaEventoAsc(UUID notificacionId);
}

