package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DetalleLoteGeneracion;

import java.util.List;
import java.util.UUID;

@Repository
public interface DetalleLoteGeneracionRepository extends JpaRepository<DetalleLoteGeneracion, UUID> {

    List<DetalleLoteGeneracion> findByLoteId(UUID loteId);

    long countByLoteId(UUID loteId);
}
