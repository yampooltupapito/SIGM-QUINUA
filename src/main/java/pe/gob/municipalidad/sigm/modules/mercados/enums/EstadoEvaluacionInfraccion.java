package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum EstadoEvaluacionInfraccion {
    REGISTRADA("Registrada", "Presunta infracción identificada pendiente de examen preliminar", "text-slate-300 bg-slate-800/60 border-slate-700"),
    EN_EVALUACION("En Evaluación", "Análisis de tipicidad, medios probatorios y competencia", "text-amber-300 bg-amber-900/40 border-amber-700/60"),
    PENDIENTE_INFORMACION("Pendiente Información", "Requerimiento de descargos o informes técnicos adicionales", "text-purple-300 bg-purple-900/40 border-purple-700/60"),
    DESCARTADA("Descartada", "Desestimada preliminarmente por atipicidad o falta de elementos", "text-emerald-300 bg-emerald-900/40 border-emerald-700/60"),
    DERIVADA_A_PROCEDIMIENTO("Derivada a Procedimiento", "Informe preliminar derivado formalmente a autoridad instructora", "text-blue-300 bg-blue-900/40 border-blue-700/60"),
    CERRADA("Cerrada", "Evaluación concluida formalmente con acto resolutivo o archivo", "text-teal-300 bg-teal-900/40 border-teal-700/60");

    private final String label;
    private final String descripcion;
    private final String badgeClass;

    EstadoEvaluacionInfraccion(String label, String descripcion, String badgeClass) {
        this.label = label;
        this.descripcion = descripcion;
        this.badgeClass = badgeClass;
    }
}
