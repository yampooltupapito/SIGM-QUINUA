package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ProgramacionServicioSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramacionServicioSerenazgoRepository extends JpaRepository<ProgramacionServicioSerenazgo, UUID> {

    // Métodos heredados del Prompt 36
    List<ProgramacionServicioSerenazgo> findByFechaServicioOrderByTurnoHoraInicioAsc(LocalDate fechaServicio);

    List<ProgramacionServicioSerenazgo> findByPersonalIdAndFechaServicioBetween(UUID personalId, LocalDate desde, LocalDate hasta);

    Optional<ProgramacionServicioSerenazgo> findByPersonalIdAndFechaServicioAndTurnoId(UUID personalId, LocalDate fechaServicio, UUID turnoId);

    // Nuevos métodos Prompt 40
    Optional<ProgramacionServicioSerenazgo> findByCodigo(String codigo);

    List<ProgramacionServicioSerenazgo> findByFechaServicioBetweenOrderByFechaHoraInicioAsc(LocalDate desde, LocalDate hasta);

    List<ProgramacionServicioSerenazgo> findByFechaServicio(LocalDate fechaServicio);

    @Query("SELECT p FROM ProgramacionServicioSerenazgo p WHERE " +
            "(:fechaDesde IS NULL OR p.fechaServicio >= :fechaDesde) AND " +
            "(:fechaHasta IS NULL OR p.fechaServicio <= :fechaHasta) AND " +
            "(:estado IS NULL OR p.estado = :estado) AND " +
            "(:sectorId IS NULL OR p.sector.id = :sectorId) AND " +
            "(:turnoId IS NULL OR p.turno.id = :turnoId) AND " +
            "(LOWER(p.codigo) LIKE LOWER(CONCAT('%', :termino, '%')) " +
            "OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :termino, '%')) " +
            "OR LOWER(p.sector.nombre) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
            "ORDER BY p.fechaServicio DESC, p.fechaHoraInicio ASC")
    Page<ProgramacionServicioSerenazgo> buscarConFiltrosConTermino(@Param("termino") String termino,
                                                                  @Param("fechaDesde") LocalDate fechaDesde,
                                                                  @Param("fechaHasta") LocalDate fechaHasta,
                                                                  @Param("estado") EstadoProgramacionServicio estado,
                                                                  @Param("sectorId") UUID sectorId,
                                                                  @Param("turnoId") UUID turnoId,
                                                                  Pageable pageable);

    @Query("SELECT p FROM ProgramacionServicioSerenazgo p WHERE " +
            "(:fechaDesde IS NULL OR p.fechaServicio >= :fechaDesde) AND " +
            "(:fechaHasta IS NULL OR p.fechaServicio <= :fechaHasta) AND " +
            "(:estado IS NULL OR p.estado = :estado) AND " +
            "(:sectorId IS NULL OR p.sector.id = :sectorId) AND " +
            "(:turnoId IS NULL OR p.turno.id = :turnoId) " +
            "ORDER BY p.fechaServicio DESC, p.fechaHoraInicio ASC")
    Page<ProgramacionServicioSerenazgo> buscarConFiltrosSinTermino(@Param("fechaDesde") LocalDate fechaDesde,
                                                                  @Param("fechaHasta") LocalDate fechaHasta,
                                                                  @Param("estado") EstadoProgramacionServicio estado,
                                                                  @Param("sectorId") UUID sectorId,
                                                                  @Param("turnoId") UUID turnoId,
                                                                  Pageable pageable);

    // Detección de conflicto horario para personal (regla de intervalos semiabiertos: inicioA < finB && inicioB < finA)
    @Query("SELECT p FROM ProgramacionServicioSerenazgo p " +
            "JOIN p.personalAsignado pa " +
            "WHERE pa.personal.id = :personalId " +
            "AND pa.estadoAsignacion NOT IN (pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAsignacionPersonal.CANCELADO) " +
            "AND p.estado NOT IN (pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio.CANCELADA, pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio.CANCELADO) " +
            "AND p.fechaHoraInicio < :fin " +
            "AND p.fechaHoraFin > :inicio " +
            "AND (:excluirId IS NULL OR p.id != :excluirId)")
    List<ProgramacionServicioSerenazgo> buscarConflictosPersonal(@Param("personalId") UUID personalId,
                                                               @Param("inicio") OffsetDateTime inicio,
                                                               @Param("fin") OffsetDateTime fin,
                                                               @Param("excluirId") UUID excluirId);

    // Detección de conflicto horario para unidad móvil
    @Query("SELECT p FROM ProgramacionServicioSerenazgo p " +
            "JOIN p.unidadesReservadas pu " +
            "WHERE pu.unidad.id = :unidadId " +
            "AND pu.estadoReserva NOT IN (pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoReservaRecurso.LIBERADA, pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoReservaRecurso.CANCELADA) " +
            "AND p.estado NOT IN (pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio.CANCELADA, pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio.CANCELADO) " +
            "AND p.fechaHoraInicio < :fin " +
            "AND p.fechaHoraFin > :inicio " +
            "AND (:excluirId IS NULL OR p.id != :excluirId)")
    List<ProgramacionServicioSerenazgo> buscarConflictosUnidad(@Param("unidadId") UUID unidadId,
                                                             @Param("inicio") OffsetDateTime inicio,
                                                             @Param("fin") OffsetDateTime fin,
                                                             @Param("excluirId") UUID excluirId);

    // Detección de conflicto horario para equipamiento individualizado
    @Query("SELECT p FROM ProgramacionServicioSerenazgo p " +
            "JOIN p.equipamientoReservado pe " +
            "WHERE pe.equipamiento.id = :equipamientoId " +
            "AND pe.estadoReserva NOT IN (pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoReservaRecurso.LIBERADA, pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoReservaRecurso.CANCELADA) " +
            "AND p.estado NOT IN (pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio.CANCELADA, pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio.CANCELADO) " +
            "AND p.fechaHoraInicio < :fin " +
            "AND p.fechaHoraFin > :inicio " +
            "AND (:excluirId IS NULL OR p.id != :excluirId)")
    List<ProgramacionServicioSerenazgo> buscarConflictosEquipamiento(@Param("equipamientoId") UUID equipamientoId,
                                                                   @Param("inicio") OffsetDateTime inicio,
                                                                   @Param("fin") OffsetDateTime fin,
                                                                   @Param("excluirId") UUID excluirId);

    long countByEstado(EstadoProgramacionServicio estado);

    @Query("SELECT COUNT(p) FROM ProgramacionServicioSerenazgo p WHERE p.estado IN :estados")
    long countByEstadoIn(@Param("estados") List<EstadoProgramacionServicio> estados);
}
