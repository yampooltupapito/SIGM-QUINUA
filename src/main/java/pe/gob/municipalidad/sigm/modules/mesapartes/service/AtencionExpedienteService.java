package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;

public interface AtencionExpedienteService {

    Page<AtencionItemResponse> listarBandejaAtencion(
            Long oficinaId,
            EstadoExpediente estado,
            Long tipoTramiteId,
            String termino,
            Pageable pageable,
            String username
    );

    AtencionItemResponse iniciarAtencion(Long expedienteId, IniciarAtencionRequest request, String username);

    DetalleAtencionResponse obtenerDetalleAtencion(Long expedienteId, String username);
}
