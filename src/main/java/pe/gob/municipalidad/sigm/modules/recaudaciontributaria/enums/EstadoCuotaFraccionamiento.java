package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados financieros y de cumplimiento de cada cuota de fraccionamiento.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoCuotaFraccionamiento {
    PENDIENTE("Pendiente de Pago", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    PARCIALMENTE_PAGADA("Parcialmente Pagada", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    PAGADA("Cancelada Totalmente", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    VENCIDA("Vencida Impaga", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    SUSPENDIDA("Suspendida por Trámite", "bg-purple-500/10 text-purple-400 border-purple-500/20"),
    AJUSTADA("Ajustada por Modificación", "bg-indigo-500/10 text-indigo-400 border-indigo-500/20");

    private final String descripcion;
    private final String badgeClass;
}
