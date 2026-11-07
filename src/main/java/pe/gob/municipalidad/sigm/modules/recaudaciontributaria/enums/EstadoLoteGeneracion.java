package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estado del proceso de generación por lotes de obligaciones tributarias.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoLoteGeneracion {
    EN_PROCESO("En Proceso", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    COMPLETADO("Completado Exitosamente", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    COMPLETADO_CON_ERRORES("Completado con Observaciones/Errores", "bg-orange-500/10 text-orange-400 border-orange-500/20"),
    FALLIDO("Fallido / Cancelado", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String descripcion;
    private final String badgeClass;
}
