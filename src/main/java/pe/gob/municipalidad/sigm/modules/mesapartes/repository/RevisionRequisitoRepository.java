package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.RevisionRequisito;

import java.util.List;
import java.util.Optional;

@Repository
public interface RevisionRequisitoRepository extends JpaRepository<RevisionRequisito, Long> {

    @Query("SELECT r FROM RevisionRequisito r " +
           "JOIN FETCH r.requisito req " +
           "LEFT JOIN FETCH r.documento doc " +
           "LEFT JOIN FETCH r.usuarioRevisor usr " +
           "WHERE r.expediente.id = :expedienteId " +
           "ORDER BY req.orden ASC, req.id ASC")
    List<RevisionRequisito> findByExpedienteIdWithDetails(@Param("expedienteId") Long expedienteId);

    Optional<RevisionRequisito> findByExpedienteIdAndRequisitoId(Long expedienteId, Long requisitoId);

    boolean existsByExpedienteId(Long expedienteId);
}
