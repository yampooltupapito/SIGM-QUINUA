package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados de vigencia de la representación legal.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoRepresentanteLegal {
    ACTIVO("Vigente / Activo", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    REVOCADO("Revocado", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    VENCIDO("Vencido por Plazo", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    SUSPENDIDO("Suspendido Cautelarmente", "bg-purple-500/10 text-purple-400 border-purple-500/20");

    private final String descripcion;
    private final String badgeClass;
}
