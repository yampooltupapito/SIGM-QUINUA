package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AutorizacionComercial;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AutorizacionComercialRepository extends JpaRepository<AutorizacionComercial, UUID> {

    @Query(value = "SELECT nextval('seq_mercados_codigo_autorizacion')", nativeQuery = true)
    Long obtenerSiguienteCorrelativo();

    Optional<AutorizacionComercial> findByNumeroAutorizacion(String numeroAutorizacion);

    Optional<AutorizacionComercial> findByCodigoAutorizacion(String codigoAutorizacion);

    Optional<AutorizacionComercial> findByCodigoSeguro(String codigoSeguro);

    List<AutorizacionComercial> findByAsignacionIdOrderByCreatedAtDesc(UUID asignacionId);

    List<AutorizacionComercial> findByComercianteId(UUID comercianteId);

    Page<AutorizacionComercial> findByComercianteId(UUID comercianteId, Pageable pageable);

    long countByComercianteIdAndEstadoIn(UUID comercianteId, java.util.Collection<EstadoAutorizacion> estados);

    Page<AutorizacionComercial> findByEstado(EstadoAutorizacion estado, Pageable pageable);

    List<AutorizacionComercial> findByPuestoIdOrderByCreatedAtDesc(UUID puestoId);

    @Query("SELECT a FROM AutorizacionComercial a " +
           "JOIN FETCH a.comerciante com " +
           "LEFT JOIN FETCH com.persona " +
           "LEFT JOIN FETCH com.personaJuridica " +
           "LEFT JOIN FETCH a.puesto p " +
           "LEFT JOIN FETCH p.mercado m " +
           "WHERE a.id = :id")
    Optional<AutorizacionComercial> findByIdConRelaciones(@Param("id") UUID id);

    @Query("SELECT a FROM AutorizacionComercial a " +
           "JOIN FETCH a.comerciante com " +
           "LEFT JOIN FETCH com.persona " +
           "LEFT JOIN FETCH com.personaJuridica " +
           "LEFT JOIN FETCH a.puesto p " +
           "LEFT JOIN FETCH p.mercado m " +
           "WHERE a.codigoSeguro = :codigoSeguro")
    Optional<AutorizacionComercial> findByCodigoSeguroConRelaciones(@Param("codigoSeguro") String codigoSeguro);

    @Query(value = "SELECT * FROM mercados_autorizaciones a " +
                   "WHERE a.puesto_id = :puestoId " +
                   "AND a.estado IN ('VIGENTE', 'EMITIDA') " +
                   "AND (CAST(:excluirId AS uuid) IS NULL OR a.id != CAST(:excluirId AS uuid)) " +
                   "AND (a.fecha_fin IS NULL OR :fechaInicio <= a.fecha_fin) " +
                   "AND (CAST(:fechaFin AS date) IS NULL OR a.fecha_inicio <= CAST(:fechaFin AS date))",
           nativeQuery = true)
    List<AutorizacionComercial> detectarSolapamientoVigente(
            @Param("puestoId") UUID puestoId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("excluirId") UUID excluirId);

    @Query(value = "SELECT a FROM AutorizacionComercial a " +
           "JOIN FETCH a.comerciante com " +
           "LEFT JOIN FETCH com.persona " +
           "LEFT JOIN FETCH com.personaJuridica " +
           "LEFT JOIN FETCH a.puesto p " +
           "LEFT JOIN FETCH p.mercado m " +
           "WHERE (cast(:mercadoId as uuid) IS NULL OR (p IS NOT NULL AND m.id = :mercadoId)) " +
           "AND (:estado IS NULL OR a.estado = :estado) " +
           "AND (:tipoAutorizacion IS NULL OR a.tipoAutorizacion = :tipoAutorizacion) " +
           "AND (cast(:fechaDesde as date) IS NULL OR (a.fechaInicio IS NOT NULL AND a.fechaInicio >= :fechaDesde)) " +
           "AND (cast(:fechaHasta as date) IS NULL OR (a.fechaInicio IS NOT NULL AND a.fechaInicio <= :fechaHasta)) " +
           "AND (LOWER(COALESCE(a.codigoAutorizacion, a.numeroAutorizacion, '')) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "     OR (p IS NOT NULL AND LOWER(p.codigo) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
           "     OR (com.persona IS NOT NULL AND (LOWER(com.persona.nombres) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(com.persona.apellidoPaterno) LIKE LOWER(CONCAT('%', :termino, '%')) OR com.persona.numeroDocumento LIKE CONCAT('%', :termino, '%'))) " +
           "     OR (com.personaJuridica IS NOT NULL AND (LOWER(com.personaJuridica.razonSocial) LIKE LOWER(CONCAT('%', :termino, '%')) OR com.personaJuridica.ruc LIKE CONCAT('%', :termino, '%'))))",
           countQuery = "SELECT COUNT(a) FROM AutorizacionComercial a " +
           "JOIN a.comerciante com " +
           "LEFT JOIN a.puesto p " +
           "LEFT JOIN p.mercado m " +
           "WHERE (cast(:mercadoId as uuid) IS NULL OR (p IS NOT NULL AND m.id = :mercadoId)) " +
           "AND (:estado IS NULL OR a.estado = :estado) " +
           "AND (:tipoAutorizacion IS NULL OR a.tipoAutorizacion = :tipoAutorizacion) " +
           "AND (cast(:fechaDesde as date) IS NULL OR (a.fechaInicio IS NOT NULL AND a.fechaInicio >= :fechaDesde)) " +
           "AND (cast(:fechaHasta as date) IS NULL OR (a.fechaInicio IS NOT NULL AND a.fechaInicio <= :fechaHasta)) " +
           "AND (LOWER(COALESCE(a.codigoAutorizacion, a.numeroAutorizacion, '')) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "     OR (p IS NOT NULL AND LOWER(p.codigo) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
           "     OR (com.persona IS NOT NULL AND (LOWER(com.persona.nombres) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(com.persona.apellidoPaterno) LIKE LOWER(CONCAT('%', :termino, '%')) OR com.persona.numeroDocumento LIKE CONCAT('%', :termino, '%'))) " +
           "     OR (com.personaJuridica IS NOT NULL AND (LOWER(com.personaJuridica.razonSocial) LIKE LOWER(CONCAT('%', :termino, '%')) OR com.personaJuridica.ruc LIKE CONCAT('%', :termino, '%'))))")
    Page<AutorizacionComercial> buscarConFiltrosConTermino(
            @Param("termino") String termino,
            @Param("mercadoId") UUID mercadoId,
            @Param("estado") EstadoAutorizacion estado,
            @Param("tipoAutorizacion") String tipoAutorizacion,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable);

    @Query(value = "SELECT a FROM AutorizacionComercial a " +
           "JOIN FETCH a.comerciante com " +
           "LEFT JOIN FETCH com.persona " +
           "LEFT JOIN FETCH com.personaJuridica " +
           "LEFT JOIN FETCH a.puesto p " +
           "LEFT JOIN FETCH p.mercado m " +
           "WHERE (cast(:mercadoId as uuid) IS NULL OR (p IS NOT NULL AND m.id = :mercadoId)) " +
           "AND (:estado IS NULL OR a.estado = :estado) " +
           "AND (:tipoAutorizacion IS NULL OR a.tipoAutorizacion = :tipoAutorizacion) " +
           "AND (cast(:fechaDesde as date) IS NULL OR (a.fechaInicio IS NOT NULL AND a.fechaInicio >= :fechaDesde)) " +
           "AND (cast(:fechaHasta as date) IS NULL OR (a.fechaInicio IS NOT NULL AND a.fechaInicio <= :fechaHasta))",
           countQuery = "SELECT COUNT(a) FROM AutorizacionComercial a " +
           "LEFT JOIN a.puesto p " +
           "LEFT JOIN p.mercado m " +
           "WHERE (cast(:mercadoId as uuid) IS NULL OR (p IS NOT NULL AND m.id = :mercadoId)) " +
           "AND (:estado IS NULL OR a.estado = :estado) " +
           "AND (:tipoAutorizacion IS NULL OR a.tipoAutorizacion = :tipoAutorizacion) " +
           "AND (cast(:fechaDesde as date) IS NULL OR (a.fechaInicio IS NOT NULL AND a.fechaInicio >= :fechaDesde)) " +
           "AND (cast(:fechaHasta as date) IS NULL OR (a.fechaInicio IS NOT NULL AND a.fechaInicio <= :fechaHasta))")
    Page<AutorizacionComercial> buscarConFiltrosSinTermino(
            @Param("mercadoId") UUID mercadoId,
            @Param("estado") EstadoAutorizacion estado,
            @Param("tipoAutorizacion") String tipoAutorizacion,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable);

    @Query("SELECT a FROM AutorizacionComercial a " +
           "JOIN FETCH a.comerciante com " +
           "LEFT JOIN FETCH a.puesto p " +
           "LEFT JOIN FETCH p.mercado m " +
           "WHERE a.estado IN ('VIGENTE', 'EMITIDA') " +
           "AND a.fechaFin IS NOT NULL " +
           "AND a.fechaFin BETWEEN :hoy AND :fechaLimite " +
           "ORDER BY a.fechaFin ASC")
    List<AutorizacionComercial> consultarProximosVencimientos(
            @Param("hoy") LocalDate hoy,
            @Param("fechaLimite") LocalDate fechaLimite);

    @Query("SELECT a FROM AutorizacionComercial a " +
           "JOIN FETCH a.comerciante com " +
           "LEFT JOIN FETCH a.puesto p " +
           "LEFT JOIN FETCH p.mercado m " +
           "WHERE a.estado IN ('VIGENTE', 'EMITIDA') " +
           "AND a.fechaFin IS NOT NULL " +
           "AND a.fechaFin < :hoy " +
           "ORDER BY a.fechaFin ASC")
    List<AutorizacionComercial> consultarVencidos(@Param("hoy") LocalDate hoy);

    long countByEstado(EstadoAutorizacion estado);

    @Query("SELECT COUNT(a) FROM AutorizacionComercial a WHERE a.puesto.mercado.id = :mercadoId AND a.estado = :estado")
    long countByMercadoIdAndEstado(@Param("mercadoId") UUID mercadoId, @Param("estado") EstadoAutorizacion estado);
}
