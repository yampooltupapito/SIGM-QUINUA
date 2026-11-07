package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados del convenio formal de facilidades de pago / fraccionamiento tributario.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoConvenioFraccionamiento {
    APROBADO("Aprobado Formalmente", "bg-indigo-500/10 text-indigo-400 border-indigo-500/20"),
    VIGENTE("Vigente / En Cumplimiento", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    EN_REVISION("En Revisión por Incumplimiento", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    SUSPENDIDO("Suspendido Administrativamente", "bg-purple-500/10 text-purple-400 border-purple-500/20"),
    CUMPLIDO("Cumplido Totalmente", "bg-teal-500/10 text-teal-400 border-teal-500/20"),
    RESUELTO("Resuelto / Pérdida Declarada", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    ANULADO("Anulado", "bg-slate-500/10 text-slate-400 border-slate-500/20");

    private final String descripcion;
    private final String badgeClass;
}
