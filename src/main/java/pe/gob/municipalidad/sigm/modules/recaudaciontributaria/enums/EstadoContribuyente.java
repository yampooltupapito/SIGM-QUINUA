package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados administrativos del contribuyente tributario municipal según ordenanzas y normativa vigente.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoContribuyente {
    ACTIVO("Activo y Habilitado", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    INACTIVO("Inactivo Administrativo", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    OBSERVADO("Observado / Pendiente de Sustento", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    BLOQUEADO_ADMINISTRATIVAMENTE("Bloqueado Administrativamente", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    SUSPENDIDO("Suspendido Temporalmente", "bg-purple-500/10 text-purple-400 border-purple-500/20"),
    FALLECIDO("Fallecido", "bg-neutral-500/10 text-neutral-400 border-neutral-500/20"),
    EXTINTO("Persona Jurídica Extinta", "bg-red-500/10 text-red-400 border-red-500/20");

    private final String descripcion;
    private final String badgeClass;
}
