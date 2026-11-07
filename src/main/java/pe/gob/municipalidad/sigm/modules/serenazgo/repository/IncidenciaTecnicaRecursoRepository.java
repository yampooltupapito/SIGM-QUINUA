package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.IncidenciaTecnicaRecurso;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaTecnica;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidenciaTecnicaRecursoRepository extends JpaRepository<IncidenciaTecnicaRecurso, UUID>, JpaSpecificationExecutor<IncidenciaTecnicaRecurso> {

    @Query(value = "SELECT nextval('seq_serenazgo_incidencia_tecnica')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<IncidenciaTecnicaRecurso> findByCodigo(String codigo);

    List<IncidenciaTecnicaRecurso> findByUnidadIdOrderByFechaReporteDesc(UUID unidadId);

    List<IncidenciaTecnicaRecurso> findByEquipamientoIdOrderByFechaReporteDesc(UUID equipamientoId);

    long countByEstado(EstadoIncidenciaTecnica estado);

    @Query("SELECT count(i) FROM IncidenciaTecnicaRecurso i WHERE i.estado NOT IN (pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaTecnica.RESUELTA, pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaTecnica.DESESTIMADA)")
    long countIncidenciasAbiertas();
}
