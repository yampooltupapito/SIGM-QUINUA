package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.SectorMercado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SectorMercadoRepository extends JpaRepository<SectorMercado, UUID> {

    List<SectorMercado> findByMercadoIdOrderByOrdenVisualAsc(UUID mercadoId);

    List<SectorMercado> findByMercadoIdOrderByOrdenVisualAscCodigoAsc(UUID mercadoId);

    List<SectorMercado> findByMercadoIdAndEstadoOrderByOrdenVisualAsc(UUID mercadoId, String estado);

    Optional<SectorMercado> findByMercadoIdAndCodigo(UUID mercadoId, String codigo);

    Optional<SectorMercado> findByIdAndMercadoId(UUID id, UUID mercadoId);

    boolean existsByMercadoIdAndCodigo(UUID mercadoId, String codigo);

    boolean existsByIdAndMercadoId(UUID id, UUID mercadoId);

    long countByMercadoId(UUID mercadoId);

    long countByMercadoIdAndEstado(UUID mercadoId, String estado);

    @Query("SELECT s FROM SectorMercado s WHERE s.mercado.id = :mercadoId " +
           "AND (:estado IS NULL OR s.estado = :estado) " +
           "AND (:categoria IS NULL OR s.categoriaComercial = :categoria)")
    Page<SectorMercado> filtrarSinTermino(
            @Param("mercadoId") UUID mercadoId,
            @Param("estado") String estado,
            @Param("categoria") String categoria,
            Pageable pageable
    );

    @Query("SELECT s FROM SectorMercado s WHERE s.mercado.id = :mercadoId " +
           "AND (:estado IS NULL OR s.estado = :estado) " +
           "AND (:categoria IS NULL OR s.categoriaComercial = :categoria) " +
           "AND (LOWER(s.codigo) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(s.descripcion) LIKE LOWER(CONCAT('%', :termino, '%')))")
    Page<SectorMercado> filtrarConTermino(
            @Param("mercadoId") UUID mercadoId,
            @Param("estado") String estado,
            @Param("categoria") String categoria,
            @Param("termino") String termino,
            Pageable pageable
    );
}
