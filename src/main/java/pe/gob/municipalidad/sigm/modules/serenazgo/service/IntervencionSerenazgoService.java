package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoComunicacionIntervencion;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoDocumentoIntervencion;

import java.util.UUID;

public interface IntervencionSerenazgoService {

    IntervencionDetailResponse crearIntervencion(IntervencionCrearRequest request, String username);

    IntervencionDetailResponse actualizarIntervencion(UUID id, IntervencionEditarRequest request, String username);

    IntervencionDetailResponse obtenerPorId(UUID id, String username);

    IntervencionDetailResponse obtenerPorCodigo(String codigo, String username);

    Page<IntervencionResponse> listarIntervenciones(IntervencionFilter filtro, Pageable pageable);

    DashboardIntervencionesResponse obtenerDashboard();

    IntervencionDetailResponse agregarParticipante(UUID intervencionId, IntervencionParticipanteRequest request, String username);

    IntervencionDetailResponse removerParticipante(UUID intervencionId, UUID personalId, String username);

    IntervencionDetailResponse registrarActuacion(UUID intervencionId, IntervencionActuacionRequest request, String username);

    IntervencionDetailResponse registrarComunicacion(UUID intervencionId, IntervencionComunicacionRequest request, String username);

    IntervencionDetailResponse actualizarEstadoComunicacion(UUID comunicacionId, EstadoComunicacionIntervencion estado, String confirmadoPor, String resultado, String username);

    IntervencionDetailResponse asociarEquipamiento(UUID intervencionId, UUID equipamientoId, Integer cantidad, String estadoUso, String observaciones, String username);

    IntervencionDetailResponse adjuntarDocumento(UUID intervencionId, TipoDocumentoIntervencion tipo, String nombreArchivo, String rutaArchivo, String mimeType, Long tamanioBytes, String descripcion, Boolean visibleCiudadano, String username);

    IntervencionDetailResponse enviarARevision(UUID id, String username);

    IntervencionDetailResponse observarIntervencion(UUID id, IntervencionObservarRequest request, String username);

    IntervencionDetailResponse subsanarIntervencion(UUID id, String motivoSubsanacion, String username);

    IntervencionDetailResponse validarIntervencion(UUID id, IntervencionValidarRequest request, String username);

    IntervencionDetailResponse archivarIntervencion(UUID id, IntervencionArchivarRequest request, String username);

    byte[] exportarPdf(IntervencionFilter filtro);

    byte[] exportarExcel(IntervencionFilter filtro);

    byte[] generarFichaPdf(UUID id);
}
