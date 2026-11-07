package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PresuntaInfraccionMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoEvaluacionInfraccion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PresuntaInfraccionMercadoRepository extends JpaRepository<PresuntaInfraccionMercado, UUID> {

    Optional<PresuntaInfraccionMercado> findByCodigo(String codigo);

    List<PresuntaInfraccionMercado> findByIncidenciaId(UUID incidenciaId);

    List<PresuntaInfraccionMercado> findByInspeccionId(UUID inspeccionId);

    List<PresuntaInfraccionMercado> findByComercianteIdOrderByFechaDeteccionDesc(UUID comercianteId);

    List<PresuntaInfraccionMercado> findByPuestoIdOrderByFechaDeteccionDesc(UUID puestoId);

    @Query("SELECT p FROM PresuntaInfraccionMercado p WHERE " +
           "(:mercadoId IS NULL OR p.mercado.id = :mercadoId) AND " +
           "(:comercianteId IS NULL OR p.comerciante.id = :comercianteId) AND " +
           "(:puestoId IS NULL OR p.puesto.id = :puestoId) AND " +
           "(:tipificacionId IS NULL OR p.tipificacion.id = :tipificacionId) AND " +
           "(:estado IS NULL OR p.estadoEvaluacion = :estado) AND " +
           "(:fechaDesde IS NULL OR p.fechaDeteccion >= :fechaDesde) AND " +
           "(:fechaHasta IS NULL OR p.fechaDeteccion <= :fechaHasta) AND " +
           "(:q IS NULL OR LOWER(p.codigo) LIKE CAST(:q AS string) OR LOWER(p.descripcionHechos) LIKE CAST(:q AS string))")
    Page<PresuntaInfraccionMercado> buscarPaginado(
            @Param("mercadoId") UUID mercadoId,
            @Param("comercianteId") UUID comercianteId,
            @Param("puestoId") UUID puestoId,
            @Param("tipificacionId") UUID tipificacionId,
            @Param("estado") EstadoEvaluacionInfraccion estado,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("q") String q,
            Pageable pageable);

    long countByEstadoEvaluacion(EstadoEvaluacionInfraccion estado);

    @Query("SELECT COUNT(p) FROM PresuntaInfraccionMercado p WHERE p.estadoEvaluacion IN ('REGISTRADA', 'EN_EVALUACION', 'PENDIENTE_INFORMACION')")
    long countEnEvaluacionActiva();

    @Query(value = "SELECT nextval('mercados_infracciones_seq')", nativeQuery = true)
    Long obtenerSiguienteCorrelativo();
}
