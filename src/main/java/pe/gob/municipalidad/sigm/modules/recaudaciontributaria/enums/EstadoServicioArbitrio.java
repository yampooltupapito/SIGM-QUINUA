package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

@Getter
public enum EstadoServicioArbitrio {
    ACTIVO("Activo", "bg-emerald-500/20 text-emerald-400 border border-emerald-500/30"),
    INACTIVO("Inactivo", "bg-slate-700/50 text-slate-400 border border-slate-600/30"),
    SUSPENDIDO("Suspendido", "bg-amber-500/20 text-amber-400 border border-amber-500/30");

    private final String descripcion;
    private final String badgeClass;

    EstadoServicioArbitrio(String descripcion, String badgeClass) {
        this.descripcion = descripcion;
        this.badgeClass = badgeClass;
    }
}
