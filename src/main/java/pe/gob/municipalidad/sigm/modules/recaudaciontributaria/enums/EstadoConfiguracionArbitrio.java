package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

@Getter
public enum EstadoConfiguracionArbitrio {
    BORRADOR("Borrador", "bg-slate-700/50 text-slate-300 border border-slate-600/30"),
    EN_REVISION("En Revisión", "bg-blue-500/20 text-blue-400 border border-blue-500/30"),
    OBSERVADO("Observado", "bg-amber-500/20 text-amber-400 border border-amber-500/30"),
    APROBADO("Aprobado", "bg-indigo-500/20 text-indigo-300 border border-indigo-500/30"),
    ACTIVO("Activo", "bg-emerald-500/20 text-emerald-400 border border-emerald-500/30"),
    INACTIVO("Inactivo", "bg-slate-700/50 text-slate-400 border border-slate-600/30"),
    ARCHIVADO("Archivado", "bg-rose-500/20 text-rose-400 border border-rose-500/30");

    private final String descripcion;
    private final String badgeClass;

    EstadoConfiguracionArbitrio(String descripcion, String badgeClass) {
        this.descripcion = descripcion;
        this.badgeClass = badgeClass;
    }
}
