package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CuotaObligacionTributaria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CuotaObligacionTributariaRepository extends JpaRepository<CuotaObligacionTributaria, UUID> {

    List<CuotaObligacionTributaria> findByObligacionIdOrderByNumeroCuotaAsc(UUID obligacionId);

    Optional<CuotaObligacionTributaria> findByObligacionIdAndNumeroCuota(UUID obligacionId, Integer numeroCuota);

    void deleteByObligacionId(UUID obligacionId);
}
