package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.DocumentoDisponibleDto;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.EventoLineaTiempoCiudadanaDto;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.ObservacionCiudadanaDto;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.SeguimientoCiudadanoDetalleDto;

import java.util.List;
import java.util.UUID;

public interface SeguimientoCiudadanoService {

    SeguimientoCiudadanoDetalleDto obtenerSeguimientoDetalle(Long expedienteId, String username);

    SeguimientoCiudadanoDetalleDto obtenerSeguimientoDetallePorUuid(UUID expedienteUuid, String username);

    List<EventoLineaTiempoCiudadanaDto> obtenerLineaTiempoCiudadana(Long expedienteId, String username);

    List<ObservacionCiudadanaDto> obtenerObservacionesCiudadanas(Long expedienteId, String username);

    List<DocumentoDisponibleDto> obtenerDocumentosDisponibles(Long expedienteId, String username);

    byte[] descargarDocumentoAutorizado(Long expedienteId, Long documentoId, String username);
}
