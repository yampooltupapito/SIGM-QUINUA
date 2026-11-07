package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas.GenerarReporteRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas.ReporteHistorialDto;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ReporteEstadisticoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoReporteEstadistico;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Servicio de generación, exportación y auditoría de reportes institucionales
 * en formatos PDF y Excel (XLSX).
 */
public interface ReportesEstadisticosSerenazgoService {

    /**
     * Genera un reporte institucional (PDF o Excel) según la plantilla solicitada.
     */
    ReporteHistorialDto generarReporte(GenerarReporteRequest req, CustomUserDetails user, String ip);

    /**
     * Consulta el historial paginado de reportes generados.
     */
    Page<ReporteHistorialDto> listarHistorialReportes(TipoReporteEstadistico tipo, LocalDate desde, LocalDate hasta, Pageable pageable);

    /**
     * Descarga el archivo del reporte generado previa verificación de permisos.
     */
    Resource descargarReporte(UUID id, CustomUserDetails user);

    /**
     * Consulta la entidad del reporte por ID.
     */
    ReporteEstadisticoSerenazgo obtenerPorId(UUID id);
}
