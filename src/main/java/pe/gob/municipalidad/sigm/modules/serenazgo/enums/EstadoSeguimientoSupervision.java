package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;

@Getter
public enum EstadoSeguimientoSupervision {
    ABIERTO("Abierto", "Seguimiento iniciado pendiente de asignación o actuación", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    EN_REVISION("En Revisión", "Bajo evaluación y análisis por el supervisor asignado", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    OBSERVADO("Observado", "Con observaciones pendientes de subsanación por el responsable", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    EN_SUBSANACION("En Subsanación", "El responsable ha ingresado descargos o correcciones", "bg-purple-500/10 text-purple-400 border-purple-500/20"),
    RESUELTO("Resuelto", "Actuación o deficiencia atendida y validada técnicamente", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    CERRADO("Cerrado", "Seguimiento formalmente concluido y archivado en supervisión", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    CANCELADO("Cancelado", "Seguimiento anulado o desestimado con sustento", "bg-zinc-500/10 text-zinc-400 border-zinc-500/20");

    private final String descripcion;
    private final String detalle;
    private final String badgeClass;

    EstadoSeguimientoSupervision(String descripcion, String detalle, String badgeClass) {
        this.descripcion = descripcion;
        this.detalle = detalle;
        this.badgeClass = badgeClass;
    }
}
