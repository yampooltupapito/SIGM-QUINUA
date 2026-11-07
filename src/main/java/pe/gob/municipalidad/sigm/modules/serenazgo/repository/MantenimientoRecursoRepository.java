package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.MantenimientoRecurso;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoMantenimientoRecurso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MantenimientoRecursoRepository extends JpaRepository<MantenimientoRecurso, UUID>, JpaSpecificationExecutor<MantenimientoRecurso> {

    @Query(value = "SELECT nextval('seq_serenazgo_mantenimiento')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<MantenimientoRecurso> findByCodigo(String codigo);

    List<MantenimientoRecurso> findByUnidadIdOrderByFechaProgramadaDesc(UUID unidadId);

    List<MantenimientoRecurso> findByEquipamientoIdOrderByFechaProgramadaDesc(UUID equipamientoId);

    long countByEstado(EstadoMantenimientoRecurso estado);
}
