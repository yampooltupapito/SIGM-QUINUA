package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoPropuesta;

import java.util.List;

public interface PropuestaDecisionService {

    PropuestaResponse crearPropuesta(Long expedienteId, CrearPropuestaRequest request, String username);

    PropuestaResponse enviarARevision(Long propuestaId, String username);

    PropuestaResponse cancelarPropuesta(Long propuestaId, String motivo, String username);

    List<PropuestaResponse> listarPropuestasPorExpediente(Long expedienteId, String username);

    Page<PropuestaResponse> listarBandejaPropuestas(Long oficinaId, EstadoPropuesta estado, Pageable pageable, String username);

    PropuestaResponse obtenerPorId(Long propuestaId, String username);
}
