package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados administrativos del ciclo de vida de una obligación tributaria municipal.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoObligacionTributaria {
    REGISTRADA("Registrada", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    PENDIENTE_VALIDACION("Pendiente de Validación", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    VALIDADA("Validada Formalmente", "bg-indigo-500/10 text-indigo-400 border-indigo-500/20"),
    OBSERVADA("Observada", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    VIGENTE("Vigente / Exigible", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    SUSPENDIDA("Suspendida Legalmente", "bg-purple-500/10 text-purple-400 border-purple-500/20"),
    RECTIFICADA("Rectificada", "bg-amber-500/10 text-amber-300 border-amber-500/20"),
    ANULADA("Anulada / Desestimada", "bg-rose-500/10 text-rose-400 border-rose-500/20"),

    // Compatibilidad retrospectiva
    BORRADOR("Borrador", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    DETERMINADA("Determinada Oficialmente", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    NOTIFICADA("Notificada al Contribuyente", "bg-indigo-500/10 text-indigo-400 border-indigo-500/20"),
    PARCIALMENTE_PAGADA("Parcialmente Pagada", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    PAGADA("Pagada Totalmente", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    FRACCIONADA("Fraccionada en Convenio", "bg-purple-500/10 text-purple-400 border-purple-500/20");

    private final String descripcion;
    private final String badgeClass;
}
