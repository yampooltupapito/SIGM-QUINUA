package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.IncidenciaConvenioFraccionamiento;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidenciaConvenioFraccionamientoRepository
        extends JpaRepository<IncidenciaConvenioFraccionamiento, UUID> {

    List<IncidenciaConvenioFraccionamiento> findByConvenioIdOrderByFechaRegistroDesc(UUID convenioId);
}
