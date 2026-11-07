package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;

@Getter
public enum TipoObservacionSupervision {
    OBSERVACION("Observación", "Observación formulada por el supervisor"),
    SUBSANACION("Subsanación", "Descargo o documentación ingresada por el responsable"),
    REVISION("Revisión", "Evaluación de cumplimiento de subsanación"),
    ESCALAMIENTO("Escalamiento", "Elevación del caso a oficina o autoridad competente"),
    DECISION("Decisión Administrativa", "Resolución o disposición administrativa de cierre");

    private final String descripcion;
    private final String detalle;

    TipoObservacionSupervision(String descripcion, String detalle) {
        this.descripcion = descripcion;
        this.detalle = detalle;
    }
}
