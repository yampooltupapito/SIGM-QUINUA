package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AuditoriaPlanoMercado;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditoriaPlanoMercadoRepository extends JpaRepository<AuditoriaPlanoMercado, UUID> {

    List<AuditoriaPlanoMercado> findByPlanoIdOrderByCreatedAtDesc(UUID planoId);

    List<AuditoriaPlanoMercado> findByPlanoIdAndVersionPlanoIdOrderByCreatedAtDesc(UUID planoId, UUID versionPlanoId);
}
