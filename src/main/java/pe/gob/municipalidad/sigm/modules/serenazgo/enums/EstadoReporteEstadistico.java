package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estado de generación del reporte estadístico.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoReporteEstadistico {

    GENERANDO("En Generación", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    COMPLETADO("Completado", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    ERROR("Error en Generación", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String descripcion;
    private final String badgeClass;
}
