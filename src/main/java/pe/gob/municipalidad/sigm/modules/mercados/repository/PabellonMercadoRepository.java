package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PabellonMercado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PabellonMercadoRepository extends JpaRepository<PabellonMercado, UUID> {

    List<PabellonMercado> findByMercadoIdOrderByOrdenVisualAsc(UUID mercadoId);

    List<PabellonMercado> findByMercadoIdAndSectorIdIsNullOrderByOrdenVisualAsc(UUID mercadoId);

    List<PabellonMercado> findBySectorIdOrderByOrdenVisualAsc(UUID sectorId);

    Optional<PabellonMercado> findByMercadoIdAndCodigo(UUID mercadoId, String codigo);

    Optional<PabellonMercado> findByIdAndMercadoId(UUID id, UUID mercadoId);

    boolean existsByMercadoIdAndCodigo(UUID mercadoId, String codigo);

    boolean existsByIdAndMercadoId(UUID id, UUID mercadoId);

    long countByMercadoId(UUID mercadoId);

    long countByMercadoIdAndEstado(UUID mercadoId, String estado);

    long countBySectorId(UUID sectorId);

    @Query("SELECT p FROM PabellonMercado p WHERE p.mercado.id = :mercadoId " +
           "AND (:sectorId IS NULL OR p.sector.id = :sectorId) " +
           "AND (:estado IS NULL OR p.estado = :estado) " +
           "AND (:nivel IS NULL OR p.nivel = :nivel)")
    Page<PabellonMercado> filtrarSinTermino(
            @Param("mercadoId") UUID mercadoId,
            @Param("sectorId") UUID sectorId,
            @Param("estado") String estado,
            @Param("nivel") Integer nivel,
            Pageable pageable
    );

    @Query("SELECT p FROM PabellonMercado p WHERE p.mercado.id = :mercadoId " +
           "AND (:sectorId IS NULL OR p.sector.id = :sectorId) " +
           "AND (:estado IS NULL OR p.estado = :estado) " +
           "AND (:nivel IS NULL OR p.nivel = :nivel) " +
           "AND (LOWER(p.codigo) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :termino, '%')))")
    Page<PabellonMercado> filtrarConTermino(
            @Param("mercadoId") UUID mercadoId,
            @Param("sectorId") UUID sectorId,
            @Param("estado") String estado,
            @Param("nivel") Integer nivel,
            @Param("termino") String termino,
            Pageable pageable
    );
}
