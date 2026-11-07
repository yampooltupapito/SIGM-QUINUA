package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ServicioArbitrioMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoServicioArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoServicioArbitrio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicioArbitrioMunicipalRepository extends JpaRepository<ServicioArbitrioMunicipal, UUID>, JpaSpecificationExecutor<ServicioArbitrioMunicipal> {

    Optional<ServicioArbitrioMunicipal> findByCodigoIgnoreCase(String codigo);

    List<ServicioArbitrioMunicipal> findByEstadoOrderByNombreAsc(EstadoServicioArbitrio estado);

    List<ServicioArbitrioMunicipal> findByTipoServicioAndEstado(TipoServicioArbitrio tipoServicio, EstadoServicioArbitrio estado);

    boolean existsByCodigoIgnoreCase(String codigo);
}
