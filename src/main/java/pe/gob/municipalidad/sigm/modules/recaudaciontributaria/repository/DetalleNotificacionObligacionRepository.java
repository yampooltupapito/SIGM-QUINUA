package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DetalleNotificacionObligacion;

import java.util.List;
import java.util.UUID;

@Repository
public interface DetalleNotificacionObligacionRepository extends JpaRepository<DetalleNotificacionObligacion, UUID> {

    List<DetalleNotificacionObligacion> findByNotificacionId(UUID notificacionId);

    void deleteByNotificacionId(UUID notificacionId);
}

