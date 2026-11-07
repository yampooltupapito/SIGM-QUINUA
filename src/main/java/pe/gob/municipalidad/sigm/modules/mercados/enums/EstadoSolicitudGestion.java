package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum EstadoSolicitudGestion {
    BORRADOR("Borrador", "bg-slate-700/60 text-slate-300 border-slate-600"),
    PRESENTADA("Presentada", "bg-blue-900/60 text-blue-300 border-blue-700"),
    EN_REVISION("En Revisión", "bg-indigo-900/60 text-indigo-300 border-indigo-700"),
    OBSERVADA("Observada", "bg-amber-900/60 text-amber-300 border-amber-700"),
    PENDIENTE_SUBSANACION("Pendiente de Subsanación", "bg-orange-900/60 text-orange-300 border-orange-700"),
    EN_EVALUACION("En Evaluación", "bg-cyan-900/60 text-cyan-300 border-cyan-700"),
    APROBADA("Aprobada", "bg-emerald-900/60 text-emerald-300 border-emerald-700"),
    RECHAZADA("Rechazada", "bg-rose-900/60 text-rose-300 border-rose-700"),
    PENDIENTE_FORMALIZACION("Pendiente de Formalización", "bg-purple-900/60 text-purple-300 border-purple-700"),
    FORMALIZADA("Formalizada", "bg-teal-900/60 text-teal-300 border-teal-700"),
    FINALIZADA("Finalizada", "bg-gray-800 text-gray-300 border-gray-600"),
    DESISTIDA("Desistida", "bg-slate-800 text-slate-400 border-slate-700"),
    ANULADA("Anulada", "bg-red-950 text-red-400 border-red-800");

    private final String label;
    private final String badgeClass;

    EstadoSolicitudGestion(String label, String badgeClass) {
        this.label = label;
        this.badgeClass = badgeClass;
    }
}
