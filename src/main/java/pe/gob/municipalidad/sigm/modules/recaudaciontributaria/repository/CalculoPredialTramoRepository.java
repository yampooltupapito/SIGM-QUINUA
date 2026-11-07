package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CalculoPredialTramo;

import java.util.List;
import java.util.UUID;

@Repository
public interface CalculoPredialTramoRepository extends JpaRepository<CalculoPredialTramo, UUID> {
    List<CalculoPredialTramo> findByCalculoIdOrderByNumeroTramoAsc(UUID calculoId);
}
