package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NormaTributariaConfigurada;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidacionNorma;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoNormaTributaria;

import java.util.List;
import java.util.UUID;

@Repository
public interface NormaTributariaConfiguradaRepository extends JpaRepository<NormaTributariaConfigurada, UUID>, JpaSpecificationExecutor<NormaTributariaConfigurada> {

    List<NormaTributariaConfigurada> findByTipoNorma(TipoNormaTributaria tipoNorma);

    List<NormaTributariaConfigurada> findByEstadoValidacion(EstadoValidacionNorma estadoValidacion);

    Page<NormaTributariaConfigurada> findAllByOrderByFechaInicioVigenciaDesc(Pageable pageable);

    List<NormaTributariaConfigurada> findAllByOrderByFechaInicioVigenciaDesc();

    long countByEstadoValidacion(EstadoValidacionNorma estadoValidacion);
}
