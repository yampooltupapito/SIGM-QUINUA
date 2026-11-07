package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ControlPlazoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPlazoNotificacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ControlPlazoNotificacionRepository extends JpaRepository<ControlPlazoNotificacion, UUID> {

    Optional<ControlPlazoNotificacion> findByNotificacionId(UUID notificacionId);

    List<ControlPlazoNotificacion> findByEstadoPlazo(EstadoPlazoNotificacion estado);

    @Query("SELECT cp FROM ControlPlazoNotificacion cp WHERE cp.estadoPlazo = 'VIGENTE' AND cp.fechaVencimiento <= :fechaLimite")
    List<ControlPlazoNotificacion> findProximosAVencer(LocalDate fechaLimite);
}

