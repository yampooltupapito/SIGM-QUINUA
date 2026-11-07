package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;

import java.util.List;

public interface ExpedienteService {

    ExpedienteResponse crearBorrador(CrearBorradorRequest request, String username);

    ExpedienteResponse actualizarBorrador(Long id, ActualizarBorradorRequest request, String username);

    ExpedienteResponse presentarExpediente(Long id, PresentarExpedienteRequest request, String username);

    ExpedienteResponse asociarDocumento(Long expedienteId, AsociarDocumentoRequest request, String username);

    void desasociarDocumento(Long expedienteId, Long documentoId, String username);

    ExpedienteResponse recibirExpediente(Long id, String observaciones, String username);

    ExpedienteResponse derivarExpediente(Long id, DerivarExpedienteRequest request, String username);

    ExpedienteResponse observarExpediente(Long id, ObservarExpedienteRequest request, String username);

    ExpedienteResponse subsanarExpediente(Long id, SubsanarExpedienteRequest request, String username);

    ExpedienteResponse resolverExpediente(Long id, CambiarEstadoRequest request, String username);

    ExpedienteResponse archivarExpediente(Long id, CambiarEstadoRequest request, String username);

    ExpedienteResponse anularExpediente(Long id, CambiarEstadoRequest request, String username);

    ExpedienteDetalleResponse obtenerDetalle(Long id, String username);

    Page<ExpedienteResponse> listarExpedientes(EstadoExpediente estado, Long oficinaId, Long tipoTramiteId, String termino, Pageable pageable, String username);

    List<ExpedienteHistorialResponse> obtenerHistorial(Long id, String username);

    List<ExpedienteDocumentoResponse> obtenerDocumentos(Long id, String username);

    MpvEstadisticasResponse obtenerEstadisticas(String username);
}
