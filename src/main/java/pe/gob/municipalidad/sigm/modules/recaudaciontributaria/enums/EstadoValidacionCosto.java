package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

@Getter
public enum EstadoValidacionCosto {
    DECLARADO("Declarado", "bg-slate-700/50 text-slate-300 border border-slate-600/30"),
    REVISADO("Revisado Técnicamente", "bg-blue-500/20 text-blue-400 border border-blue-500/30"),
    APROBADO("Aprobado y Sustentado", "bg-emerald-500/20 text-emerald-400 border border-emerald-500/30"),
    RECHAZADO("Rechazado", "bg-rose-500/20 text-rose-400 border border-rose-500/30");

    private final String descripcion;
    private final String badgeClass;

    EstadoValidacionCosto(String descripcion, String badgeClass) {
        this.descripcion = descripcion;
        this.badgeClass = badgeClass;
    }
}
