package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

@Getter
public enum EstadoCalculoArbitrio {
    SIMULACION("Simulación Referencial", "bg-amber-500/20 text-amber-300 border border-amber-500/30"),
    PRELIMINAR("Cálculo Preliminar", "bg-sky-500/20 text-sky-400 border border-sky-500/30"),
    EN_REVISION("En Revisión Administrativa", "bg-blue-500/20 text-blue-400 border border-blue-500/30"),
    OBSERVADO("Observado Técnicamente", "bg-rose-500/20 text-rose-300 border border-rose-500/30"),
    APROBADO("Aprobado / Autorizado", "bg-emerald-500/20 text-emerald-400 border border-emerald-500/30"),
    ANULADO("Anulado", "bg-slate-700/50 text-slate-400 border border-slate-600/30");

    private final String descripcion;
    private final String badgeClass;

    EstadoCalculoArbitrio(String descripcion, String badgeClass) {
        this.descripcion = descripcion;
        this.badgeClass = badgeClass;
    }
}
