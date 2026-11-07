package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estado legal de exigibilidad jurídica de una obligación tributaria municipal.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoExigibilidadObligacion {
    NO_EXIGIBLE("No Exigible Legalmente", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    EXIGIBLE("Exigible / Vencida", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    EN_COBRANZA_ORDINARIA("En Cobranza Ordinaria", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    SUSPENDIDA_LEGALMENTE("Suspendida por Reclamo/Impugnación", "bg-purple-500/10 text-purple-400 border-purple-500/20");

    private final String descripcion;
    private final String badgeClass;
}
