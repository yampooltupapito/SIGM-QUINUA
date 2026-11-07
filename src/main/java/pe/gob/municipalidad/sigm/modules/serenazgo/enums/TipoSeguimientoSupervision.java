package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;

@Getter
public enum TipoSeguimientoSupervision {
    REVISION("Revisión Administrativa", "Revisión técnica y formal de actuaciones", "clipboard-check"),
    OBSERVACION("Observación de Procedimiento", "Constatación de deficiencias u omisiones en el registro", "alert-circle"),
    SUBSANACION("Solicitud de Subsanación", "Requerimiento de aclaración, corrección o sustento al personal", "file-edit"),
    SEGUIMIENTO("Seguimiento Continuo", "Acompañamiento y monitoreo de acciones en curso", "activity"),
    ESCALAMIENTO("Escalamiento a Oficina", "Remisión a unidad orgánica o autoridad competente", "arrow-up-right"),
    VERIFICACION("Verificación de Registro", "Comprobación de veracidad o consistencia de datos", "check-square");

    private final String descripcion;
    private final String detalle;
    private final String icono;

    TipoSeguimientoSupervision(String descripcion, String detalle, String icono) {
        this.descripcion = descripcion;
        this.detalle = detalle;
        this.icono = icono;
    }
}
