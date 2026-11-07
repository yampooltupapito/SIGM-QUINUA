package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estado financiero de cobertura y saldo de una obligación tributaria municipal.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoFinancieroObligacion {
    SIN_MOVIMIENTOS("Sin Movimientos", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    PENDIENTE("Pendiente de Pago", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    PARCIALMENTE_CUBIERTA("Parcialmente Cubierta", "bg-sky-500/10 text-sky-400 border-sky-500/20"),
    CANCELADA("Cancelada / Pagada", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    CON_SALDO_A_FAVOR_APLICABLE("Con Saldo a Favor Aplicable", "bg-purple-500/10 text-purple-400 border-purple-500/20");

    private final String descripcion;
    private final String badgeClass;
}
