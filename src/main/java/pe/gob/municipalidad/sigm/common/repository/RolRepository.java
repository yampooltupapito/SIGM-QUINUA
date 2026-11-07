package pe.gob.municipalidad.sigm.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.model.Rol;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Rol> findByEstado(String estado);

    long countByEstado(String estado);

    List<Rol> findByEstadoOrderByNombreAsc(String estado);

    @Query("SELECT DISTINCT r FROM Rol r LEFT JOIN FETCH r.permisos p WHERE r IN :roles")
    List<Rol> findRolesWithPermisos(@Param("roles") Collection<Rol> roles);

    @Query("SELECT DISTINCT r FROM Rol r LEFT JOIN FETCH r.permisos p WHERE r.id = :id")
    Optional<Rol> findWithPermisosById(@Param("id") Long id);

    @Query("SELECT r FROM Rol r WHERE " +
            "(:term IS NULL OR :term = '' OR " +
            "LOWER(r.codigo) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(r.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :term, '%'))) AND " +
            "(:estado IS NULL OR :estado = '' OR r.estado = :estado)")
    Page<Rol> searchRoles(
            @Param("term") String term,
            @Param("estado") String estado,
            Pageable pageable
    );
}
