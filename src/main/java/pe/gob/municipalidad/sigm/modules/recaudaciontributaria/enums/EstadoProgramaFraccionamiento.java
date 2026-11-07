package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados administrativos del ciclo de vida de un Programa de Fraccionamiento Tributario.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoProgramaFraccionamiento {
    BORRADOR("Borrador", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    EN_REVISION("En Revisión", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    APROBADO("Aprobado", "bg-indigo-500/10 text-indigo-400 border-indigo-500/20"),
    ACTIVO("Activo / Vigente", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    SUSPENDIDO("Suspendido", "bg-purple-500/10 text-purple-400 border-purple-500/20"),
    CERRADO("Cerrado", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String descripcion;
    private final String badgeClass;
}
