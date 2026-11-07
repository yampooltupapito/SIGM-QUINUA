package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.AsignacionEquipamiento;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAsignacionRecurso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AsignacionEquipamientoRepository extends JpaRepository<AsignacionEquipamiento, UUID>, JpaSpecificationExecutor<AsignacionEquipamiento> {

    List<AsignacionEquipamiento> findByEquipamientoIdOrderByFechaEntregaDesc(UUID equipamientoId);

    Optional<AsignacionEquipamiento> findFirstByEquipamientoIdAndEstadoAsignacion(UUID equipamientoId, EstadoAsignacionRecurso estadoAsignacion);

    boolean existsByEquipamientoIdAndEstadoAsignacion(UUID equipamientoId, EstadoAsignacionRecurso estadoAsignacion);

    List<AsignacionEquipamiento> findByPersonalIdOrderByFechaEntregaDesc(UUID personalId);

    List<AsignacionEquipamiento> findByUnidadIdOrderByFechaEntregaDesc(UUID unidadId);

    long countByEstadoAsignacion(EstadoAsignacionRecurso estadoAsignacion);
}
