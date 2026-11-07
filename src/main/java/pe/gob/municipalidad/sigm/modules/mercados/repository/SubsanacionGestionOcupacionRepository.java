package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.SubsanacionGestionOcupacion;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubsanacionGestionOcupacionRepository extends JpaRepository<SubsanacionGestionOcupacion, Long> {

    List<SubsanacionGestionOcupacion> findByObservacionIdOrderByFechaSubsanacionAsc(Long observacionId);

    List<SubsanacionGestionOcupacion> findBySolicitudIdOrderByFechaSubsanacionDesc(UUID solicitudId);
}
