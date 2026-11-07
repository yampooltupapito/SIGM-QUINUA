package pe.gob.municipalidad.sigm.common.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionPlantilla;

import java.util.Optional;

@Repository
public interface NotificacionPlantillaRepository extends JpaRepository<NotificacionPlantilla, Long> {

    Optional<NotificacionPlantilla> findByCodigo(String codigo);

    Optional<NotificacionPlantilla> findByCodigoAndActivoTrue(String codigo);

    boolean existsByCodigo(String codigo);
}
