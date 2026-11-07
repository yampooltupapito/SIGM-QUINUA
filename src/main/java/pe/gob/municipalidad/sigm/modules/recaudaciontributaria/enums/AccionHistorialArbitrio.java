package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

@Getter
public enum AccionHistorialArbitrio {
    CREACION("Creación de Registro"),
    MODIFICACION("Modificación Técnica"),
    ENVIO_REVISION("Remisión a Revisión Administrativa"),
    OBSERVADO("Registro de Observaciones"),
    APROBACION("Aprobación Formal"),
    ACTIVACION("Activación Operativa"),
    DESACTIVACION("Desactivación"),
    ANULACION("Anulación Justificada"),
    RECALCULO("Recálculo en Nueva Versión");

    private final String descripcion;

    AccionHistorialArbitrio(String descripcion) {
        this.descripcion = descripcion;
    }
}
