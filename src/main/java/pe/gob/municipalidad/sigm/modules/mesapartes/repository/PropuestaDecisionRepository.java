package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.PropuestaDecision;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoPropuesta;

import java.util.List;

@Repository
public interface PropuestaDecisionRepository extends JpaRepository<PropuestaDecision, Long> {

    List<PropuestaDecision> findByExpedienteIdOrderByFechaCreacionDesc(Long expedienteId);

    Page<PropuestaDecision> findByEstado(EstadoPropuesta estado, Pageable pageable);

    Page<PropuestaDecision> findByOficinaId(Long oficinaId, Pageable pageable);

    Page<PropuestaDecision> findByOficinaIdAndEstado(Long oficinaId, EstadoPropuesta estado, Pageable pageable);

    Page<PropuestaDecision> findByUsuarioElaboradorId(Long usuarioElaboradorId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM PropuestaDecision p WHERE p.estado = :estado AND (:oficinaId IS NULL OR p.oficina.id = :oficinaId)")
    long countByEstadoAndOficina(@Param("estado") EstadoPropuesta estado, @Param("oficinaId") Long oficinaId);
}
