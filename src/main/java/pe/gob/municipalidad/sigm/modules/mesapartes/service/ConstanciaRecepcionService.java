package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.ConstanciaRecepcionResponse;

/**
 * Servicio para generación de la Constancia Oficial de Recepción Administrativa en PDF.
 */
public interface ConstanciaRecepcionService {

    byte[] generarConstanciaRecepcionPdf(Long expedienteId, String baseUrl, String username);

    ConstanciaRecepcionResponse obtenerDatosConstancia(Long expedienteId, String baseUrl, String username);
}
