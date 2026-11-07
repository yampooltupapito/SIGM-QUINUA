package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpedienteRepository extends JpaRepository<Expediente, Long>, JpaSpecificationExecutor<Expediente> {

    Optional<Expediente> findByUuid(UUID uuid);

    Optional<Expediente> findByNumeroExpediente(String numeroExpediente);

    @Query("SELECT DISTINCT e FROM Expediente e " +
           "LEFT JOIN FETCH e.tipoTramite " +
           "LEFT JOIN FETCH e.solicitante " +
           "LEFT JOIN FETCH e.oficinaResponsable " +
           "LEFT JOIN FETCH e.documentos " +
           "WHERE e.id = :id")
    Optional<Expediente> findByIdWithDetalles(@Param("id") Long id);

    @Query("SELECT DISTINCT e FROM Expediente e " +
           "LEFT JOIN FETCH e.tipoTramite " +
           "LEFT JOIN FETCH e.solicitante " +
           "LEFT JOIN FETCH e.oficinaResponsable " +
           "LEFT JOIN FETCH e.documentos " +
           "WHERE e.uuid = :uuid")
    Optional<Expediente> findByUuidWithDetalles(@Param("uuid") UUID uuid);

    @Query("SELECT e FROM Expediente e WHERE " +
           "(:estado IS NULL OR e.estadoActual = :estado) AND " +
           "(:oficinaId IS NULL OR e.oficinaResponsable.id = :oficinaId) AND " +
           "(:tipoTramiteId IS NULL OR e.tipoTramite.id = :tipoTramiteId) AND " +
           "(:solicitanteId IS NULL OR e.solicitante.id = :solicitanteId) AND " +
           "(:anio IS NULL OR e.anio = :anio) AND " +
           "(:termino IS NULL OR " +
           "  LOWER(COALESCE(e.numeroExpediente, '')) LIKE :termino OR " +
           "  LOWER(e.asunto) LIKE :termino OR " +
           "  (e.descripcion IS NOT NULL AND LOWER(e.descripcion) LIKE :termino)) " +
           "ORDER BY e.createdAt DESC")
    Page<Expediente> buscarConFiltros(
            @Param("estado") EstadoExpediente estado,
            @Param("oficinaId") Long oficinaId,
            @Param("tipoTramiteId") Long tipoTramiteId,
            @Param("solicitanteId") Long solicitanteId,
            @Param("anio") Integer anio,
            @Param("termino") String termino,
            Pageable pageable
    );

    @Query("SELECT COUNT(e) FROM Expediente e WHERE (:oficinaId IS NULL OR e.oficinaResponsable.id = :oficinaId)")
    long countByOficina(@Param("oficinaId") Long oficinaId);

    @Query("SELECT COUNT(e) FROM Expediente e WHERE e.estadoActual = :estado AND (:oficinaId IS NULL OR e.oficinaResponsable.id = :oficinaId)")
    long countByEstadoAndOficina(@Param("estado") EstadoExpediente estado, @Param("oficinaId") Long oficinaId);

    @Query(value = "SELECT e FROM Expediente e " +
           "LEFT JOIN FETCH e.tipoTramite " +
           "LEFT JOIN FETCH e.solicitante " +
           "LEFT JOIN FETCH e.oficinaResponsable " +
           "WHERE " +
           "(:estado IS NULL OR e.estadoActual = :estado) AND " +
           "(:oficinaId IS NULL OR e.oficinaResponsable.id = :oficinaId) AND " +
           "(:tipoTramiteId IS NULL OR e.tipoTramite.id = :tipoTramiteId) AND " +
           "(:prioridad IS NULL OR e.prioridad = :prioridad) AND " +
           "(:canalIngreso IS NULL OR e.canalIngreso = :canalIngreso) AND " +
           "((e.fechaPresentacion IS NOT NULL AND e.fechaPresentacion >= :fechaDesde AND e.fechaPresentacion <= :fechaHasta) OR " +
           " (e.fechaPresentacion IS NULL AND e.createdAt >= :fechaDesde AND e.createdAt <= :fechaHasta)) AND " +
           "(:termino IS NULL OR " +
           "  LOWER(COALESCE(e.numeroExpediente, '')) LIKE :termino OR " +
           "  LOWER(e.asunto) LIKE :termino OR " +
           "  LOWER(COALESCE(e.solicitante.nombres, '')) LIKE :termino OR " +
           "  LOWER(COALESCE(e.solicitante.apellidoPaterno, '')) LIKE :termino OR " +
           "  LOWER(COALESCE(e.solicitante.numeroDocumento, '')) LIKE :termino)",
           countQuery = "SELECT COUNT(e) FROM Expediente e " +
           "LEFT JOIN e.solicitante " +
           "WHERE " +
           "(:estado IS NULL OR e.estadoActual = :estado) AND " +
           "(:oficinaId IS NULL OR e.oficinaResponsable.id = :oficinaId) AND " +
           "(:tipoTramiteId IS NULL OR e.tipoTramite.id = :tipoTramiteId) AND " +
           "(:prioridad IS NULL OR e.prioridad = :prioridad) AND " +
           "(:canalIngreso IS NULL OR e.canalIngreso = :canalIngreso) AND " +
           "((e.fechaPresentacion IS NOT NULL AND e.fechaPresentacion >= :fechaDesde AND e.fechaPresentacion <= :fechaHasta) OR " +
           " (e.fechaPresentacion IS NULL AND e.createdAt >= :fechaDesde AND e.createdAt <= :fechaHasta)) AND " +
           "(:termino IS NULL OR " +
           "  LOWER(COALESCE(e.numeroExpediente, '')) LIKE :termino OR " +
           "  LOWER(e.asunto) LIKE :termino OR " +
           "  LOWER(COALESCE(e.solicitante.nombres, '')) LIKE :termino OR " +
           "  LOWER(COALESCE(e.solicitante.apellidoPaterno, '')) LIKE :termino OR " +
           "  LOWER(COALESCE(e.solicitante.numeroDocumento, '')) LIKE :termino)")
    Page<Expediente> buscarBandejaConFiltros(
            @Param("estado") EstadoExpediente estado,
            @Param("oficinaId") Long oficinaId,
            @Param("tipoTramiteId") Long tipoTramiteId,
            @Param("prioridad") pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.PrioridadExpediente prioridad,
            @Param("canalIngreso") pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.CanalIngreso canalIngreso,
            @Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @Param("fechaHasta") java.time.OffsetDateTime fechaHasta,
            @Param("termino") String termino,
            Pageable pageable
    );

    long countByEstadoActual(EstadoExpediente estadoActual);

    long countByOficinaResponsableId(Long oficinaId);

    long countByOficinaResponsableIdAndEstadoActual(Long oficinaId, EstadoExpediente estadoActual);

    Page<Expediente> findByResponsableActualId(Long responsableActualId, Pageable pageable);

    Page<Expediente> findByOficinaResponsableId(Long oficinaId, Pageable pageable);

    long countByOficinaResponsableIdAndResponsableActualIsNull(Long oficinaId);

    long countByOficinaResponsableIdAndResponsableActualIsNotNull(Long oficinaId);

    long countByResponsableActualId(Long responsableActualId);

    @Query(value = "SELECT e FROM Expediente e " +
           "LEFT JOIN FETCH e.tipoTramite " +
           "LEFT JOIN FETCH e.solicitante " +
           "LEFT JOIN FETCH e.oficinaResponsable " +
           "LEFT JOIN FETCH e.responsableActual " +
           "WHERE " +
           "(:responsableId IS NULL OR e.responsableActual.id = :responsableId) AND " +
           "(:oficinaId IS NULL OR e.oficinaResponsable.id = :oficinaId) AND " +
           "(:estado IS NULL OR e.estadoActual = :estado) AND " +
           "(:tipoTramiteId IS NULL OR e.tipoTramite.id = :tipoTramiteId) AND " +
           "(:termino IS NULL OR " +
           "  LOWER(COALESCE(e.numeroExpediente, '')) LIKE :termino OR " +
           "  LOWER(e.asunto) LIKE :termino OR " +
           "  LOWER(COALESCE(e.solicitante.nombres, '')) LIKE :termino OR " +
           "  LOWER(COALESCE(e.solicitante.apellidoPaterno, '')) LIKE :termino OR " +
           "  LOWER(COALESCE(e.solicitante.numeroDocumento, '')) LIKE :termino)",
           countQuery = "SELECT COUNT(e) FROM Expediente e " +
           "LEFT JOIN e.solicitante " +
           "WHERE " +
           "(:responsableId IS NULL OR e.responsableActual.id = :responsableId) AND " +
           "(:oficinaId IS NULL OR e.oficinaResponsable.id = :oficinaId) AND " +
           "(:estado IS NULL OR e.estadoActual = :estado) AND " +
           "(:tipoTramiteId IS NULL OR e.tipoTramite.id = :tipoTramiteId) AND " +
           "(:termino IS NULL OR " +
           "  LOWER(COALESCE(e.numeroExpediente, '')) LIKE :termino OR " +
           "  LOWER(e.asunto) LIKE :termino OR " +
           "  LOWER(COALESCE(e.solicitante.nombres, '')) LIKE :termino OR " +
           "  LOWER(COALESCE(e.solicitante.apellidoPaterno, '')) LIKE :termino OR " +
           "  LOWER(COALESCE(e.solicitante.numeroDocumento, '')) LIKE :termino)")
    Page<Expediente> buscarAtencionConFiltros(
            @Param("responsableId") Long responsableId,
            @Param("oficinaId") Long oficinaId,
            @Param("estado") EstadoExpediente estado,
            @Param("tipoTramiteId") Long tipoTramiteId,
            @Param("termino") String termino,
            Pageable pageable
    );

    @Query("SELECT e.estadoActual, COUNT(e) FROM Expediente e WHERE " +
           "(:oficinaId IS NULL OR e.oficinaResponsable.id = :oficinaId) AND " +
           "((e.fechaPresentacion IS NOT NULL AND e.fechaPresentacion >= :fechaDesde AND e.fechaPresentacion <= :fechaHasta) OR " +
           " (e.fechaPresentacion IS NULL AND e.createdAt >= :fechaDesde AND e.createdAt <= :fechaHasta)) " +
           "GROUP BY e.estadoActual")
    java.util.List<Object[]> countGroupByEstado(
            @Param("oficinaId") Long oficinaId,
            @Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );

    @Query("SELECT e.oficinaResponsable.id, e.oficinaResponsable.nombre, COUNT(e) FROM Expediente e WHERE " +
           "((e.fechaPresentacion IS NOT NULL AND e.fechaPresentacion >= :fechaDesde AND e.fechaPresentacion <= :fechaHasta) OR " +
           " (e.fechaPresentacion IS NULL AND e.createdAt >= :fechaDesde AND e.createdAt <= :fechaHasta)) " +
           "GROUP BY e.oficinaResponsable.id, e.oficinaResponsable.nombre")
    java.util.List<Object[]> countGroupByOficina(
            @Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );

    @Query("SELECT e.tipoTramite.id, e.tipoTramite.nombre, e.tipoTramite.codigo, COUNT(e) FROM Expediente e WHERE " +
           "(:oficinaId IS NULL OR e.oficinaResponsable.id = :oficinaId) AND " +
           "((e.fechaPresentacion IS NOT NULL AND e.fechaPresentacion >= :fechaDesde AND e.fechaPresentacion <= :fechaHasta) OR " +
           " (e.fechaPresentacion IS NULL AND e.createdAt >= :fechaDesde AND e.createdAt <= :fechaHasta)) " +
           "GROUP BY e.tipoTramite.id, e.tipoTramite.nombre, e.tipoTramite.codigo")
    java.util.List<Object[]> countGroupByTipoTramite(
            @Param("oficinaId") Long oficinaId,
            @Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );

    @Query("SELECT e FROM Expediente e " +
           "LEFT JOIN FETCH e.tipoTramite " +
           "LEFT JOIN FETCH e.oficinaResponsable " +
           "WHERE " +
           "(:oficinaId IS NULL OR e.oficinaResponsable.id = :oficinaId) AND " +
           "((e.fechaPresentacion IS NOT NULL AND e.fechaPresentacion >= :fechaDesde AND e.fechaPresentacion <= :fechaHasta) OR " +
           " (e.fechaPresentacion IS NULL AND e.createdAt >= :fechaDesde AND e.createdAt <= :fechaHasta))")
    java.util.List<Expediente> findParaIndicadores(
            @Param("oficinaId") Long oficinaId,
            @Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );

    @Query("SELECT COUNT(e) FROM Expediente e WHERE " +
           "(:oficinaId IS NULL OR e.oficinaResponsable.id = :oficinaId) AND " +
           "((e.fechaPresentacion IS NOT NULL AND e.fechaPresentacion >= :fechaDesde AND e.fechaPresentacion <= :fechaHasta) OR " +
           " (e.fechaPresentacion IS NULL AND e.createdAt >= :fechaDesde AND e.createdAt <= :fechaHasta))")
    long countPresentadosEnRango(
            @Param("oficinaId") Long oficinaId,
            @Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );

    @Query("SELECT COUNT(e) FROM Expediente e WHERE " +
           "(:oficinaId IS NULL OR e.oficinaResponsable.id = :oficinaId) AND " +
           "e.fechaCierre IS NOT NULL AND e.fechaCierre >= :fechaDesde AND e.fechaCierre <= :fechaHasta")
    long countResueltosEnRango(
            @Param("oficinaId") Long oficinaId,
            @Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );

    @Query("SELECT COUNT(DISTINCT e) FROM Expediente e " +
           "JOIN e.observaciones o WHERE " +
           "(:oficinaId IS NULL OR e.oficinaResponsable.id = :oficinaId) AND " +
           "o.fechaRegistro >= :fechaDesde AND o.fechaRegistro <= :fechaHasta")
    long countObservadosEnRango(
            @Param("oficinaId") Long oficinaId,
            @Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );

    long countByTipoTramiteId(Long tipoTramiteId);

    long countByTipoTramiteIdAndEstadoActualNotIn(Long tipoTramiteId, java.util.Collection<EstadoExpediente> estados);
}
