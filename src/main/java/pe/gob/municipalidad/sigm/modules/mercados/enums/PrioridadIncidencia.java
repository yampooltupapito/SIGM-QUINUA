package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum PrioridadIncidencia {
    BAJA("Baja", "Atención programada ordinaria sin riesgo inmediato", "text-slate-300 bg-slate-800/60 border-slate-700"),
    MEDIA("Media", "Atención preferente en el plazo estándar de gestión", "text-blue-300 bg-blue-900/40 border-blue-700/60"),
    ALTA("Alta", "Atención prioritaria por afectación a servicios o higiene", "text-amber-300 bg-amber-900/40 border-amber-700/60"),
    CRITICA("Crítica", "Atención inmediata por riesgo a la seguridad física o salubridad", "text-rose-300 bg-rose-900/50 border-rose-700/70");

    private final String label;
    private final String descripcion;
    private final String badgeClass;

    PrioridadIncidencia(String label, String descripcion, String badgeClass) {
        this.label = label;
        this.descripcion = descripcion;
        this.badgeClass = badgeClass;
    }
}
