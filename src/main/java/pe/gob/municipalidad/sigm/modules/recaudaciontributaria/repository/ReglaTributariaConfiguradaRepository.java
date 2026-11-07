package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ReglaTributariaConfigurada;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReglaTributariaConfiguradaRepository extends JpaRepository<ReglaTributariaConfigurada, UUID> {

    List<ReglaTributariaConfigurada> findByConfiguracionIdOrderByOrdenAplicacionAsc(UUID configuracionId);

    boolean existsByConfiguracionIdAndCodigo(UUID configuracionId, String codigo);
}
