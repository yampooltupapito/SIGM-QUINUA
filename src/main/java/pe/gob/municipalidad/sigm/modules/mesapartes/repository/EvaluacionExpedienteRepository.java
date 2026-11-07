package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.EvaluacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoEvaluacion;

import java.util.List;

@Repository
public interface EvaluacionExpedienteRepository extends JpaRepository<EvaluacionExpediente, Long> {

    List<EvaluacionExpediente> findByExpedienteIdOrderByFechaEvaluacionDesc(Long expedienteId);

    Page<EvaluacionExpediente> findByExpedienteId(Long expedienteId, Pageable pageable);

    Page<EvaluacionExpediente> findByOficinaId(Long oficinaId, Pageable pageable);

    Page<EvaluacionExpediente> findByUsuarioEvaluadorId(Long usuarioEvaluadorId, Pageable pageable);

    @Query("SELECT COUNT(e) FROM EvaluacionExpediente e WHERE e.estado = :estado AND (:oficinaId IS NULL OR e.oficina.id = :oficinaId)")
    long countByEstadoAndOficina(@Param("estado") EstadoEvaluacion estado, @Param("oficinaId") Long oficinaId);
}
