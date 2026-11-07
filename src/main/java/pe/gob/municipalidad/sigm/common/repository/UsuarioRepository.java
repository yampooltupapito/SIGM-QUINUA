package pe.gob.municipalidad.sigm.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.persona p LEFT JOIN FETCH u.roles r WHERE u.username = :username")
    Optional<Usuario> findByUsernameWithRolesAndPersona(@Param("username") String username);

    @Query("SELECT DISTINCT u FROM Usuario u JOIN FETCH u.persona p LEFT JOIN FETCH u.roles r WHERE u.id = :id")
    Optional<Usuario> findWithRolesAndPersonaById(@Param("id") Long id);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.persona p LEFT JOIN FETCH u.roles r WHERE LOWER(u.username) = LOWER(:identifier) OR LOWER(p.correo) = LOWER(:identifier)")
    Optional<Usuario> findByUsernameOrPersonaCorreo(@Param("identifier") String identifier);

    Optional<Usuario> findByPersonaId(Long personaId);

    boolean existsByPersonaId(Long personaId);

    long countByEnabledTrue();

    long countByEnabledFalse();

    long countByAccountLockedTrue();

    @Query("SELECT COUNT(u) FROM Usuario u JOIN u.roles r WHERE r.codigo = :rolCodigo AND u.enabled = true AND u.accountLocked = false")
    long countActiveUsersWithRole(@Param("rolCodigo") String rolCodigo);

    @Query("SELECT r.nombre, COUNT(u) FROM Usuario u JOIN u.roles r GROUP BY r.nombre ORDER BY COUNT(u) DESC")
    java.util.List<Object[]> countUsuariosGroupedByRol();

    @Query("SELECT u FROM Usuario u JOIN u.persona p WHERE " +
            "(:term IS NULL OR :term = '' OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(p.nombres) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(p.apellidoPaterno) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(p.apellidoMaterno) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "p.numeroDocumento LIKE CONCAT('%', :term, '%')) AND " +
            "(:enabled IS NULL OR u.enabled = :enabled) AND " +
            "(:locked IS NULL OR u.accountLocked = :locked)")
    Page<Usuario> searchUsuarios(
            @Param("term") String term,
            @Param("enabled") Boolean enabled,
            @Param("locked") Boolean locked,
            Pageable pageable
    );

    @Query("SELECT u FROM Usuario u JOIN u.persona p JOIN PersonaOficina po ON po.persona.id = p.id " +
           "WHERE po.oficina.id = :oficinaId AND po.estado = 'ACTIVO' AND u.enabled = true AND u.accountLocked = false")
    java.util.List<Usuario> findActivosByOficinaId(@Param("oficinaId") Long oficinaId);
}
