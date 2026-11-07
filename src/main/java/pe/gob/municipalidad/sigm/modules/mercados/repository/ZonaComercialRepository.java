package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ZonaComercial;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoZona;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ZonaComercialRepository extends JpaRepository<ZonaComercial, UUID> {

    List<ZonaComercial> findByMercadoIdOrderByOrdenVisualAsc(UUID mercadoId);

    List<ZonaComercial> findByMercadoIdAndEstadoOrderByOrdenVisualAsc(UUID mercadoId, EstadoMercado estado);

    Optional<ZonaComercial> findByMercadoIdAndCodigo(UUID mercadoId, String codigo);

    Optional<ZonaComercial> findByIdAndMercadoId(UUID id, UUID mercadoId);

    boolean existsByMercadoIdAndCodigo(UUID mercadoId, String codigo);

    boolean existsByIdAndMercadoId(UUID id, UUID mercadoId);

    long countByMercadoId(UUID mercadoId);

    long countByMercadoIdAndEstado(UUID mercadoId, EstadoMercado estado);

    long countByMercadoIdAndEsAsignable(UUID mercadoId, Boolean esAsignable);

    @Query("SELECT z FROM ZonaComercial z WHERE z.mercado.id = :mercadoId " +
           "AND (:estado IS NULL OR z.estado = :estado) " +
           "AND (:tipoZona IS NULL OR z.tipoZona = :tipoZona) " +
           "AND (:esAsignable IS NULL OR z.esAsignable = :esAsignable)")
    Page<ZonaComercial> filtrarSinTermino(
            @Param("mercadoId") UUID mercadoId,
            @Param("estado") EstadoMercado estado,
            @Param("tipoZona") TipoZona tipoZona,
            @Param("esAsignable") Boolean esAsignable,
            Pageable pageable
    );

    @Query("SELECT z FROM ZonaComercial z WHERE z.mercado.id = :mercadoId " +
           "AND (:estado IS NULL OR z.estado = :estado) " +
           "AND (:tipoZona IS NULL OR z.tipoZona = :tipoZona) " +
           "AND (:esAsignable IS NULL OR z.esAsignable = :esAsignable) " +
           "AND (LOWER(z.codigo) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(z.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(z.descripcion) LIKE LOWER(CONCAT('%', :termino, '%')))")
    Page<ZonaComercial> filtrarConTermino(
            @Param("mercadoId") UUID mercadoId,
            @Param("estado") EstadoMercado estado,
            @Param("tipoZona") TipoZona tipoZona,
            @Param("esAsignable") Boolean esAsignable,
            @Param("termino") String termino,
            Pageable pageable
    );
}
