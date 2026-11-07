package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.InspeccionMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoInspeccionMercado;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InspeccionMercadoRepository extends JpaRepository<InspeccionMercado, UUID> {

    @Query(value = "SELECT nextval('mercados_inspecciones_seq')", nativeQuery = true)
    Long obtenerSiguienteCorrelativo();

    boolean existsByCodigoInspeccion(String codigoInspeccion);

    Optional<InspeccionMercado> findByCodigoInspeccion(String codigoInspeccion);

    List<InspeccionMercado> findByPuestoIdOrderByFechaProgramadaDesc(UUID puestoId);

    List<InspeccionMercado> findByComercianteIdOrderByFechaProgramadaDesc(UUID comercianteId);

    List<InspeccionMercado> findByMercadoIdAndFechaProgramada(UUID mercadoId, LocalDate fechaProgramada);

    List<InspeccionMercado> findByFechaProgramadaBetweenOrderByFechaProgramadaAscHoraProgramadaAsc(LocalDate desde, LocalDate hasta);

    @Query("SELECT i FROM InspeccionMercado i WHERE " +
           "(:mercadoId IS NULL OR i.mercado.id = :mercadoId) AND " +
           "(:sectorId IS NULL OR i.sector.id = :sectorId) AND " +
           "(:pabellonId IS NULL OR i.pabellon.id = :pabellonId) AND " +
           "(:puestoId IS NULL OR i.puesto.id = :puestoId) AND " +
           "(:comercianteId IS NULL OR i.comerciante.id = :comercianteId) AND " +
           "(:tipo IS NULL OR i.tipoInspeccion = :tipo) AND " +
           "(:estado IS NULL OR i.estado = :estado) AND " +
           "(:inspectorId IS NULL OR i.inspectorResponsable.id = :inspectorId) AND " +
           "(:fechaDesde IS NULL OR i.fechaProgramada >= :fechaDesde) AND " +
           "(:fechaHasta IS NULL OR i.fechaProgramada <= :fechaHasta) AND " +
           "(:q IS NULL OR LOWER(i.codigoInspeccion) LIKE CAST(:q AS string) OR LOWER(i.motivo) LIKE CAST(:q AS string))")
    Page<InspeccionMercado> buscarPaginado(
            @Param("mercadoId") UUID mercadoId,
            @Param("sectorId") UUID sectorId,
            @Param("pabellonId") UUID pabellonId,
            @Param("puestoId") UUID puestoId,
            @Param("comercianteId") UUID comercianteId,
            @Param("tipo") TipoInspeccionMercado tipo,
            @Param("estado") EstadoInspeccion estado,
            @Param("inspectorId") Long inspectorId,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("q") String q,
            Pageable pageable);

    long countByEstado(EstadoInspeccion estado);

    long countByTipoInspeccion(TipoInspeccionMercado tipo);

    @Query("SELECT COUNT(i) FROM InspeccionMercado i WHERE i.fechaProgramada = :fecha AND i.estado != 'CANCELADA'")
    long countInspeccionesDelDia(@Param("fecha") LocalDate fecha);
}
