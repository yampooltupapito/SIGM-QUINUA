package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.VerificarDocumentoResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.DecisionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.VerificacionDocumental;

public interface VerificacionDocumentalService {

    VerificarDocumentoResponse verificarDocumento(String codigoOQr, String ipOrigen, String userAgent);

    VerificacionDocumental registrarVerificacionDocumento(
            Documento documento,
            Expediente expediente,
            DecisionExpediente decision,
            String tipoDocumento,
            String numeroDocumento,
            String autoridadFirmante,
            String cargoFirmante,
            String baseUrl
    );

    void anularDocumento(Long verificacionId, String motivo, String username);
}
