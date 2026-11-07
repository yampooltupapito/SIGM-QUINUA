package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;

@Getter
public enum AccionHistorialSupervision {
    CREACION("Creación de seguimiento"),
    ASIGNACION("Asignación de responsable"),
    OBSERVACION("Registro de observación"),
    SUBSANACION("Ingreso de subsanación"),
    REVISION("Revisión de descargos"),
    ESCALAMIENTO("Escalamiento a oficina"),
    RESOLUCION("Resolución administrativa"),
    CIERRE("Cierre de seguimiento"),
    CANCELACION("Cancelación de seguimiento");

    private final String descripcion;

    AccionHistorialSupervision(String descripcion) {
        this.descripcion = descripcion;
    }
}
