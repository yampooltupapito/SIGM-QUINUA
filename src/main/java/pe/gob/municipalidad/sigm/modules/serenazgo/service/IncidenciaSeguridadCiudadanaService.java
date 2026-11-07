package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface IncidenciaSeguridadCiudadanaService {

    Page<IncidenciaResponse> listarConFiltros(IncidenciaFilter filtro, Pageable pageable);

    IncidenciaDetailResponse obtenerDetallePorId(UUID id, boolean puedeVerDatosSensibles);

    IncidenciaResponse obtenerPorCodigo(String codigo);

    IncidenciaResponse registrarIncidencia(IncidenciaCreateRequest request, String username);

    IncidenciaResponse actualizarIncidencia(UUID id, IncidenciaUpdateRequest request, String username);

    IncidenciaResponse cambiarEstado(UUID id, CambioEstadoIncidenciaRequest request, String username);

    IncidenciaResponse cambiarPrioridad(UUID id, CambioPrioridadIncidenciaRequest request, String username);

    IncidenciaResponse cerrarIncidencia(UUID id, CierreIncidenciaRequest request, String username);

    IncidenciaResponse descartarIncidencia(UUID id, DescarteIncidenciaRequest request, String username);

    IncidenciaComentarioResponse agregarComentario(UUID id, AgregarComentarioRequest request, String username);

    IncidenciaDocumentoResponse adjuntarDocumento(UUID id, String nombreArchivo, String mime, byte[] contenido, String descripcion, boolean esFoto, String username);

    List<PosibleDuplicadoResponse> buscarPosiblesDuplicados(UUID tipoId, UUID sectorId, OffsetDateTime fecha, UUID excluirId);

    DashboardIncidenciasResponse obtenerDashboardIncidencias();

    byte[] exportarFichaPdf(UUID id);

    byte[] exportarIncidenciasExcel(LocalDate desde, LocalDate hasta);
}
