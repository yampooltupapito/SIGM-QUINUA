package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Clasificación administrativa y temporal de la cartera de deudas tributarias municipales.
 */
@Getter
@RequiredArgsConstructor
public enum ClasificacionDeudaTributaria {
    PENDIENTE_NO_VENCIDA("Pendiente No Vencida (Corriente)", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    VENCIDA_PENDIENTE_VERIFICACION("Vencida Pendiente Verificación", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    VENCIDA_EXIGIBLE("Vencida Legalmente Exigible", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    SUSPENDIDA("Suspendida Administrativamente", "bg-purple-500/10 text-purple-400 border-purple-500/20"),
    OBSERVADA("Observada / En Auditoría", "bg-orange-500/10 text-orange-400 border-orange-500/20"),
    EN_REVISION("En Revisión Técnica", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    CON_FRACCIONAMIENTO_VIGENTE("Con Fraccionamiento Vigente", "bg-indigo-500/10 text-indigo-400 border-indigo-500/20"),
    CANCELADA("Cancelada / Sin Deuda", "bg-slate-500/10 text-slate-400 border-slate-500/20");

    private final String descripcion;
    private final String badgeClass;
}
