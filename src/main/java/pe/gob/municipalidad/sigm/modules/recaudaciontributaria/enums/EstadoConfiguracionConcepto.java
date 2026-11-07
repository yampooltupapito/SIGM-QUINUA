package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados del ciclo de vida y versionado de una configuración tributaria.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoConfiguracionConcepto {
    BORRADOR("Borrador", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    EN_REVISION("En Revisión", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    APROBADO("Aprobado", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    ACTIVO("Activo / Vigente", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    INACTIVO("Inactivo / Desactivado", "bg-slate-600/10 text-slate-500 border-slate-600/20"),
    ARCHIVADO("Archivado / Histórico", "bg-purple-500/10 text-purple-400 border-purple-500/20");

    private final String descripcion;
    private final String badgeClass;

    public String getNombre() {
        return descripcion;
    }
}
