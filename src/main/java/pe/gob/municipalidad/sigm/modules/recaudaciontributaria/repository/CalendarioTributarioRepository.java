package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CalendarioTributario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalendarioTributarioRepository extends JpaRepository<CalendarioTributario, UUID> {

    List<CalendarioTributario> findByConceptoIdAndPeriodoIdOrderByNumeroCuotaAsc(UUID conceptoId, UUID periodoId);

    List<CalendarioTributario> findByPeriodoIdOrderByFechaVencimientoAsc(UUID periodoId);

    Optional<CalendarioTributario> findByConceptoIdAndPeriodoIdAndNumeroCuota(UUID conceptoId, UUID periodoId, Integer numeroCuota);

    boolean existsByConceptoIdAndPeriodoIdAndNumeroCuota(UUID conceptoId, UUID periodoId, Integer numeroCuota);
}
