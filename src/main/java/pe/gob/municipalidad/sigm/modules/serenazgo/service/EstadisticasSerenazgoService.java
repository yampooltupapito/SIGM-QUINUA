package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.util.List;

/**
 * Servicio analítico de indicadores, series temporales y métricas de Seguridad Ciudadana.
 */
public interface EstadisticasSerenazgoService {

    /**
     * Calcula en tiempo de consulta el panel estadístico integral, incluyendo
     * comparación con período equivalente previo, series cronológicas y distribuciones.
     */
    DashboardEstadisticoResponse obtenerDashboardEstadistico(FiltroEstadisticoRequest filtro, CustomUserDetails user);

    /**
     * Consulta el catálogo técnico oficial de indicadores con sus fórmulas y limitaciones.
     */
    List<CatalogoIndicadorDto> obtenerCatalogoIndicadores();

    /**
     * Agregación territorial de novedades e intervenciones por sector y zona.
     */
    List<EstadisticasTerritorialesDto> obtenerEstadisticasPorSector(FiltroEstadisticoRequest filtro);

    /**
     * Cálculo de promedios, medianas y percentil 90 de tiempos de atención operativa.
     */
    TiemposAtencionEstadisticasDto calcularTiemposAtencion(FiltroEstadisticoRequest filtro);

    /**
     * Cálculo de cumplimiento de patrullajes, duración y verificación de puntos de control.
     */
    EstadisticasPatrullajeDto calcularEstadisticasPatrullaje(FiltroEstadisticoRequest filtro);
}
