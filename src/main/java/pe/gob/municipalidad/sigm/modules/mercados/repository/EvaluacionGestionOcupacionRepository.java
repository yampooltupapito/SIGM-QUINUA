package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.EvaluacionGestionOcupacion;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvaluacionGestionOcupacionRepository extends JpaRepository<EvaluacionGestionOcupacion, Long> {

    List<EvaluacionGestionOcupacion> findBySolicitudIdOrderByFechaEvaluacionDesc(UUID solicitudId);
}
