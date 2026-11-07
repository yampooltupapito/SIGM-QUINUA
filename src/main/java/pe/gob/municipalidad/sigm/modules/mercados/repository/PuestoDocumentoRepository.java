package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PuestoDocumento;

import java.util.List;
import java.util.UUID;

@Repository
public interface PuestoDocumentoRepository extends JpaRepository<PuestoDocumento, Long> {

    List<PuestoDocumento> findByPuestoIdOrderByCreatedAtDesc(UUID puestoId);

    boolean existsByPuestoIdAndDocumentoId(UUID puestoId, Long documentoId);

    boolean existsByPuestoIdAndDocumentoIdentificadorPublico(UUID puestoId, UUID identificadorPublico);
}
