package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.HistorialConfiguracionTributaria;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialConfiguracionTributariaRepository extends JpaRepository<HistorialConfiguracionTributaria, UUID> {

    List<HistorialConfiguracionTributaria> findByConfiguracionIdOrderByFechaRegistroDesc(UUID configuracionId);
}
