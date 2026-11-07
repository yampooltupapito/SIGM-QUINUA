package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados del flujo administrativo de solicitudes de fraccionamiento tributario.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoSolicitudFraccionamiento {
    BORRADOR("Borrador", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    PRESENTADA("Presentada Formalmente", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    EN_EVALUACION("En Evaluación Técnica/Legal", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    OBSERVADA("Observada con Requerimiento", "bg-orange-500/10 text-orange-400 border-orange-500/20"),
    SUBSANADA("Subsanada", "bg-indigo-500/10 text-indigo-400 border-indigo-500/20"),
    APROBADA("Aprobada para Convenio", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    RECHAZADA("Rechazada / Denegada", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    DESISTIDA("Desistida por Contribuyente", "bg-slate-500/10 text-slate-300 border-slate-500/20"),
    ARCHIVADA("Archivada Definitivamente", "bg-zinc-500/10 text-zinc-400 border-zinc-500/20");

    private final String descripcion;
    private final String badgeClass;
}
