package pe.gob.municipalidad.sigm.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.model.Cargo;

import java.util.List;
import java.util.Optional;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Long> {

    Optional<Cargo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Cargo> findByEstado(String estado);

    List<Cargo> findByEstadoOrderByNombreAsc(String estado);

    Page<Cargo> findByEstado(String estado, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Cargo c WHERE " +
            "(:term IS NULL OR :term = '' OR " +
            "LOWER(c.codigo) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :term, '%'))) AND " +
            "(:estado IS NULL OR :estado = '' OR c.estado = :estado)")
    Page<Cargo> searchCargos(
            @org.springframework.data.repository.query.Param("term") String term,
            @org.springframework.data.repository.query.Param("estado") String estado,
            Pageable pageable
    );
}
