package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.AsignacionNotificadorTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAsignacionNotificador;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AsignacionNotificadorTributarioRepository extends JpaRepository<AsignacionNotificadorTributario, UUID>, JpaSpecificationExecutor<AsignacionNotificadorTributario> {

    List<AsignacionNotificadorTributario> findByNotificacionIdOrderByFechaAsignacionDesc(UUID notificacionId);

    Optional<AsignacionNotificadorTributario> findFirstByNotificacionIdAndEstadoOrderByFechaAsignacionDesc(UUID notificacionId, EstadoAsignacionNotificador estado);

    Page<AsignacionNotificadorTributario> findByNotificadorId(Long notificadorId, Pageable pageable);

    Page<AsignacionNotificadorTributario> findByNotificadorIdAndEstado(Long notificadorId, EstadoAsignacionNotificador estado, Pageable pageable);

    long countByNotificadorIdAndEstado(Long notificadorId, EstadoAsignacionNotificador estado);
}

