package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ContratoOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContratoOcupacionRepository extends JpaRepository<ContratoOcupacion, UUID> {

    @Query(value = "SELECT nextval('seq_mercados_codigo_contrato')", nativeQuery = true)
    Long obtenerSiguienteCorrelativo();

    Optional<ContratoOcupacion> findByCodigoContrato(String codigoContrato);

    Optional<ContratoOcupacion> findByCodigoSeguro(String codigoSeguro);

    List<ContratoOcupacion> findByAsignacionIdOrderByCreatedAtDesc(UUID asignacionId);

    List<ContratoOcupacion> findByComercianteIdOrderByCreatedAtDesc(UUID comercianteId);

    List<ContratoOcupacion> findByComercianteId(UUID comercianteId);

    Page<ContratoOcupacion> findByComercianteId(UUID comercianteId, Pageable pageable);

    long countByComercianteIdAndEstado(UUID comercianteId, EstadoContrato estado);

    List<ContratoOcupacion> findByPuestoIdOrderByCreatedAtDesc(UUID puestoId);

    @Query("SELECT c FROM ContratoOcupacion c " +
           "JOIN FETCH c.comerciante com " +
           "JOIN FETCH c.puesto p " +
           "JOIN FETCH p.mercado m " +
           "WHERE c.id = :id")
    Optional<ContratoOcupacion> findByIdConRelaciones(@Param("id") UUID id);

    @Query("SELECT c FROM ContratoOcupacion c " +
           "JOIN FETCH c.comerciante com " +
           "JOIN FETCH c.puesto p " +
           "JOIN FETCH p.mercado m " +
           "WHERE c.codigoSeguro = :codigoSeguro")
    Optional<ContratoOcupacion> findByCodigoSeguroConRelaciones(@Param("codigoSeguro") String codigoSeguro);

    @Query(value = "SELECT * FROM mercados_contratos c " +
                   "WHERE c.puesto_id = :puestoId " +
                   "AND c.estado = 'VIGENTE' " +
                   "AND (CAST(:excluirContratoId AS uuid) IS NULL OR c.id != CAST(:excluirContratoId AS uuid)) " +
                   "AND (c.fecha_fin IS NULL OR :fechaInicio <= c.fecha_fin) " +
                   "AND (CAST(:fechaFin AS date) IS NULL OR c.fecha_inicio <= CAST(:fechaFin AS date))",
           nativeQuery = true)
    List<ContratoOcupacion> detectarSolapamientoVigente(
            @Param("puestoId") UUID puestoId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("excluirContratoId") UUID excluirContratoId);

    @Query("SELECT c FROM ContratoOcupacion c " +
           "JOIN FETCH c.comerciante com " +
           "JOIN FETCH c.puesto p " +
           "JOIN FETCH p.mercado m " +
           "WHERE (cast(:mercadoId as uuid) IS NULL OR p.mercado.id = :mercadoId) " +
           "AND (:estado IS NULL OR c.estado = :estado) " +
           "AND (:tipoContratoCodigo IS NULL OR c.tipoContratoCodigo = :tipoContratoCodigo) " +
           "AND (cast(:fechaDesde as date) IS NULL OR c.fechaInicio >= :fechaDesde) " +
           "AND (cast(:fechaHasta as date) IS NULL OR c.fechaInicio <= :fechaHasta) " +
           "AND (LOWER(c.codigoContrato) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "     OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "     OR (com.persona IS NOT NULL AND (LOWER(com.persona.nombres) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(com.persona.apellidoPaterno) LIKE LOWER(CONCAT('%', :termino, '%')) OR com.persona.numeroDocumento LIKE CONCAT('%', :termino, '%'))) " +
           "     OR (com.personaJuridica IS NOT NULL AND (LOWER(com.personaJuridica.razonSocial) LIKE LOWER(CONCAT('%', :termino, '%')) OR com.personaJuridica.ruc LIKE CONCAT('%', :termino, '%'))))")
    Page<ContratoOcupacion> buscarConFiltrosConTermino(
            @Param("termino") String termino,
            @Param("mercadoId") UUID mercadoId,
            @Param("estado") EstadoContrato estado,
            @Param("tipoContratoCodigo") String tipoContratoCodigo,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable);

    @Query("SELECT c FROM ContratoOcupacion c " +
           "JOIN FETCH c.comerciante com " +
           "JOIN FETCH c.puesto p " +
           "JOIN FETCH p.mercado m " +
           "WHERE (cast(:mercadoId as uuid) IS NULL OR p.mercado.id = :mercadoId) " +
           "AND (:estado IS NULL OR c.estado = :estado) " +
           "AND (:tipoContratoCodigo IS NULL OR c.tipoContratoCodigo = :tipoContratoCodigo) " +
           "AND (cast(:fechaDesde as date) IS NULL OR c.fechaInicio >= :fechaDesde) " +
           "AND (cast(:fechaHasta as date) IS NULL OR c.fechaInicio <= :fechaHasta)")
    Page<ContratoOcupacion> buscarConFiltrosSinTermino(
            @Param("mercadoId") UUID mercadoId,
            @Param("estado") EstadoContrato estado,
            @Param("tipoContratoCodigo") String tipoContratoCodigo,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable);

    @Query("SELECT c FROM ContratoOcupacion c " +
           "JOIN FETCH c.comerciante com " +
           "JOIN FETCH c.puesto p " +
           "JOIN FETCH p.mercado m " +
           "WHERE c.estado = 'VIGENTE' " +
           "AND c.fechaFin IS NOT NULL " +
           "AND c.fechaFin BETWEEN :hoy AND :fechaLimite " +
           "ORDER BY c.fechaFin ASC")
    List<ContratoOcupacion> consultarProximosVencimientos(
            @Param("hoy") LocalDate hoy,
            @Param("fechaLimite") LocalDate fechaLimite);

    @Query("SELECT c FROM ContratoOcupacion c " +
           "JOIN FETCH c.comerciante com " +
           "JOIN FETCH c.puesto p " +
           "JOIN FETCH p.mercado m " +
           "WHERE c.estado = 'VIGENTE' " +
           "AND c.fechaFin IS NOT NULL " +
           "AND c.fechaFin < :hoy " +
           "ORDER BY c.fechaFin ASC")
    List<ContratoOcupacion> consultarVencidos(@Param("hoy") LocalDate hoy);

    long countByEstado(EstadoContrato estado);

    @Query("SELECT COUNT(c) FROM ContratoOcupacion c WHERE c.puesto.mercado.id = :mercadoId AND c.estado = :estado")
    long countByMercadoIdAndEstado(@Param("mercadoId") UUID mercadoId, @Param("estado") EstadoContrato estado);
}
