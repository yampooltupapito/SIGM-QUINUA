package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DetalleCalculoArbitrio;

import java.util.List;
import java.util.UUID;

@Repository
public interface DetalleCalculoArbitrioRepository extends JpaRepository<DetalleCalculoArbitrio, UUID> {

    List<DetalleCalculoArbitrio> findByCalculoId(UUID calculoId);
}
