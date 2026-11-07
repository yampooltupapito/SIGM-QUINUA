package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionDevolucion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AsignacionDevolucionRepository extends JpaRepository<AsignacionDevolucion, Long> {

    Optional<AsignacionDevolucion> findFirstByAsignacionIdOrderByFechaDevolucionDesc(UUID asignacionId);

    List<AsignacionDevolucion> findByPuestoIdOrderByFechaDevolucionDesc(UUID puestoId);
}
