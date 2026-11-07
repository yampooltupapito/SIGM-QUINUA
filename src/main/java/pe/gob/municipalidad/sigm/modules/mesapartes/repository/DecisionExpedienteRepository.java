package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.DecisionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoEmisionDocumento;

import java.util.List;
import java.util.Optional;

@Repository
public interface DecisionExpedienteRepository extends JpaRepository<DecisionExpediente, Long> {

    List<DecisionExpediente> findByExpedienteIdOrderByFechaDecisionDesc(Long expedienteId);

    Optional<DecisionExpediente> findFirstByExpedienteIdAndEstadoEmisionOrderByFechaDecisionDesc(
            Long expedienteId, EstadoEmisionDocumento estadoEmision);

    Page<DecisionExpediente> findByOficinaEmisoraId(Long oficinaEmisoraId, Pageable pageable);

    Page<DecisionExpediente> findByEstadoEmision(EstadoEmisionDocumento estadoEmision, Pageable pageable);

    @Query("SELECT COUNT(d) FROM DecisionExpediente d WHERE d.estadoEmision = :estado AND (:oficinaId IS NULL OR d.oficinaEmisora.id = :oficinaId)")
    long countByEstadoEmisionAndOficina(@Param("estado") EstadoEmisionDocumento estado, @Param("oficinaId") Long oficinaId);
}
