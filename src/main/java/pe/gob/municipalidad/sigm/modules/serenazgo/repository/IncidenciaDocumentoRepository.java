package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.IncidenciaDocumento;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidenciaDocumentoRepository extends JpaRepository<IncidenciaDocumento, UUID> {
    List<IncidenciaDocumento> findByIncidenciaIdOrderByCreatedAtDesc(UUID incidenciaId);
    long countByIncidenciaId(UUID incidenciaId);
}
