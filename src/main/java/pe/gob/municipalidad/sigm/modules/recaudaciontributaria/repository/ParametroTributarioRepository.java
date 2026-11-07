package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ParametroTributario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParametroTributarioRepository extends JpaRepository<ParametroTributario, UUID> {

    List<ParametroTributario> findByConfiguracionIdOrderByOrdenVisualAsc(UUID configuracionId);

    Optional<ParametroTributario> findByConfiguracionIdAndCodigo(UUID configuracionId, String codigo);

    boolean existsByConfiguracionIdAndCodigo(UUID configuracionId, String codigo);
}
