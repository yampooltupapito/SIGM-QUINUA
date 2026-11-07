package pe.gob.municipalidad.sigm.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.model.Oficina;

import java.util.List;
import java.util.Optional;

@Repository
public interface OficinaRepository extends JpaRepository<Oficina, Long> {

    Optional<Oficina> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Oficina> findByEstado(String estado);

    long countByEstado(String estado);

    Page<Oficina> findByEstado(String estado, Pageable pageable);

    List<Oficina> findByOficinaPadreId(Long oficinaPadreId);

    List<Oficina> findByMunicipalidadId(Long municipalidadId);

    @org.springframework.data.jpa.repository.Query("SELECT o.nombre, COUNT(po) FROM PersonaOficina po JOIN po.oficina o WHERE po.estado = 'ACTIVO' GROUP BY o.nombre ORDER BY COUNT(po) DESC")
    List<Object[]> countPersonasGroupedByOficina();

    @org.springframework.data.jpa.repository.Query("SELECT o FROM Oficina o LEFT JOIN FETCH o.oficinaPadre WHERE " +
            "(:term IS NULL OR :term = '' OR " +
            "LOWER(o.codigo) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(o.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(o.sigla) LIKE LOWER(CONCAT('%', :term, '%'))) AND " +
            "(:estado IS NULL OR :estado = '' OR o.estado = :estado)")
    Page<Oficina> searchOficinas(
            @org.springframework.data.repository.query.Param("term") String term,
            @org.springframework.data.repository.query.Param("estado") String estado,
            Pageable pageable
    );
}
