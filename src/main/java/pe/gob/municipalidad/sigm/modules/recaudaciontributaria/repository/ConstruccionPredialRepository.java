package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConstruccionPredial;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConstruccionPredialRepository extends JpaRepository<ConstruccionPredial, UUID> {

    List<ConstruccionPredial> findByPredioIdOrderByNivelPisoAsc(UUID predioId);
}
