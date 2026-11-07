package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados de validación individual de obligaciones incorporadas a una solicitud.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoValidacionDetalleFraccionamiento {
    PENDIENTE("Pendiente de Evaluación", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    CONFORME("Conforme / Elegible", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    OBSERVADO("Observado / Cuestionado", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    EXCLUIDO("Excluido de Fraccionamiento", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String descripcion;
    private final String badgeClass;
}
