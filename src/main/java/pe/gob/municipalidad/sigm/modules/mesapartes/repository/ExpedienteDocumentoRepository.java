package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ExpedienteDocumento;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpedienteDocumentoRepository extends JpaRepository<ExpedienteDocumento, Long> {

    List<ExpedienteDocumento> findByExpedienteIdOrderByOrdenAsc(Long expedienteId);

    @org.springframework.data.jpa.repository.Query("SELECT ed FROM ExpedienteDocumento ed JOIN FETCH ed.documento WHERE ed.expediente.id = :expedienteId ORDER BY ed.orden ASC")
    List<ExpedienteDocumento> findByExpedienteIdWithDocumento(@org.springframework.data.repository.query.Param("expedienteId") Long expedienteId);

    Optional<ExpedienteDocumento> findByExpedienteIdAndDocumentoId(Long expedienteId, Long documentoId);

    boolean existsByExpedienteIdAndDocumentoId(Long expedienteId, Long documentoId);

    long countByExpedienteId(Long expedienteId);
}
