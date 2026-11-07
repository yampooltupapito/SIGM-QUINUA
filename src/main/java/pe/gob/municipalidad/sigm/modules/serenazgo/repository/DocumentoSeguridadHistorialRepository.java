package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.DocumentoSeguridadHistorial;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentoSeguridadHistorialRepository extends JpaRepository<DocumentoSeguridadHistorial, UUID> {

    List<DocumentoSeguridadHistorial> findByDocumentoSeguridadIdOrderByFechaHoraDesc(UUID documentoSeguridadId);
}
