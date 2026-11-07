package pe.gob.municipalidad.sigm.common.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionPreferencias;

import java.util.Optional;

@Repository
public interface NotificacionPreferenciasRepository extends JpaRepository<NotificacionPreferencias, Long> {

    Optional<NotificacionPreferencias> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}
