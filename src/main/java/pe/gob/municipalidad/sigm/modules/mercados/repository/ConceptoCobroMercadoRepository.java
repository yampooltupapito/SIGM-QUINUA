package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ConceptoCobroMercado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConceptoCobroMercadoRepository extends JpaRepository<ConceptoCobroMercado, UUID> {

    Optional<ConceptoCobroMercado> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<ConceptoCobroMercado> findByEstado(String estado);

    @Query(value = "SELECT * FROM mercados_conceptos_cobro c " +
                   "WHERE (:estado IS NULL OR c.estado = :estado) " +
                   "AND (:q IS NULL OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%'))) " +
                   "ORDER BY c.nombre ASC",
           countQuery = "SELECT COUNT(*) FROM mercados_conceptos_cobro c " +
                        "WHERE (:estado IS NULL OR c.estado = :estado) " +
                        "AND (:q IS NULL OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')))",
           nativeQuery = true)
    Page<ConceptoCobroMercado> buscarPaginado(@Param("estado") String estado,
                                             @Param("q") String q,
                                             Pageable pageable);
}
