package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteDetalleResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoRelacionDocumentoExpediente;

import java.util.UUID;

public interface SolicitudCiudadanaService {

    ExpedienteDetalleResponse guardarBorrador(UUID expedienteUuid, GuardarBorradorSolicitudRequest request, String username);

    ExpedienteDetalleResponse obtenerBorrador(UUID expedienteUuid, String username);

    ExpedienteDetalleResponse subirDocumentoBorrador(UUID expedienteUuid, MultipartFile archivo,
                                                    TipoRelacionDocumentoExpediente tipoRelacion, String username);

    void eliminarDocumentoBorrador(UUID expedienteUuid, UUID documentoUuid, String username);

    ExpedienteResponse presentarSolicitud(UUID expedienteUuid, PresentarSolicitudRequest request, String username);

    Page<MisSolicitudesItemResponse> listarMisSolicitudes(EstadoExpediente estado, String termino, Pageable pageable, String username);

    ExpedienteDetalleResponse obtenerMiExpediente(UUID expedienteUuid, String username);

    byte[] descargarConstanciaPdf(UUID expedienteUuid, String baseUrl, String username);
}
