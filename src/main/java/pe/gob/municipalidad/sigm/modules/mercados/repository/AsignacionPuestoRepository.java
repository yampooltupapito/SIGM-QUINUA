package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoAsignacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AsignacionPuestoRepository extends JpaRepository<AsignacionPuesto, UUID> {

    @Query(value = "SELECT nextval('seq_mercados_codigo_asignacion')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<AsignacionPuesto> findByCodigoAsignacion(String codigoAsignacion);

    @Query("SELECT a FROM AsignacionPuesto a " +
           "LEFT JOIN FETCH a.puesto p " +
           "LEFT JOIN FETCH p.mercado m " +
           "LEFT JOIN FETCH p.sector s " +
           "LEFT JOIN FETCH p.pabellon pb " +
           "LEFT JOIN FETCH a.comerciante c " +
           "LEFT JOIN FETCH c.persona per " +
           "LEFT JOIN FETCH c.personaJuridica pj " +
           "LEFT JOIN FETCH a.documentoSustento " +
           "WHERE a.id = :id")
    Optional<AsignacionPuesto> findByIdCompleto(@Param("id") UUID id);

    @Query("SELECT DISTINCT a FROM AsignacionPuesto a " +
           "LEFT JOIN FETCH a.puesto p " +
           "LEFT JOIN FETCH p.mercado m " +
           "LEFT JOIN FETCH a.comerciante c " +
           "LEFT JOIN FETCH c.persona per " +
           "LEFT JOIN FETCH c.personaJuridica pj " +
           "WHERE a.estado IN (:estados) " +
           "ORDER BY a.createdAt DESC")
    List<AsignacionPuesto> findByEstadoInConRelaciones(@Param("estados") List<EstadoAsignacion> estados);

    @Query("SELECT DISTINCT a FROM AsignacionPuesto a " +
           "LEFT JOIN FETCH a.puesto p " +
           "LEFT JOIN FETCH p.mercado m " +
           "LEFT JOIN FETCH a.comerciante c " +
           "LEFT JOIN FETCH c.persona per " +
           "LEFT JOIN FETCH c.personaJuridica pj " +
           "WHERE a.estado = :estado " +
           "ORDER BY a.codigoAsignacion ASC")
    List<AsignacionPuesto> findByEstadoConRelaciones(@Param("estado") EstadoAsignacion estado);

    Optional<AsignacionPuesto> findFirstByPuestoIdAndEstado(UUID puestoId, EstadoAsignacion estado);

    boolean existsByPuestoIdAndEstado(UUID puestoId, EstadoAsignacion estado);

    long countByComercianteIdAndEstado(UUID comercianteId, EstadoAsignacion estado);

    List<AsignacionPuesto> findByPuestoIdOrderByFechaInicioDesc(UUID puestoId);

    List<AsignacionPuesto> findByComercianteIdOrderByFechaInicioDesc(UUID comercianteId);

    Page<AsignacionPuesto> findByComercianteId(UUID comercianteId, Pageable pageable);

    Page<AsignacionPuesto> findByEstado(EstadoAsignacion estado, Pageable pageable);

    List<AsignacionPuesto> findByEstado(EstadoAsignacion estado);

    long countByEstado(EstadoAsignacion estado);

    @Query("SELECT a FROM AsignacionPuesto a " +
           "JOIN a.puesto p " +
           "JOIN a.comerciante c " +
           "LEFT JOIN c.persona per " +
           "LEFT JOIN c.personaJuridica pj " +
           "WHERE (:mercadoId IS NULL OR p.mercado.id = :mercadoId) " +
           "AND (:sectorId IS NULL OR p.sector.id = :sectorId) " +
           "AND (:pabellonId IS NULL OR p.pabellon.id = :pabellonId) " +
           "AND (:comercianteId IS NULL OR c.id = :comercianteId) " +
           "AND (:estado IS NULL OR a.estado = :estado) " +
           "AND (:tipoAsignacion IS NULL OR a.tipoAsignacion = :tipoAsignacion) " +
           "AND (:fechaInicioDesde IS NULL OR a.fechaInicio >= :fechaInicioDesde) " +
           "AND (:fechaInicioHasta IS NULL OR a.fechaInicio <= :fechaInicioHasta) " +
           "AND (LOWER(a.codigoAsignacion) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "   LOWER(p.codigo) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "   LOWER(p.numero) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "   LOWER(c.codigoComerciante) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "   LOWER(per.nombres) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "   LOWER(per.apellidoPaterno) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "   LOWER(per.numeroDocumento) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "   LOWER(pj.razonSocial) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "   LOWER(pj.ruc) LIKE LOWER(CONCAT('%', :termino, '%')))")
    Page<AsignacionPuesto> buscarConFiltrosConTermino(
            @Param("termino") String termino,
            @Param("mercadoId") UUID mercadoId,
            @Param("sectorId") UUID sectorId,
            @Param("pabellonId") UUID pabellonId,
            @Param("comercianteId") UUID comercianteId,
            @Param("estado") EstadoAsignacion estado,
            @Param("tipoAsignacion") TipoAsignacion tipoAsignacion,
            @Param("fechaInicioDesde") LocalDate fechaInicioDesde,
            @Param("fechaInicioHasta") LocalDate fechaInicioHasta,
            Pageable pageable
    );

    @Query("SELECT a FROM AsignacionPuesto a " +
           "JOIN a.puesto p " +
           "JOIN a.comerciante c " +
           "LEFT JOIN c.persona per " +
           "LEFT JOIN c.personaJuridica pj " +
           "WHERE (:mercadoId IS NULL OR p.mercado.id = :mercadoId) " +
           "AND (:sectorId IS NULL OR p.sector.id = :sectorId) " +
           "AND (:pabellonId IS NULL OR p.pabellon.id = :pabellonId) " +
           "AND (:comercianteId IS NULL OR c.id = :comercianteId) " +
           "AND (:estado IS NULL OR a.estado = :estado) " +
           "AND (:tipoAsignacion IS NULL OR a.tipoAsignacion = :tipoAsignacion) " +
           "AND (:fechaInicioDesde IS NULL OR a.fechaInicio >= :fechaInicioDesde) " +
           "AND (:fechaInicioHasta IS NULL OR a.fechaInicio <= :fechaInicioHasta)")
    Page<AsignacionPuesto> buscarConFiltrosSinTermino(
            @Param("mercadoId") UUID mercadoId,
            @Param("sectorId") UUID sectorId,
            @Param("pabellonId") UUID pabellonId,
            @Param("comercianteId") UUID comercianteId,
            @Param("estado") EstadoAsignacion estado,
            @Param("tipoAsignacion") TipoAsignacion tipoAsignacion,
            @Param("fechaInicioDesde") LocalDate fechaInicioDesde,
            @Param("fechaInicioHasta") LocalDate fechaInicioHasta,
            Pageable pageable
    );

    @Query("SELECT a FROM AsignacionPuesto a " +
           "WHERE a.puesto.mercado.id = :mercadoId " +
           "AND (:estado IS NULL OR a.estado = :estado)")
    Page<AsignacionPuesto> findByMercadoIdAndEstado(
            @Param("mercadoId") UUID mercadoId,
            @Param("estado") EstadoAsignacion estado,
            Pageable pageable
    );

    long countByPuestoMercadoIdAndEstado(UUID mercadoId, EstadoAsignacion estado);

    @Query("SELECT COUNT(a) FROM AsignacionPuesto a WHERE a.puesto.sector.id = :sectorId AND a.estado = :estado")
    long countByPuestoSectorIdAndEstado(@Param("sectorId") UUID sectorId, @Param("estado") EstadoAsignacion estado);

    @Query("SELECT COUNT(a) FROM AsignacionPuesto a WHERE a.puesto.pabellon.id = :pabellonId AND a.estado = :estado")
    long countByPuestoPabellonIdAndEstado(@Param("pabellonId") UUID pabellonId, @Param("estado") EstadoAsignacion estado);

    @Query("SELECT a FROM AsignacionPuesto a " +
           "WHERE a.estado = 'VIGENTE' " +
           "AND a.fechaFin IS NOT NULL " +
           "AND a.fechaFin BETWEEN :desde AND :hasta " +
           "ORDER BY a.fechaFin ASC")
    List<AsignacionPuesto> findAsignacionesProximasAVencer(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );
}
