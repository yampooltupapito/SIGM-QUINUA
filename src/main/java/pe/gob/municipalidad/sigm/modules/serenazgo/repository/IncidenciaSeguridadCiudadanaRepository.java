package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.IncidenciaSeguridadCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidenciaSeguridadCiudadanaRepository extends JpaRepository<IncidenciaSeguridadCiudadana, UUID>, JpaSpecificationExecutor<IncidenciaSeguridadCiudadana> {

    @Query(value = "SELECT nextval('seq_serenazgo_incidencia')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<IncidenciaSeguridadCiudadana> findByCodigo(String codigo);

    List<IncidenciaSeguridadCiudadana> findByEstado(EstadoIncidenciaSeguridad estado);

    List<IncidenciaSeguridadCiudadana> findByEstadoIn(java.util.Collection<EstadoIncidenciaSeguridad> estados);

    /** Listados acotados (máx. 200, más recientes) para selectores; precargan tipo, sector y zona. */
    @EntityGraph(attributePaths = {"tipoIncidencia", "sector", "zona"})
    List<IncidenciaSeguridadCiudadana> findTop200ByOrderByFechaReporteDesc();

    @EntityGraph(attributePaths = {"tipoIncidencia", "sector", "zona"})
    List<IncidenciaSeguridadCiudadana> findTop200ByEstadoOrderByFechaReporteDesc(EstadoIncidenciaSeguridad estado);

    @EntityGraph(attributePaths = {"tipoIncidencia", "sector", "zona"})
    List<IncidenciaSeguridadCiudadana> findTop200ByEstadoInOrderByFechaReporteDesc(java.util.Collection<EstadoIncidenciaSeguridad> estados);


    Page<IncidenciaSeguridadCiudadana> findByEstado(EstadoIncidenciaSeguridad estado, Pageable pageable);

    List<IncidenciaSeguridadCiudadana> findBySectorId(UUID sectorId);

    List<IncidenciaSeguridadCiudadana> findByPrioridad(PrioridadIncidenciaSeguridad prioridad);

    long countByEstado(EstadoIncidenciaSeguridad estado);

    long countByPrioridad(PrioridadIncidenciaSeguridad prioridad);

    long countByFechaReporteBetween(OffsetDateTime desde, OffsetDateTime hasta);

    boolean existsByCodigo(String codigo);

    // Consulta con filtros y término de búsqueda libre
    @Query("SELECT i FROM IncidenciaSeguridadCiudadana i WHERE " +
            "(:estado IS NULL OR i.estado = :estado) AND " +
            "(:prioridad IS NULL OR i.prioridad = :prioridad) AND " +
            "(:tipoId IS NULL OR i.tipoIncidencia.id = :tipoId) AND " +
            "(:sectorId IS NULL OR i.sector.id = :sectorId) AND " +
            "(:zonaId IS NULL OR i.zona.id = :zonaId) AND " +
            "(:confidencialidad IS NULL OR i.nivelConfidencialidad = :confidencialidad) AND " +
            "(:desde IS NULL OR i.fechaReporte >= :desde) AND " +
            "(:hasta IS NULL OR i.fechaReporte <= :hasta) AND " +
            "(LOWER(i.codigo) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            " LOWER(i.titulo) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            " LOWER(i.descripcion) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            " LOWER(i.ubicacionReferencia) LIKE LOWER(CONCAT('%', :termino, '%')))")
    Page<IncidenciaSeguridadCiudadana> buscarConFiltrosConTermino(
            @Param("estado") EstadoIncidenciaSeguridad estado,
            @Param("prioridad") PrioridadIncidenciaSeguridad prioridad,
            @Param("tipoId") UUID tipoId,
            @Param("sectorId") UUID sectorId,
            @Param("zonaId") UUID zonaId,
            @Param("confidencialidad") NivelConfidencialidad confidencialidad,
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta,
            @Param("termino") String termino,
            Pageable pageable);

    // Consulta con filtros sin término de búsqueda libre (evita bug de lower(bytea) en PostgreSQL)
    @Query("SELECT i FROM IncidenciaSeguridadCiudadana i WHERE " +
            "(:estado IS NULL OR i.estado = :estado) AND " +
            "(:prioridad IS NULL OR i.prioridad = :prioridad) AND " +
            "(:tipoId IS NULL OR i.tipoIncidencia.id = :tipoId) AND " +
            "(:sectorId IS NULL OR i.sector.id = :sectorId) AND " +
            "(:zonaId IS NULL OR i.zona.id = :zonaId) AND " +
            "(:confidencialidad IS NULL OR i.nivelConfidencialidad = :confidencialidad) AND " +
            "(:desde IS NULL OR i.fechaReporte >= :desde) AND " +
            "(:hasta IS NULL OR i.fechaReporte <= :hasta)")
    Page<IncidenciaSeguridadCiudadana> buscarConFiltrosSinTermino(
            @Param("estado") EstadoIncidenciaSeguridad estado,
            @Param("prioridad") PrioridadIncidenciaSeguridad prioridad,
            @Param("tipoId") UUID tipoId,
            @Param("sectorId") UUID sectorId,
            @Param("zonaId") UUID zonaId,
            @Param("confidencialidad") NivelConfidencialidad confidencialidad,
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta,
            Pageable pageable);

    // Detección asistida de posibles duplicados (mismo tipo, fecha cercana y sector coincidente)
    @Query("SELECT i FROM IncidenciaSeguridadCiudadana i WHERE " +
            "i.tipoIncidencia.id = :tipoId AND " +
            "(:sectorId IS NULL OR i.sector.id = :sectorId) AND " +
            "i.fechaReporte >= :desde AND i.fechaReporte <= :hasta AND " +
            "(:excluirId IS NULL OR i.id <> :excluirId)")
    List<IncidenciaSeguridadCiudadana> buscarPosiblesDuplicados(
            @Param("tipoId") UUID tipoId,
            @Param("sectorId") UUID sectorId,
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta,
            @Param("excluirId") UUID excluirId);

    // Listado para exportación por rango de fechas
    @Query("SELECT i FROM IncidenciaSeguridadCiudadana i " +
            "LEFT JOIN FETCH i.tipoIncidencia " +
            "LEFT JOIN FETCH i.sector " +
            "LEFT JOIN FETCH i.zona " +
            "WHERE (:desde IS NULL OR i.fechaReporte >= :desde) AND " +
            "      (:hasta IS NULL OR i.fechaReporte <= :hasta) " +
            "ORDER BY i.fechaReporte DESC")
    List<IncidenciaSeguridadCiudadana> findParaExportacion(
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta);

    /** Bandeja de supervisión: últimas 50 incidencias por estado, con sector precargado. */
    @EntityGraph(attributePaths = {"sector"})
    List<IncidenciaSeguridadCiudadana> findTop50ByEstadoOrderByFechaReporteDesc(EstadoIncidenciaSeguridad estado);
}
