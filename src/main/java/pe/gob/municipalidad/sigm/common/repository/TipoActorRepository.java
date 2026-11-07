package pe.gob.municipalidad.sigm.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.model.TipoActor;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoActorRepository extends JpaRepository<TipoActor, Long> {

    Optional<TipoActor> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<TipoActor> findByEstado(String estado);

    long countByEstado(String estado);

    List<TipoActor> findByEstadoOrderByNombreAsc(String estado);

    Page<TipoActor> findByEstado(String estado, Pageable pageable);

    @Query("SELECT t FROM TipoActor t WHERE " +
            "(:term IS NULL OR :term = '' OR " +
            "LOWER(t.codigo) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(t.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(t.descripcion) LIKE LOWER(CONCAT('%', :term, '%'))) AND " +
            "(:estado IS NULL OR :estado = '' OR t.estado = :estado)")
    Page<TipoActor> searchTiposActores(
            @Param("term") String term,
            @Param("estado") String estado,
            Pageable pageable
    );
}
