package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.HistorialArbitrioMunicipal;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialArbitrioMunicipalRepository extends JpaRepository<HistorialArbitrioMunicipal, UUID> {

    List<HistorialArbitrioMunicipal> findByEntidadIdOrderByFechaRegistroDesc(UUID entidadId);
}
