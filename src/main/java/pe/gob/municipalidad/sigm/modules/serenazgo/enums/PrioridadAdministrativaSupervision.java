package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;

@Getter
public enum PrioridadAdministrativaSupervision {
    ALTA("Alta", "Atención prioritaria inmediata", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    MEDIA("Media", "Atención en plazo operativo estándar", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    BAJA("Baja", "Seguimiento preventivo o de rutina", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20");

    private final String descripcion;
    private final String detalle;
    private final String badgeClass;

    PrioridadAdministrativaSupervision(String descripcion, String detalle, String badgeClass) {
        this.descripcion = descripcion;
        this.detalle = detalle;
        this.badgeClass = badgeClass;
    }
}
