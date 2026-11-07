package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CronogramaFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCronogramaFraccionamiento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CronogramaFraccionamientoTributarioRepository
        extends JpaRepository<CronogramaFraccionamientoTributario, UUID> {

    List<CronogramaFraccionamientoTributario> findBySolicitudIdOrderByVersionCronogramaDesc(UUID solicitudId);

    Optional<CronogramaFraccionamientoTributario> findFirstBySolicitudIdOrderByVersionCronogramaDesc(UUID solicitudId);

    Optional<CronogramaFraccionamientoTributario> findBySolicitudIdAndEstado(UUID solicitudId, EstadoCronogramaFraccionamiento estado);
}
