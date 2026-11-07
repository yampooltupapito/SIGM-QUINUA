package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum EstadoIncidencia {
    REGISTRADA("Registrada", "Incidencia registrada pendiente de revisión preliminar", "text-slate-300 bg-slate-800/60 border-slate-700"),
    EN_REVISION("En Revisión", "Evaluación de admisibilidad y pertinencia por oficina técnica", "text-indigo-300 bg-indigo-900/40 border-indigo-700/60"),
    ASIGNADA("Asignada", "Asignada a responsable u oficina para gestión operativa", "text-blue-300 bg-blue-900/40 border-blue-700/60"),
    EN_ATENCION("En Atención", "Actuaciones operativas y diligencias de campo en curso", "text-amber-300 bg-amber-900/40 border-amber-700/60"),
    PENDIENTE_INFORMACION("Pendiente Información", "A la espera de antecedentes, descargos o informe complementario", "text-purple-300 bg-purple-900/40 border-purple-700/60"),
    RESUELTA("Resuelta", "Atención operativa concluida con subsanación o medidas aplicadas", "text-emerald-300 bg-emerald-900/40 border-emerald-700/60"),
    CERRADA("Cerrada", "Incidencia archivada y concluida formalmente con trazabilidad", "text-teal-300 bg-teal-900/40 border-teal-700/60"),
    DESCARTADA("Descartada", "Desestimada por improcedencia, duplicidad o falta de sustento", "text-rose-300 bg-rose-900/40 border-rose-700/60");

    private final String label;
    private final String descripcion;
    private final String badgeClass;

    EstadoIncidencia(String label, String descripcion, String badgeClass) {
        this.label = label;
        this.descripcion = descripcion;
        this.badgeClass = badgeClass;
    }

    public String getNombre() {
        return label;
    }
}
