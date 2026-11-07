package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.DocumentoSeguridadVerificacion;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentoSeguridadVerificacionRepository extends JpaRepository<DocumentoSeguridadVerificacion, UUID> {

    List<DocumentoSeguridadVerificacion> findByDocumentoSeguridadIdOrderByFechaVerificacionDesc(UUID documentoSeguridadId);
}
