package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;

/**
 * Servicio principal de reportes y coordinación de consultas administrativas de Mesa de Partes.
 */
public interface ReporteMesaPartesService {

    /**
     * Obtiene el resumen consolidado de expedientes según filtros y alcance de oficina.
     */
    ResumenEstadisticoDto obtenerResumen(FiltroReporteRequest filtro, String username);

    /**
     * Lista paginada de expedientes para la tabla de reportes detallados.
     */
    Page<ExpedienteReporteItemDto> listarExpedientes(FiltroReporteRequest filtro, Pageable pageable, String username);

    /**
     * Obtiene el historial de exportaciones para auditoría institucional.
     */
    Page<ExportacionHistorialDto> obtenerHistorialExportaciones(Pageable pageable, String username);

    /**
     * Resuelve el ID de oficina efectivo considerando los permisos del usuario (Anti-IDOR y scope institucional).
     */
    Long resolverOficinaEfectiva(Long oficinaSolicitada, String username);
}
