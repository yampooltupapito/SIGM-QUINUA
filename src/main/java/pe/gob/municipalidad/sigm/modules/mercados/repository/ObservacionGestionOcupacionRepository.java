package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ObservacionGestionOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoObservacionGestion;

import java.util.List;
import java.util.UUID;

@Repository
public interface ObservacionGestionOcupacionRepository extends JpaRepository<ObservacionGestionOcupacion, Long> {

    List<ObservacionGestionOcupacion> findBySolicitudIdOrderByFechaObservacionAsc(UUID solicitudId);

    List<ObservacionGestionOcupacion> findBySolicitudIdAndEstado(UUID solicitudId, EstadoObservacionGestion estado);

    long countBySolicitudIdAndEstado(UUID solicitudId, EstadoObservacionGestion estado);
}
