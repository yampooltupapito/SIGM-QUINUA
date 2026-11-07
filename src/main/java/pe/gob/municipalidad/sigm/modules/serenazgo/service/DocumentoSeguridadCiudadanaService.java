package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.documentos.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.RolRelacionDocumento;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRelacionDocumentoSeguridad;

import java.util.UUID;

public interface DocumentoSeguridadCiudadanaService {

    DocumentoSeguridadResponse registrarDocumento(DocumentoCargaRequest request, CustomUserDetails user, String ip);

    Page<DocumentoSeguridadResponse> listar(DocumentoFiltroRequest filtro, Pageable pageable);

    DocumentoSeguridadDetalleResponse obtenerDetalle(UUID id, CustomUserDetails user);

    Resource descargar(UUID id, CustomUserDetails user, String ip);

    Resource obtenerVistaPrevia(UUID id, CustomUserDetails user);

    VerificacionIntegridadResponse verificarIntegridad(UUID id, CustomUserDetails user);

    DocumentoSeguridadResponse revisar(UUID id, TransicionEstadoRequest req, CustomUserDetails user);

    DocumentoSeguridadResponse observar(UUID id, TransicionEstadoRequest req, CustomUserDetails user);

    DocumentoSeguridadResponse validarAdministrativamente(UUID id, TransicionEstadoRequest req, CustomUserDetails user);

    DocumentoSeguridadResponse restringir(UUID id, TransicionEstadoRequest req, CustomUserDetails user);

    DocumentoSeguridadResponse archivar(UUID id, TransicionEstadoRequest req, CustomUserDetails user);

    DocumentoSeguridadResponse crearNuevaVersion(UUID id, NuevaVersionRequest req, CustomUserDetails user, String ip);

    void asociarRegistro(UUID id, TipoRelacionDocumentoSeguridad tipo, UUID entidadId, RolRelacionDocumento rol, CustomUserDetails user);

    DocumentoEstadisticasResponse obtenerEstadisticas();

    byte[] exportarExcel(DocumentoFiltroRequest filtro);

    byte[] exportarPdfFicha(UUID id, CustomUserDetails user);
}
