package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.AlertaDocumento;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertaDocumentoRepository extends JpaRepository<AlertaDocumento, UUID> {
    List<AlertaDocumento> findByAlertaIdOrderByCreatedAtDesc(UUID alertaId);
}
