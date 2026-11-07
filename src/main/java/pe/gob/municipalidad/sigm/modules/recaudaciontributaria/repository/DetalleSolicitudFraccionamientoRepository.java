package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DetalleSolicitudFraccionamiento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DetalleSolicitudFraccionamientoRepository
        extends JpaRepository<DetalleSolicitudFraccionamiento, UUID> {

    List<DetalleSolicitudFraccionamiento> findBySolicitudId(UUID solicitudId);

    Optional<DetalleSolicitudFraccionamiento> findBySolicitudIdAndObligacionId(UUID solicitudId, UUID obligacionId);

    boolean existsByObligacionIdAndSolicitudEstadoNotIn(UUID obligacionId, List<pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSolicitudFraccionamiento> estadosExcluidos);
}
