package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estado de cada cuota individual de una obligación tributaria.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoCuotaTributaria {
    PENDIENTE("Pendiente de Pago", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    VENCIDA("Vencida / Exigible", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    PARCIALMENTE_PAGADA("Parcialmente Pagada", "bg-sky-500/10 text-sky-400 border-sky-500/20"),
    CANCELADA("Cancelada / Pagada", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    ANULADA("Anulada", "bg-slate-500/10 text-slate-400 border-slate-500/20");

    private final String descripcion;
    private final String badgeClass;
}
