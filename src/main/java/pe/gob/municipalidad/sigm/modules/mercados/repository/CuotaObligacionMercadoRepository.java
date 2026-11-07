package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.CuotaObligacionMercado;

import java.util.List;
import java.util.UUID;

@Repository
public interface CuotaObligacionMercadoRepository extends JpaRepository<CuotaObligacionMercado, UUID> {
    List<CuotaObligacionMercado> findByObligacionIdOrderByNumeroCuotaAsc(UUID obligacionId);
}
