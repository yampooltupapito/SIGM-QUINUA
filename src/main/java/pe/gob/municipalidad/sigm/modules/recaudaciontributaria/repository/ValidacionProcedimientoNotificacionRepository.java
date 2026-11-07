package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ValidacionProcedimientoNotificacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ValidacionProcedimientoNotificacionRepository extends JpaRepository<ValidacionProcedimientoNotificacion, UUID> {

    List<ValidacionProcedimientoNotificacion> findByNotificacionIdOrderByFechaValidacionDesc(UUID notificacionId);

    Optional<ValidacionProcedimientoNotificacion> findFirstByNotificacionIdOrderByFechaValidacionDesc(UUID notificacionId);
}

