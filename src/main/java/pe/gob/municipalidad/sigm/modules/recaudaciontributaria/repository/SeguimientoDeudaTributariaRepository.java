package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SeguimientoDeudaTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ClasificacionDeudaTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSeguimientoDeuda;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeguimientoDeudaTributariaRepository
        extends JpaRepository<SeguimientoDeudaTributaria, UUID>, JpaSpecificationExecutor<SeguimientoDeudaTributaria> {

    Optional<SeguimientoDeudaTributaria> findByObligacionId(UUID obligacionId);

    List<SeguimientoDeudaTributaria> findByContribuyenteIdOrderByFechaInicioDesc(UUID contribuyenteId);

    List<SeguimientoDeudaTributaria> findByResponsableIdOrderByFechaUltimaRevisionDesc(Long responsableId);

    long countByClasificacion(ClasificacionDeudaTributaria clasificacion);

    long countByEstadoSeguimiento(EstadoSeguimientoDeuda estadoSeguimiento);
}
