package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CostoServicioArbitrio;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface CostoServicioArbitrioRepository extends JpaRepository<CostoServicioArbitrio, UUID> {

    List<CostoServicioArbitrio> findByConfiguracionId(UUID configuracionId);

    @Query("SELECT COALESCE(SUM(c.importe), 0) FROM CostoServicioArbitrio c WHERE c.configuracion.id = :configuracionId")
    BigDecimal sumImporteByConfiguracionId(@Param("configuracionId") UUID configuracionId);
}
