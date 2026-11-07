package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.SolicitudGestionOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoSolicitudGestion;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolicitudGestionOcupacionRepository extends JpaRepository<SolicitudGestionOcupacion, UUID>, JpaSpecificationExecutor<SolicitudGestionOcupacion> {

    Optional<SolicitudGestionOcupacion> findByCodigoSolicitud(String codigoSolicitud);

    Optional<SolicitudGestionOcupacion> findByTokenVerificacionQr(String tokenVerificacionQr);

    List<SolicitudGestionOcupacion> findByPuestoIdOrderByFechaPresentacionDesc(UUID puestoId);

    List<SolicitudGestionOcupacion> findByComercianteActualIdOrderByFechaPresentacionDesc(UUID comercianteId);

    Page<SolicitudGestionOcupacion> findByComercianteActualId(UUID comercianteId, Pageable pageable);

    long countByComercianteActualIdAndEstadoIn(UUID comercianteId, Collection<EstadoSolicitudGestion> estados);

    List<SolicitudGestionOcupacion> findByComerciantePropuestoIdOrderByFechaPresentacionDesc(UUID comercianteId);

    List<SolicitudGestionOcupacion> findByAsignacionOrigenId(UUID asignacionId);

    boolean existsByPuestoIdAndEstadoIn(UUID puestoId, Collection<EstadoSolicitudGestion> estados);

    boolean existsByAsignacionOrigenIdAndEstadoIn(UUID asignacionId, Collection<EstadoSolicitudGestion> estados);

    long countByEstado(EstadoSolicitudGestion estado);

    long countByTipoSolicitud(TipoSolicitudGestion tipoSolicitud);

    long countByTipoSolicitudAndEstado(TipoSolicitudGestion tipoSolicitud, EstadoSolicitudGestion estado);

    @Query(value = "SELECT nextval('seq_mercados_solicitud_gestion')", nativeQuery = true)
    Long obtenerSiguienteCorrelativo();
}
