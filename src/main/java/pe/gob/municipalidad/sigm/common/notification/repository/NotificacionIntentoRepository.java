package pe.gob.municipalidad.sigm.common.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionIntento;

import java.util.List;

@Repository
public interface NotificacionIntentoRepository extends JpaRepository<NotificacionIntento, Long> {

    List<NotificacionIntento> findByEnvioIdOrderByFechaIntentoAsc(Long envioId);
}
