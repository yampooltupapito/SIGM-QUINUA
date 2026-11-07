package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.IncidenciaMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.OrigenIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PrioridadIncidencia;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidenciaMercadoRepository extends JpaRepository<IncidenciaMercado, UUID>, JpaSpecificationExecutor<IncidenciaMercado> {

    Optional<IncidenciaMercado> findByCodigoIncidencia(String codigoIncidencia);

    List<IncidenciaMercado> findByPuestoIdOrderByFechaReporteDesc(UUID puestoId);

    List<IncidenciaMercado> findByComercianteIdOrderByFechaReporteDesc(UUID comercianteId);

    List<IncidenciaMercado> findByInspeccionId(UUID inspeccionId);

    @Query("SELECT i FROM IncidenciaMercado i WHERE " +
           "(:mercadoId IS NULL OR i.mercado.id = :mercadoId) AND " +
           "(:sectorId IS NULL OR i.sector.id = :sectorId) AND " +
           "(:pabellonId IS NULL OR i.pabellon.id = :pabellonId) AND " +
           "(:puestoId IS NULL OR i.puesto.id = :puestoId) AND " +
           "(:comercianteId IS NULL OR i.comerciante.id = :comercianteId) AND " +
           "(:tipoId IS NULL OR i.tipoIncidencia.id = :tipoId) AND " +
           "(:origen IS NULL OR i.origen = :origen) AND " +
           "(:prioridad IS NULL OR i.prioridad = :prioridad) AND " +
           "(:estado IS NULL OR i.estado = :estado) AND " +
           "(:responsableId IS NULL OR i.responsable.id = :responsableId) AND " +
           "(:fechaDesde IS NULL OR i.fechaReporte >= :fechaDesde) AND " +
           "(:fechaHasta IS NULL OR i.fechaReporte <= :fechaHasta) AND " +
           "(:inspeccionId IS NULL OR i.inspeccion.id = :inspeccionId) AND " +
           "(:q IS NULL OR LOWER(i.codigoIncidencia) LIKE CAST(:q AS string) OR LOWER(i.descripcion) LIKE CAST(:q AS string))")
    Page<IncidenciaMercado> buscarPaginado(
            @Param("mercadoId") UUID mercadoId,
            @Param("sectorId") UUID sectorId,
            @Param("pabellonId") UUID pabellonId,
            @Param("puestoId") UUID puestoId,
            @Param("comercianteId") UUID comercianteId,
            @Param("tipoId") UUID tipoId,
            @Param("origen") OrigenIncidencia origen,
            @Param("prioridad") PrioridadIncidencia prioridad,
            @Param("estado") EstadoIncidencia estado,
            @Param("responsableId") Long responsableId,
            @Param("fechaDesde") OffsetDateTime fechaDesde,
            @Param("fechaHasta") OffsetDateTime fechaHasta,
            @Param("inspeccionId") UUID inspeccionId,
            @Param("q") String q,
            Pageable pageable);

    long countByEstado(EstadoIncidencia estado);

    long countByPrioridad(PrioridadIncidencia prioridad);

    long countByResponsableIsNullAndEstadoNotIn(Collection<EstadoIncidencia> estadosExcluidos);

    @Query("SELECT COUNT(i) FROM IncidenciaMercado i WHERE i.prioridad = 'CRITICA' AND i.estado NOT IN ('RESUELTA', 'CERRADA', 'DESCARTADA')")
    long countCriticasAbiertas();

    @Query("SELECT COUNT(i) FROM IncidenciaMercado i WHERE i.estado NOT IN ('RESUELTA', 'CERRADA', 'DESCARTADA')")
    long countAbiertas();

    @Query(value = "SELECT nextval('mercados_incidencias_seq')", nativeQuery = true)
    Long obtenerSiguienteCorrelativo();
}
