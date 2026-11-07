package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoDerivacion;

public interface DerivacionExpedienteService {

    Page<DerivacionResponse> listarDerivaciones(Long oficinaId, EstadoDerivacion estado, Pageable pageable, String username);

    Page<DerivacionResponse> listarDerivacionesRecibidas(EstadoDerivacion estado, Pageable pageable, String username);

    DerivacionResponse obtenerPorId(Long derivacionId, String username);

    DerivacionResponse derivarExpediente(Long expedienteId, DerivarExpedienteRequest request, String username);

    DerivacionResponse recibirDerivacion(Long derivacionId, RecepcionDerivacionRequest request, String username);

    DerivacionResponse rechazarDerivacion(Long derivacionId, RechazoDevolucionRequest request, String username);

    DerivacionResponse devolverDerivacion(Long derivacionId, RechazoDevolucionRequest request, String username);
}
