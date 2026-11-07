package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.DocumentoSeguridadRelacion;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRelacionDocumentoSeguridad;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentoSeguridadRelacionRepository extends JpaRepository<DocumentoSeguridadRelacion, UUID> {

    List<DocumentoSeguridadRelacion> findByDocumentoSeguridadIdOrderByFechaAsociacionDesc(UUID documentoSeguridadId);

    List<DocumentoSeguridadRelacion> findByTipoEntidadAndEntidadId(TipoRelacionDocumentoSeguridad tipoEntidad, UUID entidadId);

    boolean existsByDocumentoSeguridadIdAndTipoEntidadAndEntidadId(UUID documentoSeguridadId, TipoRelacionDocumentoSeguridad tipoEntidad, UUID entidadId);

    void deleteByDocumentoSeguridadIdAndTipoEntidadAndEntidadId(UUID documentoSeguridadId, TipoRelacionDocumentoSeguridad tipoEntidad, UUID entidadId);
}
