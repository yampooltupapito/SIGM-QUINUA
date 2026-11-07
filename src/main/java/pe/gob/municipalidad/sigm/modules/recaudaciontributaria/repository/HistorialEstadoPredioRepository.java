package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.HistorialEstadoPredio;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialEstadoPredioRepository extends JpaRepository<HistorialEstadoPredio, UUID> {

    List<HistorialEstadoPredio> findByPredioIdOrderByFechaRegistroDesc(UUID predioId);
}
