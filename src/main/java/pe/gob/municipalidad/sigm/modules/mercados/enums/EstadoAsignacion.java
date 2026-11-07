package pe.gob.municipalidad.sigm.modules.mercados.enums;

/**
 * Estados del ciclo de vida de la asignación de un puesto comercial a un comerciante.
 */
public enum EstadoAsignacion {
    BORRADOR("Borrador", "bg-slate-700 text-slate-300 border-slate-600"),
    SOLICITADA("Solicitada", "bg-blue-900/60 text-blue-300 border-blue-700"),
    EN_EVALUACION("En Evaluación", "bg-amber-900/60 text-amber-300 border-amber-700"),
    OBSERVADA("Observada", "bg-orange-900/60 text-orange-300 border-orange-700"),
    APROBADA("Aprobada", "bg-teal-900/60 text-teal-300 border-teal-700"),
    RECHAZADA("Rechazada", "bg-rose-900/60 text-rose-300 border-rose-700"),
    VIGENTE("Vigente (Ocupado)", "bg-emerald-900/60 text-emerald-300 border-emerald-700"),
    SUSPENDIDA("Suspendida", "bg-purple-900/60 text-purple-300 border-purple-700"),
    FINALIZADA("Finalizada", "bg-zinc-800 text-zinc-400 border-zinc-700"),
    ANULADA("Anulada", "bg-red-950 text-red-400 border-red-800");

    private final String descripcion;
    private final String badgeClase;

    EstadoAsignacion(String descripcion, String badgeClase) {
        this.descripcion = descripcion;
        this.badgeClase = badgeClase;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getBadgeClase() {
        return badgeClase;
    }

    public boolean esActiva() {
        return this == VIGENTE;
    }

    public boolean permiteEdicion() {
        return this == BORRADOR || this == OBSERVADA;
    }

    public boolean permiteEvaluacion() {
        return this == SOLICITADA || this == EN_EVALUACION || this == OBSERVADA;
    }

    public boolean permiteAprobacion() {
        return this == EN_EVALUACION;
    }

    public boolean permiteActivacion() {
        return this == APROBADA;
    }

    public boolean permiteSuspension() {
        return this == VIGENTE;
    }

    public boolean permiteReanudacion() {
        return this == SUSPENDIDA;
    }

    public boolean permiteFinalizacion() {
        return this == VIGENTE || this == SUSPENDIDA;
    }

    public boolean permiteDevolucion() {
        return this == VIGENTE || this == SUSPENDIDA || this == FINALIZADA;
    }
}
