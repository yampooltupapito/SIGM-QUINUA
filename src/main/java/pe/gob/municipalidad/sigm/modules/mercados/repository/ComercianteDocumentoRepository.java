package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ComercianteDocumento;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComercianteDocumentoRepository extends JpaRepository<ComercianteDocumento, Long> {

    List<ComercianteDocumento> findByComercianteIdOrderByCreatedAtDesc(UUID comercianteId);

    boolean existsByComercianteIdAndDocumentoId(UUID comercianteId, Long documentoId);

    boolean existsByComercianteIdAndDocumentoIdentificadorPublico(UUID comercianteId, UUID identificadorPublico);
}
