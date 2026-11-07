package pe.gob.municipalidad.sigm.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.model.Permiso;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    Optional<Permiso> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Permiso> findByModuloId(Long moduloId);

    List<Permiso> findByAccion(String accion);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Permiso p JOIN FETCH p.modulo m WHERE p.estado = 'ACTIVO' AND m.estado = 'ACTIVO' ORDER BY m.orden ASC, p.id ASC")
    List<Permiso> findAllActivosWithModulo();
}
