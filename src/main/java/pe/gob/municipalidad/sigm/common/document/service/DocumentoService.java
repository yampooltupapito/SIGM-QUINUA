package pe.gob.municipalidad.sigm.common.document.service;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.*;
import pe.gob.municipalidad.sigm.common.document.entity.CategoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso;
import pe.gob.municipalidad.sigm.common.document.entity.EstadoDocumento;

import java.util.List;
import java.util.UUID;

public interface DocumentoService {

    DocumentoResponse subirDocumento(MultipartFile file, SubirDocumentoRequest request, String username);

    DocumentoResponse crearNuevaVersion(Long documentoId, MultipartFile file, NuevaVersionRequest request, String username);

    DocumentoResponse obtenerPorId(Long documentoId, String username);

    DocumentoResponse obtenerPorUuid(UUID uuid, String username);

    DocumentoResponse actualizarMetadatos(Long documentoId, ActualizarDocumentoRequest request, String username);

    Resource descargarDocumento(Long documentoId, Integer versionOpcional, String username);

    Resource vistaPreviaDocumento(Long documentoId, String username);

    VerificacionIntegridadResponse verificarIntegridad(Long documentoId, Integer versionOpcional, String username);

    Page<DocumentoResponse> listarDocumentos(
            EstadoDocumento estado,
            CategoriaDocumento categoria,
            ClasificacionAcceso clasificacion,
            String termino,
            Pageable pageable,
            String username
    );

    DocumentoResponse archivarDocumento(Long documentoId, EliminarDocumentoRequest request, String username);

    DocumentoResponse eliminarLogicamente(Long documentoId, EliminarDocumentoRequest request, String username);

    DocumentoResponse cambiarBloqueoConservacion(Long documentoId, boolean bloqueo, String username);

    List<DocumentoVersionResponse> listarVersiones(Long documentoId, String username);

    List<DocumentoAuditoriaResponse> listarAuditoria(Long documentoId, String username);

    DocumentoEstadisticasResponse obtenerEstadisticas();

    void asociarDocumento(Long documentoId, String modulo, String tipoEntidad, String entidadId, String rol);

    DocumentoResponse generarYGuardarPdf(GenerarPdfRequest request, String username);
}
