package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estado operativo de la cuenta corriente tributaria del contribuyente.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoCuentaCorriente {
    ACTIVA("Activa", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    BLOQUEADA("Bloqueada por Auditoría", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    SUSPENDIDA("Suspendida Temporalmente", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    CERRADA("Cerrada / Inactiva", "bg-slate-500/10 text-slate-400 border-slate-500/20");

    private final String descripcion;
    private final String badgeClass;
}
