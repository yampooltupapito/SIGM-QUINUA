package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.HistorialCalculoPredial;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialCalculoPredialRepository extends JpaRepository<HistorialCalculoPredial, UUID> {
    List<HistorialCalculoPredial> findByCalculoIdOrderByFechaRegistroDesc(UUID calculoId);
}
